package com.numplates.nomera3.modules.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import com.meera.core.extensions.empty
import com.meera.core.utils.DownloadHelper
import com.meera.core.utils.files.IMAGE_JPEG
import com.meera.core.utils.files.TEXT_PLAIN
import com.meera.core.utils.graphics.NGraphics
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.ui.action.ShareContentTypes
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMessageViewEvent
import timber.log.Timber
import java.io.File
import java.io.IOException

class ShareContentDelegate(
    private val context: Context,
    private val downloadHelper: DownloadHelper,
    private val onViewEvent: (ChatMessageViewEvent) -> Unit,
    private val onDownloadProgress: (progress: Int) -> Unit
) {

    fun handleShareContent(types: ShareContentTypes) {
        when (types) {
            is ShareContentTypes.TextContent -> shareTextContent(types.message)
            is ShareContentTypes.SingleMedia -> downloadSingleMediaAndShare(types.message, types.isShareText)
            is ShareContentTypes.MultipleMedias -> downloadMultipleMediasAndShare(types.message)
        }
    }

    private fun shareTextContent(message: MessageEntity) {
        val text: String = message.tagSpan?.text ?: return
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = TEXT_PLAIN
            putExtra(Intent.EXTRA_TEXT, text)
        }
        onViewEvent(ChatMessageViewEvent.OnShareContent(intent))
    }

    private fun downloadSingleMediaAndShare(
        message: MessageEntity,
        isShareText: Boolean
    ) {
        val url = message.attachment.url
        val messageText = message.tagSpan?.text ?: String.empty()
        val extraText = if (isShareText) messageText else String.empty()
        runCatching {
            downloadHelper.download(
                context = context,
                url = url,
                onProgress = { progress ->
                    onDownloadProgress(progress)
                },
                onComplete = { file ->
                    if (file == null) return@download
                    val intent = getIntentWhenShareSingleMediaContent(url, file, extraText)
                    onViewEvent(ChatMessageViewEvent.OnShareContent(
                            intent = intent,
                            isDismissProgress = true
                        )
                    )
                },
                onLoadFailed = { error ->
                    Timber.e("FAIL Load media for share:$error")
                    onViewEvent(ChatMessageViewEvent.OnHideBottomDownloadMediaProgress)
                    onViewEvent(ChatMessageViewEvent.OnFailShareContent)
                }
            )
        }.onFailure { error ->
            Timber.e("FAIL Load media for share:$error")
            onViewEvent(ChatMessageViewEvent.OnFailShareContent)
        }
    }

    private fun downloadMultipleMediasAndShare(message: MessageEntity) {
        val urls: List<String> = message.attachments.map { it.url }
        runCatching {
            val uris = mutableListOf<Uri>()
            urls.forEach { url ->
                downloadHelper.download(
                    context = context,
                    url = url,
                    onComplete = { file ->
                        if (file == null) return@download
                        val downloadedFileUri = NGraphics.cacheFile(context, url, file)
                        uris.add(downloadedFileUri)
                        if (uris.size == urls.size) {
                            val intent = getIntentWhenShareMultipleMediaContent(uris)
                            onViewEvent(ChatMessageViewEvent.OnShareContent(intent))
                        }
                    },
                    onLoadFailed = { error ->
                        Timber.e("FAIL Load media for share:$error")
                        onViewEvent(ChatMessageViewEvent.OnFailShareContent)
                    }
                )
            }
        }.onFailure { error ->
            Timber.e("FAIL Load media for share:$error")
            onViewEvent(ChatMessageViewEvent.OnFailShareContent)
        }
    }

    @Throws(IOException::class)
    private fun getIntentWhenShareSingleMediaContent(url: String, downloadedFile: File, text: String?): Intent {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val downloadedFileUri = NGraphics.cacheFile(context, url, downloadedFile)
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, downloadedFileUri)
            type = mimeType
            if (text.isNullOrEmpty().not()) this.putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    private fun getIntentWhenShareMultipleMediaContent(uris: List<Uri>): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            type = IMAGE_JPEG
        }
    }

}

package com.numplates.nomera3.modules.chat.helpers

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.View
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import com.meera.core.utils.files.IMAGE_GIF
import com.meera.core.utils.files.IMAGE_JPEG
import com.meera.core.utils.files.IMAGE_PNG
import com.meera.core.utils.files.IMAGE_WEBP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class InputReceiveContentListener(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val contentListener: (Uri, mimeType: String) -> Unit = { _,_ ->},
    private val isIgnoreMimeTypes: Boolean = false
) : OnReceiveContentListener {

    override fun onReceiveContent(view: View, payload: ContentInfoCompat): ContentInfoCompat? {
        val splitContent = payload.partition { item -> item.uri != null }
        val uriContent = splitContent.first
        val remainingContent = splitContent.second
        uriContent?.let { processContent(it) }
        return remainingContent
    }

    private fun processContent(content: ContentInfoCompat) {
        val contentResolver = appContext.contentResolver
        scope.launch(Dispatchers.IO) {
            val uris = collectUris(content.clip, contentResolver)
            val uri = uris.firstOrNull()
            uri?.let {
                val mimeType = contentResolver.getType(uri)
                withContext(Dispatchers.Main) {
                    contentListener.invoke(uri, mimeType.orEmpty())
                }
            }
        }
    }

    private fun collectUris(clip: ClipData, contentResolver: ContentResolver): List<Uri> {
        val uris = mutableListOf<Uri>()
        for (i in 0 until clip.itemCount) {
            val uri = clip.getItemAt(i).uri
            val mimeType = contentResolver.getType(uri)

            if (isIgnoreMimeTypes) uris.add(uri)

            if (isIgnoreMimeTypes.not() && uri != null && SUPPORTED_MIME_TYPES.contains(mimeType)) {
                uris.add(uri)
            }
        }
        return uris
    }

    companion object {
        val SUPPORTED_MIME_TYPES = arrayOf(IMAGE_PNG, IMAGE_GIF, IMAGE_JPEG, IMAGE_WEBP)
    }
}

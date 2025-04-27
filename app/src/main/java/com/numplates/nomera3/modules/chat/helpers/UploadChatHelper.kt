package com.numplates.nomera3.modules.chat.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.google.gson.Gson
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.application_api.media.model.VideoMetadataModel
import com.meera.core.extensions.empty
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.utils.graphics.ExifUtils
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.isDefault
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.App
import com.numplates.nomera3.HTTPS_SCHEME
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.MEDIA_AUDIO
import com.numplates.nomera3.MEDIA_EXT_GIF
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.domain.interactornew.SendVoiceMessageUseCase
import com.numplates.nomera3.modules.chat.helpers.sendmessage.PAYLOAD_PARAM
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialUploadChatImageUseCase
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialUploadChatVideoUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressImageForUploadUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressVideoUseCase
import com.numplates.nomera3.presentation.utils.makeEntity
import com.numplates.nomera3.presentation.view.utils.NSupport
import timber.log.Timber
import java.io.File
import javax.inject.Inject

const val EXTENSION_PNG = ".png"
private const val DOT = "."

class UploadChatHelper @Inject constructor(
    private val appContext: Context,
    private val partialUploadChatVideoUseCase: PartialUploadChatVideoUseCase,
    private val partialUploadChatImageUseCase: PartialUploadChatImageUseCase,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    private val gson: Gson,
    private val fileManager: FileManager,
    private val metaDataDelegate: MediaFileMetaDataDelegate,
    private val getCropInfoUseCase: GetCropInfoUseCase,
    private val compressVideoUseCase: CompressVideoUseCase,
    private val compressImageForUploadUseCase: CompressImageForUploadUseCase
) {

    private val tempUploadFiles = mutableListOf<File?>()


    suspend fun uploadChatMedia(
        mediaToUpload: List<Uri>,
    ): List<MessageAttachment> {
        check(mediaToUpload.isNotEmpty()) { "Media list to upload can not be empty." }
        val uploadResultMap = HashMap<String, Any>()
        if (fileManager.getMediaType(mediaToUpload.first()) == MEDIA_TYPE_VIDEO) {
            uploadChatVideos(
                videos = mediaToUpload,
                payload = hashMapOf(),
                successUpload = { payload: HashMap<String, Any> ->
                    uploadResultMap.putAll(payload)
                    return@uploadChatVideos Any()
                },
                failureUpload = { }
            )
        } else {
            uploadChatImages(
                images = mediaToUpload,
                payload = hashMapOf(),
                aspectRatio = 0.0,
                successUpload = { payload: HashMap<String, Any> ->
                    uploadResultMap.putAll(payload)
                    return@uploadChatImages Any()
                },
                failureUpload = { }
            )
        }
        return uploadResultMap.makeEntity<MessageEntity>(gson).let { payload ->
            payload.attachments
                .plus(payload.attachment)
                .filter { !it.isDefault() }
                .toList()
        }
    }

    suspend fun uploadChatVideos(
        videos: List<Uri>,
        payload: HashMap<String, Any>,
        successUpload: suspend (payload: HashMap<String, Any>) -> Any,
        failureUpload: suspend (payload: HashMap<String, Any>) -> Any
    ) {
        try {
            check(videos.size == 1) { "The app doesn't support multiple video uploading." }
            val path = videos[0].path ?: error("Error image data for upload")

            val attachment = partialUploadChatVideoUseCase.invoke(
                fileToUpload = File(path),
            )
            val imageArray = arrayListOf<String>()
            if (!attachment.url.isNullOrBlank()) imageArray.add(attachment.url)
            if (imageArray.isEmpty()) error("Image list for upload are empty")

            val responseMetadata = attachment.metadata
            val messageAttachment = hashMapOf<String, Any?>()
            messageAttachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = attachment.url
            messageAttachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = TYPING_TYPE_VIDEO
            val metadata = hashMapOf<Any, Any?>()
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_DURATION.key] = responseMetadata?.duration
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_IS_SILENT.key] = responseMetadata?.isSilent
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_LOW_QUALITY.key] = responseMetadata?.lowQuality
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_PREVIEW.key] = responseMetadata?.preview
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_RATIO.key] = responseMetadata?.ratio
            messageAttachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            payload[PAYLOAD_PARAM.ATTACHMENT.key] = messageAttachment
            successUpload.invoke(payload)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("FAIL Upload video for message")
            failureUpload(payload)
        } finally {
            videos.mapNotNull { it.path }.forEach(::deleteTempVideoFile)
        }
    }


    suspend fun uploadChatImages(
        images: List<Uri>,
        payload: HashMap<String, Any>,
        aspectRatio: Double?,
        successUpload: suspend (payload: HashMap<String, Any>) -> Any,
        failureUpload: suspend (payload: HashMap<String, Any>) -> Any
    ) {
        try {
            val localImages = images.filter { uri -> uri.scheme != HTTPS_SCHEME && uri.scheme != HTTP_SCHEME }
            val networkImages = images.filter { uri -> uri.scheme == HTTPS_SCHEME || uri.scheme == HTTP_SCHEME }

            val totalAttachments = mutableListOf<HashMap<String, Any?>>()

            if (networkImages.isNotEmpty()) {
                val attachments = generateNetworkPayloadForAttachments(networkImages, aspectRatio)
                totalAttachments.addAll(attachments)
            }

            if (localImages.isNotEmpty()) {
                uploadImagesAndGetPayload(
                    images = localImages,
                    payload = payload,
                    successUpload = { attachments ->
                        totalAttachments.addAll(attachments)
                    },
                    failureUpload = failureUpload
                )
            }

            if (totalAttachments.size == 1) {
                payload[PAYLOAD_PARAM.ATTACHMENT.key] = totalAttachments[0]
            } else {
                payload[PAYLOAD_PARAM.ATTACHMENTS.key] = totalAttachments
            }

            successUpload.invoke(payload)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("FAIL Upload images for message:$e")
            errorUploadImageMessage(images, payload) { failureUpload(payload) }
        }
    }

    suspend fun uploadImages(
        images: List<Uri>,
        payload: HashMap<String, Any>,
    ): ResultUploadImages {
        runCatching {
            val uploadData: List<Uri> = getUploadData(images)

            val imageList = arrayListOf<String>()
            val resAttach = mutableListOf<HashMap<String, Any?>>()
            uploadData.forEach { imageUri ->
                val response = partialUploadChatImageUseCase.invoke(fileToUpload = File(imageUri.path.toString()))
                if (response.url?.isNotEmpty() == true) imageList.add(response.url)
                if (imageList.isEmpty()) error("Image list for upload are empty")

                val attachment = hashMapOf<String, Any?>()
                attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = response.url
                attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] =
                    if (response.url?.isGifUrl() == true) TYPING_TYPE_GIF else TYPING_TYPE_IMAGE
                val metadata = hashMapOf<Any, Any?>()
                metadata[PAYLOAD_PARAM.ATTACHMENT_RATIO.key] = response.metadata?.ratio
                attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
                resAttach.add(attachment)
            }

            deleteTempFiles(uploadData, images)
            return ResultUploadImages(successResult = resAttach)
        }.onFailure {
            return ResultUploadImages(errorResult = payload)
        }
        return ResultUploadImages(errorResult = payload)
    }

    private suspend fun uploadImagesAndGetPayload(
        images: List<Uri>,
        payload: HashMap<String, Any>,
        successUpload: suspend (resAttach: List<HashMap<String, Any?>>) -> Any,
        failureUpload: suspend (payload: HashMap<String, Any>) -> Any
    ) {
        runCatching {
            val uploadData: List<Uri> = getUploadData(images)
            val imageList = arrayListOf<String>()
            val resAttach = mutableListOf<HashMap<String, Any?>>()
            uploadData.forEach { imageUri ->
                val response = partialUploadChatImageUseCase.invoke(fileToUpload = File(imageUri.path.toString()))
                if (response.url?.isNotEmpty() == true) imageList.add(response.url)
                if (imageList.isEmpty()) error("Image list for upload are empty")

                val attachment = hashMapOf<String, Any?>()
                attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = response.url
                attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] =
                    if (response.url?.isGifUrl() == true) TYPING_TYPE_GIF else TYPING_TYPE_IMAGE
                val metadata = hashMapOf<Any, Any?>()
                metadata[PAYLOAD_PARAM.ATTACHMENT_RATIO.key] = response.metadata?.ratio
                attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
                resAttach.add(attachment)
            }

            deleteTempFiles(uploadData, images)
            successUpload.invoke(resAttach)
        }.onFailure {
            failureUpload.invoke(payload)
            Timber.e("FAIL Upload image(s)")
        }
    }

    private fun generateNetworkPayloadForAttachments(
        images: List<Uri>,
        aspectRatio: Double?
    ): List<HashMap<String, Any?>> {
        val resAttach = mutableListOf<HashMap<String, Any?>>()
        images.forEach { uri ->
            val attachment = hashMapOf<String, Any?>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = uri.toString()
            attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] =
                if (uri.toString()?.isGifUrl() == true) TYPING_TYPE_GIF
                else TYPING_TYPE_IMAGE
            val metadata = hashMapOf<Any, Any?>()
            metadata[PAYLOAD_PARAM.ATTACHMENT_RATIO.key] = aspectRatio ?: 0.0
            attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            resAttach.add(attachment)
        }
        return resAttach
    }

    suspend fun uploadVoiceMessage(
        audioPath: String,
        amplitudes: List<Int>,
        duration: Long?,
        payload: HashMap<String, Any>,
        successUpload: suspend (payload: HashMap<String, Any>) -> Any,
        failureUpload: () -> Any
    ) {
        try {
            sendVoiceMessageUseCase.setParams(audioPath, amplitudes)
            val response = sendVoiceMessageUseCase.sendVoiceMessage()
            if (response?.data != null) {
                val waveform = response.data.voiceData?.waveForm
                response.data.link?.let { link ->
                    val attachment = hashMapOf<String, Any>()
                    attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = link
                    attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = MEDIA_AUDIO
                    val metadata = hashMapOf<Any, Any>()
                    waveform?.let { data -> metadata.put(PAYLOAD_PARAM.WAVE_FORM.key, data) }
                    duration?.let { data -> metadata.put(PAYLOAD_PARAM.ATTACHMENT_METADATA_DURATION.key, data) }
                    attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
                    payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachment
                    successUpload.invoke(payload)
                }
            } else {
                errorUploadAudioMessage(audioPath, amplitudes, payload, failureUpload)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorUploadAudioMessage(audioPath, amplitudes, payload, failureUpload)
        }
    }

    data class ResultUploadVideo(
        val successResult: HashMap<String, Any>? = null,
        val errorResult: HashMap<String, Any>? = null
    )

    suspend fun uploadVideo(
        videoUri: Uri,
        payload: HashMap<String, Any>
    ): ResultUploadVideo {
        return try {
            val videoMetadata = metaDataDelegate.getVideoMetadata(videoUri)
            val attachmentPath = compressAndGetAttachmentPath(
                videoMetadata = videoMetadata,
                videoPath = videoUri.path ?: String.empty()
            )
            val attachment = partialUploadChatVideoUseCase.invoke(fileToUpload = File(attachmentPath))

            val imageArray = arrayListOf<String>()
            if (!attachment.url.isNullOrBlank()) imageArray.add(attachment.url)
            if (imageArray.isEmpty()) error("Image list for upload are empty")

            val responseMetadata = attachment.metadata
            val messageAttachment = hashMapOf<String, Any>()
            messageAttachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = requireNotNull(attachment.url)
            messageAttachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = TYPING_TYPE_VIDEO
            val metadata = hashMapOf<Any, Any?>()
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_DURATION.key] = responseMetadata?.duration
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_IS_SILENT.key] = responseMetadata?.isSilent
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_LOW_QUALITY.key] = responseMetadata?.lowQuality
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_PREVIEW.key] = responseMetadata?.preview
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_RATIO.key] = responseMetadata?.ratio
            messageAttachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            payload[PAYLOAD_PARAM.ATTACHMENT.key] = messageAttachment
            ResultUploadVideo(successResult = payload)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorPayload = errorUploadImagePayload(listOf(videoUri), payload)
            ResultUploadVideo(errorResult = errorPayload)
        }
    }

    private suspend fun compressAndGetAttachmentPath(
        videoMetadata: VideoMetadataModel?,
        videoPath: String
    ): String {
        val cropInfo = getCropInfoUseCase.invoke(
            fileType = MEDIA_TYPE_VIDEO,
            mediaPlace = MediaControllerOpenPlace.Chat
        ) ?: return videoPath
        val needCompress = cropInfo.needCompressMedia(
            currentWidth = videoMetadata?.width ?: 0,
            currentBitrate = videoMetadata?.bitrate ?: 0
        )
        return if (needCompress) {
            compressVideoUseCase.execute(videoPath, cropInfo)
        } else {
            videoPath
        }
    }

    private suspend fun compressImage(uri: Uri): Uri =
        getCropInfoUseCase.invoke(
            fileType = fileManager.getMediaType(uri),
            mediaPlace = MediaControllerOpenPlace.Post
        )?.let {
            compressImageForUploadUseCase(
                imageUri = uri,
                cropInfo = it
            ).let(Uri::parse)
        } ?: uri

    private suspend fun getUploadData(images: List<Uri>): List<Uri> {
        return images.map { uri ->
            val isContentUriScheme = fileManager.isContentUri(uri)
            val compressedUri = compressImage(uri)
            return@map if (isContentUriScheme) {
                val tempFileFromContentUri = fileManager.getFileForContentUri(compressedUri)
                tempUploadFiles.add(tempFileFromContentUri)
                val uriFromTempFile = Uri.fromFile(tempFileFromContentUri)
                convertImage(uriFromTempFile)
            } else {
                convertImage(compressedUri)
            }
        }
    }

    private fun errorUploadAudioMessage(
        audioPath: String,
        amplitudes: List<Int>,
        payload: HashMap<String, Any>,
        failureUpload: () -> Any
    ) {
        val attachment = hashMapOf<String, Any>()
        attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = audioPath
        attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = MEDIA_AUDIO
        attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] =
            hashMapOf<Any, List<Int>>(PAYLOAD_PARAM.WAVE_FORM.key to amplitudes)
        payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachment
        failureUpload.invoke()
    }

    private fun errorUploadImagePayload(
        imageUri: List<Uri>,
        payload: HashMap<String, Any>,
    ): HashMap<String, Any> {
        return if (imageUri.size == 1) {
            val attachment = hashMapOf<String, Any>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = imageUri[0].path.orEmpty()
            attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = MEDIA_IMAGE
            val metadata = hashMapOf<Any, Any>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachment
            payload[PAYLOAD_PARAM.RESEND_IMAGES.key] = mutableListOf(imageUri[0].path.orEmpty())
            payload
        } else {
            val resAttach = mutableListOf<HashMap<String, Any>>()
            val resendImages = mutableListOf<String>()
            imageUri.forEach { url ->
                val attach = hashMapOf<String, Any>()
                attach[PAYLOAD_PARAM.ATTACHMENT_URL.key] = url.path.orEmpty()
                attach[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = MEDIA_IMAGE
                val metadata = hashMapOf<Any, Any>()
                attach[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
                resAttach.add(attach)
                resendImages.add(url.path.orEmpty())
            }
            payload[PAYLOAD_PARAM.RESEND_IMAGES.key] = resendImages
            payload[PAYLOAD_PARAM.ATTACHMENTS.key] = resAttach
            payload
        }
    }

    private suspend fun errorUploadImageMessage(
        imageUri: List<Uri>,
        payload: HashMap<String, Any>,
        failureUpload: suspend () -> Any
    ) {
        if (imageUri.size == 1) {
            val attachment = hashMapOf<String, Any>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = imageUri[0].path.orEmpty()
            attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = MEDIA_IMAGE
            val metadata = hashMapOf<Any, Any>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachment
            payload[PAYLOAD_PARAM.RESEND_IMAGES.key] = mutableListOf(imageUri[0].path.orEmpty())
        } else {
            val resAttach = mutableListOf<HashMap<String, Any>>()
            val resendImages = mutableListOf<String>()
            imageUri.forEach { url ->
                val attach = hashMapOf<String, Any>()
                attach[PAYLOAD_PARAM.ATTACHMENT_URL.key] = url.path.orEmpty()
                attach[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = MEDIA_IMAGE
                val metadata = hashMapOf<Any, Any>()
                attach[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
                resAttach.add(attach)
                resendImages.add(url.path.orEmpty())
            }
            payload[PAYLOAD_PARAM.RESEND_IMAGES.key] = resendImages
            payload[PAYLOAD_PARAM.ATTACHMENTS.key] = resAttach
        }
        failureUpload.invoke()
    }

    /**
     * Sometimes backend is not accept some types of images and also
     * exist problems with rotation images on some device.
     * This method creates, new image file based on source image, to avoid problems
     *
     * @return return same uri while error
     * */
    private fun convertImage(uri: Uri): Uri {
        val extension = DOT.plus(uri.path?.substringAfterLast('.', ""))
        if (extension.isEmpty()) return uri
        if (extension != MEDIA_EXT_GIF) {
            var photoFile: File?
            var bitmap: Bitmap?
            try {
                photoFile = if (fileManager.isGooglePhoto(uri)) {
                    File(fileManager.saveImageFromGoogleDrives(uri))
                } else {
                    File(NSupport.getPath(appContext, uri))
                }
                val options = BitmapFactory.Options()
                bitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e("ERROR decode File from uri:$uri E:$e")
                bitmap = null
                photoFile = null
            }
            return if (bitmap != null) {
                val matrix = Matrix()
                matrix.postRotate(ExifUtils.setNormalOrientation(photoFile)) // 90
                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                val isPng = extension == EXTENSION_PNG
                val temp = if (isPng) fileManager.createPngImageFile(appContext)
                else fileManager.createImageFile()
                val filePath = fileManager.saveBitmapInFile(bitmap, temp.absolutePath, isPng)
                Uri.parse(filePath)
            } else {
                uri
            }
        } else {
            return uri
        }
    }

    /**
     * Delete list of images
     * filesPath - array with modifyed images
     * originalFiles - files provided by user to upload - should not be deleted
     * */
    private fun deleteTempFiles(
        filesPath: List<Uri?>,
        originalFiles: List<Uri?>
    ) {
        if (filesPath.size != originalFiles.size) return
        for (i in originalFiles.indices) {
            if ((filesPath[i] != originalFiles[i])) {
                val fileToDelete = filesPath[i]?.path
                if (!fileToDelete.isNullOrEmpty()) {
                    deleteTempFile(fileToDelete)
                }
            }
        }
    }

    /**
     * Delete temp image after apload it to server
     * */
    private fun deleteTempFile(filePath: String) {
        try {
            val extension = filePath.substring(filePath.lastIndexOf("."))
            if (extension != MEDIA_EXT_GIF) {
                fileManager.deleteFile(filePath)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Delete temp video file from local storage.
     */
    private fun deleteTempVideoFile(file: String) {
        if (appContext.applicationContext is App) {
            val app = appContext.applicationContext as App
            if (!app.hashSetVideoToDelete.contains(file)) {
                return
            }
            try {
                val deleted = fileManager.deleteFile(file)
                Timber.d("Bazaleev fileDeleted = $deleted FILE:$file")
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    data class ResultUploadImages(
        val successResult: List<HashMap<String, Any?>>? = null,
        val errorResult: HashMap<String, Any>? = null
    )
}

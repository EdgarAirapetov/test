package com.numplates.nomera3.modules.fileuploads.data.repository

import com.meera.core.utils.files.IMAGE_GIF
import com.meera.core.utils.files.IMAGE_JPEG
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.modules.chat.helpers.isGifUrl
import com.numplates.nomera3.modules.fileuploads.data.api.ApiUpload
import com.numplates.nomera3.modules.fileuploads.data.api.ApiUploadStorage
import com.numplates.nomera3.modules.fileuploads.data.mapper.ChatAttachmentPartialUploadMapper
import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.data.model.SendPartialUploadDataResponseDto
import com.numplates.nomera3.modules.fileuploads.domain.FileUploadRepository
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.math.ceil

//1048576
private const val MAX_UPLOAD_CHUNK_SIZE = 1024 * 1024
private const val UPLOAD_FILE_FORM_DATA_NAME = "file"
private const val PARTS_COUNT = 1
private const val FILE_NAME = "file"

class FileUploadRepositoryImpl @Inject constructor(
    private val apiStorage: ApiUploadStorage,
    private val api: ApiUpload,
    private val chatAttachmentPartialUploadMapper: ChatAttachmentPartialUploadMapper
) : FileUploadRepository {

    override suspend fun partialUploadChatVideo(
        fileToUpload: File,
        progress: suspend (Float) -> Unit,
    ): ChatAttachmentPartialUploadModel {
        val uploadId = partialFileUpload(
            fileToUpload = fileToUpload,
            mediaType = TYPING_TYPE_VIDEO.toMediaTypeOrNull(),
            progress = progress,
            sourceType = PartialUploadSourceType.CHAT
        )
        return getChatAttachmentPartialUpload(uploadId)
    }

    override suspend fun partialUploadChatImage(
        fileToUpload: File,
        progress: suspend (Float) -> Unit,
    ): ChatAttachmentPartialUploadModel {
        val mediaType = (if (fileToUpload.name.isGifUrl()) IMAGE_GIF else IMAGE_JPEG).toMediaTypeOrNull()
        val uploadId = partialFileUpload(
            fileToUpload = fileToUpload,
            mediaType = mediaType,
            sourceType = PartialUploadSourceType.CHAT,
            progress = progress,
        )
        return getChatAttachmentPartialUpload(uploadId)
    }

    override suspend fun partialFileUpload(
        fileToUpload: File,
        mediaType: MediaType?,
        sourceType: PartialUploadSourceType,
        progress: suspend (Float) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        val parts = if (fileToUpload.length() <= MAX_UPLOAD_CHUNK_SIZE) {
            PARTS_COUNT
        } else {
            ceil(fileToUpload.length().toDouble() / MAX_UPLOAD_CHUNK_SIZE).toInt()
        }
        val response = api.createPartialUpload(
            fileToUpload.length(),
            fileToUpload.name,
            parts,
            sourceType.value
        )
        response.data?.partialUploadId?.let { uploadId ->
            println("Upload id - ${uploadId}")
            if (fileToUpload.length() <= MAX_UPLOAD_CHUNK_SIZE) {
                val filePart = MultipartBody.Part.createFormData(
                    FILE_NAME,
                    fileToUpload.name,
                    fileToUpload.asRequestBody(mediaType)
                )
                apiStorage.uploadFileToStorage(partialUploadId = uploadId, offset = 0, fileData = filePart)
            } else {
                fileToUpload.inputStream().use { inputStream ->

                    var offset = 0
                    var bytesProgress = 0L
                    var readBytesCount: Int
//                    var sendPartialDataResponse: SendPartialUploadDataResponseDto? = null
                    val differed = mutableListOf<Deferred<SendPartialUploadDataResponseDto>>()
                    val chunks = hashMapOf<Int, Chunk>()

                    for (i in 0 until parts) {
//                        Возможно стоить обнулять буфера после отправки
                        val buffer = ByteArray(MAX_UPLOAD_CHUNK_SIZE)
                        readBytesCount = inputStream.read(buffer, 0, MAX_UPLOAD_CHUNK_SIZE)
                        bytesProgress += readBytesCount
                        progress.invoke(bytesProgress.toFloat() / fileToUpload.length())
                        val chunk = buffer.toRequestBody(mediaType, 0, readBytesCount)
                        val part = chunk.toFormData(UPLOAD_FILE_FORM_DATA_NAME, fileToUpload.name)
                        offset = if (i == 0) 0 else MAX_UPLOAD_CHUNK_SIZE * i
                        chunks[i] = Chunk(offset = offset, part = part)
                    }

                    chunks.forEach { (key, chank) ->
                        differed.add(
                            async(Dispatchers.IO) {
//                                println("Offset -${chank.offset}, i - $key")
                                apiStorage.uploadFileToStorage(
                                    partialUploadId = uploadId,
                                    offset = chank.offset,
                                    fileData = chank.part
                                )
                            }
                        )
                    }
                    differed.awaitAll()
                }
            }

            return@withContext uploadId

        } ?: error(
            "Couldn't create partial upload request, " +
                "fileName=${fileToUpload.name}, size=${fileToUpload.length()}"
        )
    }

    override suspend fun getChatAttachmentPartialUpload(uploadId: String): ChatAttachmentPartialUploadModel {
        val response = api.getChatAttachmentPartialUpload(
            partialUploadId = uploadId,
            source = PartialUploadSourceType.CHAT.value
        ).data ?: error("GET chat attachment data partial upload ERROR")
        return chatAttachmentPartialUploadMapper.mapChatAttachmentPartialUpload(response)
    }

    private fun RequestBody.toFormData(name: String, fileName: String?) =
        MultipartBody.Part.createFormData(name, fileName, this)

    inner class Chunk(
        val offset: Int,
        val part: MultipartBody.Part
    )
}

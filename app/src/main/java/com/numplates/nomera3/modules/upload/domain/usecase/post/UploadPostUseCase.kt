package com.numplates.nomera3.modules.upload.domain.usecase.post

import android.net.Uri
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.core.extensions.toJson
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE
import com.meera.db.models.UploadItem
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.App
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoUseCase
import com.numplates.nomera3.modules.upload.data.UploadResult
import com.numplates.nomera3.modules.upload.data.post.UploadMediaModel
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.domain.usecase.UploadBaseUseCase
import com.numplates.nomera3.modules.upload.mapper.PostBundleWorkerMapper
import com.numplates.nomera3.modules.upload.mapper.WorkInfoStateMapper
import com.numplates.nomera3.modules.upload.util.getState
import com.numplates.nomera3.modules.upload.util.waitWorkStateAsync
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.presentation.upload.UploadPostWorker
import com.numplates.nomera3.presentation.utils.isEditorTempFile
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class UploadPostUseCase @Inject constructor(
    rootScope: CoroutineScope,
    uploadItem: UploadItem,
    updateStoreCallback: (UploadItem) -> Unit,
    private val metaDataDelegate: MediaFileMetaDataDelegate
) : UploadBaseUseCase(rootScope, uploadItem, updateStoreCallback) {

    private var uploadBundle: UploadPostBundle = uploadItem.getUploadBundle(UploadPostBundle::class.java)

    @Inject
    lateinit var deleteVideoUseCase: PostVideoDeleteUseCase

    @Inject
    lateinit var deleteImageUseCase: PostImageDeleteExceptGifUseCase

    @Inject
    lateinit var compressVideoUseCase: CompressVideoUseCase

    @Inject
    lateinit var compressImageForUploadUseCase: CompressImageForUploadUseCase

    @Inject
    lateinit var workManager: Lazy<WorkManager>

    @Inject
    @Named(CACHE_DIR)
    lateinit var cacheDir: File

    @Inject
    lateinit var appInfoUseCase: GetAppInfoUseCase

    @Inject
    lateinit var getCropInfoUseCase: GetCropInfoUseCase

    @Inject
    lateinit var fileManager: FileManager

    init {
        App.component.inject(this)
    }

    private val isWorkerFinish: (WorkInfo.State?) -> Boolean = { state ->
        state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED || state == null
    }

    override suspend fun execute(): UploadResult {
        val operationId = uploadBundle.getUid()
        val currentState = operationId?.let { workManager.get().getState(it) }
        if (operationId != null
            && currentState != null
            && currentState != WorkInfo.State.FAILED
            && currentState != WorkInfo.State.SUCCEEDED
        ) {
            return if (isWorkerFinish(currentState)) {
                WorkInfoStateMapper.mapFinishState(currentState)
            } else {
                waitUpload(operationId)
            }
        } else {
            if (!uploadBundle.wasCompressed) {
                when {
                    uploadBundle.hasVideo() -> uploadBundle = uploadBundle.prepareVideoToUpload()
                    uploadBundle.hasStaticImage() -> uploadBundle = uploadBundle.prepareImageToUpload()
                }
            }

            if (uploadBundle.hasNoCompressedMedia()) {
                uploadBundle = uploadBundle.prepareMediaToUpload()
            }

            uploadBundle = runUploadWorkAndSaveOperationId(uploadBundle)

            saveUploadItem(
                uploadItem.copy(
                    uploadBundleStringify = uploadBundle.toJson()
                )
            )

            return waitUpload(uploadBundle.getUid()!!)
        }
    }

    private fun UploadPostBundle.hasVideo(): Boolean {
        return videoPath != null
    }

    private fun UploadPostBundle.hasStaticImage(): Boolean {
        return fileManager.getMediaType(Uri.parse(imagePath?: return false)) == MEDIA_TYPE_IMAGE
    }

    private suspend fun UploadPostBundle.prepareMediaToUpload(): UploadPostBundle {
        val compressedMediaList = arrayListOf<UploadMediaModel>()
        mediaList?.forEach { media ->
            if (media.mediaWasCompressed) {
                compressedMediaList.add(media)
            } else {
                when (media.mediaType) {
                    AttachmentPostType.ATTACHMENT_VIDEO -> {
                        val compressedVideoPath = compressMediaVideo(media.mediaUriPath)
                        if (compressedVideoPath == null) {
                            compressedMediaList.add(media)
                        } else {
                            compressedMediaList.add(
                                media.copy(
                                    mediaUriPath = compressedVideoPath,
                                    mediaWasCompressed = true
                                )
                            )
                        }
                    }

                    AttachmentPostType.ATTACHMENT_PHOTO -> {
                        val compressedImagePath = compressMediaImage(media.mediaUriPath)
                        if (compressedImagePath == null) {
                            compressedMediaList.add(media)
                        } else {
                            compressedMediaList.add(
                                media.copy(
                                    mediaUriPath = compressedImagePath,
                                    mediaWasCompressed = true
                                )
                            )
                        }
                    }

                    else -> compressedMediaList.add(media)
                }
            }
        }
        return this.copy(mediaList = compressedMediaList)
    }

    private suspend fun UploadPostBundle.prepareVideoToUpload(): UploadPostBundle {
        val videoPath = videoPath ?: return this
        val compressedVideoPath = compressMediaVideo(videoPath)?: return this
        return copy(
            videoPath = compressedVideoPath
        ).apply {
            wasCompressed = true
        }
    }

    private suspend fun UploadPostBundle.prepareImageToUpload(): UploadPostBundle {
        val imagePath = imagePath ?: return this
        val compressedImagePath = compressMediaImage(imagePath) ?: return this
        return copy(
            imagePath = compressedImagePath
        ).apply {
            wasCompressed = true
        }
    }

    private suspend fun compressMediaImage(imagePath: String): String? {
        val cropInfo = getCropInfoUseCase.invoke(
            fileType = MEDIA_TYPE_IMAGE,
            mediaPlace = MediaControllerOpenPlace.Post
        ) ?: return null
        val compressedImagePath = compressImageForUploadUseCase(
            imagePath = imagePath,
            cropInfo = cropInfo
        )

        clearCachedMedia()
        return compressedImagePath
    }

    private suspend fun compressMediaVideo(videoPath: String): String? {
        val metadata = metaDataDelegate.getVideoMetadata(Uri.parse(videoPath))
        val compressedVideoPath = metadata?.let {
            val cropInfo = getCropInfoUseCase.invoke(
                fileType = FileUtilsImpl.MEDIA_TYPE_VIDEO,
                mediaPlace = MediaControllerOpenPlace.Post
            ) ?: return null
            val needCompress = cropInfo.needCompressMedia(
                currentWidth = it.width,
                currentBitrate = it.bitrate
            )
            if (needCompress) {
                compressVideoUseCase.execute(videoPath, cropInfo)
            } else {
                videoPath
            }
        } ?: videoPath

        clearCachedMedia()
        return compressedVideoPath
    }

    private suspend fun waitUpload(operationId: UUID): UploadResult {
        val waitJob = rootScope.waitWorkStateAsync(workManager.get(), operationId, isWorkerFinish)
        val resultState = waitJob.await()

        if (resultState == WorkInfo.State.SUCCEEDED) {
            onSuccess()
        }

        return WorkInfoStateMapper.mapFinishState(resultState)
    }

    private suspend fun onSuccess() {
        clearCachedMedia()
    }

    private fun clearCachedMedia() {
        if (uploadBundle.imagePath.isEditorTempFile(cacheDir)) {
            deleteImageUseCase.execute(uploadBundle.imagePath)
        }
        if (uploadBundle.videoPath.isEditorTempFile(cacheDir)) {
            deleteVideoUseCase.execute(uploadBundle.videoPath)
        }
    }

    private fun runUploadWorkAndSaveOperationId(uploadBundle: UploadPostBundle): UploadPostBundle {
        val constraints = Constraints.Builder()
            .build()

        val builder = PostBundleWorkerMapper.map(uploadBundle)
        val inputData = builder.build()
        val addPostWorkRequest = OneTimeWorkRequestBuilder<UploadPostWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)

        val addPostRequestBuilder = addPostWorkRequest.build()
        val operationId = addPostRequestBuilder.id

        uploadBundle.setUid(operationId)

        workManager.get().enqueue(addPostRequestBuilder)

        return uploadBundle
    }

}

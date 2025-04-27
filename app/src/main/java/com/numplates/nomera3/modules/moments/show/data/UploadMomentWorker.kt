package com.numplates.nomera3.modules.moments.show.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.App
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeMoment
import com.numplates.nomera3.modules.moments.settings.util.getSetting
import com.numplates.nomera3.modules.moments.show.domain.DeleteMomentUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.NotifyNewMomentCreatedUseCase
import com.numplates.nomera3.modules.moments.util.CheckMomentsLimitUtil
import com.numplates.nomera3.modules.moments.util.SaveMomentToGalleryUtil
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostImageDeleteExceptGifUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.PostVideoDeleteUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import java.io.File
import javax.inject.Inject
import javax.inject.Named

private const val DEFAULT_SAVE_MOMENT_TO_GALLERY_VALUE = true

class UploadMomentWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    @Inject
    lateinit var repository: MomentsRepositoryImpl

    @Inject
    lateinit var deleteVideoUtil: PostVideoDeleteUseCase

    @Inject
    lateinit var deleteImageUtil: PostImageDeleteExceptGifUseCase

    @Inject
    lateinit var saveMomentToGalleryUtil: SaveMomentToGalleryUtil

    @Inject
    lateinit var deleteMomentUseCase: DeleteMomentUseCase

    @Inject
    lateinit var getMomentDataUseCase: GetMomentDataUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var amplitudeMoment: AmplitudeMoment

    @Inject
    lateinit var notifyNewMomentCreatedUseCase: NotifyNewMomentCreatedUseCase

    @Inject
    @Named(CACHE_DIR)
    lateinit var cacheDir: File

    @Inject
    lateinit var context: Context

    private val checkMomentsLimitUtil = CheckMomentsLimitUtil()

    companion object {
        const val MOMENTS_PACK_SIZE = "MOMENTS_PACK_SIZE"
        const val MOMENT_UPLOAD_ORDER = "MOMENT_UPLOAD_ORDER"
        const val MOMENT_VIDEO_DURATION = "MOMENT_VIDEO_DURATION"
        const val FILE_PATH = "MOMENT_IMAGE_PATH"
        const val IS_VIDEO = "MOMENT_IS_VIDEO"
        const val MEDIA = "MOMENT_MEDIA"
        const val MEDIA_KEYBOARD = "MOMENT_MEDIA_KEYBOARD"
        const val GPS_X = "MOMENT_GPS_X"
        const val GPS_Y = "MOMENT_GPS_Y"
    }

    init {
        App.component.inject(this)
    }

    override suspend fun doWork(): Result {
        val filePath = inputData.getString(FILE_PATH) ?: String.empty()
        val isVideo = inputData.getBoolean(IS_VIDEO, false)
        val media = inputData.getString(MEDIA) ?: String.empty()
        val mediaKeyboard = inputData.getStringArray(MEDIA_KEYBOARD) ?: emptyArray()
        val gpsX = inputData.getDouble(GPS_X, 0.0)
        val gpsY = inputData.getDouble(GPS_Y, 0.0)
        val momentsUploadingPackSize = inputData.getInt(MOMENTS_PACK_SIZE, 0)
        val momentUploadOrder = inputData.getInt(MOMENT_UPLOAD_ORDER, 0)
        val result = kotlin.runCatching {
            repository.addMoment(
                filePath = filePath,
                isVideo = isVideo,
                gpsX = gpsX,
                gpsY = gpsY,
                media = media,
                mediaKeyboard = mediaKeyboard
            )
        }.onSuccess {
            if (isNeedSaveToGallery()) {
                tryToSaveToGallery(filePath)
            }
            clearCachedMedia(
                filePath = filePath,
                isVideo = isVideo
            )
            deleteOverLimitMoments()
            newMomentCreated()
            logCreateMoment(
                momentVideoDuration = it.outputData.getInt(ARG_MOMENT_DURATION, 0),
                momentsUploadingPackSize = momentsUploadingPackSize,
                momentUploadOrder = momentUploadOrder,
                momentId = it.outputData.getLong(ARG_MOMENT_ID, 0)
            )
        }.onFailure {
            val err = it
            err.printStackTrace()
            if (isNeedSaveToGallery()) {
                tryToSaveToGallery(filePath)
            }
        }

        return result.getOrDefault(Result.failure())
    }

    private fun logCreateMoment(
        momentVideoDuration: Int,
        momentsUploadingPackSize: Int,
        momentUploadOrder: Int,
        momentId: Long,
    ) {
        amplitudeMoment.onCreateMoment(
            authorId = getUserUidUseCase.invoke(),
            momentDuration = momentVideoDuration,
            momentsCount = momentsUploadingPackSize,
            momentNumber = momentUploadOrder,
            momentId = momentId
        )
    }

    private fun newMomentCreated() {
        notifyNewMomentCreatedUseCase.invoke()
    }

    private fun tryToSaveToGallery(filePath: String) = kotlin.runCatching {
        saveMomentToGalleryUtil.duplicateMomentAndAddToGallery(filePath)
    }

    private suspend fun isNeedSaveToGallery(): Boolean {
        return getSettingsUseCase.invoke()
            .getSetting(SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY)?.value?.toBoolean()
            ?: DEFAULT_SAVE_MOMENT_TO_GALLERY_VALUE
    }

    private fun clearCachedMedia(filePath: String, isVideo: Boolean) {
        if (isVideo) {
            deleteVideoUtil.execute(filePath)
        } else {
            deleteImageUtil.execute(filePath)
        }
    }

    private suspend fun deleteOverLimitMoments() {
        val currentMoments = getMomentDataUseCase.invoke(
            getFromCache = true,
            momentsSource = GetMomentDataUseCase.MomentsSource.Main
        ).momentGroups.firstOrNull { momentGroupModel ->  momentGroupModel.isMine }?.moments ?: return

        val overLimitMoments = checkMomentsLimitUtil.getOverLimitMoments(moments = currentMoments)
        for (moment in overLimitMoments) {
            deleteMomentUseCase.invoke(momentId = moment.id, userId = moment.userId)
        }
    }
}

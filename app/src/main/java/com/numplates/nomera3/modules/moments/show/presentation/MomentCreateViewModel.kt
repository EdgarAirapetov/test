package com.numplates.nomera3.modules.moments.show.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.feed.domain.usecase.ForceUpdatePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostUseCase
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.MomentDelegate
import com.numplates.nomera3.modules.moments.show.data.CarouselMomentsHelper
import com.numplates.nomera3.modules.moments.show.data.MomentToUpload
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.UploadMomentUseCase
import com.numplates.nomera3.modules.moments.util.CheckMomentsLimitUtil
import com.numplates.nomera3.modules.moments.util.LiveDataExtension
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.usecase.post.GetUploadStateUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.GetVideoLengthUseCase
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val MOMENTS_LEFT_SHOW_WARNING = 3

/**
 * Глобальная view-модель отвечающая за создание моментов
 */
class MomentCreateViewModel : ViewModel(), LiveDataExtension {

    @Inject
    lateinit var uploadMomentUseCase: UploadMomentUseCase

    @Inject
    lateinit var getMomentDataUseCase: GetMomentDataUseCase

    @Inject
    lateinit var uploadMomentStateUseCase: GetUploadStateUseCase

    @Inject
    lateinit var updatePostUseCase: ForceUpdatePostUseCase

    @Inject
    lateinit var hidePostUseCase: HidePostUseCase

    @Inject
    lateinit var getVideoLengthUseCase: GetVideoLengthUseCase

    @Inject
    lateinit var carouselMomentsHelper: CarouselMomentsHelper

    @Inject
    lateinit var delegate: MomentDelegate

    val eventStream: LiveData<MomentsEvent> = SingleLiveEvent()

    private val checkMomentsLimitUtil = CheckMomentsLimitUtil()

    private var momentsToUpload = mutableListOf<MomentToUpload>()

    private var momentsAmplitudeParams = UploadMomentUseCase.AmplitudeMomentUploadParams(0)

    init {
        App.component.inject(this)
        delegate.initRoadType(RoadTypesEnum.MAIN)
        delegate.initCoroutineScope(viewModelScope)

        uploadMomentStateUseCase.invoke()
            .filter { it.status is UploadStatus.Success }
            .onEach { uploadNextMoment() }
            .launchIn(viewModelScope)
    }

    fun uploadMoments(moments: List<MomentToUpload>) {
        createUploadParams(momentsPackSize = moments.size)
        momentsToUpload = moments.toMutableList()
        uploadNextMoment()
    }

    private fun uploadNextMoment() = runCatching {
        momentsToUpload.firstOrNull()?.let {
            momentsAmplitudeParams.loadOrderNumber++
            uploadMomentUseCase.invoke(it, momentsAmplitudeParams)
            momentsToUpload.remove(it)
        } ?: kotlin.run {
            delegate.initialLoadMoments()
        }
    }

    suspend fun getLimitWarning(): LimitWarningType? {
        val currentMoments = getMomentDataUseCase.invoke(
            getFromCache = true,
            momentsSource = GetMomentDataUseCase.MomentsSource.Main
        ).momentGroups.firstOrNull { momentGroupModel -> momentGroupModel.isMine }?.moments ?: return null

        val momentsLeft = checkMomentsLimitUtil.momentsLeft(moments = currentMoments)

        if (momentsLeft <= 0) return LimitWarningType.LimitOver
        if (momentsLeft <= MOMENTS_LEFT_SHOW_WARNING) return LimitWarningType.LimitSoon(momentsLeft)
        return null
    }

    private fun createUploadParams(momentsPackSize:Int){
        momentsAmplitudeParams = UploadMomentUseCase.AmplitudeMomentUploadParams(
            momentsPackSize = momentsPackSize
        )
    }

    sealed class LimitWarningType {
        data class LimitSoon(val momentsLeft: Int) : LimitWarningType()
        object LimitOver : LimitWarningType()
    }
}

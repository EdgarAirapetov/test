package com.numplates.nomera3.modules.moments.show.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeMoment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowFlipped
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.domain.usecase.GetFeedStateUseCase
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.MomentDelegate
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.data.mapper.MomentsUiMapper
import com.numplates.nomera3.modules.moments.show.domain.GetMomentByIdUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.MomentsAction
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UpdateProfileUserMomentsStateUseCase
import com.numplates.nomera3.modules.moments.show.domain.UpdateUserMomentsStateUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.fragment.MomentsFragmentClosingAnimationState
import com.numplates.nomera3.modules.moments.show.presentation.viewevents.ViewMomentEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.ViewMomentState
import com.numplates.nomera3.modules.moments.util.LiveDataExtension
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * View-модель отвечающая за просмотр моментов.
 */
class ViewMomentViewModel @Inject constructor(
    val getMomentDataUseCase: GetMomentDataUseCase,
    val momentMapper: MomentsUiMapper,
    val updateUserMomentsStateUseCase: UpdateUserMomentsStateUseCase,
    val updateProfileUserMomentsStateUseCase: UpdateProfileUserMomentsStateUseCase,
    val delegate: MomentDelegate,
    val subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase,
    val getFeedStateUseCase: GetFeedStateUseCase,
    private val getUidUseCase: GetUserUidUseCase,
    private val amplitudeMoment: AmplitudeMoment,
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
) : ViewModel(), LiveDataExtension {

    private val disposable = CompositeDisposable()

    val viewMomentState: LiveData<ViewMomentState> = SingleLiveEvent()
    private val momentsGroupsLiveData: LiveData<List<MomentGroupUiModel>> = SingleLiveEvent()
    private var fragmentClosingAnimationState: MomentsFragmentClosingAnimationState =
        MomentsFragmentClosingAnimationState.NOT_STARTED

    private var startMomentGroupId: Long? = null
    private var startMomentGroupIdForPaginationData: Long? = null

    fun init(startMomentGroupId: Long?, roadType: RoadTypesEnum?) {
        this.startMomentGroupId = startMomentGroupId
        this.startMomentGroupIdForPaginationData = startMomentGroupId
        if (roadType == null) return

        delegate.initCoroutineScope(viewModelScope)
        delegate.initRoadType(roadType)
        observeMomentsEvents()
    }

    fun onTriggerViewEvent(event: ViewMomentEvent) {
        when (event) {
            is ViewMomentEvent.FetchMoments -> fetchMoments(
                momentsSource = event.momentsSource,
                userId = event.userId,
                targetMomentId = event.targetMomentId,
                singleMomentId = event.singleMomentId
            )
            is ViewMomentEvent.ChangedClosingState -> onClosingStateChanged(event.momentsFragmentClosingState)
        }
    }

    fun updateProfileUserMomentsState(userId: Long) {
        viewModelScope.launch {
            updateProfileUserMomentsStateUseCase.invoke(userId)
        }
    }

    fun getClosingAnimationState() = fragmentClosingAnimationState

    fun requestNewMomentsGroupsPage() = delegate.requestMomentsPage()

    fun logEndMoments(momentsCount: Int) {
        amplitudeMoment.onMomentsEnd(
            momentsCount = momentsCount,
            userIdFrom = getUidUseCase.invoke()
        )
    }

    fun logAmplitudeFlipMoment(howFlipped: AmplitudePropertyMomentHowFlipped) {
        amplitudeMoment.onMomentFlip(howFlipped)
    }


    private fun observeMomentsEvents() {
        getFeedStateUseCase.execute(DefParams()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleMomentsUpdate){}.addDisposable()
    }

    private fun onClosingStateChanged(momentsFragmentClosingState: MomentsFragmentClosingAnimationState) {
        fragmentClosingAnimationState = momentsFragmentClosingState
    }

    private fun fetchMoments(
        momentsSource: GetMomentDataUseCase.MomentsSource,
        userId: Long?,
        targetMomentId: Long?,
        singleMomentId: Long?
    ) {
        when {
            singleMomentId != null -> {
                loadSingleMoment(singleMomentId)
            }
            else -> {
                getMomentGroups(momentsSource, userId, targetMomentId)
            }
        }
    }

    private fun loadSingleMoment(singleMomentId: Long) {
        viewModelScope.launch {
            val momentGroup = runCatching {
                val moment = getMomentByIdUseCase.invoke(singleMomentId)
                val momentUiModel = momentMapper.mapToViewItemUiModel(moment)
                momentUiModel ?: return@runCatching getEmptyGroupForErrorMoment()
                momentMapper.mapMomentToMomentGroup(momentUiModel)
            }.getOrDefault(getEmptyGroupForErrorMoment())
            val updateEvent = ViewMomentState.MomentsDataReceived(listOf(momentGroup))
            viewMomentState.setValue(updateEvent)
        }
    }

    private fun getEmptyGroupForErrorMoment(): MomentGroupUiModel {
        return MomentGroupUiModel(
            id = MomentsUiMapper.ID_SINGLE_MOMENT_GROUP,
            moments = emptyList(),
            userId = getUidUseCase.invoke(),
            isMine = false,
            placeholder = null
        )
    }

    private fun getMomentGroups(
        momentsSource: GetMomentDataUseCase.MomentsSource,
        userId: Long?,
        targetMomentId: Long?
    ) {
        viewModelScope.launch {
            runCatching {
                val newMoments = getMomentDataUseCase.invoke(
                    getFromCache = userId == null || userId == 0L,
                    userId = userId,
                    targetMomentId = targetMomentId,
                    momentsSource = momentsSource
                )
                val momentGroups = momentMapper.mapToViewUiModel(newMoments)?.momentGroups
                val notEmptyMomentGroups = momentGroups?.filter { it.moments.isNotEmpty() }
                if (notEmptyMomentGroups.isNullOrEmpty()) {
                    updateUserMomentsState(userId)
                }
                val filteredMomentGroups = getFilteredMomentGroups(notEmptyMomentGroups)
                resetStartMomentGroupId()
                updateMoments(filteredMomentGroups.orEmpty())
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun getFilteredMomentGroups(momentGroups: List<MomentGroupUiModel>?): List<MomentGroupUiModel>? {
        if (startMomentGroupId == null) return momentGroups

        val startedMomentGroup = momentGroups?.firstOrNull { it.id == startMomentGroupId } ?: return momentGroups

        if (startedMomentGroup.isViewed) return momentGroups

        return momentGroups.filter { !it.isViewed }
    }

    private fun getFilterPaginationData(momentGroups: List<MomentGroupUiModel>?): List<MomentGroupUiModel>? {
        if (startMomentGroupIdForPaginationData == null) return momentGroups

        val startedMomentGroup = momentGroups?.firstOrNull { it.id == startMomentGroupIdForPaginationData } ?: return momentGroups

        if (startedMomentGroup.isViewed) return momentGroups

        return momentGroups.filter { !it.isViewed }
    }

    private fun resetStartMomentGroupId() { startMomentGroupId = null }

    private suspend fun updateUserMomentsState(userId: Long?) {
        if (userId != null && userId != 0L) {
            val userMomentsStateUpdate = UserMomentsStateUpdateModel(
                userId = userId,
                hasMoments = false,
                hasNewMoments = false
            )
            updateUserMomentsStateUseCase.invoke(
                action = MomentsAction.VIEWED,
                userMomentsStateUpdate = userMomentsStateUpdate
            )
        }
    }

    private fun handleMomentsUpdate(updatedItem: FeedUpdateEvent?) {
        when (updatedItem) {
            is FeedUpdateEvent.FeedUpdateMoments -> {
                val roadType = updatedItem.roadType
                val moments = updatedItem.momentsInfo
                handleMomentsGroupsUpdate(roadType, moments)
            }
            else -> Unit
        }
    }

    private fun handleMomentsGroupsUpdate(eventRoadType: RoadTypesEnum?, moments: MomentInfoModel?) {
        viewModelScope.launch {
            val roadType = delegate.getRoadType()
            if (roadType != eventRoadType) return@launch

            val newMomentsGroups = momentMapper.mapToViewUiModel(moments)?.momentGroups ?: return@launch
            val momentsGroups = momentsGroupsLiveData.value ?: return@launch

            val allMomentsGroup = arrayListOf<MomentGroupUiModel>()
            allMomentsGroup.addAll(momentsGroups)

            for (newMomentsGroup in newMomentsGroups) {
                val alreadyExistGroup = momentsGroups.firstOrNull { it.id == newMomentsGroup.id }
                if (alreadyExistGroup == null && !newMomentsGroup.isMine) allMomentsGroup.add(newMomentsGroup)
            }

            val filteredAllMomentsGroup = getFilterPaginationData(allMomentsGroup)
            momentsGroupsLiveData.postValue(filteredAllMomentsGroup.orEmpty())

            val updateEvent = ViewMomentState.MomentsPaginatedDataReceived(filteredAllMomentsGroup.orEmpty())
            viewMomentState.setValue(updateEvent)
        }
    }

    private fun updateMoments(momentGroups: List<MomentGroupUiModel>) {
        val updateEvent = ViewMomentState.MomentsDataReceived(momentGroups)
        momentsGroupsLiveData.postValue(momentGroups)
        viewMomentState.setValue(updateEvent)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    private fun Disposable.addDisposable() {
        disposable.add(this)
    }
}

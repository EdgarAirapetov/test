package com.numplates.nomera3.modules.moments.show.presentation

import android.os.CountDownTimer
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.simpleName
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.IsUserAuthorizedUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.UnsubscribeUserUseCaseNew
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeMoment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowFlipped
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowScreenClosed
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentMenuActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentWhose
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyPostWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyPostWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactions
import com.numplates.nomera3.modules.complains.domain.usecase.ComplainOnMomentUseCase
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowAddExclusionUseCase
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowDeleteExclusionUseCase
import com.numplates.nomera3.modules.moments.show.MomentDelegate
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.data.mapper.MomentsUiMapper
import com.numplates.nomera3.modules.moments.show.domain.AllowCommentsUseCase
import com.numplates.nomera3.modules.moments.show.domain.CommentsAvailabilityType
import com.numplates.nomera3.modules.moments.show.domain.DeleteMomentUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentByIdUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase.MomentsSource.Companion.toRoadTypesEnum
import com.numplates.nomera3.modules.moments.show.domain.GetMomentLinkUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetUpdatedMomentViewCountUseCase
import com.numplates.nomera3.modules.moments.show.domain.SetMomentViewedUseCase
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentItemUpdateUseCase
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.show.presentation.controller.MomentsPreloadController
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupPositionType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentNavigationType
import com.numplates.nomera3.modules.moments.show.presentation.viewevents.PositionViewMomentEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MeeraPositionViewMomentState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentMessageState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentScreenActionEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentTimelineState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.PositionViewMomentState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.ViewMomentNavigationState
import com.numplates.nomera3.modules.moments.util.LiveDataExtension
import com.numplates.nomera3.modules.moments.util.SaveMomentToGalleryUtil
import com.numplates.nomera3.modules.moments.util.updateMomentGroupByMoment
import com.numplates.nomera3.modules.moments.util.updateMomentReactionsById
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.navigator.NavigationListener
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

const val DEFAULT_MOMENT_CONTENT_TIME_LENGTH_MS = 5_000L
const val DEFAULT_MOMENT_CONTENT_VIEWED_TIMER_LENGTH_MS = 0L
const val MOMENT_VIEW_TAG = "MOMENT_VIEW_TAG"
private const val NO_COMMENT_ID = -1L
private const val DEFAULT_COMMENT_ID = 0L

/**
 * View-модель отвечающая за группу моментов ViewPager2 в просмотре моментов.
 */
class ViewMomentPositionViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val getMomentDataUseCase: GetMomentDataUseCase,
    private val getMomentByIdUseCase: GetMomentByIdUseCase,
    private val deleteMomentUseCase: DeleteMomentUseCase,
    private val allowCommentsUseCase: AllowCommentsUseCase,
    private val momentMapper: MomentsUiMapper,
    private val reactionRepository: ReactionRepository,
    private val subscribeUserUseCaseNew: SubscribeUserUseCaseNew,
    private val unsubscribeUserUseCaseNew: UnsubscribeUserUseCaseNew,
    private val pushFriendStatusChangedUseCase: PushFriendStatusChangedUseCase,
    private val reactiveUpdateSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val hideUserMomentsUseCase: MomentSettingsNotShowAddExclusionUseCase,
    private val showUserMomentsUseCase: MomentSettingsNotShowDeleteExclusionUseCase,
    private val subscribeMomentItemUpdateUseCase: SubscribeMomentItemUpdateUseCase,
    private val setMomentViewedUseCase: SetMomentViewedUseCase,
    private val getUpdatedMomentViewCountUseCase: GetUpdatedMomentViewCountUseCase,
    private val complainOnMomentUseCase: ComplainOnMomentUseCase,
    private val preloadController: MomentsPreloadController,
    private val delegate: MomentDelegate,
    private val saveMomentToGalleryUtil: SaveMomentToGalleryUtil,
    private val getMomentLinkUseCase: GetMomentLinkUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val networkStatusProvider: NetworkStatusProvider,
    private val navigationListener: NavigationListener,
    private val isUserAuthorizedUseCase: IsUserAuthorizedUseCase,
    private val amplitudeReactions: AmplitudeReactions,
    private val amplitudeComments: AmplitudeCommentsAnalytics,
    private val amplitudeMoment: AmplitudeMoment,
) : BaseViewModel(), LiveDataExtension {

    private val _liveMomentState = MutableSharedFlow<PositionViewMomentState>()
    private val _liveMomentMeeraState = MutableSharedFlow<MeeraPositionViewMomentState>()
    val liveMomentState = _liveMomentState.asSharedFlow().distinctUntilChanged()
    val liveMomentMeeraState = _liveMomentMeeraState.asSharedFlow().distinctUntilChanged()
    val liveMomentMessageState = SingleLiveEvent<MomentMessageState>()
    val momentScreenAction = SingleLiveEvent<MomentScreenActionEvent>()
    val momentNavigation = SingleLiveEvent<ViewMomentNavigationState>()

    private var currentGroup: MomentGroupUiModel? = null
    private var currentItem: MomentItemUiModel? = null
    private var errorState: ErrorState? = null
    private var loadResources: Boolean = false
    private var isActiveItem: Boolean = false
    private var isPreviewLoaded: Boolean = false
    private var requestedPlaybackState: MomentPlaybackState? = null

    private var setMomentViewedTimer: SetMomentViewedTimer? = null
    private val viewCounterUpdates: HashMap<Long, Job?> = hashMapOf()

    init {
        startNavigationObserve()
    }

    override fun onCleared() {
        super.onCleared()
        cancelMomentViewedTimer()
        navigationListener.removeSubscriber(this::simpleName.name)
        viewCounterUpdates.clear()
        preloadController.onViewModelCleared()
    }

    fun getUid(): Long {
        return appSettings.readUID()
    }

    fun onTriggerViewEvent(event: PositionViewMomentEvent) {
        Timber.tag("MomentEventLog").d("$event")
        when (event) {
            PositionViewMomentEvent.ClickedNextPositionMoment -> {
                onClickNextOrPreviousPositionMoment(
                    MomentNavigationType.NEXT,
                    AmplitudePropertyMomentHowFlipped.NEXT_TAP
                    )
            }

            PositionViewMomentEvent.ClickedPrevPositionMoment -> {
                onClickNextOrPreviousPositionMoment(MomentNavigationType.PREVIOUS,
                    AmplitudePropertyMomentHowFlipped.BACK_TAP)
            }

            PositionViewMomentEvent.MomentContentRequested -> onContentRequested()
            is PositionViewMomentEvent.MomentContentLoaded -> onContentLoaded(event.isPreview)
            PositionViewMomentEvent.MomentPlaybackResumed -> onPlaybackResumed()
            PositionViewMomentEvent.MomentPlaybackEnded -> onPlaybackEnded()
            PositionViewMomentEvent.OnUserProfileOpened,
            PositionViewMomentEvent.MomentPlaybackPaused -> onPlaybackPaused()
            PositionViewMomentEvent.OnSettingsOpened,
            PositionViewMomentEvent.PausePositionMoment -> movePlayerToPlaybackState(MomentPlaybackState.PAUSED)

            PositionViewMomentEvent.OnSettingsOpened -> {
                logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.MOMENT_SETTINGS)
                movePlayerToPlaybackState(MomentPlaybackState.PAUSED)
            }

            PositionViewMomentEvent.OnUserProfileOpened,
            PositionViewMomentEvent.PausePositionMoment,
            -> movePlayerToPlaybackState(MomentPlaybackState.PAUSED)

            PositionViewMomentEvent.ResumePositionMoment -> movePlayerToPlaybackState(MomentPlaybackState.RESUMED)
            is PositionViewMomentEvent.OnFragmentPaused -> onFragmentPaused(
                isNowOffscreenInPager = event.isNowOffscreenInPager,
                groupPositionType = event.groupPositionType
            )

            is PositionViewMomentEvent.OnFragmentResumed -> {
                if (!event.isDialogsCreated) {
                    val newState = if (requestedPlaybackState == MomentPlaybackState.PAUSED) {
                        MomentPlaybackState.RESUMED
                    } else {
                        MomentPlaybackState.STARTED
                    }
                    updateIsActiveItemState(true)
                    movePlayerToPlaybackState(newState)
                }
            }

            PositionViewMomentEvent.OnAppHidden -> onAppHidden()
            PositionViewMomentEvent.DeletePositionMoment -> {
                movePlayerToPlaybackState(MomentPlaybackState.RESUMED)
                cancelMomentViewedTimer(forbidSettingAsViewedForMomentId = currentItem?.id)
                deleteCurrentMoment()
                logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.MOMENT_DELETE)
            }

            is PositionViewMomentEvent.SetMomentCommentAvailability -> {
                setMomentCommentAvailability(event.commentsAvailabilityType)
            }

            is PositionViewMomentEvent.SubscribeToUser -> subscribeToUser(event.userId)
            is PositionViewMomentEvent.UnsubscribeFromUser -> unsubscribeFromUser(event.userId)
            is PositionViewMomentEvent.HideUserMoments -> {
                cancelMomentViewedTimer(forbidSettingAsViewedForUserId = event.userId)
                hideUserMoments(event.userId)
            }
            is PositionViewMomentEvent.ShowUserMoments -> {
                showUserMoments(event.userId)
            }
            is PositionViewMomentEvent.UserMomentsHiddenAfterComplain -> {
                cancelMomentViewedTimer(forbidSettingAsViewedForUserId = event.userId)
                liveMomentMessageState.postValue(MomentMessageState.ShowSuccess(R.string.user_complain_moments_hidden))
                filterOutUserIdMoments(event.userId)
            }

            is PositionViewMomentEvent.OnRepostMoment -> updateMomentActionBarState(event.momentItemUiModel)
            is PositionViewMomentEvent.OnGetCommentCount -> updateMomentActionBarState(event.momentItemUiModel)
            is PositionViewMomentEvent.OnGetViewsCount -> updateMomentActionBarState(event.momentItemUiModel)
            PositionViewMomentEvent.CopyLink -> {
                currentItem?.id?.let { getMomentLink(it) }
                logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.COPY_LINK)
            }

            is PositionViewMomentEvent.ComplainToMoment -> complaintToMoment()
            is PositionViewMomentEvent.ScreenshotTaken -> getMomentDataForScreenshotPopup(event.momentId)
        }
    }

    private fun getMomentLink(momentId: Long) {
        viewModelScope.launch {
            runCatching {
                getMomentLinkUseCase.invoke(momentId = momentId)
            }.onSuccess { linkModel ->
                _liveMomentState.emit(
                    PositionViewMomentState.LinkCopied(
                        copyLink = linkModel.deepLinkUrl,
                    )
                )
                _liveMomentMeeraState.emit(
                    MeeraPositionViewMomentState.LinkCopied(
                        copyLink = linkModel.deepLinkUrl,
                    )
                )
            }.onFailure {
                Timber.e(it)
                errorState = ErrorState.mapException(it)
            }
        }
    }

    fun init(scope: CoroutineScope, momentId: Long, commentId: Long? = null) {
        errorState = null
        initDelegateParams(scope = scope, momentsSource = GetMomentDataUseCase.MomentsSource.Main)
        viewModelScope.launch {
            movePlayerToPlaybackState(MomentPlaybackState.PAUSED)
            currentItem = initMomentFromId(momentId)
            updateLoadResourcesState(loadResources = true)
            updateMomentState()
            checkInitScreenAction(commentId, currentItem)
            updateViewCountForCurrentMomentIfNeeded()

        }
        subscribeToUpdates()
    }

    fun init(
        scope: CoroutineScope,
        momentGroupId: Long,
        momentUserId: Long,
        targetMomentId: Long,
        targetCommentId: Long,
        momentsSource: GetMomentDataUseCase.MomentsSource,
        groupPositionType: MomentGroupPositionType?
    ) {
        errorState = null
        initDelegateParams(scope = scope, momentsSource = momentsSource)
        viewModelScope.launch {
            movePlayerToPlaybackState(MomentPlaybackState.PAUSED)
            runCatching {
                initMomentGroupFromId(
                    momentGroupId = momentGroupId,
                    momentUserId = momentUserId,
                    momentsSource = momentsSource
                )
            }.onSuccess { model ->
                currentGroup = model
                changeCurrentMomentToRequired(groupPositionType, targetMomentId)
                updateLoadResourcesState(loadResources = true)
                updateMomentState()
                checkInitScreenAction(targetCommentId, currentItem)
                updateViewCountForCurrentMomentIfNeeded()
                subscribeToUpdates()
                managePreloadIfNeeded()
            }.onFailure {
                errorState = ErrorState.mapException(it)
            }
        }
    }

    private fun startNavigationObserve() {
        if(navigationListener.checkIfExists(this::simpleName.name)) return
        navigationListener.addSubscribers(this::simpleName.name)
        viewModelScope.launch {
            navigationListener.sharedFlow.collectLatest { type ->
                onTriggerViewEvent(PositionViewMomentEvent.MomentPlaybackPaused)
            }
        }
    }

    suspend fun downloadMomentAndAddToGallery(url: String) {
        saveMomentToGalleryUtil.downloadMomentAndAddToGallery(url)
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer {
        return featureTogglesContainer
    }

    fun logStatisticReactionsTap() {
        amplitudeReactions.statisticReactionsTap(
            where = AmplitudePropertyReactionWhere.MOMENT,
            whence = AmplitudePropertyWhence.OTHER,
            recFeed = false
        )
    }

    fun logOnCommentsClicked(moment: MomentItemUiModel) {
        amplitudeComments.logOpenPost(
            postId = 0,
            authorId = moment.userId,
            momentId = moment.id,
            postType = AmplitudePropertyPostType.NONE,
            postContentType = AmplitudePropertyContentType.SINGLE,
            where = AmplitudePropertyPostWhere.MOMENT,
            commentCount = moment.commentsCount,
            haveText = false,
            havePic = moment.contentType == MomentContentType.IMAGE.value,
            haveVideo = moment.contentType == MomentContentType.VIDEO.value,
            haveGif = false,
            haveMusic = moment.media != null,
            recFeed = false,
            whence = AmplitudePropertyPostWhence.OTHER
        )
    }

    fun logCloseMomentByUser(closeButton: AmplitudePropertyMomentHowScreenClosed) {
        amplitudeMoment.onMomentScreenClose(closeButton)
    }

    fun logMomentStopByUser(momentId: Long) {
        amplitudeMoment.onMomentStop(momentId)
    }

    fun logMomentMenuAction(
        actionType: AmplitudePropertyMomentMenuActionType,
    ) {
        val moment = currentItem ?: return
        val whoseMoment = if (moment.id == getUid()) {
            AmplitudePropertyMomentWhose.MY_MOMENT
        } else {
            AmplitudePropertyMomentWhose.USER_MOMENT
        }
        amplitudeMoment.onMomentMenuAction(
            whoseMomentOf = whoseMoment,
            actionType = actionType,
            userIdActionFrom = getUid(),
            momentAuthorId = moment.userId
        )
    }

    fun isMomentAuthor(momentAuthorId: Long) : Boolean = momentAuthorId == getUid()

    private fun logAmplitudeFlipMoment(howFlipped: AmplitudePropertyMomentHowFlipped) {
        amplitudeMoment.onMomentFlip(howFlipped)
    }

    private fun initDelegateParams(
        scope: CoroutineScope,
        momentsSource: GetMomentDataUseCase.MomentsSource
    ) {
        momentsSource.toRoadTypesEnum()?.let { delegate.initRoadType(it) }
        delegate.initCoroutineScope(scope)
    }

    private suspend fun initMomentFromId(
        momentId: Long
    ): MomentItemUiModel? {
        return runCatching {
            val moment = getMomentByIdUseCase.invoke(momentId)
            momentMapper.mapToViewItemUiModel(moment)
        }.onFailure { exception ->
            errorState = ErrorState.mapException(exception)
        }.getOrNull()
    }

    private suspend fun initMomentGroupFromId(
        momentGroupId: Long,
        momentUserId: Long,
        momentsSource: GetMomentDataUseCase.MomentsSource
    ): MomentGroupUiModel? {
        val newMoments = getMomentDataUseCase.invoke(
            getFromCache = false,
            userId = momentUserId,
            momentsSource = momentsSource
        )
        val momentGroups = momentMapper.mapToViewUiModel(newMoments)?.momentGroups
        return momentGroups
            ?.firstOrNull { it.id == momentGroupId }
            ?: run {
                errorState = ErrorState.MomentNotFound
                null
            }
    }

    private fun subscribeToUpdates() {
        subscribeReactionUpdate()
        subscribeMomentItemUpdate()
    }

    private fun onFragmentPaused(isNowOffscreenInPager: Boolean, groupPositionType: MomentGroupPositionType?) {
        if (isNowOffscreenInPager) {
            updateIsActiveItemState(isActive = false)
            updateIsPreviewLoadedState(isPreviewLoaded = false)
            changeCurrentMomentToRequired(groupPositionType)
            updateLoadResourcesState(loadResources = true)
            movePlayerToPlaybackState(MomentPlaybackState.STOPPED)
            cancelMomentViewedTimer()
            managePreloadIfNeeded()
        } else {
            movePlayerToPlaybackState(MomentPlaybackState.PAUSED)
        }
    }

    private fun onAppHidden() {
        movePlayerToPlaybackState(MomentPlaybackState.STOPPED)
        cancelMomentViewedTimer()
    }

    private fun movePlayerToPlaybackState(playbackState: MomentPlaybackState) {
        requestedPlaybackState = playbackState
        updateMomentState()
    }

    private fun changeCurrentMomentToRequired(
        groupPositionType: MomentGroupPositionType?,
        targetMomentId: Long? = null
    ) {
        val isMomentViewed = currentGroup?.isViewed ?: false
        val groupMoments = currentGroup?.moments
        currentItem = if (targetMomentId != null && targetMomentId != 0L) {
            groupMoments?.firstOrNull { it.id == targetMomentId }
        } else if (isMomentViewed && groupPositionType == MomentGroupPositionType.BEHIND) {
            groupMoments?.firstOrNull { it.id == (currentGroup?.lastMomentId ?: it.id) }
        } else {
            groupMoments?.firstOrNull { it.id == (currentGroup?.firstNotViewedMomentId ?: it.id) }
        } ?: currentItem
    }

    private fun subscribeReactionUpdate() {
        reactionRepository
            .getCommandReactionStream()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleReactionUpdate)
            .addDisposable()

        reactionRepository
            .getCommandReactionStreamMeera()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleReactionUpdateMeera)
            .addDisposable()
    }

    private fun handleReactionUpdate(reactionUpdate: ReactionUpdate) {
        when (val reactionSource = reactionUpdate.reactionSource) {
            is ReactionSource.Moment -> {
                val updatedMomentId = reactionSource.momentId
                val group = currentGroup ?: run {
                    currentItem = currentItem?.copy(reactions = reactionUpdate.reactionList)
                    viewModelScope.launch {
                        _liveMomentState.emit(
                            PositionViewMomentState.UpdateMoment(
                                momentItemModel = currentItem,
                                reactionSource = reactionSource
                            )
                        )
                    }
                    return
                }
                val updatedGroup = group.updateMomentReactionsById(
                    momentId = updatedMomentId,
                    reactionsList = reactionUpdate.reactionList
                ) ?: return
                currentGroup = updatedGroup
                val moment = updatedGroup.moments.find { it.id == updatedMomentId } ?: kotlin.run {
                    currentItem?.copy(reactions = reactionUpdate.reactionList)
                }
                currentItem = moment
                viewModelScope.launch {
                    _liveMomentState.emit(
                        PositionViewMomentState.UpdateMoment(
                            momentItemModel = currentItem,
                            reactionSource = reactionSource
                        )
                    )
                }
            }

            else -> Unit
        }
    }

    private fun handleReactionUpdateMeera(reactionUpdate: MeeraReactionUpdate) {
        when (val reactionSource = reactionUpdate.reactionSource) {
            is MeeraReactionSource.Moment -> {
                val updatedMomentId = reactionSource.momentId
                val group = currentGroup ?: run {
                    currentItem = currentItem?.copy(reactions = reactionUpdate.reactionList)
                    viewModelScope.launch {
                        _liveMomentMeeraState.emit(
                            MeeraPositionViewMomentState.UpdateMoment(
                                momentItemModel = currentItem,
                                reactionSource = reactionSource
                            )
                        )
                    }
                    return
                }
                val updatedGroup = group.updateMomentReactionsById(
                    momentId = updatedMomentId,
                    reactionsList = reactionUpdate.reactionList
                ) ?: return
                currentGroup = updatedGroup
                val moment = updatedGroup.moments.find { it.id == updatedMomentId } ?: kotlin.run {
                    currentItem?.copy(reactions = reactionUpdate.reactionList)
                }
                currentItem = moment
                viewModelScope.launch {
                    _liveMomentMeeraState.emit(
                        MeeraPositionViewMomentState.UpdateMoment(
                            momentItemModel = currentItem,
                            reactionSource = reactionSource
                        )
                    )
                }
            }

            else -> Unit
        }
    }

    private fun subscribeMomentItemUpdate() {
        subscribeMomentItemUpdateUseCase.invoke(
            lifecycleScope = viewModelScope,
            onSuccess = { repositoryEvent ->
                when (repositoryEvent) {
                    is MomentRepositoryEvent.MomentActionBarStateUpdated -> {
                        val momentItemUiModel = momentMapper.mapToViewItemUiModel(
                            model = repositoryEvent.updatedItem
                        ) ?: return@invoke
                        updateMomentActionBarState(momentItemUiModel)
                    }

                    is MomentRepositoryEvent.MomentUserSubscriptionUpdated -> {
                        updateUserSubscriptionStatus(
                            userId = repositoryEvent.userId,
                            isSubscribed = repositoryEvent.isSubscribed
                        )
                    }

                    is MomentRepositoryEvent.MomentUserBlockStatusUpdated -> {
                        updateUserBlockStatus(
                            userId = repositoryEvent.userId,
                            isBlockedByMe = repositoryEvent.isBlockedByMe
                        )
                    }

                    else -> Unit
                }
            },
            onError = { Timber.e(it) }
        )
    }

    private fun updateMomentActionBarState(momentItemUiModel: MomentItemUiModel) {
        currentGroup?.updateMomentGroupByMoment(momentItemUiModel)?.let { currentGroup = it }
        currentItem = momentItemUiModel.takeIf { it.id == currentItem?.id } ?: currentItem
        viewModelScope.launch {
            _liveMomentState.emit(
                PositionViewMomentState.UpdateMoment(
                    momentItemModel = currentItem,
                    reactionSource = ReactionSource.Moment(
                        momentId = momentItemUiModel.id,
                        reactionHolderViewId = ContentActionBar.ReactionHolderViewId.generate()
                    )
                )
            )
            _liveMomentMeeraState.emit(
                MeeraPositionViewMomentState.UpdateMoment(
                    momentItemModel = currentItem,
                    reactionSource = MeeraReactionSource.Moment(
                        momentId = momentItemUiModel.id,
                        reactionHolderViewId = MeeraContentActionBar.ReactionHolderViewId.generate()
                    )
                )
            )
        }
    }

    private fun subscribeToUser(userId: Long) {
        viewModelScope.launch {
            runCatching {
                subscribeUserUseCaseNew.invoke(userId)
                updateUserSubscriptionStatus(userId = userId, isSubscribed = true)
            }.onSuccess {
                pushFriendStatusChangedUseCase.invoke(userId)
                reactiveUpdateSubscribeUserUseCase.execute(
                    params = UpdateSubscriptionUserParams(
                        userId = userId,
                        isSubscribed = true,
                        needToHideFollowButton = true,
                        isBlocked = false
                    ),
                    success = {},
                    fail = { Timber.d("Failed to update subscription status. Error=$it") }
                )
            }.onFailure { Timber.e(it) }
        }
    }

    private fun unsubscribeFromUser(userId: Long) {
        viewModelScope.launch {
            runCatching {
                unsubscribeUserUseCaseNew.invoke(userId)
                updateUserSubscriptionStatus(userId = userId, isSubscribed = false)
            }.onSuccess {
                pushFriendStatusChangedUseCase.invoke(userId)
                reactiveUpdateSubscribeUserUseCase.execute(
                    params = UpdateSubscriptionUserParams(
                        userId = userId,
                        isSubscribed = false,
                        needToHideFollowButton = false,
                        isBlocked = false
                    ),
                    success = {},
                    fail = { Timber.d("Failed to update subscription status. Error=$it") }
                )
            }.onFailure { Timber.e(it) }
        }
    }

    private fun updateUserSubscriptionStatus(userId: Long, isSubscribed: Boolean) {
        val modifiedMoments = currentGroup?.moments?.map {
            if (it.userId == userId) {
                it.copy(isSubscribedToUser = isSubscribed)
            } else {
                it
            }
        }
        val currentMomentModified =
            currentItem?.takeIf { it.userId == userId }?.copy(isSubscribedToUser = isSubscribed)
        modifiedMoments?.let { currentGroup = currentGroup?.copy(moments = it) }
        currentMomentModified?.let { currentItem = it }
        updateMomentState()
    }

    private fun updateUserBlockStatus(userId: Long, isBlockedByMe: Boolean) {
        val modifiedMoments = currentGroup?.moments?.map {
            if (it.userId == userId) {
                it.copy(isUserBlackListByMe = isBlockedByMe)
            } else {
                it
            }
        }
        val currentMomentModified =
            currentItem?.takeIf { it.userId == userId }?.copy(isUserBlackListByMe = isBlockedByMe)
        modifiedMoments?.let { currentGroup = currentGroup?.copy(moments = it) }
        currentMomentModified?.let {
            currentItem = it
            if (isBlockedByMe) updateMomentState() else onContentRequested()
        }
    }

    private fun updateViewCountForCurrentMomentIfNeeded() {
        val item = currentItem ?: return
        val currentId = item.id
        when {
            viewCounterUpdates.containsKey(currentId) -> return
            item.userId != appSettings.readUID() -> viewCounterUpdates[currentId] = null
            else -> {
                viewCounterUpdates[currentId] = viewModelScope.launch {
                    runCatching {
                        val updatedViewCount = getUpdatedMomentViewCountUseCase.invoke(currentId)
                        val modifiedMoments = currentGroup?.moments?.map {
                            if (it.id == currentId) {
                                it.copy(viewsCount = updatedViewCount)
                            } else {
                                it
                            }
                        } ?: return@launch
                        currentGroup = currentGroup?.copy(moments = modifiedMoments)
                        currentItem = currentItem?.takeIf { it.id == currentId }
                            ?.copy(viewsCount = updatedViewCount) ?: currentItem
                        updateMomentState()
                    }.onSuccess {
                        viewCounterUpdates[currentId] = null
                    }.onFailure {
                        viewCounterUpdates.remove(currentId)
                    }
                }
            }
        }
    }

    private fun hideUserMoments(userId: Long) {
        viewModelScope.launch {
            runCatching {
                filterOutUserIdMoments(userId)
                hideUserMomentsUseCase.invoke(listOf(userId))
            }.onSuccess {
                liveMomentMessageState.postValue(MomentMessageState.ShowSuccess(R.string.user_complain_moments_hidden))
                logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.USER_MOMENT_HIDE)
            }.onFailure {
                Timber.e(it)
                liveMomentMessageState.postValue(MomentMessageState.ShowError(R.string.error_try_later))
            }
        }
    }

    private fun showUserMoments(userId: Long) {
        viewModelScope.launch {
            runCatching {
                updateShowUsersMomentsStatus(userId, doNotShowUser = false)
                showUserMomentsUseCase.invoke(listOf(userId))
            }.onSuccess {
                liveMomentMessageState.postValue(MomentMessageState.ShowSuccess(R.string.user_complain_moments_show))
            }.onFailure {
                Timber.e(it)
                liveMomentMessageState.postValue(MomentMessageState.ShowError(R.string.error_try_later))
            }
        }
    }

    private fun updateShowUsersMomentsStatus(userId: Long, doNotShowUser: Boolean) {
        val modifiedMoments = currentGroup?.moments?.map {
            if (it.userId == userId) {
                it.copy(doNotShowUser = doNotShowUser)
            } else {
                it
            }
        }
        val currentMomentModified =
            currentItem?.takeIf { it.userId == userId }?.copy(doNotShowUser = doNotShowUser)
        modifiedMoments?.let { currentGroup = currentGroup?.copy(moments = it) }
        currentMomentModified?.let { currentItem = it }
        updateMomentState()
    }

    private fun filterOutUserIdMoments(userId: Long) {
        val group = currentGroup ?: run {
            goToNextGroupRequest(null)
            return
        }
        val item = currentItem ?: return
        val currentIndex = group.moments.indexOf(item)
        var nextItem: MomentItemUiModel? = null
        val filteredMoments = group.moments.filterIndexed { index, moment ->
            if (index > currentIndex && moment.userId != userId && nextItem == null) nextItem = moment
            moment.userId != userId
        }
        when {
            filteredMoments.isEmpty() || nextItem == null -> {
                goToNextGroupRequest(
                    groupId = group.id,
                    invalidateCurrentGroup = filteredMoments.isEmpty()
                )
            }

            else -> {
                currentGroup = group.copy(moments = filteredMoments)
                currentItem = nextItem
                updateLoadResourcesState(loadResources = true)
                updateMomentState()
                updateViewCountForCurrentMomentIfNeeded()
                managePreloadIfNeeded()
            }
        }
    }

    private fun onClickNextOrPreviousPositionMoment(
        navigationType: MomentNavigationType,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped,
    ) {
        if (currentGroup == null) return
        setCurrentItem(navigationType, howUserFlipMoment)
    }

    private fun setCurrentItem(
        navigationType: MomentNavigationType,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped
    ) {
        val currentGroup = currentGroup ?: run {
            momentNavigation.postValue(ViewMomentNavigationState.CloseScreenRequest)
            return
        }
        val groupMoments = currentGroup.moments
        cancelMomentViewedTimer()
        if (navigationType == MomentNavigationType.NEXT && isLastMoment(groupMoments)) {
            goToNextGroupRequest(groupId = currentGroup.id, howUserFlipMoment = howUserFlipMoment)
            return
        }
        if (navigationType == MomentNavigationType.PREVIOUS && isFirstMoment(groupMoments)) {
            momentNavigation.postValue(
                ViewMomentNavigationState.GoToPreviousGroupRequest(
                    currentGroupId = currentGroup.id,
                    howUserFlipMoment = howUserFlipMoment
                )
            )
            return
        }
        currentItem = when (navigationType) {
            MomentNavigationType.NEXT -> getNextMoment(groupMoments)
            MomentNavigationType.PREVIOUS -> getPreviousMoment(groupMoments)
        }

        updateLoadResourcesState(loadResources = true)
        updateIsPreviewLoadedState(isPreviewLoaded = false)
        updateMomentState()
        updateViewCountForCurrentMomentIfNeeded()
        managePreloadIfNeeded()
        logAmplitudeFlipMoment(howUserFlipMoment)
    }

    private fun onContentRequested() {
        updateLoadResourcesState(loadResources = true)
        updateMomentState()
    }

    private fun onContentLoaded(isPreview: Boolean) {
        if (isPreview) {
            updateIsPreviewLoadedState(isPreviewLoaded = true)
        } else {
            updateLoadResourcesState(loadResources = false)
        }
        updateMomentState()
    }

    private fun onPlaybackResumed() {
        startMomentViewedTimerIfNeeded()
        if (requestedPlaybackState == MomentPlaybackState.STARTED) {
            movePlayerToPlaybackState(MomentPlaybackState.RESUMED)
        }
        val current = currentItem ?: return
        setMomentViewed(current.id)
    }

    private fun onPlaybackEnded() {
        cancelMomentViewedTimer()
        currentItem?.let { current -> setMomentViewed(current.id) }
        setCurrentItem(
            MomentNavigationType.NEXT,
            AmplitudePropertyMomentHowFlipped.AUTO_FLIP
        )
    }

    private fun onPlaybackPaused() {
        movePlayerToPlaybackState(MomentPlaybackState.STOPPED)
    }

    private fun updateLoadResourcesState(loadResources: Boolean) {
        this.loadResources = loadResources
    }

    private fun updateIsPreviewLoadedState(isPreviewLoaded: Boolean) {
        this.isPreviewLoaded = isPreviewLoaded
    }

    private fun updateIsActiveItemState(isActive: Boolean) {
        isActiveItem = isActive
    }

    private fun checkInitScreenAction(commentId: Long?, moment: MomentItemUiModel?) {
        if (commentId == null || commentId == NO_COMMENT_ID
            || commentId == DEFAULT_COMMENT_ID || moment?.isInteractionAllowed() != true) return

        momentScreenAction.postValue(
            MomentScreenActionEvent.OpenCommentBottomSheet(commentId)
        )
    }

    private fun managePreloadIfNeeded() {
        preloadController.momentGroupUpdated(currentItem, currentGroup)
    }

    private fun updateMomentState() {
        val timelineState = MomentTimelineState(
            currentBar = currentGroup?.moments?.indexOf(currentItem) ?: 0,
            totalBars = currentGroup?.moments?.size ?: 1
        )

        viewModelScope.launch {
            _liveMomentState.emit(
                PositionViewMomentState.UpdateMoment(
                    momentItemModel = currentItem,
                    timelineState = timelineState,
                    loadResources = loadResources,
                    isActiveItem = isActiveItem,
                    isPreviewLoaded = isPreviewLoaded,
                    playbackState = requestedPlaybackState,
                    error = errorState
                )
            )
            _liveMomentMeeraState.emit(
                MeeraPositionViewMomentState.UpdateMoment(
                    momentItemModel = currentItem,
                    timelineState = timelineState,
                    loadResources = loadResources,
                    isActiveItem = isActiveItem,
                    isPreviewLoaded = isPreviewLoaded,
                    playbackState = requestedPlaybackState,
                    error = errorState
                )
            )
        }
    }

    /**
     * Start the moment viewed timer.
     * To actually start, it has to meet following requirements:
     *  1) [SetMomentViewedTimer.forbidSettingAsViewed] must not be set to true for current momentId
     *  2) [loadResources] must be true
     *  3) [requestedPlaybackState] must be EITHER [MomentPlaybackState.STARTED] OR [MomentPlaybackState.RESUMED]
     *  4) [MomentItemUiModel.isViewed] must be false
     *  5) [MomentItemUiModel.userId] must NOT be equal to [AppSettings.readUId]
     *  6) [setMomentViewedTimer] must be null
     */
    private fun startMomentViewedTimerIfNeeded() {
        val moment = currentItem ?: return
        if (moment.id == setMomentViewedTimer?.momentId && setMomentViewedTimer?.forbidSettingAsViewed == true) return
        if (moment.id != setMomentViewedTimer?.momentId) cancelMomentViewedTimer()
        if (checkIfCurrentPlayerStateIsValidForViewTimer()) return
        if (checkIfCanSetCurrentAsViewed(moment).not()) return

        if (setMomentViewedTimer == null) {
            Timber.tag(MOMENT_VIEW_TAG).d("creating viewed timer for id=${moment.id}")
            setMomentViewedTimer = SetMomentViewedTimer(moment.id)
            setMomentViewedTimer?.start()
        }
    }

    private fun setMomentViewed(momentId: Long) {
        val group = currentGroup ?: kotlin.run {
            val current = currentItem?.takeIf { it.id == momentId } ?: return
            setMomentViewed(momentId = momentId, moment = current)
            return
        }
        val watchedItem = group.moments.find { it.id == momentId } ?: return
        setMomentViewed(momentId = momentId, moment = watchedItem)
        val modifiedMoments = group.moments.map {
            if (it.id == momentId) it.copy(isViewed = true) else it
        }
        currentGroup = group.copy(moments = modifiedMoments)
        val current = currentItem?.takeIf { it.id == momentId } ?: return
        currentItem = current.copy(isViewed = true)
    }

    private fun setMomentViewed(momentId: Long, moment: MomentItemUiModel) {
        if (checkIfCanSetCurrentAsViewed(moment).not()) return

        viewModelScope.launch {
            runCatching {
                Timber.tag(MOMENT_VIEW_TAG).d("setting moment as viewed(id=$momentId)")
                Timber.tag(MOMENT_VIEW_TAG).d("setting moment as viewed(moment=$moment)")
                setMomentViewedUseCase.invoke(momentId, moment.userId)
            }.onFailure { Timber.e(it) }
        }
    }

    private fun checkIfCurrentPlayerStateIsValidForViewTimer(): Boolean {
        return loadResources || (requestedPlaybackState != MomentPlaybackState.RESUMED
            && requestedPlaybackState != MomentPlaybackState.STARTED)
    }

    private fun checkIfCanSetCurrentAsViewed(moment: MomentItemUiModel): Boolean {
        return moment.isActive && moment.isDeleted.not() && moment.isAccessDenied.not()
            && moment.isViewed.not() && appSettings.readUID() != 0L
    }

    /**
     * Cancel the View Timer
     *
     * If no arguments are provided, then we just cancel() it and reset to null
     *
     * If either momentId or userId is provided, then(if they match the momentId of the timer)
     * we cancel() it and set [SetMomentViewedTimer.forbidSettingAsViewed] property.
     *
     * This property helps us avoid recreating a timer for momentId
     * immediately after canceling it
     */
    private fun cancelMomentViewedTimer(
        forbidSettingAsViewedForMomentId: Long? = null,
        forbidSettingAsViewedForUserId: Long? = null
    ) {
        if (forbidSettingAsViewedForMomentId == null && forbidSettingAsViewedForUserId == null) {
            setMomentViewedTimer?.cancel()
            setMomentViewedTimer = null
            return
        }
        if (forbidSettingAsViewedForMomentId != null && forbidSettingAsViewedForMomentId == setMomentViewedTimer?.momentId) {
            setMomentViewedTimer?.forbidSettingAsViewed = true
            setMomentViewedTimer?.cancel()
            return
        }
        val timerMomentId = setMomentViewedTimer?.momentId
        if (forbidSettingAsViewedForUserId != null && timerMomentId != null) {
            val matchingUser = currentGroup?.moments?.any {
                it.id == timerMomentId && it.userId == forbidSettingAsViewedForUserId
            } ?: return
            if (matchingUser) {
                setMomentViewedTimer?.forbidSettingAsViewed = true
                setMomentViewedTimer?.cancel()
            }
        }
    }

    private fun deleteCurrentMoment() {
        val currentMomentId = currentItem?.id ?: return
        val currentMomentUserId = currentItem?.userId ?: return
        viewModelScope.launch {
            runCatching {
                deleteMomentUseCase.invoke(momentId = currentMomentId, userId = currentMomentUserId)
                delegate.initialLoadMoments()
                filterOutMomentId(currentMomentId)
            }.onSuccess {
                amplitudeMoment.onMomentDelete(momentId = currentMomentId)
            }.onFailure {
                Timber.e(it)
                liveMomentMessageState.postValue(MomentMessageState.ShowError(R.string.error_try_later))
            }
        }
    }

    private fun filterOutMomentId(momentId: Long) {
        val group = currentGroup ?: kotlin.run {
            goToNextGroupRequest(null)
            return
        }
        val isFirst = isFirstMoment(group.moments)
        val isLast = isLastMoment(group.moments)
        val nextMoment = when {
            isLast -> getPreviousMoment(group.moments)
            else -> getNextMoment(group.moments)
        }
        when {
            isFirst && isLast -> {
                invalidateCurrentGroup()
                return
            }
        }
        currentGroup = currentGroup?.copy(moments = group.moments.filter { it.id != momentId })
        currentItem = nextMoment
        updateLoadResourcesState(loadResources = true)
        updateMomentState()
        updateViewCountForCurrentMomentIfNeeded()
        managePreloadIfNeeded()
    }

    private fun invalidateCurrentGroup() {
        momentNavigation.postValue(ViewMomentNavigationState.InvalidateCurrentGroup)
    }

    private fun goToNextGroupRequest(
        groupId: Long?,
        invalidateCurrentGroup: Boolean = false,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped? = null,
    ) {
        if (groupId == null) {
            momentNavigation.postValue(ViewMomentNavigationState.CloseScreenRequest)
            return
        }
        momentNavigation.postValue(
            ViewMomentNavigationState.GoToNextGroupRequest(
                currentGroupId = groupId,
                invalidateCurrentGroup = invalidateCurrentGroup,
                howUserFlipMoment = howUserFlipMoment
            )
        )
    }

    private fun setMomentCommentAvailability(commentAvailability: CommentsAvailabilityType) {
        val currentMomentId = currentItem?.id ?: return
        viewModelScope.launch {
            runCatching {
                allowCommentsUseCase.invoke(
                    momentId = currentMomentId,
                    commentAvailability = commentAvailability
                )
            }.onFailure {
                Timber.e(it)
                liveMomentMessageState.postValue(MomentMessageState.ShowError(R.string.error_while_sending_settings))
            }
        }
    }

    private fun complaintToMoment() {
        viewModelScope.launch {
            if (networkStatusProvider.isInternetConnected()) {
                runCatching {
                    val momentId = currentItem?.id

                    if (momentId == null) {
                        momentScreenAction.postValue(MomentScreenActionEvent.ShowCommonError(R.string.error_try_later))
                        return@launch
                    }

                    complainOnMomentUseCase.invoke(momentId = momentId)
                }.onSuccess {
                    momentScreenAction.postValue(MomentScreenActionEvent.OpenComplaintMenu)
                }.onFailure {
                    momentScreenAction.postValue(MomentScreenActionEvent.ShowCommonError(R.string.error_try_later))
                }
            } else {
                momentScreenAction.postValue(MomentScreenActionEvent.ShowCommonError(R.string.error_try_later))
            }
        }
        logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.REPORT_MOMENT)
    }

    private fun getMomentDataForScreenshotPopup(momentId: Long) {
        viewModelScope.launch {
            val link = runCatching {
                getMomentLinkUseCase.invoke(momentId)
            }.getOrNull() ?: return@launch
            momentNavigation.postValue(ViewMomentNavigationState.ShowScreenshotPopup(link.deepLinkUrl))
        }
    }

    private fun isFirstMoment(
        groupMoments: List<MomentItemUiModel>,
    ): Boolean {
        val index = getMomentIndexInGroup(
            groupMoments = groupMoments,
            navigationType = MomentNavigationType.PREVIOUS
        )
        return index < 0
    }

    private fun isLastMoment(
        groupMoments: List<MomentItemUiModel>
    ): Boolean {
        val index = getMomentIndexInGroup(
            groupMoments = groupMoments,
            navigationType = MomentNavigationType.NEXT
        )
        return index > groupMoments.size - 1
    }

    private fun getPreviousMoment(
        groupMoments: List<MomentItemUiModel>
    ): MomentItemUiModel? {
        val index = getMomentIndexInGroup(
            groupMoments = groupMoments,
            navigationType = MomentNavigationType.PREVIOUS
        )
        if (isFirstMoment(groupMoments = groupMoments)) return null
        return currentGroup?.moments?.get(index)
    }

    private fun getNextMoment(
        groupMoments: List<MomentItemUiModel>
    ): MomentItemUiModel? {
        val index = getMomentIndexInGroup(
            groupMoments = groupMoments,
            navigationType = MomentNavigationType.NEXT
        )
        if (isLastMoment(groupMoments = groupMoments)) return null
        return currentGroup?.moments?.get(index)
    }

    private fun getMomentIndexInGroup(
        groupMoments: List<MomentItemUiModel>,
        navigationType: MomentNavigationType
    ): Int {
        return when (navigationType) {
            MomentNavigationType.NEXT -> {
                groupMoments.indexOf(currentItem) + 1
            }

            MomentNavigationType.PREVIOUS -> {
                groupMoments.indexOf(currentItem) - 1
            }
        }
    }

    fun isUserAuthorized() = isUserAuthorizedUseCase.invoke()

    private inner class SetMomentViewedTimer(val momentId: Long) : CountDownTimer(
        DEFAULT_MOMENT_CONTENT_VIEWED_TIMER_LENGTH_MS,
        DEFAULT_MOMENT_CONTENT_VIEWED_TIMER_LENGTH_MS
    ) {

        var forbidSettingAsViewed = false

        override fun onTick(millisUntilFinished: Long) = Unit

        override fun onFinish() {
            if (forbidSettingAsViewed) return
            viewModelScope.launch {
                setMomentViewed(momentId)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    sealed class ErrorState {
        object MomentNotFound : ErrorState()
        object UnknownError : ErrorState()

        companion object {
            fun mapException(exception: Throwable): ErrorState {
                return MomentNotFound
            }
        }
    }
}

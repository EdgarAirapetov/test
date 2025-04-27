package com.numplates.nomera3.modules.moments.show.presentation.viewevents

import com.numplates.nomera3.modules.moments.show.domain.CommentsAvailabilityType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupPositionType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel

sealed class PositionViewMomentEvent {

    object PausePositionMoment : PositionViewMomentEvent()

    object ResumePositionMoment : PositionViewMomentEvent()

    object OnSettingsOpened : PositionViewMomentEvent()

    object OnUserProfileOpened : PositionViewMomentEvent()

    object ClickedNextPositionMoment : PositionViewMomentEvent()

    object CopyLink : PositionViewMomentEvent()

    object ComplainToMoment : PositionViewMomentEvent()

    object ClickedPrevPositionMoment : PositionViewMomentEvent()

    data class OnFragmentResumed(val isDialogsCreated: Boolean) : PositionViewMomentEvent()

    data class OnFragmentPaused(
        val isNowOffscreenInPager: Boolean,
        val groupPositionType: MomentGroupPositionType?
    ) : PositionViewMomentEvent()

    object OnAppHidden : PositionViewMomentEvent()

    object MomentContentRequested : PositionViewMomentEvent()

    data class MomentContentLoaded(val isPreview: Boolean) : PositionViewMomentEvent()

    object MomentPlaybackResumed : PositionViewMomentEvent()

    object MomentPlaybackPaused : PositionViewMomentEvent()

    object MomentPlaybackEnded : PositionViewMomentEvent()

    object DeletePositionMoment : PositionViewMomentEvent()

    data class SetMomentCommentAvailability(
        val commentsAvailabilityType: CommentsAvailabilityType
    ) : PositionViewMomentEvent()

    data class SubscribeToUser(val userId: Long) : PositionViewMomentEvent()

    data class UnsubscribeFromUser(val userId: Long) : PositionViewMomentEvent()

    data class HideUserMoments(val userId: Long) : PositionViewMomentEvent()

    data class ShowUserMoments(val userId: Long): PositionViewMomentEvent()

    data class UserMomentsHiddenAfterComplain(val userId: Long) : PositionViewMomentEvent()

    data class OnRepostMoment(val momentItemUiModel: MomentItemUiModel): PositionViewMomentEvent()

    data class OnGetCommentCount(val momentItemUiModel: MomentItemUiModel): PositionViewMomentEvent()

    data class OnGetViewsCount(val momentItemUiModel: MomentItemUiModel): PositionViewMomentEvent()

    data class ScreenshotTaken(val momentId: Long) : PositionViewMomentEvent()
}

package com.numplates.nomera3.modules.moments.show.presentation.viewstates

import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowFlipped
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentNavigationType

sealed class ViewMomentNavigationState {

    sealed class GroupNavigation(
        val currentGroupId: Long,
        val invalidateCurrentGroup: Boolean = false,
        val navigationType: MomentNavigationType,
        val howUserFlipMoment: AmplitudePropertyMomentHowFlipped?
    ) : ViewMomentNavigationState()

    class GoToNextGroupRequest(
        currentGroupId: Long,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped? = null,
        invalidateCurrentGroup: Boolean = false
    ) : GroupNavigation(
        currentGroupId,
        invalidateCurrentGroup,
        MomentNavigationType.NEXT,
        howUserFlipMoment
    )

    class GoToPreviousGroupRequest(
        currentGroupId: Long,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped? = null,
        invalidateCurrentGroup: Boolean = false
    ) : GroupNavigation(
        currentGroupId,
        invalidateCurrentGroup,
        MomentNavigationType.PREVIOUS,
        howUserFlipMoment
    )

    object CloseScreenRequest : ViewMomentNavigationState()

    object InvalidateCurrentGroup : ViewMomentNavigationState()

    data class ShowScreenshotPopup(val momentLink: String) : ViewMomentNavigationState()
}

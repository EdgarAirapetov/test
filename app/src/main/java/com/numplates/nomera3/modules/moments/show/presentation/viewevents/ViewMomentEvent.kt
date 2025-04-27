package com.numplates.nomera3.modules.moments.show.presentation.viewevents

import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.presentation.fragment.MomentsFragmentClosingAnimationState

sealed class ViewMomentEvent {
    data class FetchMoments(
        val momentsSource: GetMomentDataUseCase.MomentsSource,
        val userId: Long?,
        var targetMomentId: Long? = null,
        val singleMomentId: Long? = null
    ) : ViewMomentEvent()

    data class ChangedClosingState(val momentsFragmentClosingState: MomentsFragmentClosingAnimationState) :
        ViewMomentEvent()
}

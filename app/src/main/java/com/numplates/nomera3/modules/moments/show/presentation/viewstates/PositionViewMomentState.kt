package com.numplates.nomera3.modules.moments.show.presentation.viewstates

import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentPositionViewModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource

sealed class PositionViewMomentState {

    data class UpdateMoment(
        val timelineState: MomentTimelineState? = null,
        val playbackState: MomentPlaybackState? = null,
        val reactionSource: ReactionSource? = null,
        val loadResources: Boolean = false,
        val isActiveItem: Boolean = false,
        val isPreviewLoaded: Boolean = false,
        val error: ViewMomentPositionViewModel.ErrorState? = null,
        val momentItemModel: MomentItemUiModel? = null
    ) : PositionViewMomentState() {

        fun hasError() = error != null
    }

    data class LinkCopied(
        val copyLink: String = "",
    ) : PositionViewMomentState()
}

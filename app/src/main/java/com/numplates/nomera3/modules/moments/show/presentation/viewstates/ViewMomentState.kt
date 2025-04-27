package com.numplates.nomera3.modules.moments.show.presentation.viewstates

import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel

sealed class ViewMomentState {

    class MomentsDataReceived(
        val momentGroups: List<MomentGroupUiModel>
    ) : ViewMomentState()

    class MomentsPaginatedDataReceived(
        val momentGroups: List<MomentGroupUiModel>
    ) : ViewMomentState()
}

package com.numplates.nomera3.modules.moments.show.presentation.viewstates

import androidx.annotation.StringRes

sealed class MomentScreenActionEvent {
    data class OpenCommentBottomSheet(
        val commentToOpenId:Long,
    ): MomentScreenActionEvent()

    data class ShowCommonError(@StringRes val messageResId: Int) : MomentScreenActionEvent()

    object OpenComplaintMenu : MomentScreenActionEvent()
}

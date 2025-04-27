package com.numplates.nomera3.presentation.view.utils.sharedialog

import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.share.ui.entity.UIShareMessageEntity

sealed class ShareDialogType {

    class ShareProfile(val userId: Long) : ShareDialogType()

    class ShareCommunity(val groupId: Int) : ShareDialogType()

    data object SharePost : ShareDialogType()

    class ShareMoment(val moment: MomentItemUiModel) : ShareDialogType()

    class MessageForwarding(val data: UIShareMessageEntity) : ShareDialogType()
}

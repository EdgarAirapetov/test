package com.numplates.nomera3.presentation.view.utils.sharedialog

import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel

sealed class ShareBottomSheetEvent {

    object OnSuccessShareProfile: ShareBottomSheetEvent()

    object OnErrorShareProfile: ShareBottomSheetEvent()

    class OnSuccessShareMoment(val momentItemUiModel: MomentItemUiModel): ShareBottomSheetEvent()

    object OnErrorShareMoment: ShareBottomSheetEvent()

    object OnSuccessShareCommunity: ShareBottomSheetEvent()

    object OnErrorShareCommunity: ShareBottomSheetEvent()

    object OnMoreShareButtonClick: ShareBottomSheetEvent()

    object OnErrorUnselectedUser: ShareBottomSheetEvent()

    object OnClickFindFriendButton: ShareBottomSheetEvent()

    class OnSuccessForwardChatMessage(val text: String): ShareBottomSheetEvent()

    class OnErrorForwardChatMessage(val message: String): ShareBottomSheetEvent()

    object OnFailForwardChatMessage: ShareBottomSheetEvent()

    object OnDismissDialog: ShareBottomSheetEvent()
}

package com.numplates.nomera3.presentation.viewmodel.viewevents

import androidx.annotation.StringRes
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePlaceHolderEnum

sealed class SharePostViewEvent {
    //road repost
    object onErrorRoadTypeRepost: SharePostViewEvent()
    object onSuccessRoadTypeRepost: SharePostViewEvent()

    class onSuccessSharePostLink(val postLink: String): SharePostViewEvent()
    object onErrorSharePostLink: SharePostViewEvent()

    class onSuccessShareMomentLink(val momentLink: String): SharePostViewEvent()
    object onErrorShareMomentLink: SharePostViewEvent()

    //chat message repost
    class OnErrorMessageRepost(val message: String? = null): SharePostViewEvent()
    data class onSuccessMessageRepost(val repostTargetCount: Int): SharePostViewEvent()

    //group repost
    class onErrorGroupRepost(val errorMessage: String): SharePostViewEvent()
    object onSuccessGroupRepost: SharePostViewEvent()

    // share user profile
    object OnSuccessShareUserProfile: SharePostViewEvent()
    class OnErrorShareUserProfile(val message: String): SharePostViewEvent()
    object OnFailShareUserProfile: SharePostViewEvent()

    // share moment
    class OnSuccessShareMoment(val momentItemUiModel: MomentItemUiModel?): SharePostViewEvent()
    class OnFailShareMoment(@StringRes val message: Int? = null, val messageText: String? = null): SharePostViewEvent()

    // share community
    object OnSuccessShareCommunity: SharePostViewEvent()
    class OnErrorShareCommunity(val message: String): SharePostViewEvent()
    object OnFailShareCommunity: SharePostViewEvent()

    class OnSuccessForwardChatMessage(val text: String): SharePostViewEvent()
    class OnErrorForwardChatMessage(val message: String): SharePostViewEvent()
    object OnFailForwardChatMessage: SharePostViewEvent()

    class PlaceHolderShareEvent(val placeHolder: SharePlaceHolderEnum): SharePostViewEvent()
    object BlockSendBtn : SharePostViewEvent()
    object UnBlockSendBtn : SharePostViewEvent()
}

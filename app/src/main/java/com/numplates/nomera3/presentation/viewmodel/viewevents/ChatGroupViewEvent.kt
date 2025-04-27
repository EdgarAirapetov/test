package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.userprofile.UserSimple

sealed class ChatGroupViewEvent {


    class GroupChatCreated : ChatGroupViewEvent() {
        var dialog: DialogEntity? = null
    }

    object GroupChatDeleted : ChatGroupViewEvent()

    object AdminsAdded : ChatGroupViewEvent()

    object MembersAdded : ChatGroupViewEvent()

    object SuccessTitleChanged : ChatGroupViewEvent()

    object SuccessDescriptionChanged : ChatGroupViewEvent()

    class CompleteAvatarChangedWhenEdit(
        val roomId: Long?,
        val name: String,
        val description: String
    ): ChatGroupViewEvent()

    class SuccessAvatarChangedWhenCreateChat(val room: DialogEntity?): ChatGroupViewEvent()

    class ErrorAvatarChangedWhenCreateChat(val room: DialogEntity?): ChatGroupViewEvent()

    class OnSuccessBlockUser: ChatGroupViewEvent() {
        var isBlock: Boolean? = null
    }

    object OnFailureBlockUser: ChatGroupViewEvent()

    object ErrorLoadFriendList : ChatGroupViewEvent()

    class ErrorUserMessage : ChatGroupViewEvent() {
        var mesage: String? = null
    }

    object ErrorChatDeleted : ChatGroupViewEvent()

    // ------ Call user settings ----------
    object ErrorSaveSetting : ChatGroupViewEvent()

    class OnUserAvatarClicked(val user: UserSimple) : ChatGroupViewEvent()

    class OnSuccessReloadDialogs(val room: DialogEntity?): ChatGroupViewEvent()

    object ErrorChooseFriends : ChatGroupViewEvent()

    class EditChatUsers(val roomId: Long? = null) : ChatGroupViewEvent()

}

sealed interface ChatGroupEffect {
    class EditChatUsers(val roomId: Long? = null): ChatGroupEffect
}

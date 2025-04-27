package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.content.Context
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel


fun Context.createSubscribedFriendCancelRequest(
        cancelRequest: () -> Unit,
        cancelRequestAndUnsubscribe: () -> Unit
): ConfirmDialogBuilder {
    return ConfirmDialogBuilder()
            .setHeader(getString(R.string.cancel_friendship_request_dialog_header))
            .setDescription(getString(R.string.your_friend_request_will_be_deleted))
            .setHorizontal(true)
            .setTopBtnText(getString(R.string.cancel_request_caps))
            .setMiddleBtnText(getString(R.string.cancel_friendship_request_dialog_cancel_request_and_unsub))
            .setBottomBtnText(getString(R.string.user_info_remove_from_friend_dialog_cancel))
            .setTopClickListener {
                cancelRequest.invoke()
            }
            .setMiddleClickListener {
                cancelRequestAndUnsubscribe.invoke()
            }
}

fun Context.createSubscribedFriendRemovalDialog(
    friendName: String = "",
    cancelRequest: () -> Unit,
    cancelRequestAndUnsubscribe: () -> Unit
): ConfirmDialogBuilder {
    return ConfirmDialogBuilder()
        .setHeader(getString(R.string.user_info_remove_from_friend_dialog_header))
        .setDescription(getString(R.string.remove_from_friend_desc_text, friendName))
        .setHorizontal(true)
        .setTopBtnText(getString(R.string.user_info_remove_from_friend_dialog_remove))
        .setMiddleBtnText(getString(R.string.user_info_remove_from_friend_dialog_remove_and_unsub))
        .setBottomBtnText(getString(R.string.user_info_remove_from_friend_dialog_cancel))
        .setTopClickListener {
            cancelRequest.invoke()
        }
        .setMiddleClickListener {
            cancelRequestAndUnsubscribe.invoke()
        }
}
fun Context.createUnsubscribedFriendRemovalDialog(
        friend: FriendModel,
        cancelRequest: () -> Unit
): ConfirmDialogBuilder {
    return ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_remove_from_friend_dialog_header))
            .setDescription(getString(R.string.user_info_remove_from_friend_dialog_description, friend.userModel.name))
            .setLeftBtnText(getString(R.string.user_info_remove_from_friend_dialog_cancel))
            .setRightBtnText(getString(R.string.user_info_remove_from_friend_dialog_remove))
            .setRightClickListener {
                cancelRequest.invoke()
            }
}

fun Context.createUnsubscribedFriendRemovalDialog(cancelRequest: () -> Unit): ConfirmDialogBuilder {
    return ConfirmDialogBuilder()
            .setHeader(getString(R.string.cancel_friendship_request_dialog_header))
            .setDescription(getString(R.string.your_friend_request_will_be_deleted))
            .setLeftBtnText("ЗАКРЫТЬ")
            .setRightBtnText("ОТМЕНИТЬ")
            .setRightClickListener {
                cancelRequest.invoke()
            }
}

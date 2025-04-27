package com.numplates.nomera3.modules.search.ui

import android.content.Context
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserViewModel
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

class AddFriendBottomSheet(
    val user: UserSearchResultUiEntity,
    val viewModel: SearchUserViewModel,
    context: Context
) : MeeraMenuBottomSheet(context) {
    init {
        val isSubscribed = user.isSubscribed

        when (user.friendStatus) {
            FRIEND_STATUS_NONE -> {
                addDescriptionItem(
                    R.string.add_to_friends,
                    R.drawable.ic_add_friend_purple,
                    R.string.add_friend_descr
                ) {
                    viewModel.addUserToFriend(user)
                }
            }
            FRIEND_STATUS_INCOMING -> {
                addDescriptionItem(
                    R.string.accept_request,
                    R.drawable.ic_add_friend_purple,
                    R.string.accept_request_descr
                ) {
                    viewModel.acceptUserFriendRequest(user)
                }
                addDescriptionItem(
                    R.string.reject_friend_request,
                    R.drawable.ic_reject_friend_red,
                    R.string.reject_friend_descr
                ) {
                    viewModel.declineUserFriendRequest(user)
                }
            }
        }

        if (!isSubscribed) {
            addDescriptionItem(
                R.string.general_subscribe,
                R.drawable.ic_subscribe_on_user,
                R.string.subscribe_descr
            ) {
                viewModel.subscribeUser(user)
            }
        } else {
            addDescriptionItem(
                R.string.unsubscribe,
                R.drawable.ic_unsubscribe_purple,
                R.string.unsubscribe_desc
            ) {
                showConfirmDialogUnsubscribe(user)
            }
        }
    }

    private fun showConfirmDialogUnsubscribe(user: UserSearchResultUiEntity) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.user_info_unsub_dialog_description))
            .setLeftBtnText(getString(R.string.user_info_unsub_dialog_close))
            .setRightBtnText(getString(R.string.user_info_unsub_dialog_action))
            .setRightClickListener {
                viewModel.unsubscribeUser(user)
            }
            .show(parentFragmentManager)
    }
}

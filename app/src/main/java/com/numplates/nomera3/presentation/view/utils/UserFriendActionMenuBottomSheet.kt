package com.numplates.nomera3.presentation.view.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.App
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.R
import com.numplates.nomera3.REQUEST_NOT_CONFIRMED_BY_ME
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendActionViewEvent
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.viewmodel.UserFriendActionViewModel

/**
 * Диалог предназначен для того, чтоб показать какие есть возможные действия для юзера:
 * "Добавить в друзья/Подписаться/Отписаться/Отклонить заявку в друзья"
 * @param selectedUser - Данные выбранного юзера
 * @param successFinishListener Лямбда вызывается тогда, когда какой-нибудь запрос отработал успешно
 * @param errorFinishListener - Вызывается, когда запрос отработал неудачно.
 */
class UserFriendActionMenuBottomSheet constructor(
    private val selectedUser: FriendsFollowersUiModel,
    private val fragment: Fragment,
    private val screenMode: Int,
    private val successFinishListener: ((successMsgRes: Int) -> Unit)? = null,
    private val errorFinishListener: ((errorMsgRes: Int) -> Unit)? = null,
) {

    private val viewModel by fragment.viewModels<UserFriendActionViewModel> {
        App.component.getViewModelFactory()
    }
    private var menuBottomSheet: MeeraMenuBottomSheet? = null

    init {
        initDialog()
        observeViewEvent()
        val isSubscribed = selectedUser.userSimple?.settingsFlags?.subscription_on ?: 0 == 1

        when (selectedUser.userSimple?.settingsFlags?.friendStatus) {
            FRIEND_STATUS_NONE -> {
                menuBottomSheet?.addDescriptionItem(
                    R.string.add_to_friends,
                    R.drawable.ic_add_friend_purple,
                    R.string.add_friend_descr
                ) {
                    viewModel.logAddToFriendAmplitude(
                        userId = selectedUser.userSimple.userId,
                        screenMode = screenMode,
                        approved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple.topContentMaker.toBoolean()
                    )
                    viewModel.addToFriendSocket(
                        model = selectedUser,
                        isAcceptFriendRequest = false
                    )
                }
            }
            REQUEST_NOT_CONFIRMED_BY_ME -> {
                menuBottomSheet?.addDescriptionItem(
                    R.string.accept_request,
                    R.drawable.ic_add_friend_purple,
                    R.string.accept_request_descr
                ) {
                    viewModel.addToFriendSocket(
                        model = selectedUser,
                        isAcceptFriendRequest = true
                    )
                }
                menuBottomSheet?.addDescriptionItem(
                    R.string.reject_friend_request,
                    R.drawable.ic_reject_friend_red,
                    R.string.reject_friend_descr
                ) {
                    viewModel.declineUserFriendRequest(selectedUser)
                }
            }
        }

        if (!isSubscribed) {
            menuBottomSheet?.addDescriptionItem(
                R.string.general_subscribe,
                R.drawable.ic_subscribe_on_user,
                R.string.subscribe_descr
            ) {
                viewModel.logFollowAmplitude(
                    userId = selectedUser.userSimple?.userId ?: 0,
                    screenMode = screenMode,
                    accountApproved = selectedUser.isAccountApproved,
                    topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                )
                viewModel.subscribeUser(selectedUser)
            }
        } else {
            menuBottomSheet?.addDescriptionItem(
                R.string.unsubscribe,
                R.drawable.ic_unsubscribe_purple,
                R.string.unsubscribe_desc
            ) {
                showConfirmDialogUnsubscribe(selectedUser)
            }
        }
    }

    fun show() = menuBottomSheet?.show(fragment.childFragmentManager)

    private fun showConfirmDialogUnsubscribe(model: FriendsFollowersUiModel) {
        menuBottomSheet?.let { bottomSheet ->
            ConfirmDialogBuilder()
                .setHeader(bottomSheet.getString(R.string.user_info_unsub_dialog_header))
                .setDescription(bottomSheet.getString(R.string.user_info_unsub_dialog_description))
                .setLeftBtnText(bottomSheet.getString(R.string.user_info_unsub_dialog_close))
                .setRightBtnText(bottomSheet.getString(R.string.user_info_unsub_dialog_action))
                .setRightClickListener {
                    viewModel.logUnfollowAmplitudeAction(
                        userId = selectedUser.userSimple?.userId ?: 0,
                        screenMode = screenMode,
                        accountApproved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                    )
                    viewModel.unsubscribeUser(model)
                }
                .show(fragment.parentFragmentManager)
        }
    }

    private fun observeViewEvent() {
        fragment.lifecycleScope.launchWhenStarted {
            viewModel.userFriendActionSharedFlow.collect { event ->
                handleViewEvent(event)
            }
        }
    }

    private fun handleViewEvent(event: UserFriendActionViewEvent) {
        when (event) {
            is UserFriendActionViewEvent.ShowSuccessSnackBar -> {
                successFinishListener?.invoke(event.messageRes)
            }
            is UserFriendActionViewEvent.ShowErrorSnackBar -> {
                errorFinishListener?.invoke(event.errorMessageRes)
            }
        }
    }

    private fun initDialog() {
        menuBottomSheet = MeeraMenuBottomSheet(fragment.context)
    }
}

package com.numplates.nomera3.modules.userprofile.ui.controller

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.meera.core.utils.checkAppRedesigned
import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.complains.ui.reason.UserComplainReasonBottomSheet
import com.numplates.nomera3.modules.user.ui.event.UserProfileDialogNavigation
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.userRole
import com.numplates.nomera3.modules.userprofile.utils.copyProfileLink
import com.numplates.nomera3.presentation.view.fragments.ProfileSettingsFragmentNew
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

class ProfileDialogController(
    private val submitUIAction: (UserProfileUIAction) -> Unit,
) {

    fun showPhotoSourceMenu(
        context: Context?,
        fragmentManager: FragmentManager,
        user: UserProfileUIModel?,
        position: Int
    ) {
        val menu = MeeraMenuBottomSheet(context)
        with(menu) {
            addItem(
                title = if (user?.avatarDetails?.avatarSmall?.isNotEmpty() == true) {
                    R.string.edit_photo
                } else {
                    R.string.user_personal_info_bottom_menu_gallery
                },
                icon = R.drawable.ic_open_photo_camera,
                click = {
                    submitUIAction(UserProfileUIAction.OnAvatarChangeClicked)
                })

            addItem(
                title = R.string.user_personal_info_bottom_menu_create_avatar,
                icon = R.drawable.ic_avatar_photo,
                click = {
                    submitUIAction(UserProfileUIAction.CreateAvatar)
                },
            )
            addItem(R.string.save_image, R.drawable.image_download_menu_item) {
                submitUIAction(UserProfileUIAction.SaveUserAvatarToGalleryAction(position))
            }
            addItem(R.string.edit_profile, R.drawable.ic_chat_edit_message) {
                submitUIAction(UserProfileUIAction.OnOpenProfileClicked)
            }
            show(fragmentManager)
        }
    }

    fun handleEvent(
        effect: UserProfileDialogNavigation,
        fragmentManager: FragmentManager,
        context: Context?,
        isInSnippet: Boolean
    ) {
        when (effect) {
            is UserProfileDialogNavigation.ShowCancelFriendshipRequestDialog ->
                showCancelFriendshipRequestDialog(effect, fragmentManager, context)
            is UserProfileDialogNavigation.ShowConfirmDialogRemoveFromFriend ->
                showConfirmDialogRemoveFromFriend(effect, fragmentManager, context)
            is UserProfileDialogNavigation.ShowFriendIncomingStatusMenu ->
                showFriendIncomingStatusMenu(context, fragmentManager, effect)
            is UserProfileDialogNavigation.ShowDotsMenu -> {
                if (effect.isMe) showMyDotsMenu(effect, fragmentManager, context)
                else showNotMyDotsMenu(effect, context, fragmentManager, isInSnippet)
            }
            is UserProfileDialogNavigation.ShowUnsubscribeMenu ->
                showUnsubscribeMenu(effect, fragmentManager, context)
            else -> Unit
        }
    }
    private fun showCancelFriendshipRequestDialog(
        effect: UserProfileDialogNavigation.ShowCancelFriendshipRequestDialog,
        fragmentManager: FragmentManager,
        context: Context?
    ) {
        context ?: return
        if (!effect.isSubscribed) {
            ConfirmDialogBuilder()
                .setHeader(context.getString(R.string.cancel_friendship_request_dialog_header))
                .setDescription(context.getString(R.string.cancel_friendship_request_dialog_description))
                .setLeftBtnText(context.getString(R.string.cancel_friendship_request_dialog_close))
                .setRightBtnText(context.getString(R.string.cancel_friendship_request_dialog_action))
                .setRightClickListener {
                    submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(true))
                }
                .show(fragmentManager)
        } else {
            ConfirmDialogBuilder()
                .setHeader(context.getString(R.string.cancel_friendship_request_dialog_header))
                .setDescription(context.getString(R.string.cancel_friendship_request_dialog_description))
                .setHorizontal(true)
                .setTopBtnText(context.getString(R.string.cancel_friendship_request_dialog_cancel_request))
                .setMiddleBtnText(context.getString(R.string.cancel_friendship_request_dialog_cancel_request_and_unsub))
                .setBottomBtnText(context.getString(R.string.cancel_friendship_request_dialog_cancel_action))
                .setTopClickListener {
                    submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(true))
                }
                .setMiddleClickListener {
                    submitUIAction(UserProfileUIAction.RemoveFriendAndUnsubscribe(true))
                }
                .show(fragmentManager)
        }
    }

    private fun showConfirmDialogRemoveFromFriend(
        effect: UserProfileDialogNavigation.ShowConfirmDialogRemoveFromFriend,
        fragmentManager: FragmentManager,
        context: Context?
    ) {
        context ?: return
        if (effect.isSubscribed) {
            ConfirmDialogBuilder()
                .setHeader(context.getString(R.string.user_info_remove_from_friend_dialog_header))
                .setDescription(context.getString(R.string.remove_from_friend_desc_text, effect.friendName))
                .setHorizontal(true)
                .setTopBtnText(context.getString(R.string.user_info_remove_from_friend_dialog_remove))
                .setMiddleBtnText(context.getString(R.string.user_info_remove_from_friend_dialog_remove_and_unsub))
                .setBottomBtnText(context.getString(R.string.user_info_remove_from_friend_dialog_cancel))
                .setTopClickListener {
                    submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(false))
                }
                .setMiddleClickListener {
                    submitUIAction(UserProfileUIAction.RemoveFriendAndUnsubscribe(false))
                }
                .show(fragmentManager)
        } else {
            ConfirmDialogBuilder()
                .setHeader(context.getString(R.string.user_info_remove_from_friend_dialog_header))
                .setDescription(context.getString(R.string.remove_from_friend_desc_text, effect.friendName))
                .setLeftBtnText(context.getString(R.string.user_info_remove_from_friend_dialog_cancel))
                .setRightBtnText(context.getString(R.string.user_info_remove_from_friend_dialog_remove))
                .setRightClickListener {
                    submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(false))
                }
                .show(fragmentManager)
        }
    }


    private fun showUnsubscribeMenu(
        effect: UserProfileDialogNavigation.ShowUnsubscribeMenu,
        fragmentManager: FragmentManager,
        context: Context?
    ) {
        context ?: return
        MeeraMenuBottomSheet(context).apply {
            if (effect.isProfileSuggestionFloorShown) {
                addItem(R.string.hide_suggestions, R.drawable.ic_hide_recomendations) {
                    submitUIAction(UserProfileUIAction.SetSuggestionsEnabled(enabled = false))
                }
            } else {
                addItem(R.string.show_suggestions, R.drawable.ic_show_recomendations) {
                    submitUIAction(UserProfileUIAction.SetSuggestionsEnabled(enabled = true))
                }
            }
            when {
                effect.isNotificationsAvailable && effect.isNotificationsEnabled -> {
                    addItem(R.string.disable_notifications, R.drawable.ic_notifications_disable) {
                        submitUIAction(UserProfileUIAction.ClickSubscribeNotification(false))
                    }
                }
                effect.isNotificationsAvailable && effect.isNotificationsEnabled.not() -> {
                    addItem(R.string.enable_notifications, R.drawable.ic_notifications_enable) {
                        submitUIAction(UserProfileUIAction.ClickSubscribeNotification(true))
                    }
                }
            }
            addItem(R.string.unsubscribe, R.drawable.ic_unsubscribe_red) {
                submitUIAction(UserProfileUIAction.UnsubscribeFromUserClickedAction)
            }
            show(fragmentManager)
        }
    }

    private fun showMyDotsMenu(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        fragmentManager: FragmentManager,
        context: Context?
    ) {
        context ?: return
        MeeraMenuBottomSheet(context).apply {
            addRepostItem(this)
            addCopyItem(effect, this, context)
            addItem(R.string.profile_settings, R.drawable.ic_settings_purple_new) {
                checkAppRedesigned(
                  isRedesigned = {},
                    isNotRedesigned = {
                        add(ProfileSettingsFragmentNew(), Act.LIGHT_STATUSBAR)
                    }
                )
            }
            show(fragmentManager)
        }
    }

    private fun showNotMyDotsMenu(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        context: Context?,
        fragmentManager: FragmentManager,
        isInSnippet: Boolean
    ) {
        context ?: return
        MeeraMenuBottomSheet(context).apply {
            addRepostItem(this)
            addCopyItem(effect, this, context)
            val isAnnounceUser = effect.profile.userRole == UserRole.ANNOUNCE_USER
            val isSupportUser = effect.profile.userRole == UserRole.SUPPORT_USER
            val isTechnicalAccount = isAnnounceUser || isSupportUser
            val isProfileDeleted = effect.profile.accountDetails.isAccountDeleted
            if (isProfileDeleted.not() && isTechnicalAccount.not()) {
                addNotificationBellIfNeed(effect, this)
                addMessagePermissionIfNeed(effect, this)
                addCallPermissionIfNeed(effect, this)
                addBlockUnblock(effect, this)
                addSubscribeUnsubscribe(effect, this)
                addComplaintItem(effect, this, fragmentManager, isInSnippet)
            }
            addRemoveFromFriendsIfNeed(effect, this)
            addUnsubscribeIfNeed(effect, this)
            show(fragmentManager)
        }
    }
    private fun addRepostItem(menuBottomSheet: MeeraMenuBottomSheet) {
        menuBottomSheet.addItem(R.string.share_profile, R.drawable.ic_share_purple_new) {
            submitUIAction(UserProfileUIAction.OnShareProfileClickAction)
        }
    }

    private fun addCopyItem(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet,
        context: Context
    ) {
        menuBottomSheet.addItem(R.string.copy_link, R.drawable.ic_chat_copy_message) {
            copyProfileLink(context, effect.profileLink, effect.profile.uniquename) {
                submitUIAction(UserProfileUIAction.OnCopyProfileClickedAction)
            }
        }
    }

    private fun addNotificationBellIfNeed(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet
    ) {
        val subscriptionNotificationAvailable =  effect.profile.settingsFlags.isSubscriptionOn
        val notificationEnabled = effect.profile.settingsFlags.isSubscriptionNotificationEnabled
        when {
            subscriptionNotificationAvailable && notificationEnabled -> {
                menuBottomSheet.addItem(R.string.disable_notifications, R.drawable.ic_notifications_disable) {
                    submitUIAction(UserProfileUIAction.ClickSubscribeNotification(false))
                }
            }
            subscriptionNotificationAvailable && notificationEnabled.not() -> {
                menuBottomSheet.addItem(R.string.enable_notifications, R.drawable.ic_notifications_enable) {
                    submitUIAction(UserProfileUIAction.ClickSubscribeNotification(true))
                }
            }
        }
    }

    private fun addMessagePermissionIfNeed(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet
    ) {
        effect.profile.settingsFlags.userCanChatMe.let { isUserCan ->
            val imageRes: Int
            val title: Int
            if (isUserCan) {
                imageRes = R.drawable.ic_disallow_message
                title = R.string.profile_dots_menu_disallow_messages
            } else {
                imageRes = R.drawable.ic_allow_message
                title = R.string.profile_dots_menu_allow_messages
            }
            menuBottomSheet.addItem(title, imageRes) {
                submitUIAction(UserProfileUIAction.OnChatPrivacyClickedAction)
            }
        }
    }

    private fun addCallPermissionIfNeed(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet
    ) {
        effect.profile.settingsFlags.userCanCallMe.let { isUserCan ->
            val imageRes: Int
            val title: Int

            if (isUserCan) {
                imageRes = R.drawable.ic_disallow_calls
                title = R.string.profile_dots_menu_disallow_calls
            } else {
                imageRes = R.drawable.ic_allow_calls
                title = R.string.profile_dots_menu_allow_calls
            }
            menuBottomSheet.addItem(title, imageRes) {
                submitUIAction(UserProfileUIAction.OnCallPrivacyClickedAction)
            }
        }
    }

    private fun addBlockUnblock(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet
    ) {
        var titleRes: Int
        var imageRes: Int
        effect.profile.settingsFlags.blacklistedByMe.let { isBlocked ->
            titleRes = if (isBlocked.not()) {
                imageRes = R.drawable.ic_block_user_red
                R.string.general_block
            } else {
                imageRes = R.drawable.ic_user_check_purple
                R.string.general_unblock
            }
        }
        menuBottomSheet.addItem(titleRes, imageRes) {
            submitUIAction(UserProfileUIAction.OnBlacklistUserClickedAction)
        }
    }

    private fun addSubscribeUnsubscribe(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet,
    ) {
        if (effect.profile.settingsFlags.isHideRoadPosts.not()) {
            menuBottomSheet.addItem(R.string.profile_complain_hide_all_posts, R.drawable.ic_eye_off_all_menu_item_red) {
                submitUIAction(UserProfileUIAction.ChangePostsPrivacyClickedAction(true))
            }
        } else if (effect.profile.settingsFlags.isHideRoadPosts) {
            menuBottomSheet.addItem( R.string.show_user_posts, R.drawable.ic_eye_purple) {
                submitUIAction(UserProfileUIAction.ChangePostsPrivacyClickedAction(false))
            }
        }
    }

    private fun addComplaintItem(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet,
        fragmentManager: FragmentManager,
        isInSnippet: Boolean
    ) {
        val where = if (isInSnippet) {
            AmplitudePropertyWhere.USER_SNIPPET
        } else {
            AmplitudePropertyWhere.PROFILE
        }
        menuBottomSheet.addItem(R.string.complain_about_profile, R.drawable.ic_report_profile) {
            checkAppRedesigned(
                isRedesigned = {
                    submitUIAction(
                        UserProfileUIAction.OnComplainClick(
                            userId = effect.profile.userId,
                            where = where
                        )
                    )

                },
                isNotRedesigned = {
                    UserComplainReasonBottomSheet.showInstance(
                        fragmentManager = fragmentManager,
                        userId = effect.profile.userId,
                        where = where,
                    )
                }
            )
        }
    }

    private fun addRemoveFromFriendsIfNeed(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet
    ) {
        if (effect.profile.friendStatus == FriendStatus.FRIEND_STATUS_CONFIRMED) {
            menuBottomSheet.addItem(R.string.friends_remove_friend, R.drawable.friends_remove_menu_item) {
                submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(false))
            }
        }
    }

    private fun addUnsubscribeIfNeed(
        effect: UserProfileDialogNavigation.ShowDotsMenu,
        menuBottomSheet: MeeraMenuBottomSheet
    ) {
        if (effect.profile.accountDetails.isAccountDeleted) {
            effect.profile.settingsFlags.isSubscriptionOn.let { isSubscribed ->
                if (isSubscribed) {
                    menuBottomSheet.addItem(R.string.unsubscribe_user_txt, R.drawable.ic_unsubscribe_new_purple) {
                        submitUIAction(UserProfileUIAction.UnsubscribeFromUserClickedAction)
                    }
                }
            }
        }
    }

    private fun showFriendIncomingStatusMenu(
        context: Context?,
        fragmentManager: FragmentManager,
        effect: UserProfileDialogNavigation.ShowFriendIncomingStatusMenu
    ) {
        context ?: return
        MeeraMenuBottomSheet(context).apply {
            addDescriptionItem(
                R.string.accept_request,
                R.drawable.ic_add_friend_purple,
                R.string.accept_request_descr
            ) {
                submitUIAction(
                    UserProfileUIAction.OnAddFriendClicked(
                        approved = effect.approved,
                        influencer = effect.influencer,
                        friendStatus = effect.friendStatus
                    )
                )
            }
            addDescriptionItem(
                R.string.reject_friend_request,
                R.drawable.ic_reject_friend_red,
                R.string.reject_friend_descr
            ) {
                val message = context.getString(R.string.request_rejected)
                submitUIAction(
                    UserProfileUIAction.RemoveFriendClickedAction(
                        cancellingFriendRequest = false,
                        message = message
                    )
                )
            }
            show(fragmentManager)
        }
    }
}

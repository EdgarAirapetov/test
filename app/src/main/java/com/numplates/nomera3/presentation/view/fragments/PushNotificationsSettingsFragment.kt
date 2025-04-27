package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.PushSettingsResponse
import com.numplates.nomera3.databinding.FragmentPushSettingsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.MAIN_PUSH_SETTINGS
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.OFF
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.ON
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.PUSH_ENABLED
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.message.MessageNotificationsAddUsersFragment
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.message.MessageNotificationsUsersFragment
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription.SubscriptionsNotificationAddUsersFragment
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription.SubscriptionsNotificationUsersFragment
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.PushSettingsViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.PushSettingsEvents

class PushNotificationsSettingsFragment : BaseFragmentNew<FragmentPushSettingsBinding>() {

    private val viewModel by viewModels<PushSettingsViewModel>()

    private var chatCounter: Int? = 0

    private var subscriptionCount: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObservables()
        doDelayed(100) {
            viewModel.init()
            viewModel.requestUserOptions()
        }
    }

    /**
     * Request settings for update counter
     */
    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        viewModel.requestUserOptions()
    }


    /**
     * Handle data from viewModel's live data
     * */
    private fun initObservables() {
        viewModel.liveSettings.observe(viewLifecycleOwner, Observer {
            handlePushSettings(it)
        })

        viewModel.liveEvents.observe(viewLifecycleOwner, Observer {
            when (it) {
                is PushSettingsEvents.ErrorGetSettings -> {
                    showErrorGetSettings()
                    setContentVisibility(false)
                }
                is PushSettingsEvents.ErrorSetSettings -> {
                    showErrorSetSettings()
                }
            }
        })
    }

    /**
     * Handle response init switchers
     * */
    private fun handlePushSettings(pushSettings: PushSettingsResponse?) {
        pushSettings?.let { settings ->
            binding?.apply {
                swShowMessageText.isChecked = settings.notificationsShowMessageInPush ?: false
                swShowCommentsText.isChecked = settings.notificationsShowComments ?: false
                swAllNotif.isChecked = (settings.notificationsEnabled ?: false)
                    .also { isEnabled ->
                        viewModel.amplitudeHelper.apply {
                            identifyUserProperty { iden ->
                                iden.set(PUSH_ENABLED, isEnabled)
                                iden.set(MAIN_PUSH_SETTINGS, if (isEnabled) ON else OFF)
                            }
                        }
                    }
                swNewMessages.isChecked = settings.notificationsMessages ?: false
                swNewGroupComment.isChecked = settings.notificationsGroupComment ?: false
                swNewPostComment.isChecked = settings.notificationsPostComments ?: false
                swNewMomentComment.isChecked = settings.notificationsMomentComments ?: false
                swNewPostCommentReaction.isChecked = settings.notificationsPostCommentReaction ?: false
                swNewMomentCommentReaction.isChecked = settings.notificationsMomentCommentReaction ?: false
                swNewPostReaction.isChecked = settings.notificationsPostReaction ?: false
                swNewMomentReaction.isChecked = settings.notificationsMomentReaction ?: false
                swAboutMePhotosReaction.isChecked = settings.notificationsGalleryReaction ?: false
                swNewPostCommentAnswer.isChecked = settings.notificationsPostCommentsReply ?: false
                swNewMomentCommentAnswer.isChecked = settings.notificationsMomentCommentsReply ?: false
                swNewGift.isChecked = settings.notificationsGifts ?: false
                swNewFriendRequest.isChecked = settings.notificationsFriendRequest ?: false
                swNewGroupRequest.isChecked = settings.notificationsGroupJoin ?: false
                swMentionGroupChat.isChecked = settings.groupChatMention ?: false
                swMentionPost.isChecked = settings.postMention ?: false
                swMentionComments.isChecked = settings.commentsMention ?: false
                receiveFriendBirthdayNotificationsSwitch.isChecked = settings.friendsBirthday ?: false
                swChatRequestText.isChecked = settings.notificationsChatRequest ?: false
                swCallEnabled.isChecked = settings.notificationsCallUnavailable ?: false

                if (settings.exclusions?.messageExclusions?.count != null) {
                    when (val count = settings.exclusions?.messageExclusions?.count) {
                        1 -> tvAlwaysAllow.text = getString(R.string.count_of_chats_txt_one, count)
                        2, 3, 4 -> tvAlwaysAllow.text = getString(R.string.count_of_chats_txt_2_4, count)
                        else -> tvAlwaysAllow.text = getString(R.string.count_of_chats_txt, count)
                    }
                } else {
                    tvAlwaysAllow.text = getString(R.string.general_add)
                }
                // Set chat counter
                this@PushNotificationsSettingsFragment.chatCounter = settings.exclusions?.messageExclusions?.count

                // Set subscription count
                this@PushNotificationsSettingsFragment.subscriptionCount =
                    settings.exclusions?.subscriptionExclusions?.count ?: 0
                if (this@PushNotificationsSettingsFragment.subscriptionCount == 0) {
                    tvAddFriendsAndSubscriptions.text = getString(R.string.general_add)
                } else {
                    tvAddFriendsAndSubscriptions.text =
                        getString(
                            R.string.settings_count_peoples,
                            this@PushNotificationsSettingsFragment.subscriptionCount
                        )
                }

                // Visibility sources (friends and subscriptions)
                val subscriptionNotificationLevel = settings.exclusions?.subscriptionExclusions?.showNotificationLevel
                    ?: 0
                if (subscriptionNotificationLevel.isTrue()) {
                    containerSources.visible()
                } else {
                    containerSources.gone()
                }
            }

            setBlockingSurface(settings.notificationsEnabled ?: false)
            initSwitchListeners()
            setContentVisibility(true)
        } ?: kotlin.run {
            showErrorGetSettings()
            setContentVisibility(false)
        }
    }

    private fun showErrorGetSettings() {
        NToast.with(view)
            .typeError()
            .text(getString(R.string.error_while_getting_settings))
            .show()
    }

    private fun showErrorSetSettings() {
        NToast.with(view)
            .typeError()
            .text(getString(R.string.error_while_sending_settings))
            .show()
    }

    private fun setContentVisibility(isVisible: Boolean) {
        binding?.nsvNotif?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * set listeners on switchers (send data to server)
     * */
    private fun initSwitchListeners() {
        binding?.apply {
            swAllNotif.setOnCheckedChangeListener { _, isChecked ->
                setBlockingSurface(isChecked)
                viewModel.setSetting(SETTING_TYPE_ALL_PUSHES, isChecked)
            }
            swNewMessages.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_NEW_MESSAGE, isChecked)
            }
            swNewGroupComment.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_GROUP_COMMENT, isChecked)
            }
            swNewPostComment.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_POST_COMMENT, isChecked)
            }
            swNewMomentComment.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_MOMENT_COMMENT, isChecked)
            }
            swNewPostCommentAnswer.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_POST_COMMENT_REPLY, isChecked)
            }
            swNewMomentCommentAnswer.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_MOMENT_COMMENT_REPLY, isChecked)
            }
            swNewGift.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_GIFTS, isChecked)
            }
            swNewFriendRequest.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_FRIEND_REQUEST, isChecked)
            }
            swCallEnabled.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_FRIEND_REQUEST, isChecked)
            }
            swNewGroupRequest.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_GROUP_JOIN, isChecked)
            }
            swShowMessageText.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_SHOW_MESSAGE_CONTENT, isChecked)
            }
            swShowCommentsText.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_SHOW_COMMENT_CONTENT, isChecked)
            }
            clExceptions.setOnClickListener {
                exceptionsClickAction()
            }
            tvAddFriendsAndSubscriptions.setOnClickListener {
                checkAppRedesigned (
                    isRedesigned = {},
                    isNotRedesigned = {
                        add(
                            if (subscriptionCount == 0){
                                SubscriptionsNotificationAddUsersFragment()
                            } else {
                                SubscriptionsNotificationUsersFragment()
                            },
                            Act.LIGHT_STATUSBAR)
                    }
                )
            }
            swNewPostCommentReaction.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_POST_COMMENT_REACTION, isChecked)
            }
            swNewMomentCommentReaction.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_MOMENT_COMMENT_REACTION, isChecked)
            }
            swNewPostReaction.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_POST_REACTION, isChecked)
            }
            swNewMomentReaction.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_MOMENT_REACTION, isChecked)
            }
            swAboutMePhotosReaction.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_GALLERY_REACTION, isChecked)
            }
            swChatRequestText.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_CHAT_REQUEST, isChecked)
            }
            // документация по кнопке
            // https://nomera.atlassian.net/wiki/spaces/NOM/pages/2090958857#Этаж-“День-рождения“-в-профиле-пользователя
            receiveFriendBirthdayNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_SHOW_FRIENDS_BIRTHDAY, isChecked)
            }
            registerMentionListeners()
        }
    }

    private fun exceptionsClickAction() {
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                chatCounter?.let { count ->
                    if (count > 0)
                        add(MessageNotificationsUsersFragment(), Act.LIGHT_STATUSBAR)
                    else if (count == 0) {
                        add(MessageNotificationsAddUsersFragment(), Act.LIGHT_STATUSBAR)
                    }
                } ?: kotlin.run {
                    add(MessageNotificationsAddUsersFragment(), Act.LIGHT_STATUSBAR)
                }
            }
        )
    }

    private fun registerMentionListeners() {
        binding?.apply {
            swMentionGroupChat.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_MENTION_GROUP_CHAT, isChecked)
            }
            swMentionPost.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_MENTION_POST, isChecked)
            }
            swMentionComments.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSetting(SETTING_TYPE_MENTION_COMMENTS, isChecked)
            }
        }
    }

    private fun initView() {
        binding?.ivBackPushSettings?.setOnClickListener {
            act.onBackPressed()
        }
        setContentVisibility(false)
    }

    /**
     * View witch close all push setting except all pushes and exceptions list
     * */
    private fun setBlockingSurface(block: Boolean) {
        binding?.apply {
            if (block) {
                vDisabled.gone()
                vDisabled1.gone()
                vDisabled2.gone()
                vDisabled3.gone()
            } else {
                vDisabled.visibleAppearAnimate()
                vDisabled1.visibleAppearAnimate()
                vDisabled2.visibleAppearAnimate()
                vDisabled3.visibleAppearAnimate()
            }
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPushSettingsBinding
        get() = FragmentPushSettingsBinding::inflate

    companion object {
        //PUSH SETTING TYPES
        const val SETTING_TYPE_ALL_PUSHES = 0
        const val SETTING_TYPE_NEW_MESSAGE = 1
        const val SETTING_TYPE_GIFTS = 2
        const val SETTING_TYPE_GROUP_COMMENT = 3
        const val SETTING_TYPE_POST_COMMENT = 4
        const val SETTING_TYPE_POST_COMMENT_REPLY = 5
        const val SETTING_TYPE_FRIEND_REQUEST = 6
        const val SETTING_TYPE_GROUP_JOIN = 7
        const val SETTING_TYPE_SHOW_MESSAGE_CONTENT = 8
        const val SETTING_TYPE_SHOW_COMMENT_CONTENT = 9
        const val SETTING_TYPE_MOMENT_COMMENT = 10
        const val SETTING_TYPE_MOMENT_COMMENT_REPLY = 11

        //mentions
        const val SETTING_TYPE_MENTION_GROUP_CHAT = 12
        const val SETTING_TYPE_MENTION_POST = 13
        const val SETTING_TYPE_MENTION_COMMENTS = 14

        // friend's birthdays notification
        const val SETTING_TYPE_SHOW_FRIENDS_BIRTHDAY = 15

        // reaction
        const val SETTING_POST_COMMENT_REACTION = 16
        const val SETTING_MOMENT_COMMENT_REACTION = 17
        const val SETTING_POST_REACTION = 18
        const val SETTING_MOMENT_REACTION = 19
        const val SETTING_GALLERY_REACTION = 20

        const val SETTING_CHAT_REQUEST = 21

        const val SETTING_TYPE_CALL_ENABLED = 22
    }
}

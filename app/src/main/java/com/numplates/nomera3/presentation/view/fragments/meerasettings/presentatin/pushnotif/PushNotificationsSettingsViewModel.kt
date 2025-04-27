package com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.model.PushSettingsModel
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.usecase.MeeraGetPushSettingUseCase
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.usecase.MeeraSetPushSettingUseCase
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter.PushSettingsData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PushNotificationsSettingsViewModel @Inject constructor(
    private val getPushSettingUseCase: MeeraGetPushSettingUseCase,
    private val setPushSettingUseCase: MeeraSetPushSettingUseCase,
    private val mapper: MeeraPushNotificationSettingsUiMapper
) : ViewModel() {

    private val _pushSettingState = MutableSharedFlow<PushSettingsState>()
    val pushSettingState: SharedFlow<PushSettingsState> = _pushSettingState.asSharedFlow()

    private val _notificationAction = MutableSharedFlow<MeeraPushNotificationSettingsAction>()
    val notificationAction: SharedFlow<MeeraPushNotificationSettingsAction> = _notificationAction.asSharedFlow()

    private var settings: PushSettingsModel? = null

    fun onAction(action: MeeraPushNotificationSettingsAction) {
        viewModelScope.launch {
            when (action) {
                MeeraPushNotificationSettingsAction.Feedback.AboutMePhotoReaction -> {
                    settings = settings?.copy(
                        notificationsGalleryReaction =
                        settings?.notificationsGalleryReaction?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Request.AddToCommunity -> {
                    settings = settings?.copy(
                        notificationsGroupJoin =
                        settings?.notificationsGroupJoin?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Request.AddToFriend -> {
                    settings = settings?.copy(
                        notificationsFriendRequest =
                        settings?.notificationsFriendRequest?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Request.Calls -> {
                    settings = settings?.copy(
                        notificationsCallUnavailable =
                        settings?.notificationsCallUnavailable?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Mentions.CommentMention -> {
                    settings = settings?.copy(
                        commentsMention =
                        settings?.commentsMention?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.CommunityComments -> {
                    settings = settings?.copy(
                        notificationsGroupComment =
                        settings?.notificationsGroupComment?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.EnablePush -> {
                    settings = settings?.copy(
                        notificationsEnabled =
                        settings?.notificationsEnabled?.let { !it } ?: false
                    )
                    settings?.notificationsEnabled?.let { notificationsEnabled ->
                        _notificationAction.emit(
                            MeeraPushNotificationSettingsAction.UpdateOtherSetting(
                                updateEnableSettings(notificationsEnabled)
                            )
                        )
                    }
                }

                is MeeraPushNotificationSettingsAction.Message.ExceptUser -> {
                    if (action.usersCount > 0) {
                        _notificationAction
                            .emit(MeeraPushNotificationSettingsAction.ShowMessageNotificationUserFragment)
                    } else {
                        _notificationAction
                            .emit(MeeraPushNotificationSettingsAction.ShowMessageNotificationAddUserFragment)
                    }
                }

                is MeeraPushNotificationSettingsAction.Post.FriendAndSubscriptions -> {
                    if (action.usersCount > 0) {
                        _notificationAction
                            .emit(MeeraPushNotificationSettingsAction.ShowSubscriptionsNotificationUsersFragment)
                    } else {
                        _notificationAction
                            .emit(MeeraPushNotificationSettingsAction.ShowSubscriptionsNotificationAddUsersFragment)
                    }
                }

                MeeraPushNotificationSettingsAction.ProfileEvents.FriendsBirthday -> {
                    settings = settings?.copy(
                        friendsBirthday =
                        settings?.friendsBirthday?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Mentions.GroupChatMention -> {
                    settings = settings?.copy(
                        groupChatMention =
                        settings?.groupChatMention?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.MomentCommentAnswer -> {
                    settings = settings?.copy(
                        notificationsMomentCommentsReply =
                        settings?.notificationsMomentCommentsReply?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.MomentCommentReaction -> {
                    settings = settings?.copy(
                        notificationsMomentCommentReaction =
                        settings?.notificationsMomentCommentReaction?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.MomentComments -> {
                    settings = settings?.copy(
                        notificationsMomentComments =
                        settings?.notificationsMomentComments?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.MomentReaction -> {
                    settings = settings?.copy(
                        notificationsMomentReaction =
                        settings?.notificationsMomentReaction?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Request.NewDialog -> {
                    settings = settings?.copy(
                        notificationsChatRequest =
                        settings?.notificationsChatRequest?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Message.NewMessage -> {
                    settings = settings?.copy(
                        notificationsMessages =
                        settings?.notificationsMessages?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.OnViewCreated -> getSettings()
                MeeraPushNotificationSettingsAction.Feedback.PostCommentAnswer -> {
                    settings = settings?.copy(
                        notificationsPostCommentsReply =
                        settings?.notificationsPostCommentsReply?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.PostCommentReaction -> {
                    settings = settings?.copy(
                        notificationsPostCommentReaction =
                        settings?.notificationsPostCommentReaction?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.PostComments -> {
                    settings = settings?.copy(
                        notificationsPostComments =
                        settings?.notificationsPostComments?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Mentions.PostMention -> {
                    settings = settings?.copy(
                        postMention =
                        settings?.postMention?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.PostReaction -> {
                    settings = settings?.copy(
                        notificationsPostReaction =
                        settings?.notificationsPostReaction?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.ProfileEvents.ReceiveNewGift -> {
                    settings = settings?.copy(
                        notificationsGifts =
                        settings?.notificationsGifts?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Feedback.ShowCommentsText -> {
                    settings = settings?.copy(
                        notificationsShowComments =
                        settings?.notificationsShowComments?.let { !it } ?: false
                    )
                }

                MeeraPushNotificationSettingsAction.Message.ShowTextMessage -> {
                    settings = settings?.copy(
                        notificationsShowMessageInPush =
                        settings?.notificationsShowMessageInPush?.let { !it } ?: false
                    )
                }

                else -> {
                    Timber.d("An unknown was received push notification settings action")
                }
            }
            updateSettings()
        }
    }

    private fun updateEnableSettings(isEnable: Boolean): List<PushSettingsData> {
        val localSettingItemsState = settings?.let { mapper.mapPushSettingsModelToUiState(it) }?.items
        val newSettingsList = mutableListOf<PushSettingsData>()
        localSettingItemsState?.forEach {
            if (it is PushSettingsData.PushSettingsSwitch){
                if (it.action == MeeraPushNotificationSettingsAction.EnablePush){
                    newSettingsList.add(it.copy(isChosen = isEnable, isEnable = true))
                } else {
                    newSettingsList.add(it.copy(isEnable = isEnable))
                }
            } else {
                newSettingsList.add(it)
            }
        }
        return newSettingsList
    }

    private fun getSettings() {
        viewModelScope.launch {
            settings = getPushSettingUseCase.invoke()
            settings?.let {
                _pushSettingState.emit(mapper.mapPushSettingsModelToUiState(it))
            }
        }
    }

    private fun updateSettings() {
        settings?.let {
            viewModelScope.launch {
                setPushSettingUseCase.invoke(it)
            }
        }
    }
}

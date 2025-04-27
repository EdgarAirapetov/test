package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.PushSettingsResponse
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SetPushSettingsUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_CHAT_REQUEST
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_GALLERY_REACTION
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_MOMENT_COMMENT_REACTION
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_MOMENT_REACTION
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_POST_COMMENT_REACTION
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_POST_REACTION
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_ALL_PUSHES
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_CALL_ENABLED
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_FRIEND_REQUEST
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_GIFTS
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_GROUP_COMMENT
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_GROUP_JOIN
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_MENTION_COMMENTS
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_MENTION_GROUP_CHAT
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_MENTION_POST
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_MOMENT_COMMENT
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_MOMENT_COMMENT_REPLY
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_NEW_MESSAGE
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_POST_COMMENT
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_POST_COMMENT_REPLY
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_SHOW_COMMENT_CONTENT
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_SHOW_FRIENDS_BIRTHDAY
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment.Companion.SETTING_TYPE_SHOW_MESSAGE_CONTENT
import com.numplates.nomera3.presentation.viewmodel.viewevents.PushSettingsEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class PushSettingsViewModel : ViewModel() {

    @Inject
    lateinit var pushSettings: SetPushSettingsUseCase

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    private var disposables = CompositeDisposable()

    var liveSettings = MutableLiveData<PushSettingsResponse>()
    var liveEvents = MutableLiveData<PushSettingsEvents>()

    private var userId: Long = 0
        get() = getUserUidUseCase.invoke()

    private var userSettings: PushSettingsResponse? = null

    fun init() {
        App.component.inject(this)
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    /**
     * Requesting user settings
     * */
    fun requestUserOptions() {
        if (userId != 0L) {
            val d = pushSettings.getPushSettings(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    userSettings = response.data
                    liveSettings.value = userSettings
                }, { err ->
                    Timber.e(err)
                    liveEvents.value = PushSettingsEvents.ErrorGetSettings
                })
            disposables.add(d)
        } else {
            liveEvents.value = PushSettingsEvents.ErrorGetSettings
        }
    }

    /**
     * Sending push setting to server
     * */
    private fun sendSettings() {
        userSettings?.let { settings ->
            val d = pushSettings.setPushSettings(userId, settings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("push setting successfully saved")
                    saveSettingToPref(userSettings)
                }, {
                    Timber.e(it)
                    if (it is UnknownHostException)
                        liveEvents.value = PushSettingsEvents.ErrorSetSettings
                })
            disposables.add(d)
        }
    }

    private fun saveSettingToPref(userSettings: PushSettingsResponse?) {
        if (userSettings?.notificationsEnabled == false) {
            appSettings.writeNeedToShowPushNewMessage(false)
        } else {
            userSettings?.notificationsMessages?.let { pushSettingsResponse ->
                appSettings.writeNeedToShowPushNewMessage(pushSettingsResponse)
            }
        }
    }

    fun setSetting(setting: Int, enabled: Boolean) {
        when (setting) {
            SETTING_TYPE_GIFTS -> userSettings?.notificationsGifts = enabled
            SETTING_TYPE_ALL_PUSHES -> {
                appSettings.isNotificationEnabled = enabled
                userSettings?.notificationsEnabled = enabled
            }
            SETTING_TYPE_GROUP_JOIN -> userSettings?.notificationsGroupJoin = enabled
            SETTING_TYPE_NEW_MESSAGE -> userSettings?.notificationsMessages = enabled
            SETTING_TYPE_MENTION_POST -> userSettings?.postMention = enabled
            SETTING_TYPE_POST_COMMENT -> userSettings?.notificationsPostComments = enabled
            SETTING_TYPE_MOMENT_COMMENT -> userSettings?.notificationsMomentComments = enabled
            SETTING_TYPE_GROUP_COMMENT -> userSettings?.notificationsGroupComment = enabled
            SETTING_TYPE_FRIEND_REQUEST -> userSettings?.notificationsFriendRequest = enabled
            SETTING_TYPE_MENTION_COMMENTS -> userSettings?.commentsMention = enabled
            SETTING_TYPE_MENTION_GROUP_CHAT -> userSettings?.groupChatMention = enabled
            SETTING_TYPE_POST_COMMENT_REPLY -> userSettings?.notificationsPostCommentsReply = enabled
            SETTING_TYPE_MOMENT_COMMENT_REPLY -> userSettings?.notificationsMomentCommentsReply = enabled
            SETTING_TYPE_SHOW_MESSAGE_CONTENT -> userSettings?.notificationsShowMessageInPush = enabled
            SETTING_TYPE_SHOW_COMMENT_CONTENT -> userSettings?.notificationsShowComments = enabled
            SETTING_TYPE_SHOW_FRIENDS_BIRTHDAY -> userSettings?.friendsBirthday = enabled
            SETTING_POST_COMMENT_REACTION -> userSettings?.notificationsPostCommentReaction = enabled
            SETTING_MOMENT_COMMENT_REACTION -> userSettings?.notificationsMomentCommentReaction = enabled
            SETTING_POST_REACTION -> userSettings?.notificationsPostReaction = enabled
            SETTING_MOMENT_REACTION -> userSettings?.notificationsMomentReaction = enabled
            SETTING_GALLERY_REACTION -> userSettings?.notificationsGalleryReaction = enabled
            SETTING_CHAT_REQUEST -> userSettings?.notificationsChatRequest = enabled
            SETTING_TYPE_CALL_ENABLED -> userSettings?.notificationsCallUnavailable = enabled
        }

        sendSettings()
    }

    override fun onCleared() {
        disposables.clear()
    }
}

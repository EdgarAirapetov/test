package com.numplates.nomera3.telecom

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.getNotificationPendingIntentFlag
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.ACTION_INCOMING_CALL
import com.numplates.nomera3.Act
import com.numplates.nomera3.TYPE_CALL_ACTION
import com.numplates.nomera3.data.fcm.models.PushCallObject
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer
import javax.inject.Inject

private const val REQUEST_CODE_ACTIVITY = 9001
private const val REQUEST_CODE_SERVICE = 9002

class CallIntentProvider @Inject constructor(
    private val appContext: Context,
    private val gson: Gson,
) {

    fun createOpenCallIntent(
        typeCallAction: Int,
        userChat: UserChat,
        pushObject: PushCallObject,
    ): Intent {
        return createIntentForCalling(
            intentAction = IActionContainer.ACTION_START_CALL,
            typeCallAction = typeCallAction,
            pushObject = pushObject,
            userChat = userChat,
            clazz = MeeraAct::class.java
        )
    }

    fun createRejectCallPendingIntent(
        callAction: Int,
        userChat: UserChat,
        pushObject: PushCallObject,
    ): PendingIntent? {
        val serviceIntent = createIntentForCalling(
            intentAction = ACTION_INCOMING_CALL,
            typeCallAction = callAction,
            pushObject = pushObject,
            userChat = userChat,
            clazz = SignalingService::class.java,
        )
        return PendingIntent.getService(
            appContext,
            REQUEST_CODE_SERVICE,
            serviceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    fun createFullScreenPendingIntent(
        callAction: Int,
        userChat: UserChat,
        pushObject: PushCallObject
    ): PendingIntent? {
        val activityIntent = createIntentForCalling(
            intentAction = IActionContainer.ACTION_START_CALL,
            typeCallAction = callAction,
            pushObject = pushObject,
            userChat = userChat,
            clazz = MeeraAct::class.java,
        )

        return PendingIntent.getActivity(
            appContext,
            REQUEST_CODE_ACTIVITY,
            activityIntent,
            getNotificationPendingIntentFlag()
        )
    }

    fun createAcceptCallPendingIntent(
        callAction: Int,
        userChat: UserChat,
        pushObject: PushCallObject,
    ): PendingIntent? {
        val activityIntent = createIntentForCalling(
            intentAction = IActionContainer.ACTION_START_CALL,
            typeCallAction = callAction,
            pushObject = pushObject,
            userChat = userChat,
            clazz = if (IS_APP_REDESIGNED) MeeraAct::class.java else Act::class.java
        )
        return PendingIntent.getActivity(
            appContext,
            REQUEST_CODE_ACTIVITY,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun <T : Class<*>> createIntentForCalling(
        intentAction: String,
        typeCallAction: Int,
        userChat: UserChat,
        pushObject: PushCallObject,
        clazz: T,
    ): Intent {
        return Intent(appContext, clazz)
            .setAction(intentAction)
            .addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(TYPE_CALL_ACTION, typeCallAction)
            .putExtra(IArgContainer.ARG_ROOM_ID, requireNotNull(pushObject.roomId).toLong())
            .putExtra(IArgContainer.ARG_MESSAGE_ID, requireNotNull(pushObject.messageId))
            .putExtra(IArgContainer.ARG_USER_MODEL, userChat)
            .putExtra(IArgContainer.ARG_PUSH_OBJECT, gson.toJson(pushObject))
    }
}

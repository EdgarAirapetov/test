package com.numplates.nomera3.modules.chat.notification

import android.os.Build
import androidx.annotation.RequiresApi

interface MessageStyleNotificationCreator {

    @RequiresApi(Build.VERSION_CODES.P)
    fun showNotification(data: ChatPushData)

    @RequiresApi(Build.VERSION_CODES.P)
    fun addResponseMessage(data: ChatPushData)

    fun createGroup(chanelId: String)
}

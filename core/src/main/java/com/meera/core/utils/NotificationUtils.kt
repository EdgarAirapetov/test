package com.meera.core.utils

import android.app.NotificationManager
import android.content.Context

fun Context.clearNotifications() =
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

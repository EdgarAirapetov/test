package com.meera.core.extensions

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.IntentFilter
import android.os.Build
import android.os.Handler

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun BroadcastReceiver.register(
    context: Context,
    filter: IntentFilter,
    permission: String? = null,
    scheduler: Handler? = null,
    exported: Boolean = false
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val exportedFlag = if (exported) RECEIVER_EXPORTED else RECEIVER_NOT_EXPORTED
        context.registerReceiver(
            this,
            filter,
            permission,
            scheduler,
            exportedFlag
        )
    } else {
        context.registerReceiver(
            this,
            filter,
            permission,
            scheduler
        )
    }
}

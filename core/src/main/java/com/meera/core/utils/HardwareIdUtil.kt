package com.meera.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import timber.log.Timber
import javax.inject.Inject

class HardwareIdUtil @Inject constructor(
    private val appContext: Context
) {

    @SuppressLint("HardwareIds")
    fun getHardwareId(): String {
        try {
            val id = Settings.Secure.getString(
                appContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            id?.let {
                return it
            } ?: run {
                return ""
            }
        } catch (e: Exception) {
            Timber.e(e)
            return ""
        }
    }

}

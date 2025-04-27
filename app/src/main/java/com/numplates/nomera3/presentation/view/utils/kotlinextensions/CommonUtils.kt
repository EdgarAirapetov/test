package com.numplates.nomera3.presentation.view.utils.kotlinextensions

import android.annotation.SuppressLint
import android.provider.Settings
import com.numplates.nomera3.App
import timber.log.Timber


// TODO: hardwareId - требует Activity или App (не выношу в core модуль)

@SuppressLint("HardwareIds")
fun getHardwareId(app: App): String {
    try {
        val id = Settings.Secure.getString(
            app.contentResolver,
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

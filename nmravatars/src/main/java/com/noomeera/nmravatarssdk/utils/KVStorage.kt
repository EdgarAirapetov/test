package com.noomeera.nmravatarssdk.utils

import android.content.Context
import com.noomeera.nmravatarssdk.Constants

internal class KVStorage(context: Context) {
    private val sharedPref = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun storeBoolean(key: String, value: Boolean) {
        sharedPref
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun checkBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }
}

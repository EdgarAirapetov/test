package com.meera.core.preferences

import android.content.SharedPreferences

interface PrefManager {

    fun <T> putValue(key: String, value: T)
    fun getString(key: String, defaultValue: String? = null): String
    fun getLong(key: String, defaultValue: Long? = null): Long
    fun getInt(key: String, defaultValue: Int? = null): Int
    fun getDouble(key: String, defaultValue: Double? = null): Double
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    suspend fun <T> putValueSuspend(key: String, value: T)

    fun contains(key: String): Boolean

    fun clear()

    suspend fun clearSuspend()

    fun registerOnSharedPreferenceChangeListener(accessTokenChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?)

    fun unregisterOnSharedPreferenceChangeListener(accessTokenChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?)

}

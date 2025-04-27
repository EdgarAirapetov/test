package com.meera.core.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class PrefManagerImpl(
    private val sharedPreferences: SharedPreferences,
    context: Context
) : PrefManager {

    private val newSharedPrefEditor = sharedPreferences.edit()

    // TODO: remove https://nomera.atlassian.net/browse/BR-22488
    private val oldPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val oldEditor = oldPrefs.edit()

    override fun <T> putValue(key: String, value: T) {
        when (value) {
            is Int -> {
                oldEditor.putInt(key, value).apply()
                newSharedPrefEditor.putInt(key, value).apply()
            }

            is Boolean -> {
                oldEditor.putBoolean(key, value).apply()
                newSharedPrefEditor.putBoolean(key, value).apply()
            }

            is Float -> {
                oldEditor.putFloat(key, value).apply()
                newSharedPrefEditor.putFloat(key, value).apply()
            }

            is String -> {
                oldEditor.putString(key, value).apply()
                newSharedPrefEditor.putString(key, value).apply()
            }

            is Long -> {
                oldEditor.putLong(key, value).apply()
                newSharedPrefEditor.putLong(key, value).apply()
            }
        }
    }

    override suspend fun <T> putValueSuspend(key: String, value: T) {
        when (value) {
            is Int -> {
                oldEditor.putInt(key, value).commit()
                newSharedPrefEditor.putInt(key, value).commit()
            }

            is Boolean -> {
                oldEditor.putBoolean(key, value).commit()
                newSharedPrefEditor.putBoolean(key, value).commit()
            }

            is Float -> {
                oldEditor.putFloat(key, value).commit()
                newSharedPrefEditor.putFloat(key, value).commit()
            }

            is String -> {
                oldEditor.putString(key, value).commit()
                newSharedPrefEditor.putString(key, value).commit()
            }

            is Long -> {
                oldEditor.putLong(key, value).commit()
                newSharedPrefEditor.putLong(key, value).commit()
            }
        }
    }

    override fun getString(key: String, defaultValue: String?): String =
        sharedPreferences.getString(key, defaultValue.orEmpty()).orEmpty()

    override fun getLong(key: String, defaultValue: Long?) =
        sharedPreferences.getLong(key, defaultValue ?: 0)

    override fun getInt(key: String, defaultValue: Int?): Int =
        sharedPreferences.getInt(key, defaultValue ?: 0)

    override fun getDouble(key: String, defaultValue: Double?): Double =
        java.lang.Double.longBitsToDouble(
            sharedPreferences.getLong(
                key,
                java.lang.Double.doubleToLongBits(defaultValue ?: 0.0)
            )
        )

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun contains(key: String) = sharedPreferences.contains(key)

    override fun registerOnSharedPreferenceChangeListener(accessTokenChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(accessTokenChangeListener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(accessTokenChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(accessTokenChangeListener)
    }

    override fun clear() {
        newSharedPrefEditor.clear().apply()
    }

    override suspend fun clearSuspend() {
        newSharedPrefEditor.clear().commit()
    }
}

package com.meera.core.preferences.datastore

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences

private const val SHARED_PREF_NAME_POSTFIX = "_preferences"

object SharedPreferencesMigrator {

    fun migrate(context: Context): List<DataMigration<Preferences>> {
        val sharedPrefName = "${context.packageName}$SHARED_PREF_NAME_POSTFIX"
        return listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = sharedPrefName,
                keysToMigrate = setOf()
            )
        )
    }

}

package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class PreferenceStringAdapter(
    private val dataStore: DataStore<Preferences>
): Preference.PrefAdapter<String> {

    override suspend fun get(key: String): String? {
        val storeKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<String?> {
        val storeKey = stringPreferencesKey(key)
        val preferencesFlow: Flow<String?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: String) {
        val storeKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = stringPreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }

}

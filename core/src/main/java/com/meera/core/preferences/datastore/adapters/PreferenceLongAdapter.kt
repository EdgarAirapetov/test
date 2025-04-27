package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceLongAdapter(
    private val dataStore: DataStore<Preferences>
) : Preference.PrefAdapter<Long> {

    override suspend fun get(key: String): Long? {
        val storeKey = longPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<Long?> {
        val storeKey = longPreferencesKey(key)
        val preferencesFlow: Flow<Long?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: Long) {
        val storeKey = longPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = longPreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }
}

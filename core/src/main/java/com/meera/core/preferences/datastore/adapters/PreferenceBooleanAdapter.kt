package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceBooleanAdapter(
    private val dataStore: DataStore<Preferences>
): Preference.PrefAdapter<Boolean> {

    override suspend fun get(key: String): Boolean? {
        val storeKey = booleanPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<Boolean?> {
        val storeKey = booleanPreferencesKey(key)
        val preferencesFlow: Flow<Boolean?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: Boolean) {
        val storeKey = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = booleanPreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }
}

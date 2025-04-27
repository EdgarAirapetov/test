package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceIntAdapter(
    private val dataStore: DataStore<Preferences>
): Preference.PrefAdapter<Int> {

    override suspend fun get(key: String): Int? {
        val storeKey = intPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<Int?> {
        val storeKey = intPreferencesKey(key)
        val preferencesFlow: Flow<Int?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: Int) {
        val storeKey = intPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = intPreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }
}

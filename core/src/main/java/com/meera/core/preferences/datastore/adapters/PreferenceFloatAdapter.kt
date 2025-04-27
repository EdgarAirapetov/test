package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceFloatAdapter(
    private val dataStore: DataStore<Preferences>
) : Preference.PrefAdapter<Float> {

    override suspend fun get(key: String): Float? {
        val storeKey = floatPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<Float?> {
        val storeKey = floatPreferencesKey(key)
        val preferencesFlow: Flow<Float?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: Float) {
        val storeKey = floatPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = floatPreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }
}

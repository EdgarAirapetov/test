package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceStringSetAdapter(
    private val dataStore: DataStore<Preferences>
) : Preference.PrefAdapter<Set<String>> {

    override suspend fun get(key: String): Set<String>? {
        val storeKey = stringSetPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<Set<String>?> {
        val storeKey = stringSetPreferencesKey(key)
        val preferencesFlow: Flow<Set<String>?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: Set<String>) {
        val storeKey = stringSetPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = stringSetPreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }

}

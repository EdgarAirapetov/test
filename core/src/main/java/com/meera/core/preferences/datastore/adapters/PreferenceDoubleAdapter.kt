package com.meera.core.preferences.datastore.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceDoubleAdapter(
    private val dataStore: DataStore<Preferences>
) : Preference.PrefAdapter<Double> {

    override suspend fun get(key: String): Double? {
        val storeKey = doublePreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[storeKey]
    }

    override suspend fun asFlow(key: String): Flow<Double?> {
        val storeKey = doublePreferencesKey(key)
        val preferencesFlow: Flow<Double?> = dataStore.data.map { preferences ->
            preferences[storeKey]
        }
        return preferencesFlow
    }

    override suspend fun set(key: String, value: Double) {
        val storeKey = doublePreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[storeKey] = value
        }
    }

    override suspend fun contains(key: String): Boolean {
        val storeKey = doublePreferencesKey(key)
        val value = dataStore.data.first()
        return value[storeKey] != null
    }
}

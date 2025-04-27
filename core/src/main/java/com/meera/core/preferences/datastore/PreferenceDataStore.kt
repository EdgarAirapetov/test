package com.meera.core.preferences.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.meera.core.preferences.datastore.adapters.PreferenceBooleanAdapter
import com.meera.core.preferences.datastore.adapters.PreferenceDoubleAdapter
import com.meera.core.preferences.datastore.adapters.PreferenceFloatAdapter
import com.meera.core.preferences.datastore.adapters.PreferenceIntAdapter
import com.meera.core.preferences.datastore.adapters.PreferenceLongAdapter
import com.meera.core.preferences.datastore.adapters.PreferenceStringAdapter
import com.meera.core.preferences.datastore.adapters.PreferenceStringSetAdapter

interface PreferenceDataStoreInterface {
    fun boolean(key: String, defaultValue: Boolean? = null): Preference<Boolean>
    fun int(key: String, defaultValue: Int? = null): Preference<Int>
    fun long(key: String, defaultValue: Long? = null): Preference<Long>
    fun float(key: String, defaultValue: Float? = null): Preference<Float>
    fun double(key: String, defaultValue: Double? = null): Preference<Double>
    fun string(key: String, defaultValue: String? = null): Preference<String>
    fun stringSet(key: String, defaultValue: Set<String>? = null): Preference<Set<String>>
    suspend fun clearAll()
}

class PreferenceDataStore(
    context: Context,
    preferenceNameStorage: String
) : PreferenceDataStoreInterface {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = preferenceNameStorage,
        produceMigrations = { context ->
            SharedPreferencesMigrator.migrate(context)
        }
    )

    private val dataStore: DataStore<Preferences> by lazy { context.dataStore }

    private val booleanAdapter: PreferenceBooleanAdapter by lazy { PreferenceBooleanAdapter(dataStore) }
    private val intAdapter: PreferenceIntAdapter by lazy { PreferenceIntAdapter(dataStore) }
    private val longAdapter: PreferenceLongAdapter by lazy { PreferenceLongAdapter(dataStore) }
    private val floatAdapter: PreferenceFloatAdapter by lazy { PreferenceFloatAdapter(dataStore) }
    private val doubleAdapter: PreferenceDoubleAdapter by lazy { PreferenceDoubleAdapter(dataStore) }
    private val stringSetAdapter: PreferenceStringSetAdapter by lazy { PreferenceStringSetAdapter(dataStore) }
    private val stringAdapter: PreferenceStringAdapter by lazy { PreferenceStringAdapter(dataStore) }

    override fun boolean(key: String, defaultValue: Boolean?): Preference<Boolean> =
        RealPreference(key, defaultValue, booleanAdapter)

    override fun int(key: String, defaultValue: Int?): Preference<Int> =
        RealPreference(key, defaultValue, intAdapter)

    override fun long(key: String, defaultValue: Long?): Preference<Long> =
        RealPreference(key, defaultValue, longAdapter)

    override fun float(key: String, defaultValue: Float?): Preference<Float> =
        RealPreference(key, defaultValue, floatAdapter)

    override fun double(key: String, defaultValue: Double?): Preference<Double> =
        RealPreference(key, defaultValue, doubleAdapter)

    override fun string(key: String, defaultValue: String?): Preference<String> =
        RealPreference(key, defaultValue, stringAdapter)

    override fun stringSet(key: String, defaultValue: Set<String>?): Preference<Set<String>> =
        RealPreference(key, defaultValue, stringSetAdapter)

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

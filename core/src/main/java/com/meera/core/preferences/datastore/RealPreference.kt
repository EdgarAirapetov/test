package com.meera.core.preferences.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

internal class RealPreference<T>(
    private val key: String,
    private val defaultValue: T?,
    val adapter: Preference.PrefAdapter<T>
) : Preference<T> {

    override suspend fun set(value: T) {
        adapter.set(key, value)
    }

    override suspend fun get(): T? {
        if (!adapter.contains(key) && defaultValue != null) {
            return defaultValue
        }
        return adapter.get(key)
    }

    // Todo: отрефакторить на обычный suspend без runBlocking https://nomera.atlassian.net/browse/BR-24549
    override fun getSync(): T? {
        return runBlocking {
            if (!adapter.contains(key) && defaultValue != null) {
                return@runBlocking defaultValue
            }
            return@runBlocking adapter.get(key)
        }
    }

    override suspend fun asFlow(): Flow<T?> {
        if (!adapter.contains(key) && defaultValue != null) {
            return emitDefaultValue()
        }
        return adapter.asFlow(key)
    }

    private fun emitDefaultValue() = flow {
        emit(defaultValue)
    }

    override fun key(): String = key

}



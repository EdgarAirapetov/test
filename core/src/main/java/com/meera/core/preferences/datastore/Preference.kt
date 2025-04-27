package com.meera.core.preferences.datastore

import kotlinx.coroutines.flow.Flow

interface Preference<T> {

    interface PrefAdapter<T> {

        suspend fun get(key: String): T?

        suspend fun asFlow(key: String): Flow<T?>

        suspend fun set(key: String, value: T)

        suspend fun contains(key: String): Boolean
    }

    suspend fun set(value: T)

    suspend fun get(): T?

    fun getSync(): T?

    suspend fun asFlow(): Flow<T?>

    fun key(): String

}

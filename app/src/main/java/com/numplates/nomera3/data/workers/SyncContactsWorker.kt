package com.numplates.nomera3.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.meera.core.utils.contacts.UserContactsProvider
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.SyncContactsApi
import com.numplates.nomera3.data.network.SyncContactsDto
import timber.log.Timber
import javax.inject.Inject

internal const val SYNC_CONTACTS_STATE = "syncContactsState"
internal const val SYNC_COUNT = "syncCount"

class SyncContactsWorker constructor(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var userContactsProvider: UserContactsProvider

    @Inject
    lateinit var syncContactsApi: SyncContactsApi

    init {
        App.component.inject(this)
    }

    override suspend fun doWork(): Result {
        return try {
            val response = syncContactsApi.postContacts(SyncContactsDto(userContactsProvider.provide()))
            Result.success(Data.Builder().putInt(SYNC_COUNT, response.data.found).build())
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(
                Data.Builder()
                    .putString(SYNC_CONTACTS_STATE, e.message)
                    .build()
            )
        }
    }
}

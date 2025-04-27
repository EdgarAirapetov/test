package com.numplates.nomera3.data.repository

import androidx.annotation.MainThread
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.datastore.Preference
import com.numplates.nomera3.data.workers.SyncContactsWorker
import com.numplates.nomera3.domain.repository.SyncContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val SYNC_CONTACTS_WORKER_TAG = "syncContactsWorker"

@AppScope
class SyncContactsRepositoryImpl @Inject constructor(
    private val workManager: WorkManager,
    private val appSettings: AppSettings
) : SyncContactsRepository {

    private val syncContactsFlow = MutableSharedFlow<WorkInfo?>()
    private val syncContactsScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @MainThread
    override suspend fun startSyncContacts() = withContext(Dispatchers.Main) {
        val operationId = OneTimeWorkRequestBuilder<SyncContactsWorker>()
            .addTag(SYNC_CONTACTS_WORKER_TAG)
            .build()
            .also { workManager.enqueue(it) }.id
        workManager.getWorkInfoByIdLiveData(operationId).observeForever(object : Observer<WorkInfo> {
            override fun onChanged(value: WorkInfo) {
                if (value.state.isFinished) {
                    workManager.getWorkInfoByIdLiveData(operationId).removeObserver(this)
                }
                syncContactsScope.launch {
                    syncContactsFlow.emit(value)
                }
            }
        })
    }

    override fun stopSyncContacts() {
        syncContactsScope.coroutineContext.cancelChildren()
        workManager.cancelAllWorkByTag(SYNC_CONTACTS_WORKER_TAG)
    }

    override fun observeSyncContactsWork(): Flow<WorkInfo?> {
        return syncContactsFlow.asSharedFlow()
    }

    override fun allowSyncContactsPrivacy(): Boolean {
        return appSettings.allowSyncContacts.getSync() == true
    }

    override suspend fun setSyncContactsPrivacy(allow: Boolean) {
        appSettings.allowSyncContacts.set(allow)
    }

    override fun observeSyncContactsPrivacy(): Preference<Boolean> {
        return appSettings.allowSyncContacts
    }
}

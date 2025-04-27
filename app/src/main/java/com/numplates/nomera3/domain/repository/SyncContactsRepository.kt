package com.numplates.nomera3.domain.repository

import androidx.work.WorkInfo
import com.meera.core.preferences.datastore.Preference
import kotlinx.coroutines.flow.Flow

interface SyncContactsRepository {

    suspend fun startSyncContacts()

    fun stopSyncContacts()

    fun observeSyncContactsWork(): Flow<WorkInfo?>

    fun allowSyncContactsPrivacy(): Boolean

    suspend fun setSyncContactsPrivacy(allow: Boolean)

    fun observeSyncContactsPrivacy(): Preference<Boolean>
}

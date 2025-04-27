package com.numplates.nomera3.modules.upload.domain.repository

import com.meera.db.DataStore
import com.meera.db.models.UploadItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadDataStore @Inject constructor(val dataStore: DataStore) {
    suspend fun getDataStream(): List<UploadItem> {
        return dataStore.uploadDao().getUploadItems()
    }

    suspend fun addOrReplace(uploadItem: UploadItem) {
        dataStore.uploadDao().insert(uploadItem)
    }

    suspend fun removeItem(uploadItem: UploadItem) {
        dataStore.uploadDao().remove(uploadItem.id)
    }
}

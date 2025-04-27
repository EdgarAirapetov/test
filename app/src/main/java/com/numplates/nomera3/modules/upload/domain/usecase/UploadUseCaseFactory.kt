package com.numplates.nomera3.modules.upload.domain.usecase

import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.core.di.scopes.AppScope
import com.meera.db.models.UploadItem
import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.moments.show.domain.MomentUploader
import com.numplates.nomera3.modules.upload.domain.repository.UploadDataStore
import com.numplates.nomera3.modules.upload.domain.usecase.post.UploadPostUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AppScope
class UploadUseCaseFactory @Inject constructor(
    val dataStore: UploadDataStore,
    private val metaDataDelegate: MediaFileMetaDataDelegate
) {
    suspend fun getUseCase(scope: CoroutineScope, uploadItem: UploadItem): UploadBaseUseCase {
        return when (uploadItem.type) {
            UploadType.Post, UploadType.EditPost, UploadType.EventPost -> UploadPostUseCase(
                scope,
                uploadItem,
                { item ->
                    scope.launch {
                        dataStore.addOrReplace(item)
                    }
                },
                metaDataDelegate
            )
            UploadType.Moment -> MomentUploader(scope, uploadItem) { newUploadItemToSave ->
                scope.launch {
                    dataStore.addOrReplace(newUploadItemToSave)
                }
            }
        }
    }
}

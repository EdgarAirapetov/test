package com.numplates.nomera3.modules.upload.domain.usecase

import com.meera.db.models.UploadItem
import com.numplates.nomera3.modules.upload.data.UploadResult
import kotlinx.coroutines.CoroutineScope

abstract class UploadBaseUseCase(
    protected val rootScope: CoroutineScope,
    protected val uploadItem: UploadItem,
    protected val updateStoreCallback: (UploadItem) -> Unit,
) {
    abstract suspend fun execute(): UploadResult

    protected suspend fun saveUploadItem(uploadItem: UploadItem) {
        updateStoreCallback(uploadItem)
    }
}

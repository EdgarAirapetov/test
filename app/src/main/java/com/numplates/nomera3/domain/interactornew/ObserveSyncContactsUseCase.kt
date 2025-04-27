package com.numplates.nomera3.domain.interactornew

import androidx.work.WorkInfo
import com.numplates.nomera3.domain.repository.SyncContactsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSyncContactsUseCase @Inject constructor(
    private val repository: SyncContactsRepository
) {
    fun invoke(): Flow<WorkInfo?> = repository.observeSyncContactsWork()
}

package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.domain.repository.SyncContactsRepository
import javax.inject.Inject

class StartSyncContactsUseCase @Inject constructor(
    private val repository: SyncContactsRepository
) {
    suspend fun invoke() = repository.startSyncContacts()
}

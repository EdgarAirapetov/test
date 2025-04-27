package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.domain.repository.SyncContactsRepository
import javax.inject.Inject

class SetSyncContactsPrivacyUseCase @Inject constructor(
    private val repository: SyncContactsRepository
) {
    suspend fun invoke(allowSyncContacts: Boolean) =
        repository.setSyncContactsPrivacy(allowSyncContacts)
}

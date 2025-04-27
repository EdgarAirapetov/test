package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.domain.repository.SyncContactsRepository
import javax.inject.Inject

class GetSyncContactsPrivacyUseCase @Inject constructor(
    private val repository: SyncContactsRepository
) {
    fun invoke(): Boolean = repository.allowSyncContactsPrivacy()
}

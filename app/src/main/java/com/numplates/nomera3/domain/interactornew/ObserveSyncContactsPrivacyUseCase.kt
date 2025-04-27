package com.numplates.nomera3.domain.interactornew

import com.meera.core.preferences.datastore.Preference
import com.numplates.nomera3.domain.repository.SyncContactsRepository
import javax.inject.Inject

class ObserveSyncContactsPrivacyUseCase @Inject constructor(
    private val repository: SyncContactsRepository
) {
    fun invoke(): Preference<Boolean> = repository.observeSyncContactsPrivacy()
}

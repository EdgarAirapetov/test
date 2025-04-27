package com.numplates.nomera3.modules.registration.data.repository

interface RegistrationGenerateUniqueNameRepository {
    suspend fun generate(
        source: String,
        success: (uniqueName: String?) -> Unit,
        fail: (e: Exception) -> Unit
    )
}
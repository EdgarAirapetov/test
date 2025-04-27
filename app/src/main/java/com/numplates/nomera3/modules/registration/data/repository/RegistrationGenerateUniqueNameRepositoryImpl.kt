package com.numplates.nomera3.modules.registration.data.repository

import com.numplates.nomera3.modules.registration.data.RegistrationApi
import com.numplates.nomera3.modules.registration.data.UniqueNameGenerationException

class RegistrationGenerateUniqueNameRepositoryImpl(
    private val api: RegistrationApi
): RegistrationGenerateUniqueNameRepository {
    override suspend fun generate(
        source: String,
        success: (uniqueName: String?) -> Unit,
        fail: (e: Exception) -> Unit
    ) {
        try {
            val result = api.generateUniqueName(source).data
            if (result != null) success(result)
            else fail(UniqueNameGenerationException())
        } catch (e: Exception) {
            fail(e)
        }
    }
}
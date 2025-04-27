package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.repository.RegistrationGenerateUniqueNameRepository
import javax.inject.Inject

class GenerateUniqueNameUseCase @Inject constructor(
    private val repository: RegistrationGenerateUniqueNameRepository
): BaseUseCaseCoroutine<GenerateUniqueNameParams, String?> {
    override suspend fun execute(
        params: GenerateUniqueNameParams,
        success: (String?) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.generate(
            source = params.source,
            success = success,
            fail = fail
        )
    }
}

data class GenerateUniqueNameParams(val source: String): DefParams()

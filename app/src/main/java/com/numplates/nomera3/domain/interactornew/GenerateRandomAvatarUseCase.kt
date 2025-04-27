package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.RandomAvatarResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams

class GenerateRandomAvatarUseCase(
    private val apiMain: ApiMain
) : BaseUseCaseCoroutine<GenerateRandomAvatarParam, ResponseWrapper<RandomAvatarResponse>> {

    override suspend fun execute(
        params: GenerateRandomAvatarParam,
        success: (ResponseWrapper<RandomAvatarResponse>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        try {
            val response = apiMain.generateRandomAvatar(params.gender)
            if (response.data != null) {
                success.invoke(response)
            } else {
                fail.invoke(IllegalArgumentException("${response.err.code}"))
            }
        } catch (e: Exception) {
            fail.invoke(e)
        }
    }

}

class GenerateRandomAvatarParam(val gender: Int) : DefParams()

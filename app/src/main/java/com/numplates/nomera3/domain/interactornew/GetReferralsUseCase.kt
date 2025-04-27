package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class GetReferralsUseCase(private val api: ApiMain) {

    suspend fun registerReferralCode(code: String) = api.registerReferralCode(code)
}

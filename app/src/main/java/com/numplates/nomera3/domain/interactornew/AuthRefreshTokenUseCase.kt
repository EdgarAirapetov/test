package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay

class AuthRefreshTokenUseCase(private val repository: ApiHiWay?) {

    fun refreshToken(refreshToken: String) =
            repository?.refreshToken(refreshToken)

}
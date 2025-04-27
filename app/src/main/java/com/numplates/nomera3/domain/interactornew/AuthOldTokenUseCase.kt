package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay

class AuthOldTokenUseCase(private var repository: ApiHiWay?) {

    fun migrateFromOldToken(oldToken: String) =
            repository?.oldToken(oldToken)

}
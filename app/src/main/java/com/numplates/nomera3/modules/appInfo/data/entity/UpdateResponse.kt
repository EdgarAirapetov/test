package com.numplates.nomera3.modules.appInfo.data.entity

sealed class UpdateResponse {

    class UpdateSuccessShowUpdate(
        val updateInfo: UpdateRecommendations,
        val version: String?
    ) : UpdateResponse()

    object UpdateSuccessNoUpdate : UpdateResponse()

    object UpdateError: UpdateResponse()
}

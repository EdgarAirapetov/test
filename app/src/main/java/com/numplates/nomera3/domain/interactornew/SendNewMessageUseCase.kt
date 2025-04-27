package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class SendNewMessageUseCase(private val api: ApiMain) {

    suspend fun newMessage(params: HashMap<String, Any>) = api.sendNewMessage(params)
}
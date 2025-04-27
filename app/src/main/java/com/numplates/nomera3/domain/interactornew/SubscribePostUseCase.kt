package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class SubscribePostUseCase(private val apiMain: ApiMain) {

    suspend fun subscribePostV2(postId: Long) = apiMain.subscribePost(postId)

}

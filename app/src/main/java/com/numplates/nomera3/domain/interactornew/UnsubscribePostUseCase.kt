package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class UnsubscribePostUseCase(private val apiMain: ApiMain) {


    suspend fun unsubscribePostV2(postId: Long) = apiMain.unsubscribePost(postId)


}

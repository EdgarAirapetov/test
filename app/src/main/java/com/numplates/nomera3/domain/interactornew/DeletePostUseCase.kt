package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class DeletePostUseCase(private val apiMain: ApiMain) {

    suspend fun deletePostV2(postId: Long) = apiMain.deletePost(postId)

}

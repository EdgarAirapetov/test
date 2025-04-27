package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.network.core.ResponseWrapper

class GetPostUseCase(private val apiMain: ApiMain) {

    suspend fun getPostV2(postId: Long): ResponseWrapper<Post?>? {
        return apiMain.getPost(postId)
    }
}

package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.Api
import com.numplates.nomera3.data.network.CommentEntity
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class AddPostCommentUseCase(private val api: Api) {

    fun addPostComment(postId: Long, text: String, commentId: Long) : Flowable<ResponseWrapper<CommentEntity>>{
        return api.addPostComment(postId, text, commentId)
    }

}

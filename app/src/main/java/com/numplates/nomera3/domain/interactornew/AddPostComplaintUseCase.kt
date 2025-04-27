package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class AddPostComplaintUseCase(private val apiMain: ApiMain) {

    suspend fun addPostComplaintV2(postId: Long) = apiMain.addComplainV2(hashMapOf(
            "post_id" to postId
    ))

    /**
     * https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/2043478085/Complaints#Create-complaint
     * */
    suspend fun addPostCommentComplaintV2(commentId: Long) = apiMain.addComplainV2(hashMapOf(
            "comment_id" to commentId
    ))
}

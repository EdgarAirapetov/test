package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper

class RepostUseCase(private val endpoints: ApiMain) {

    suspend fun repostRoadType(postId: Long, comment: String, commentSettings: Int): ResponseWrapper<Any>{
        return endpoints.repostRoadtape(postId, hashMapOf(
                "comment" to comment,
                "comment_availability" to commentSettings
        ))
    }

    suspend fun repostMessage(
        postId: Long,
        comment: String,
        idUsers: List<Long>,
        idRooms: List<Long>,
    ): ResponseWrapper<Any>{
        return endpoints.repostMessage(postId, hashMapOf(
                "comment" to comment,
                "user_ids" to idUsers,
                "room_ids" to idRooms
        ))
    }

    suspend fun repostGroup(postId: Long, comment: String, groupId: Long, commentSettings: Int): ResponseWrapper<Any>{
        return endpoints.repostGroup(postId, hashMapOf(
                "comment" to comment,
                "group_id" to groupId,
                "comment_availability" to commentSettings
        ))
    }

}
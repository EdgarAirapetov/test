package com.numplates.nomera3.data.newmessenger

import com.google.gson.annotations.SerializedName
import com.meera.db.models.chatmembers.UserEntity

data class GroupEntity(

        @SerializedName("created_at")
        var createdAt: Long,

        @SerializedName("creator")
        var creator: UserEntity,

        @SerializedName("description")
        var description: String,

        @SerializedName("id")
        var roomId: Long,

        @SerializedName("members_count")
        var membersCount: Int,

        @SerializedName("title")
        var title: String,

        @SerializedName("type")
        var type: String,

        @SerializedName("updated_at")
        var updatedAt: Long

)

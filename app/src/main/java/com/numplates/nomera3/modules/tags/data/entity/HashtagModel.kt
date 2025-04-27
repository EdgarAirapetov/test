package com.numplates.nomera3.modules.tags.data.entity

import com.google.gson.annotations.SerializedName

data class HashtagModel(
        @SerializedName("id")
        val id: Int,

        @SerializedName("text")
        val text: String,

        @SerializedName("count")
        val count: Long,

        @SerializedName("moderated")
        val moderated: String, // # "approved" | "not_moderated" | "approved" | "blocked"

        @SerializedName("created_at")
        val createdAt: Int,

        @SerializedName("updated_at")
        val updatedAt: Int
)
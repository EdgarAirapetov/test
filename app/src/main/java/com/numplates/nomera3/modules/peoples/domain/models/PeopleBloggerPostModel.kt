package com.numplates.nomera3.modules.peoples.domain.models

data class PeopleBloggerPostModel(
    val id: Long,
    val isAdultContent: Boolean,
    val cityId: Int,
    val countryId: Int,
    val createdAt: Long,
    val isDeleted: Boolean,
    val dislikes: Int,
    val isAllowedToComment: Boolean,
    val moderated: String,
    val privacy: String,
    val isSubscription: Boolean,
    val userId: Long,
    val updatedAt: Long,
    val preview: String?,
    val duration: Int?,
    val mediaContentUrl: String,
    val mediaContentType: String,
)

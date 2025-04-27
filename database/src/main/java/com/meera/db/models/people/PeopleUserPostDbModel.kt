package com.meera.db.models.people

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PeopleUserPostDbModel(
    val id: Long,
    val isAdultContent: Boolean,
    val cityId: Int,
    val commentAvailability: String,
    val comments: Int,
    val countryId: Int,
    val createdAt: Long,
    val isDeleted: Boolean,
    val dislikes: Int,
    val groupId: Int,
    val groupName: String?,
    val isAllowedToComment: Boolean,
    val likes: Int,
    val moderated: String,
    val privacy: String,
    val reposts: Int?,
    val isSubscription: Boolean,
    val userId: Long,
    val updatedAt: Long,
    val preview: String?,
    val duration: Int?,
    val mediaContentUrl: String,
    val mediaContentType: String,
) : Parcelable

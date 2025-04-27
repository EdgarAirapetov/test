package com.meera.db.models.message

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ParentMessage (
    var type: Int = -1,
    val eventCode: Int = -1,
    var creatorName: String = "",
    var imagePreview: String = "",
    var videoPreview: String = "",
    var messageContent: String = "",
    var imageCount: Int = 0,
    var createdAt: Long = -1,
    var parentId: String = "",
    var sharedProfileUrl: String = "",
    var isDeletedSharedProfile: Boolean = false,
    var sharedCommunityUrl: String = "",
    var isPrivateCommunity: Boolean = false,
    var isDeletedSharedCommunity: Boolean = false,
    var isEvent: Boolean = false,
    var isMoment:Boolean = false,
    var metadata: MessageMetadata? = null,
): Parcelable

package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Events(
        @SerializedName("friend_events") var friendEvents: Int,
        @SerializedName("messages") var messages: Int,
        @SerializedName("news") var news: Int,
        @SerializedName("friends_not_confirmed") var friendsNotConfirmed: Int,
        @SerializedName("friends_confirmed") var friends_confirmed: Int,
        @SerializedName("comment_events") var commentEvents: Int,
        @SerializedName("date") var date: Long,
        @SerializedName("version_state") var versionState: String,
        @SerializedName("update") var update: UpdateInfo,
        @SerializedName("group_comment_events") var groupCommentEvents: Int,
        @SerializedName("gift_events") var giftEvents: Int
): Serializable
package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.newmessenger.FriendEntity

/**
 * Json Messages response from server
 */
data class ResponseFriends(

        @SerializedName("response")
        var response: Friends

)


data class Friends(

        @SerializedName("friends")
        var friends: List<FriendEntity>
)

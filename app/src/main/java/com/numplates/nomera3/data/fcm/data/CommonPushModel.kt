package com.numplates.nomera3.data.fcm.data

data class CommonPushModel (
    val id: Int = -1,
    val channelIdBase: String,
    val action: String,
    val contentTitle: String,
    val contentText: String,
    val bigImage: String?,
    val avatar: String?,

    val roomId: Long? = 0,
    val userId: Long? = null,
    var eventId: String? = null,
    val postId: Long? = 0,
    var commentId: Long?,
    val groupId: Int? = null,
    val lastReaction: String? = null,
    var url: String? = "",
    val name: String? = null,
    val eventGroupId: String? = null,
    val momentId: Long? = null,
    val momentAuthorId: Long? = null
)

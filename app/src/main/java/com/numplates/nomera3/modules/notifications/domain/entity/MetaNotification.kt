package com.numplates.nomera3.modules.notifications.domain.entity

import com.meera.db.models.MomentAsset
import com.meera.db.models.PostAsset
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameEntity

data class MetaNotification(

        val postId: Long? = null,

        val postText: String? = null,

        val postAsset: PostAsset? = null,

        val momentAsset: MomentAsset? = null,

        val commentId: Long? = null,

        val comment: String? = null,

        val replyComment: String? = null,

        val giftId: Long? = null,

        val image: String? = null,

        val title: String? = null,

        val groupId: Long? = null,

        val groupName: String? = null,

        val roomId: Long? = null,

        val text: String? = null,

        val avatar: String? = null,

        val link: String? = null,

        var tagSpan: ParsedUniquename? = null,

        var tags: List<UniquenameEntity>? = null,

        var postTags: List<UniquenameEntity>? = null,

        var commentTags: List<UniquenameEntity>? = null,

        val communityAvatar: String? = null,

        val communityId: Int? = null,

        val communityName: String? = null,

        val isAnonym: Boolean? = null,

        val fromUserId: Long? = null,

        val media: Media? = null,

        var reaction:String? = null,

        val userBlocReason: String? = null,

        val userBlockedTo: Long? = null,

        val momentId:Long? = null,

        val momentAuthorId:Long? = null,

        val eventTitle: String? = null,

        val eventImageUrl: String? = null,

        val hasEventOnMap: Boolean? = null
)

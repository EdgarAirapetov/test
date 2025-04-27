package com.numplates.nomera3.modules.comments.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.UniquenameEntity
import com.numplates.nomera3.data.network.UserComments
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.android.parcel.RawValue

/**
 * Этот объект используется во всех слоях, что не по CLEAN
 * Задача на Техдолг:
 * TODO https://nomera.atlassian.net/browse/BR-17558
 */
data class CommentEntityResponse(
    @SerializedName("id")
    var id: Long,

    @SerializedName("moment_id")
    var momentId: Long,

    @SerializedName("deleted")
    var deleted: Int,

    @SerializedName("uniqname")
    var uniqname: String? = null,

    @SerializedName("uid")
    val uid: Long,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("number")
    val number: String? = null,

    @SerializedName("account_type")
    val accountType: Int,

    @SerializedName("account_color")
    val accountColor: Int,

    @SerializedName("text")
    var text: String? = null,

    @SerializedName("date")
    val date: Long,

    @SerializedName("resp_name")
    val respName: String = "",

    @SerializedName("cid")
    val cid: Int = 0,

    @SerializedName("image")
    val image: String,

    @SerializedName("user")
    val user: UserComments,

    @SerializedName("parent_id")
    val parentId: Long? = null,

    @SerializedName("deleted_by")
    val deletedBy: String?,

    //Поле выставляется в null или не проставляется в случае если никто из автора комментария и запрашивающего
    // комментарий пользователя не блокировали друг друга
    //Поле выставляется в "user" если автор комментария заблокировал запрашивающего комментарий пользователя
    //Поле выставляется в "me" если запрашивающий комментарий пользователь заблокировал автора комментария
    @SerializedName("blocked_by")
    val blockedBy: String? = null,

    @SerializedName("comments")
    val comments: CommentsEntityResponse? = null,

    var needToShowReply: Boolean? = null,

    @SerializedName("tags")
    var tags: @RawValue List<UniquenameEntity?>? = mutableListOf(),

    @SerializedName("reactions")
    var reactions: List<ReactionEntity>
)

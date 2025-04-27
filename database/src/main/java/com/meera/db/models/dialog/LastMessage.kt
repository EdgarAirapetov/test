package com.meera.db.models.dialog

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.MessageAttachment

import com.meera.db.models.message.MessageMetadata
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameEntity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class LastMessage(

    @SerializedName("id")
    val msgId: String,

    @SerializedName("content")
    var content: String? = null,

    @SerializedName("type")
    val type: String = "",

    @SerializedName("attachment")
    var attachment: @RawValue MessageAttachment? = null,

    @SerializedName("attachments")
    var attachments: @RawValue List<MessageAttachment>? = null,

    @SerializedName("code")
    var eventCode: Int? = null,

    @SerializedName("metadata")
    var metadata: MessageMetadata? = null,

    @SerializedName("creator")
    var creator: UserChat = UserChat(),

    @SerializedName("created_at")
    var createdAt: Long = 0L,

    @SerializedName("updated_at")
    var updatedAt: Long = 0L,

    @SerializedName("deleted")
    var deleted: Boolean = false,

    @SerializedName("delivered")
    var delivered: Boolean? = false,

    @SerializedName("readed")
    var readed: Boolean? = false,

    /**
     * Is successfully sent message to server
     */
    @ColumnInfo(name = "is_sent")
    var sent: Boolean = true,

    @SerializedName("tags")
    var tags: List<UniquenameEntity?>? = mutableListOf(),

    @ColumnInfo(name = "tags_span_data")
    var tagSpan: ParsedUniquename? = null


) : Parcelable {

    constructor() : this("")

}

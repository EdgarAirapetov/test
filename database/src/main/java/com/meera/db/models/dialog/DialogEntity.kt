package com.meera.db.models.dialog

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.meera.db.models.DraftUiModel
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "dialogs")
data class DialogEntity(

    @NonNull
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "room_id")
    var roomId: Long = 0L,

    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: String = "",

    @SerializedName("approved")
    @ColumnInfo(name = "approved")
    var approved: Int? = DialogApproved.ALLOW.key,

    // # optional, don't show for dialogs-like
    @SerializedName("title")
    @ColumnInfo(name = "title")
    var title: String? = "",

    @SerializedName("group_avatar")
    @ColumnInfo(name = "group_avatar")
    var groupAvatar: String? = "",

    // # optional, don't show for dialogs-like
    @SerializedName("description")
    @ColumnInfo(name = "description")
    var description: String? = "",

    @SerializedName("blocked")
    @ColumnInfo(name = "blocked")
    var blocked: Boolean? = false,

    @SerializedName("last_message")
    @ColumnInfo(name = "last_message")
    var lastMessage: LastMessage? = null,

    // # optional, don't show for dialogs-like
    @SerializedName("members_count")
    @ColumnInfo(name = "members_count")
    var membersCount: Int? = 0,

    @SerializedName("admins_count")
    @ColumnInfo(name = "admins_count")
    var adminsCount: Int? = 0,

    @SerializedName("companion")
    @Embedded(prefix = "companion_")
    var companion: UserChat = UserChat(),

    @SerializedName("creator")
    @Embedded(prefix = "creator_")
    var creator: UserChat = UserChat(),

    // Timestamp in milliseconds
    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0L,

    // Timestamp in milliseconds
    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = 0L,

    @SerializedName("deleted")
    @ColumnInfo(name = "deleted")
    var deleted: Boolean = false,

    @SerializedName("unreaded")
    @ColumnInfo(name = "unread_message_count")
    var unreadMessageCount: Int? = 0,

    // Field for ordering rooms
    @ColumnInfo(name = "last_message_updated_at")
    var lastMessageUpdatedAt: Long = 0L,

    @ColumnInfo(name = "creator_uid")
    var creatorUid: Long = 0L,

    @ColumnInfo(name = "companion_uid")
    var companionUid: Long = 0L,

    @SerializedName("unreaded_first_at")
    @ColumnInfo(name = "unreaded_first_at")
    var firstUnreadMessageTs: Long = 0,

    @ColumnInfo(name = "last_text_input_message")
    var lastTextInputMessage: String = "",

    @ColumnInfo(name = "companion_notifications_off")
    var companionNotificationsOff: Int? = 0,

    @SerializedName("mentionings_unread_count")
    @ColumnInfo(name = "mentionings_unread_count")
    var unreadMentionsCount: Int? = 0,

    @SerializedName("is_muted")
    @ColumnInfo(name = "is_muted")
    var isMuted: Boolean? = false,

    @SerializedName("need_to_show_unread_badge")
    @ColumnInfo(name = "need_to_show_unread_badge")
    var needToShowUnreadBadge: Boolean? = true,

    @SerializedName("style")
    @ColumnInfo(name = "dialog_style")
    var style: DialogStyle? = null,

    @ColumnInfo(name = "last_message_sent")
    var lastMessageSent: Boolean? = null,

    @ColumnInfo(name = "is_hidden")
    var isHidden: Boolean = companion.blacklistedByMe == 1,

    @Ignore
    var draft: DraftUiModel? = null

) : Parcelable

enum class DialogApproved(val key: Int) {
    NOT_DEFINED(0),
    ALLOW(1),
    FORBIDDEN(2);

    fun toInt() = this.key

    companion object {
        fun fromInt(intValue: Int) = when(intValue) {
            0 -> NOT_DEFINED
            1 -> ALLOW
            2 -> FORBIDDEN
            else -> null
        }
    }
}


package com.meera.db.models.message

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.meera.db.models.dialog.UserChat


@Entity(tableName = "messages_new")
data class MessageEntity(
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "message_id")
    var msgId: String = "",

    @SerializedName("parent_id")
    @ColumnInfo(name = "parent_id")
    var parentId: String? = null,

    @Ignore
    @SerializedName("parent")
    var parent: MessageEntity? = null,

    @SerializedName("author")
    var author: UserChat? = null,

    @SerializedName("room_id")
    @ColumnInfo(name = "room_id")
    var roomId: Long = 0L,

    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: String = "",

    @SerializedName("content")
    @ColumnInfo(name = "content")
    var content: String = "",

    @SerializedName("metadata")
    @ColumnInfo(name = "metadata")
    var metadata: MessageMetadata? = MessageMetadata(),

    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0L,

    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = 0L,

    @SerializedName("edited_at")
    @ColumnInfo(name = "edited_at")
    var editedAt: Long = 0L,

    @SerializedName("creator")
    @Embedded(prefix = "creator_")
    var creator: UserChat? = UserChat(),

    @SerializedName("deleted")
    @ColumnInfo(name = "deleted")
    var deleted: Boolean = false,

    /**
     * Доставлено ли сообщение другому пользователю - отправил ли сервер сообщение, если есть connection
     * (получил ли сообщение другой пользователь)
     */
    @SerializedName("delivered")
    @ColumnInfo(name = "delivered")
    var delivered: Boolean = false,

    /**
     * Прочитано ли сообщение другим пользователем
     */
    @SerializedName("readed")
    @ColumnInfo(name = "readed")
    var readed: Boolean = false,


    @SerializedName("attachment")
    @Embedded(prefix = "attachment_")
    var attachment: MessageAttachment = MessageAttachment(),


    /**
     * Is successfully sent message to server
     * Default sent successfully
     */
    @ColumnInfo(name = "sent", defaultValue = "true")
    var sent: Boolean = true,

    /**
     * Хэндлим одновременно sent и resendProgress для показа прогресса
     * или значка ошибки в зависимости от состояния ресенда
     */
    @ColumnInfo(name = "is_resend_progress", defaultValue = "false")
    var isResendProgress: Boolean = false,

    @ColumnInfo(name = "is_show_loading_progress", defaultValue = "false")
    var isShowLoadingProgress: Boolean = false,

    /**
     * Доступен ли ресенд для сообщения
     * Если сообщение содержит только медиа файл, и он был удалён локально,
     * то такое сообшение можно только удалить и нельзе переотправить
     */
    @ColumnInfo(name = "is_resend_available", defaultValue = "true")
    var isResendAvailable: Boolean = true,

    /**
     * Был ли показан Пуш о том, что существует непрочитанное сообщение
     * Пуш показывается 1 раз
     */
    @ColumnInfo(name = "is_resend_show_push", defaultValue = "false")
    var isResendShowPush: Boolean = false,

    @ColumnInfo(name = "is_editing_progress", defaultValue = "false")
    var isEditingProgress: Boolean = false,

    @Deprecated("Use msg.creator.userId")
    @ColumnInfo(name = "creator_uid", defaultValue = "0")
    var creatorUid: Long = 0,

    @SerializedName("code")
    @ColumnInfo(name = "event_code")
    var eventCode: Int? = null,

    /**
     * Field for trigger onBind view holder adapter
     */
    @ColumnInfo(name = "is_show_avatar", defaultValue = "true")
    var isShowAvatar: Boolean = true,


    /**
     * used for multi loading
     */
    @SerializedName("attachments")
    @ColumnInfo(name = "attachments")
    var attachments: List<MessageAttachment> = mutableListOf(),

    /**
     * when images is not uploaded(bad internet ect.) here will be saved local uri
     */
    @ColumnInfo(name = "resendImages")
    var resendImages: List<String> = arrayListOf(),

    @ColumnInfo(name = "is_server_message", defaultValue = "true")
    var isServerMessage: Boolean = true,


    @ColumnInfo(name = "is_play_voice_message", defaultValue = "false")
    var isPlayVoiceMessage: Boolean = false,

    /**
     * This field for refresh item in adapter
     * - increment counter for refresh item DAO - refreshMessageItem(...)
     */
    @ColumnInfo(name = "refresh_message_item", defaultValue = "0")
    var refreshMessageItem: Int = 0,


    @ColumnInfo(name = "show_unread_divider", defaultValue = "false")
    var isShowUnreadDivider: Boolean = false,

    @ColumnInfo(name = "response_data")
    var responseData: ResponseData = ResponseData(),

    @ColumnInfo(name = "item_type", defaultValue = "-1")
    var itemType: Int = -1,

    /**
     * Не сохраняем в БД. Сохраняем распаршенные данные тегов
     */
    @SerializedName("tags")
    var tags: List<UniquenameEntity?>? = mutableListOf(),

    @ColumnInfo(name = "tags_span_data")
    var tagSpan: ParsedUniquename? = null,

    @ColumnInfo(name = "parent_message")
    var parentMessage: ParentMessage? = null,

    @ColumnInfo(name = "is_show_giphy_watermark")
    var isShowGiphyWatermark: Boolean? = false,

    /**
     * null - кнопка expand невидима
     * true / false - кнопка видима, текст скрыт или раскрыт
     */
    @ColumnInfo(name = "is_expanded_recognized_text")
    var isExpandedRecognizedText: Boolean? = null,

    @ColumnInfo(name = "is_show_image_blur_chat_request")
    var isShowImageBlurChatRequest: Boolean? = false,

    /**
     * Хранит коллекцию из range.
     * Необходимо определить слова, которые возможно содержат поздравительные слова
     */
    @ColumnInfo(name = "birthday_range_list")
    var birthdayRangesList: List<IntRange>? = null,
)

data class MessageEntityNetwork(

    @SerializedName("id")
    @ColumnInfo(name = "message_id")
    var msgId: String = "",

    @SerializedName("parent_id")
    @ColumnInfo(name = "parent_id")
    var parentId: String? = null,

    @Ignore
    @SerializedName("parent")
    var parent: MessageEntity? = null,

    @SerializedName("author")
    var author: UserChat? = null,

    @SerializedName("room_id")
    @ColumnInfo(name = "room_id")
    var roomId: Long = 0L,

    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: String = "",

    @ColumnInfo(name = "item_type", defaultValue = "-1")
    var itemType: Int = -1,

    @SerializedName("content")
    @ColumnInfo(name = "content")
    var content: String = "",

    @ColumnInfo(name = "tags_span_data")
    var tagSpan: ParsedUniquename? = null,

    @SerializedName("metadata")
    @ColumnInfo(name = "metadata")
    var metadata: MessageMetadata? = MessageMetadata(),

    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0L,

    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = 0L,

    @SerializedName("edited_at")
    @ColumnInfo(name = "edited_at")
    var editedAt: Long = 0L,

    @SerializedName("creator")
    @Embedded(prefix = "creator_")
    var creator: UserChat? = UserChat(),

    @SerializedName("deleted")
    @ColumnInfo(name = "deleted")
    var deleted: Boolean = false,

    @SerializedName("delivered")
    @ColumnInfo(name = "delivered")
    var delivered: Boolean = false,

    @SerializedName("readed")
    @ColumnInfo(name = "readed")
    var readed: Boolean = false,

    @SerializedName("attachment")
    @Embedded(prefix = "attachment_")
    var attachment: MessageAttachment = MessageAttachment(),

    @SerializedName("attachments")
    @ColumnInfo(name = "attachments")
    var attachments: List<MessageAttachment> = mutableListOf(),

    @ColumnInfo(name = "sent", defaultValue = "true")
    var sent: Boolean = true,

    @ColumnInfo(name = "is_resend_progress", defaultValue = "false")
    var isResendProgress: Boolean = false,

    @ColumnInfo(name = "resendImages")
    var resendImages: List<String> = arrayListOf(),

    @ColumnInfo(name = "response_data")
    var responseData: ResponseData = ResponseData(),

    @ColumnInfo(name = "parent_message")
    var parentMessage: ParentMessage? = null,

    @ColumnInfo(name = "is_show_giphy_watermark")
    var isShowGiphyWatermark: Boolean? = false,

    @ColumnInfo(name = "is_show_image_blur_chat_request")
    var isShowImageBlurChatRequest: Boolean? = false,

    @ColumnInfo(name = "birthday_range_list")
    var birthdayRangesList: List<IntRange>? = null,

    )

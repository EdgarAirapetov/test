package com.numplates.nomera3.modules.chat

enum class ChatPayloadKeys(val key: String) {
    ID("id"),
    IDS("ids"),
    USER_ID("user_id"),
    ROOM_ID("room_id"),
    CONTENT("content"),
    ROOM_TYPE("type"),
    USER_TYPE("user_type"),
    TYPING_TYPE("type"),
    PARENT_ID("parent_id"),
    DIRECTION("direction"),
    LIMIT("limit"),
    TS("ts"),
    IS_BOTH("both"),

    ATTACHMENT("attachment"),
    ATTACHMENTS("attachments"),
    ATTACHMENT_DATA("data"),
    ATTACHMENT_URL("url"),
    ATTACHMENT_TYPE("type"),
    ATTACHMENT_LINK("link"),
    ATTACHMENT_RATIO("ratio"),
    ATTACHMENT_FAVORITE("favourite_id"),
    ATTACHMENT_RECENT("recent_id"),
    ATTACHMENT_STICKER("id"),
    ATTACHMENT_LOTTIE_URL("lottie_url"),
    ATTACHMENT_WEBP_URL("webp_url"),
    ATTACHMENT_EMOJI("emoji"),

    ATTACHMENT_METADATA("metadata"),
    ATTACHMENT_METADATA_RATIO("ratio"),
    ATTACHMENT_METADATA_POST("post"),
    ATTACHMENT_METADATA_MOMENT("moment"),
    ATTACHMENT_METADATA_DURATION("duration"),
    ATTACHMENT_METADATA_PREVIEW("preview"),
    ATTACHMENT_METADATA_IS_SILENT("is_silent"),
    ATTACHMENT_METADATA_LOW_QUALITY("low_quality"),

    RESEND_IMAGES("resendImages"),
    WAVE_FORM("wave_form"),
    VOICE_RECOGNIZED_TEXT("recognized_text"),

    SOCKET_STATUS("status"),
    CUSTOM_TITLE("custom_title")
}

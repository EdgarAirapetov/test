package com.numplates.nomera3.modules.notifications.data.api

enum class WebSocketApiEnum(val request: String) {
    WEB_SOCKET_MARK_POST_NOTIFICATION("post_readed"),
    WEB_SOCKET_MARK_POST_COMMENT_NOTIFICATION("comments_readed")
}
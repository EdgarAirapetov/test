package com.numplates.nomera3.modules.chat.domain.params


enum class MessagePaginationDirection(val paramValue: String) {
    BEFORE("before"),
    AFTER("after"),
    INITIAL("initial"),
    BOTH("both"),
    AFTER_BY_CREATED_AT("after_by_created_at")
}

enum class MessagePaginationUserType(val paramValue: String) {
    USER_CHAT("UserChat"), USER_SIMPLE("UserSimple")
}

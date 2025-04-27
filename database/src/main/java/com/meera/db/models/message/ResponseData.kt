package com.meera.db.models.message


data class ResponseData(
    val type: RequestDirection = RequestDirection.ZERO,
    val messageCount: Int = 0
)

enum class RequestDirection {
    ZERO,
    BEFORE,
    AFTER
}

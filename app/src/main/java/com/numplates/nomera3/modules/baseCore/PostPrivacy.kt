package com.numplates.nomera3.modules.baseCore

enum class PostPrivacy(val status: String) {
    PUBLIC("public"),
    PRIVATE("private");

    companion object {
        fun from(status: String): PostPrivacy = values().find { it.status == status } ?: PUBLIC
    }
}

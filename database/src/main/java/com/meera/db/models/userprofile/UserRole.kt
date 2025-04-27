package com.meera.db.models.userprofile

enum class UserRole(val value: String) {
    ANNOUNCE_USER("announce_user"),
    SUPPORT_USER("support_user"),
    SYSTEM_ADMIN("system_admin"),
    USER("user")
}


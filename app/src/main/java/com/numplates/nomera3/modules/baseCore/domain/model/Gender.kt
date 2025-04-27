package com.numplates.nomera3.modules.baseCore.domain.model

enum class Gender(val value: Int?) {
    MALE(1),
    FEMALE(0),
    UNKNOWN(null);

    companion object {
        fun fromValue(value: Int): Gender {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
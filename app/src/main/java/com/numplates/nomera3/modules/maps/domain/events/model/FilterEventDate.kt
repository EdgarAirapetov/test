package com.numplates.nomera3.modules.maps.domain.events.model

enum class FilterEventDate(val value: Int) {
    ALL(0),
    TODAY(1),
    TOMORROW(2),
    THIS_WEEK(3);

    companion object {
        fun fromValue(value: Int): FilterEventDate? = values().firstOrNull { it.value == value }
    }
}

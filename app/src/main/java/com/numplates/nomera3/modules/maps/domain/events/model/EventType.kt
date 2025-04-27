package com.numplates.nomera3.modules.maps.domain.events.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EventType(
    val value: Int
) : Parcelable {
    EDUCATION(0),
    ART(1),
    CONCERT(2),
    SPORT(3),
    TOURISM(4),
    GAMES(5),
    PARTY(6);

    companion object {
        fun fromValue(value: Int): EventType = values().firstOrNull { it.value == value }
            ?: getDefault()
        fun getDefault() = EDUCATION
    }
}

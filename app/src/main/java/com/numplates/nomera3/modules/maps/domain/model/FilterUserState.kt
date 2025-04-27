package com.numplates.nomera3.modules.maps.domain.model

enum class FilterUserState(val value: Int) {
    NORMAL(0),
    PICKUP(1),
    BROKEN(2);

    companion object {

        @JvmStatic
        fun allValue(): String {
            return values()
                .map { it.value }
                .joinToString(",")
        }
    }
}
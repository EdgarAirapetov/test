package com.numplates.nomera3.modules.maps.domain.model

enum class FilterGender(val value: Int) {
    FEMALE(0),
    MALE(1);

    companion object {

        @JvmStatic
        fun allValue(): String {
            return values()
                .map { it.value }
                .joinToString(",")
        }
    }
}
package com.numplates.nomera3.modules.search.domain.mapper

import com.numplates.nomera3.modules.search.ui.fragment.SHARP_SIGN

class HashTagQueryMapper {
    fun map(query: String): String {
        val hashPosition = query.indexOf(SHARP_SIGN)

        return if (hashPosition == 0) {
            query.replaceFirst(SHARP_SIGN, "").trim()
        } else {
            query
        }
    }
}

package com.numplates.nomera3.presentation.model.enums

enum class WhoCanCommentPostEnum(var state: Int) {
    NOBODY(0),
    EVERYONE(1),
    FRIENDS(2),
    COMMUNITY_MEMBERS(3);

    companion object {
        fun getEnumFromStringValue(strValue: String?): WhoCanCommentPostEnum {
            return when (strValue) {
                "friends" -> FRIENDS
                "nobody" -> NOBODY
                "community_members" -> COMMUNITY_MEMBERS
                else -> EVERYONE
            }
        }
    }
}

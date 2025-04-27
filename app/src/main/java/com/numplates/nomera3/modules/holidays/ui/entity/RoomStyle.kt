package com.numplates.nomera3.modules.holidays.ui.entity

data class RoomStyle(
    val type: String?,
    val background_dialog: String?,
    val background_anon: String?,
    val background_group: String?,
) {
    fun getBackground(roomType: RoomType): String? {
        return when (roomType) {
            RoomType.REGULAR -> background_dialog
            RoomType.ANON -> background_anon
            else -> background_group
        }
    }

}


enum class RoomType(val type: String) {
    REGULAR("regular"),
    ANON("ANON"),
    GROUP("GROUP")
}
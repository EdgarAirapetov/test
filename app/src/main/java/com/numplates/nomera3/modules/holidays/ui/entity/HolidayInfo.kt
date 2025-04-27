package com.numplates.nomera3.modules.holidays.ui.entity

data class HolidayInfo(
    val id: Long?,
    val code: String?,
    val title: String?,
    val mainButtonLinkEntity: MainButton,
    val startTime: Long?,
    val finishTime: Long?,
    val onBoardingEntity: OnBoarding,
    val hatsLink: Hats,
    val chatRoomEntity: RoomStyle,
    val product: Product?
) {
    companion object {
        const val HOLIDAY_NEW_YEAR = "newyear"
    }
}

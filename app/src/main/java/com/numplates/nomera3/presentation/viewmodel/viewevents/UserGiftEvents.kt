package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits

sealed class UserGiftEvents {
    object UserClearAdapterEvent: UserGiftEvents()
    object ErrorRequestEvent: UserGiftEvents()

    class FailDeleteGift(val position: Int, val shouldRefreshItem: Boolean): UserGiftEvents()

    class SuccessDeleteGift(val position: Int): UserGiftEvents()

    class OwnUserProfileEvent(
        var userName: String? = null
    ): UserGiftEvents()

    data class HolidayVisitsData(val visits: HolidayVisits): UserGiftEvents()
}

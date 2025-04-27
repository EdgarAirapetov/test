package com.numplates.nomera3.modules.holidays.ui.entity

import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity

data class HolidayVisits(
        val goalDays: Int?,
        var status: String?,
        var visitDays: String?
) {
    fun getAchievedDays(): String? {
        return if (status != HolidayVisitsEntity.STATUS_ACHIEVED) visitDays
        else null
    }
}

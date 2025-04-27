package com.numplates.nomera3.modules.holidays.data.repository

import com.numplates.nomera3.modules.holidays.domain.entity.HolidayVisitsModel

interface DailyVisitsRepository {
    suspend fun getDailyVisits(
            success: (data: HolidayVisitsModel) -> Unit,
            fail: (e: Exception) -> Unit
    )
}
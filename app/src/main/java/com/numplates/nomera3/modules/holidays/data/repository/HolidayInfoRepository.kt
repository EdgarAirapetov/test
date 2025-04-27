package com.numplates.nomera3.modules.holidays.data.repository

import com.numplates.nomera3.modules.holidays.domain.entity.HolidayInfoModel

interface HolidayInfoRepository {
    suspend fun getHolidayInfo(
        success: (data: HolidayInfoModel?) -> Unit,
        fail: (e: Exception) -> Unit
    )
}
package com.numplates.nomera3.modules.holidays.domain.interactor

import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.holidays.domain.usecase.GetHolidayParams
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits

interface HolidayDailyInteractor {

    suspend fun getDailyVisits(
        params: DefParams,
        success: (HolidayVisits) -> Unit,
        fail: (Throwable) -> Unit
    )

    suspend fun getHolidayInfo(
        params: GetHolidayParams,
        success: (HolidayInfo?) -> Unit,
        fail: (Throwable) -> Unit
    )
}

package com.numplates.nomera3.modules.holidays.domain.interactor

import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.holidays.domain.usecase.GetHolidayDailyVisitsUseCase
import com.numplates.nomera3.modules.holidays.domain.usecase.GetHolidayInfoUseCase
import com.numplates.nomera3.modules.holidays.domain.usecase.GetHolidayParams
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import javax.inject.Inject

class HolidayDailyInteractorImpl @Inject constructor(
    val holidayDailyVisitsUseCase: GetHolidayDailyVisitsUseCase,
    val holidayInfoUseCase: GetHolidayInfoUseCase,
) : HolidayDailyInteractor {

    override suspend fun getDailyVisits(
        params: DefParams,
        success: (HolidayVisits) -> Unit,
        fail: (Throwable) -> Unit
    ) = holidayDailyVisitsUseCase.execute(
        params = params,
        success = success,
        fail = fail
    )

    override suspend fun getHolidayInfo(
        params: GetHolidayParams,
        success: (HolidayInfo?) -> Unit,
        fail: (Throwable) -> Unit
    ) = holidayInfoUseCase.execute(
        params = params,
        success = success,
        fail = fail
    )
}

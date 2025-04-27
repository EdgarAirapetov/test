package com.numplates.nomera3.modules.holidays.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.holidays.data.repository.DailyVisitsRepository
import com.numplates.nomera3.modules.holidays.domain.mapper.toUI
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import javax.inject.Inject

class GetHolidayDailyVisitsUseCase @Inject constructor(
        private val repository: DailyVisitsRepository
): BaseUseCaseCoroutine<DefParams, HolidayVisits> {

    override suspend fun execute(params: DefParams,
                                 success: (HolidayVisits) -> Unit,
                                 fail: (Throwable) -> Unit) {
            repository.getDailyVisits({ success.invoke(it.toUI()) }, fail)
    }
}

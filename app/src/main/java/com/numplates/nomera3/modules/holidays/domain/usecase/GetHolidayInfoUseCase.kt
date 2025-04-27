package com.numplates.nomera3.modules.holidays.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.holidays.data.repository.HolidayInfoRepository
import com.numplates.nomera3.modules.holidays.domain.mapper.toUI
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import javax.inject.Inject

class GetHolidayInfoUseCase @Inject constructor(
    private val repository: HolidayInfoRepository
): BaseUseCaseCoroutine<GetHolidayParams,HolidayInfo?> {

    override suspend fun execute(
        params: GetHolidayParams,
        success: (HolidayInfo?) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getHolidayInfo( { data->
            success.invoke(data?.toUI())
        }, fail)
    }

}

object GetHolidayParams: DefParams()

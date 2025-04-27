package com.numplates.nomera3.modules.holidays.data.repository

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.holidays.data.mapper.toModel
import com.numplates.nomera3.modules.holidays.domain.entity.HolidayVisitsModel
import javax.inject.Inject

class DailyVisitsRepositoryImpl @Inject constructor(
        private val apiMain: ApiMain,
) : DailyVisitsRepository {

    override suspend fun getDailyVisits(
            success: (data: HolidayVisitsModel) -> Unit,
            fail: (e: Exception) -> Unit,
    ) {
        try {
            val response   = apiMain.getHolidayDailyVisits()
            if (response.data != null) {
                success.invoke(response.data.toModel())
            } else {
                fail.invoke(Exception(response.message))
            }
        } catch (e: Exception) {
            fail.invoke(e)
        }
    }
}

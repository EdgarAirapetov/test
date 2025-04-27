package com.numplates.nomera3.modules.holidays.data.repository

import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.holidays.data.mapper.toModel
import com.numplates.nomera3.modules.holidays.domain.entity.HolidayInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HolidayInfoRepositoryImpl @Inject constructor(
    private val apiMain: ApiMain): HolidayInfoRepository {

    override suspend fun getHolidayInfo(success: (data: HolidayInfoModel?) -> Unit,
                                        fail: (e: Exception) -> Unit) {

        withContext(Dispatchers.IO) {
            try {
                val response = apiMain.getHolidayInfo()
                if (response.data != null) {
                    success.invoke(response.data?.holidayInfoEntity?.toModel())
                } else {
                    fail.invoke(IllegalArgumentException("${response.err.code}"))
                }
            } catch (e: Exception) {
                fail.invoke(e)
            }
        }

    }
}

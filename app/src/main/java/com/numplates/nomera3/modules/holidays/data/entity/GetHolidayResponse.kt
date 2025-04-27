package com.numplates.nomera3.modules.holidays.data.entity

import com.google.gson.annotations.SerializedName

data class GetHolidayResponse(
    @SerializedName("holiday")
    val holidayInfoEntity: HolidayInfoEntity?
)
package com.numplates.nomera3.modules.holidays.data.entity

import com.google.gson.annotations.SerializedName

data class HolidayVisitsEntity(
        @SerializedName("goal_days")
        val goalDays: Int?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("visit_days")
        val visitDays: String?
) {
    companion object {
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_DAY_SKIPPED = "skipped"
        const val STATUS_ACHIEVED = "achieved"
    }
}
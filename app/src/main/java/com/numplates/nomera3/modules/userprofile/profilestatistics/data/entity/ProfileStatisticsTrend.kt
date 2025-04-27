package com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity

import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsViewsTrendType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsVisitorsTrendType

enum class ProfileStatisticsTrend {
    POSITIVE, NEGATIVE, SAME;

    fun toVisitorsAmplitudeProperty(): AmplitudePropertyProfileStatisticsVisitorsTrendType = when (this) {
        POSITIVE -> AmplitudePropertyProfileStatisticsVisitorsTrendType.POSITIVE
        NEGATIVE -> AmplitudePropertyProfileStatisticsVisitorsTrendType.NEGATIVE
        SAME -> AmplitudePropertyProfileStatisticsVisitorsTrendType.NEUTRAL
    }

    fun toViewsAmplitudeProperty(): AmplitudePropertyProfileStatisticsViewsTrendType = when (this) {
        POSITIVE -> AmplitudePropertyProfileStatisticsViewsTrendType.POSITIVE
        NEGATIVE -> AmplitudePropertyProfileStatisticsViewsTrendType.NEGATIVE
        SAME -> AmplitudePropertyProfileStatisticsViewsTrendType.NEUTRAL
    }
}
package com.numplates.nomera3.modules.moments.util

import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.domain.DEFAULT_MOMENTS_PAGE_LIMIT
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase

fun isAddCreateMomentForRoadType(roadType: RoadTypesEnum): Boolean {
    return roadType == RoadTypesEnum.MAIN
}

fun isMomentsPagingUsedForRoadType(roadType: RoadTypesEnum): Boolean {
    return roadType == RoadTypesEnum.SUBSCRIPTION || roadType == RoadTypesEnum.MAIN
}

fun getMomentSourceForRoadType(roadType: RoadTypesEnum?): GetMomentDataUseCase.MomentsSource? {
    return when (roadType) {
        RoadTypesEnum.MAIN -> GetMomentDataUseCase.MomentsSource.Main
        RoadTypesEnum.SUBSCRIPTION -> GetMomentDataUseCase.MomentsSource.Subscription
        else -> return null
    }
}

fun getMomentPaginationLimitsForRoadType(roadType: RoadTypesEnum): Int? {
    return when (roadType) {
        RoadTypesEnum.MAIN,
        RoadTypesEnum.SUBSCRIPTION -> DEFAULT_MOMENTS_PAGE_LIMIT
        else -> null
    }
}

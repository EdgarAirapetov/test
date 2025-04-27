package com.numplates.nomera3.modules.baseCore.helper.amplitude.search

import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment

fun Int.toRoadSearchAmplitudeProperty(): AmplitudeSearchOpenWhereProperty? {
    return when (this) {
        BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index -> AmplitudeSearchOpenWhereProperty.MAIN_FEED
        BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index -> AmplitudeSearchOpenWhereProperty.SELF_FEED
        BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index -> AmplitudeSearchOpenWhereProperty.FOLLOW_FEED
        else -> null
    }
}

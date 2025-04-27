package com.numplates.nomera3.modules.maps.domain.analytics.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDateChoice
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsLastPlaceChoice
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTimeChoice
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent

data class MapEventCreatedConfigurationParamsAnalyticsModel(
    val mapMoveUse: Boolean,
    val findMeUse: Boolean,
    val writeLocationUse: Boolean,
    val lastPlaceChoice: AmplitudePropertyMapEventsLastPlaceChoice,
    val dateChoice: AmplitudePropertyMapEventsDateChoice,
    val timeChoice: AmplitudePropertyMapEventsTimeChoice,
    val defaultTypeEvent: AmplitudePropertyMapEventsTypeEvent,
    val eventNumber: Int
)

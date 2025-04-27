package com.numplates.nomera3.modules.maps.domain.analytics

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsCreateTapWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDeleteWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGeoServiceName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGetThereWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsListWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsWantToGoWhere
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedConfigurationParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventDeletedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel

interface MapAnalyticsRepository {
    fun logMapEventOnboardingAction(
        actionType: AmplitudePropertyMapEventsOnboardingActionType,
        typeEvent: AmplitudePropertyMapEventsTypeEvent,
        defaultTypeEvent: AmplitudePropertyMapEventsTypeEvent,
        onboardingType: AmplitudePropertyMapEventsOnboardingType
    )

    fun logSelfFeedVisibilityChangeToAll()

    fun logMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel: MapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel: MapEventCreatedEventParamsAnalyticsModel
    )

    fun logMapEventDelete(
        mapEventDeletedEventParamsAnalyticsModel: MapEventDeletedEventParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsDeleteWhere
    )

    fun logMapEventCreateEventTap(where: AmplitudePropertyMapEventsCreateTapWhere)

    fun logMapEventWantToGo(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsWantToGoWhere,
        mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel
    )

    fun logMapEventGetTherePress(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsGetThereWhere
    )

    fun logMapEventToNavigator(
        geoServiceName: AmplitudePropertyMapEventsGeoServiceName,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel
    )

    fun logMapEventMemberDelete(mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel)

    fun logMapEventMemberDeleteYouself(mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel)

    fun logMapEventLimitAlert()

    fun logMapEventsListPress()

    fun logMapEventsListPopupShown(where: AmplitudePropertyMapEventsListWhere)

    fun logMapEventsListFilterClosed()
}

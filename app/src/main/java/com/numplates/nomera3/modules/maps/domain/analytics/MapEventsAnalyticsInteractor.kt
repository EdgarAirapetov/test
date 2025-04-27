package com.numplates.nomera3.modules.maps.domain.analytics

import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.AmplitudeComplaints
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.RulesOpenWhere
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
import javax.inject.Inject

class MapEventsAnalyticsInteractor @Inject constructor(
    private val mapAnalyticsRepository: MapAnalyticsRepository,
    private val amplitudeComplaints: AmplitudeComplaints
) {
    fun logMapEventOnboardingAction(
        actionType: AmplitudePropertyMapEventsOnboardingActionType,
        typeEvent: AmplitudePropertyMapEventsTypeEvent,
        defaultTypeEvent: AmplitudePropertyMapEventsTypeEvent,
        onboardingType: AmplitudePropertyMapEventsOnboardingType
    ) = mapAnalyticsRepository.logMapEventOnboardingAction(
        actionType = actionType,
        typeEvent = typeEvent,
        defaultTypeEvent = defaultTypeEvent,
        onboardingType = onboardingType
    )

    fun logSelfFeedVisibilityChangeToAll() = mapAnalyticsRepository.logSelfFeedVisibilityChangeToAll()

    fun logMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel: MapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel: MapEventCreatedEventParamsAnalyticsModel
    ) = mapAnalyticsRepository.logMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel = mapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel = mapEventCreatedEventParamsAnalyticsModel
    )

    fun logMapEventDelete(
        mapEventDeletedEventParamsAnalyticsModel: MapEventDeletedEventParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsDeleteWhere
    ) = mapAnalyticsRepository.logMapEventDelete(
        mapEventDeletedEventParamsAnalyticsModel = mapEventDeletedEventParamsAnalyticsModel,
        where = where
    )

    fun logMapEventCreateEventTap(where: AmplitudePropertyMapEventsCreateTapWhere) =
        mapAnalyticsRepository.logMapEventCreateEventTap(where = where)

    fun logMapEventWantToGo(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsWantToGoWhere,
        mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel
    ) = mapAnalyticsRepository.logMapEventWantToGo(
        mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
        where = where,
        mapEventInvolvementParamsAnalyticsModel = mapEventInvolvementParamsAnalyticsModel
    )

    fun logMapEventGetTherePress(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsGetThereWhere
    ) = mapAnalyticsRepository.logMapEventGetTherePress(
        mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
        where = where
    )

    fun logMapEventToNavigator(
        geoServiceName: AmplitudePropertyMapEventsGeoServiceName,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel
    ) = mapAnalyticsRepository.logMapEventToNavigator(
        geoServiceName = geoServiceName,
        mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel
    )

    fun logMapEventMemberDelete(mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel) =
        mapAnalyticsRepository.logMapEventMemberDelete(mapEventIdParamsAnalyticsModel)

    fun logMapEventMemberDeleteYouself(mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel) =
        mapAnalyticsRepository.logMapEventMemberDeleteYouself(mapEventIdParamsAnalyticsModel)

    fun logMapEventLimitAlert() = mapAnalyticsRepository.logMapEventLimitAlert()

    fun logOpenRules() = amplitudeComplaints.rulesOpen(RulesOpenWhere.ONBOARDING_EVENTS)

    fun logMapEventsListPress() = mapAnalyticsRepository.logMapEventsListPress()

    fun logMapEventsListPopupShown(where: AmplitudePropertyMapEventsListWhere) =
        mapAnalyticsRepository.logMapEventsListPopupShown(where)

    fun logMapEventsListFilterClosed() = mapAnalyticsRepository.logMapEventsListFilterClosed()
}

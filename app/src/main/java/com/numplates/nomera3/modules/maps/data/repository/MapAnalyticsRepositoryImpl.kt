package com.numplates.nomera3.modules.maps.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySettingVisibility
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudeMapEvents
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsCreateTapWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDeleteWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGeoServiceName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGetThereWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsListWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsWantToGoWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeSelfFeedVisibilityChangeWhereProperty
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsRepository
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedConfigurationParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventDeletedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

@AppScope
class MapAnalyticsRepositoryImpl @Inject constructor(
    private val amplitudeMapEvents: AmplitudeMapEvents,
    private val amplitudeProfile: AmplitudeProfile,
    private val userRepository: UserRepository,
    private val mapEventsRepository: MapEventsRepository
) : MapAnalyticsRepository {

    private val scope = MainScope() + CoroutineExceptionHandler { _, throwable -> Timber.e(throwable) }

    override fun logMapEventOnboardingAction(
        actionType: AmplitudePropertyMapEventsOnboardingActionType,
        typeEvent: AmplitudePropertyMapEventsTypeEvent,
        defaultTypeEvent: AmplitudePropertyMapEventsTypeEvent,
        onboardingType: AmplitudePropertyMapEventsOnboardingType
    ) = amplitudeMapEvents.onMapEventOnboardingAction(
        actionType = actionType,
        typeEvent = typeEvent,
        defaultTypeEvent = defaultTypeEvent,
        onboardingType = onboardingType,
        userId = userRepository.getUserUid()
    )

    override fun logSelfFeedVisibilityChangeToAll() = amplitudeProfile.logSelfFeedVisibilityChange(
        where = AmplitudeSelfFeedVisibilityChangeWhereProperty.MAP,
        visibility = AmplitudePropertySettingVisibility.ALL,
        userId = userRepository.getUserUid()
    )

    override fun logMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel: MapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel: MapEventCreatedEventParamsAnalyticsModel
    ) = amplitudeMapEvents.onMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel = mapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel = mapEventCreatedEventParamsAnalyticsModel
    )

    override fun logMapEventDelete(
        mapEventDeletedEventParamsAnalyticsModel: MapEventDeletedEventParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsDeleteWhere
    ) {
        scope.launch {
            val activeEventCounter = mapEventsRepository.getActiveEventCount() + 1
            amplitudeMapEvents.onMapEventDelete(
                mapEventDeletedEventParamsAnalyticsModel = mapEventDeletedEventParamsAnalyticsModel,
                where = where,
                activeEventCounter = activeEventCounter
            )
        }
    }

    override fun logMapEventCreateEventTap(where: AmplitudePropertyMapEventsCreateTapWhere) =
        amplitudeMapEvents.onMapEventCreateEventTap(
            where = where,
            userId = userRepository.getUserUid()
        )

    override fun logMapEventWantToGo(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsWantToGoWhere,
        mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel
    ) = amplitudeMapEvents.onMapEventWantToGo(
        userId = userRepository.getUserUid(),
        mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
        where = where,
        mapEventInvolvementParamsAnalyticsModel = mapEventInvolvementParamsAnalyticsModel
    )

    override fun logMapEventGetTherePress(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsGetThereWhere
    ) = amplitudeMapEvents.onMapEventGetTherePress(
        userId = userRepository.getUserUid(),
        mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
        where = where
    )

    override fun logMapEventToNavigator(
        geoServiceName: AmplitudePropertyMapEventsGeoServiceName,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel
    ) = amplitudeMapEvents.onMapEventToNavigator(
        geoServiceName = geoServiceName,
        userId = userRepository.getUserUid(),
        mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel
    )

    override fun logMapEventMemberDelete(mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel) =
        amplitudeMapEvents.onMapEventMemberDelete(
            userId = userRepository.getUserUid(),
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel
        )

    override fun logMapEventMemberDeleteYouself(mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel) =
        amplitudeMapEvents.onMapEventMemberDeleteYouself(
            userId = userRepository.getUserUid(),
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel
        )

    override fun logMapEventLimitAlert() = amplitudeMapEvents.onMapEventLimitAlert(userRepository.getUserUid())

    override fun logMapEventsListPress() = amplitudeMapEvents.onMapEventsListPress(userRepository.getUserUid())

    override fun logMapEventsListPopupShown(where: AmplitudePropertyMapEventsListWhere) =
        amplitudeMapEvents.onMapEventsListPopupShown(
            userId = userRepository.getUserUid(),
            where = where
        )

    override fun logMapEventsListFilterClosed() =
        amplitudeMapEvents.onMapEventsListFilterClosed(userRepository.getUserUid())
}

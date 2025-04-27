package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedConfigurationParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventDeletedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import org.json.JSONObject
import javax.inject.Inject

interface AmplitudeMapEvents {
    fun onMapEventOnboardingAction(
        actionType: AmplitudePropertyMapEventsOnboardingActionType,
        typeEvent: AmplitudePropertyMapEventsTypeEvent,
        defaultTypeEvent: AmplitudePropertyMapEventsTypeEvent,
        onboardingType: AmplitudePropertyMapEventsOnboardingType,
        userId: Long
    )

    fun onMapEventCreateEventTap(
        where: AmplitudePropertyMapEventsCreateTapWhere,
        userId: Long
    )

    fun onMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel: MapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel: MapEventCreatedEventParamsAnalyticsModel
    )

    fun onMapEventDelete(
        mapEventDeletedEventParamsAnalyticsModel: MapEventDeletedEventParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsDeleteWhere,
        activeEventCounter: Int
    )

    fun onMapEventWantToGo(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsWantToGoWhere,
        mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel
    )

    fun onMapEventGetTherePress(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsGetThereWhere
    )

    fun onMapEventToNavigator(
        geoServiceName: AmplitudePropertyMapEventsGeoServiceName,
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
    )

    fun onMapEventMemberDelete(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
    )

    fun onMapEventMemberDeleteYouself(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
    )

    fun onMapEventLimitAlert(
        userId: Long
    )

    fun onMapEventsListPress(
        userId: Long
    )

    fun onMapEventsListPopupShown(
        userId: Long,
        where: AmplitudePropertyMapEventsListWhere
    )

    fun onMapEventsListFilterClosed(
        userId: Long
    )
}

class AmplitudeMapEventsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMapEvents {

    override fun onMapEventOnboardingAction(
        actionType: AmplitudePropertyMapEventsOnboardingActionType,
        typeEvent: AmplitudePropertyMapEventsTypeEvent,
        defaultTypeEvent: AmplitudePropertyMapEventsTypeEvent,
        onboardingType: AmplitudePropertyMapEventsOnboardingType,
        userId: Long
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_ONBOARDING_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(typeEvent)
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.DEFAULT_TYPE_EVENT,
                        value = defaultTypeEvent._value
                    )
                    addProperty(onboardingType)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun onMapEventCreateEventTap(where: AmplitudePropertyMapEventsCreateTapWhere, userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_CREATE_TAP,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun onMapEventCreated(
        mapEventCreatedConfigurationParamsAnalyticsModel: MapEventCreatedConfigurationParamsAnalyticsModel,
        mapEventCreatedEventParamsAnalyticsModel: MapEventCreatedEventParamsAnalyticsModel
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_CREATED,
            properties = {
                it.apply {
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.MAP_MOVE_USE,
                        value = mapEventCreatedConfigurationParamsAnalyticsModel.mapMoveUse
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.FIND_ME_USE,
                        value = mapEventCreatedConfigurationParamsAnalyticsModel.findMeUse
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.WRITE_LOCATION_USE,
                        value = mapEventCreatedConfigurationParamsAnalyticsModel.writeLocationUse
                    )
                    addProperty(mapEventCreatedConfigurationParamsAnalyticsModel.lastPlaceChoice)
                    addProperty(mapEventCreatedConfigurationParamsAnalyticsModel.dateChoice)
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_DATE,
                        value = mapEventCreatedEventParamsAnalyticsModel.eventDate
                    )
                    addProperty(mapEventCreatedConfigurationParamsAnalyticsModel.timeChoice)
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_TIME,
                        value = mapEventCreatedEventParamsAnalyticsModel.eventTime
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.DEFAULT_TYPE_EVENT,
                        value = mapEventCreatedConfigurationParamsAnalyticsModel.defaultTypeEvent._value
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_TYPE,
                        value = mapEventCreatedEventParamsAnalyticsModel.eventType._value
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.HAVE_PHOTO,
                        value = mapEventCreatedEventParamsAnalyticsModel.havePhoto
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_NUMBER,
                        value = mapEventCreatedConfigurationParamsAnalyticsModel.eventNumber
                    )
                    addEventIdParamsAnalyticsModel(
                        mapEventCreatedEventParamsAnalyticsModel.mapEventIdParamsAnalyticsModel
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.CHAR_DESCRIPTION_COUNT,
                        value = mapEventCreatedEventParamsAnalyticsModel.charDescriptionCount
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_NAME,
                        value = mapEventCreatedEventParamsAnalyticsModel.eventName
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_LOCATION,
                        value = mapEventCreatedEventParamsAnalyticsModel.eventLocation
                    )
                    addProperty(mapEventCreatedEventParamsAnalyticsModel.dayWeekEvent)
                }
            }
        )
    }

    override fun onMapEventDelete(
        mapEventDeletedEventParamsAnalyticsModel: MapEventDeletedEventParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsDeleteWhere,
        activeEventCounter: Int
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_DELETED,
            properties = {
                it.apply {
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.DATE_EVENT,
                        value = mapEventDeletedEventParamsAnalyticsModel.dateEvent
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.TIME_EVENT,
                        value = mapEventDeletedEventParamsAnalyticsModel.timeEvent
                    )
                    addProperty(mapEventDeletedEventParamsAnalyticsModel.typeEvent)
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.EVENT_TIMER,
                        value = mapEventDeletedEventParamsAnalyticsModel.eventTimer
                    )
                    addProperty(where)
                    addEventIdParamsAnalyticsModel(mapEventDeletedEventParamsAnalyticsModel.mapEventIdParamsAnalyticsModel)
                    addProperty(
                        propertyName = AmplitudePropertyMapEventsConst.ACTIVE_EVENT_COUNTER,
                        value = activeEventCounter
                    )
                    addInvolvementParamsAnalyticsModel(
                        mapEventDeletedEventParamsAnalyticsModel.mapEventInvolvementParamsAnalyticsModel
                    )
                }
            }
        )
    }

    override fun onMapEventWantToGo(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsWantToGoWhere,
        mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_WANT_TO_GO,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addEventIdParamsAnalyticsModel(mapEventIdParamsAnalyticsModel)
                    addProperty(where)
                    addInvolvementParamsAnalyticsModel(mapEventInvolvementParamsAnalyticsModel)
                }
            }
        )
    }

    override fun onMapEventGetTherePress(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
        where: AmplitudePropertyMapEventsGetThereWhere
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_GET_THERE_PRESS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addEventIdParamsAnalyticsModel(mapEventIdParamsAnalyticsModel)
                    addProperty(where)
                }
            }
        )
    }

    override fun onMapEventToNavigator(
        geoServiceName: AmplitudePropertyMapEventsGeoServiceName,
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.EVENT_TO_NAVIGATOR,
            properties = {
                it.apply {
                    addProperty(geoServiceName)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addEventIdParamsAnalyticsModel(mapEventIdParamsAnalyticsModel)
                }
            }
        )
    }

    override fun onMapEventMemberDelete(userId: Long, mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.EVENT_MEMBER_DELETE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addEventIdParamsAnalyticsModel(mapEventIdParamsAnalyticsModel)
                }
            }
        )
    }

    override fun onMapEventMemberDeleteYouself(
        userId: Long,
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.EVENT_MEMBER_DELETE_YOUSELF,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addEventIdParamsAnalyticsModel(mapEventIdParamsAnalyticsModel)
                }
            }
        )
    }

    override fun onMapEventLimitAlert(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENT_LIMIT_ALERT,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun onMapEventsListPress(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENTS_LIST_PRESS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun onMapEventsListPopupShown(userId: Long, where: AmplitudePropertyMapEventsListWhere) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENTS_LIST_POPUP_SHOW,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(where)
                }
            }
        )
    }

    override fun onMapEventsListFilterClosed(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMapEventsEventName.MAP_EVENTS_LIST_FILTER_CLOSED,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    private fun JSONObject.addEventIdParamsAnalyticsModel(
        mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel
    ) {
        addProperty(
            propertyName = AmplitudePropertyMapEventsConst.EVENT_ID,
            value = mapEventIdParamsAnalyticsModel.eventId
        )
        addProperty(
            propertyName = AmplitudePropertyMapEventsConst.AUTHOR_ID,
            value = mapEventIdParamsAnalyticsModel.authorId
        )
    }

    private fun JSONObject.addInvolvementParamsAnalyticsModel(
        mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel
    ) {
        addProperty(
            propertyName = AmplitudePropertyMapEventsConst.MEMBERS_COUNT,
            value = mapEventInvolvementParamsAnalyticsModel.membersCount
        )
        addProperty(
            propertyName = AmplitudePropertyMapEventsConst.REACTION_COUNT,
            value = mapEventInvolvementParamsAnalyticsModel.reactionCount
        )
        addProperty(
            propertyName = AmplitudePropertyMapEventsConst.COMMENT_COUNT,
            value = mapEventInvolvementParamsAnalyticsModel.commentCount
        )
    }
}

package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeMapEventsEventName(
    private val event: String
) : AmplitudeName {
    MAP_EVENT_ONBOARDING_ACTION("map event onb action"),
    MAP_EVENT_CREATE_TAP("map event create tap"),
    MAP_EVENT_CREATED("map event created"),
    MAP_EVENT_DELETED("map event delete"),
    MAP_EVENT_WANT_TO_GO("event want to go"),
    MAP_EVENT_GET_THERE_PRESS("event get there press"),
    EVENT_TO_NAVIGATOR("event to navigator"),
    EVENT_MEMBER_DELETE("event member delete"),
    EVENT_MEMBER_DELETE_YOUSELF("event member delete youself"),
    MAP_EVENT_LIMIT_ALERT("map event limit alert"),
    MAP_EVENTS_LIST_PRESS("events list press"),
    MAP_EVENTS_LIST_POPUP_SHOW("map events list pop up show"),
    MAP_EVENTS_LIST_FILTER_CLOSED("map events list filter closed");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyMapEventsOnboardingActionType(val property: String) :
    AmplitudeProperty {
    CREATE_EVENT("create event"),
    CLOSE("close"),
    CONFIRM("accessibly");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.ACTION_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsTypeEvent(val property: String) : AmplitudeProperty {
    EDUCATION("education"),
    ART("art"),
    CONCERT("concert"),
    SPORT("sports"),
    TOURISM("tourism"),
    GAMES("games"),
    PARTY("party"),
    NONE("none");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.TYPE_EVENT

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsOnboardingType(val property: String) : AmplitudeProperty {
    FIRST("first"),
    SECOND("second");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.ONBOARDING_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsCreateTapWhere(val property: String) : AmplitudeProperty {
    ONBOARDING("onb"),
    LONGTAP("map longtap"),
    BUTTON("map button");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsLastPlaceChoice(val property: String) : AmplitudeProperty {
    WRITE("write"),
    FIND_ME("find me"),
    MOVE("move");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.LAST_PLACE_CHOICE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsDateChoice(val property: String) : AmplitudeProperty {
    CUSTOM("custom"),
    DEFAULT("default");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.DATE_CHOICE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsTimeChoice(val property: String) : AmplitudeProperty {
    CUSTOM("custom"),
    DEFAULT("default");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.TIME_CHOICE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsDayWeekEvent(val property: String) : AmplitudeProperty {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.DAY_WEEK_EVENT

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsDeleteWhere(val property: String) : AmplitudeProperty {
    POST("post"),
    MAP("map");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsWantToGoWhere(val property: String) : AmplitudeProperty {
    FEED("feed"),
    MAP("map"),
    LIST("list");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsGetThereWhere(val property: String) : AmplitudeProperty {
    FEED("feed"),
    MAP("map"),
    LIST("list");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsGeoServiceName(val property: String) : AmplitudeProperty {
    GOOGLE("Google"),
    YANDEX("Yandex"),
    YANDEX_NAVIGATOR("Yandex navigator"),
    WAZE("Waze"),
    TWOGIS("2GIS"),
    SYGIC_GPS("Sygic GPS"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.GEOSERVICE_NAME

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapEventsListWhere(val property: String) : AmplitudeProperty {
    NEARBY("nearby"),
    MY_CREATOR("my creator"),
    MY_MEMBER("my member"),
    ARCHIVE_CREATOR("archive creator"),
    ARCHIVE_MEMBER("archive member");

    override val _name: String
        get() = AmplitudePropertyMapEventsConst.WHERE

    override val _value: String
        get() = property
}


object AmplitudePropertyMapEventsConst {
    const val ACTION_TYPE = "action type"
    const val TYPE_EVENT = "type event"
    const val DEFAULT_TYPE_EVENT = "default type"
    const val ONBOARDING_TYPE = "onb type"
    const val WHERE = "where"
    const val MAP_MOVE_USE = "map move use"
    const val FIND_ME_USE = "find me use"
    const val WRITE_LOCATION_USE = "write location use"
    const val LAST_PLACE_CHOICE = "last place choice"
    const val DATE_CHOICE = "date choice"
    const val EVENT_DATE = "event date"
    const val TIME_CHOICE = "time choice"
    const val EVENT_TIME = "event time"
    const val EVENT_TYPE = "event type"
    const val HAVE_PHOTO = "have photo"
    const val EVENT_NUMBER = "event number"
    const val EVENT_ID = "event id"
    const val AUTHOR_ID = "author id"
    const val CHAR_DESCRIPTION_COUNT = "char description count"
    const val EVENT_NAME = "event name"
    const val EVENT_LOCATION = "event location"
    const val DAY_WEEK_EVENT = "day week event"
    const val DATE_EVENT = "date event"
    const val TIME_EVENT = "time event"
    const val EVENT_TIMER = "event timer"
    const val ACTIVE_EVENT_COUNTER = "active event counter"
    const val REACTION_COUNT = "reaction count"
    const val COMMENT_COUNT = "comment count"
    const val MEMBERS_COUNT = "members count"
    const val GEOSERVICE_NAME = "geoservice name"
}

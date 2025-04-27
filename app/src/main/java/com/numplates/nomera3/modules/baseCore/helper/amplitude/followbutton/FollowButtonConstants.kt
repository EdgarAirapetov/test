package com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

const val FOLLOW_BUTTON_FROM = "from" // Отмечаем id пользователя, который подписался
const val FOLLOW_BUTTON_TO = "to" // Отмечаем id пользователя, на которого подписались
const val FOLLOW_BUTTON_WHERE = "where" // Отмечаем, откуда была совершена подписка
const val FOLLOW_BUTTON_TYPE = "type" // Отмечаем, как пользователь подписался

enum class FollowButtonConstants(
    private val event: String
) : AmplitudeName {

    FOLLOW_ACTION("follow"),
    UNFOLLOW_ACTION("unfollow");

    override val eventName: String
        get() = event
}

enum class AmplitudeFollowButtonPropertyWhere(val property: String) : AmplitudeProperty {

    SEARCH("search"),
    USER_PROFILE("user profile"),
    MOMENTS("moments"),
    USER_FRIENDS("user friends"),
    USER_FOLLOWERS("user followers"),
    USER_FOLLOWS("user follows"),
    MUTUAL_FOLLOWS("mutual follows"),
    FOLLOW_FEED("follow feed"),
    MAIN_FEED("main feed"),
    SELF_FEED("self feed"),
    USER_PROFILE_FEED("user profile feed"),
    PROFILE_FEED("profile feed"),
    CHAT("chat"),
    COMMUNITY("community"),
    HASHTAG("hashtag"),
    NOTIFICATION("notification"),
    USER_SNIPPET("user snippet"),
    EVENT_SNIPPET("event snippet"),
    ADVICE_TO_FOLLOW_PEOPLE("advice to follow people"),
    ADVICE_TO_FOLLOW_SEARCH("advice to follow search"),
    SUGGEST_MAIN_FEED("suggest main feed"),
    SUGGEST_USER_PROFILE("suggest user profile"),
    OTHER("other");

    override val _name: String
        get() = FOLLOW_BUTTON_WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyType(val property: String) : AmplitudeProperty {

    PROFILE("profile"),
    POST("post"),
    POST_MENU("post menu"),
    MAP_SNIPPET("map snippet"),
    OTHER("other");

    override val _name: String
        get() = FOLLOW_BUTTON_TYPE

    override val _value: String
        get() = property
}

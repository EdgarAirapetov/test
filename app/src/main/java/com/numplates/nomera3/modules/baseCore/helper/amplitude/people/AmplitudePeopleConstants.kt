package com.numplates.nomera3.modules.baseCore.helper.amplitude.people

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudePeopleWhereProperty(
    private val property: String
) : AmplitudeProperty {
    ICON_FRIEND("icon friend"),
    ICON_FOLLOW("icon follow"),
    FIND_FRIEND_BUTTON("find friend button"),
    TAB_BAR("tabbar"),
    SYNC_CONTACTS("sync contacts"),
    SUGGEST_MAIN_FEED_MORE("suggest main feed more"),
    SUGGEST_MAIN_FEED_SEE_ALL("suggest main feed see all"),
    SUGGEST_USER_PROFILE("suggest user profile"),
    FOUND_PEOPLE_USER_FEED("found people user feed"),
    FOUND_PEOPLE_FOLLOW_FEED("found people follow feed"),
    USER_PROFILE("user profile"),
    SEE_REC_FOLLOW_FEED("see rec follow feed"),
    PROFILE_COMMUNITIES("profile communities"),
    OTHER("other");

    override val _value: String
        get() = property

    override val _name: String
        get() = PeopleConstants.WHERE
}

enum class AmplitudePeopleWhich(
    private val property: String
) : AmplitudeProperty {
    COMMUNITY("community"),
    PEOPLE("people");

    override val _value: String
        get() = property

    override val _name: String
        get() = PeopleConstants.WHICH
}

enum class AmplitudePeopleSectionChangeEventName(
    private val event: String
) : AmplitudeName {

    PEOPLE_SECTION_CHANGE("people section change");

    override val eventName: String
        get() = event
}

enum class AmplitudePeopleContentCardEventName(
    private val event: String
) : AmplitudeName {

    PEOPLE_CONTENT_CARD("content card tap");

    override val eventName: String
        get() = event
}

enum class AmplitudePeopleContentCardProperty(
    private val property: String
) : AmplitudeProperty {

    SEARCH("search"),
    PEOPLE("people");

    override val _value: String
        get() = property

    override val _name: String
        get() = PeopleConstants.WHERE
}

enum class AmplitudePeopleSectionChangeProperty(
    private val property: String
) : AmplitudeProperty {

    PEOPLE("people"),
    COMMUNITY("community");

    override val _value: String
        get() = property

    override val _name: String
        get() = PeopleConstants.SECTION
}

enum class AmplitudePeopleName(
    private val event: String
) : AmplitudeName {
    TAB_BAR_PEOPLE("tabbar people");

    override val eventName: String
        get() = event
}

object PeopleConstants {
    const val WHERE = "where"
    const val SECTION = "section"
    const val HAVE_VIDEO = "have video"
    const val HAVE_PHOTO = "have photo"
    const val POST_ID = "post id"
    const val USER_ID = "user id"
    const val AUTHOR_ID = "author id"
    const val WHICH = "which"
}

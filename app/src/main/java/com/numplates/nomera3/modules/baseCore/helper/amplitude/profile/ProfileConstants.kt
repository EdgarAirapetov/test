package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class ProfileEvents(private val event: String) : AmplitudeName {
    PHOTO_ACTION("photo action"),
    MAIN_PHOTO_CHANGES("main photo changes"),
    ALERT_POST_WITH_NEW_AVATAR_ACTION("alert post with new avatar action"),
    PRIVACY_POST_WITH_NEW_AVATAR_CHANGE("privacy post with new avatar change"),
    USER_CARD_HIDE("user card hide"),
    PROFILE_EDIT_TAP("profile edit tap"),
    SELF_VISIBILITY_FEED_CHANGE("self feed visibility change");

    override val eventName: String
        get() = event
}


enum class AmplitudePropertyPhotoAction(val property: String) : AmplitudeProperty {
    ACTION_TYPE("action type"),
    WHOSE("whose"),
    USER_ID("user id"),
    AUTHOR_ID("author id"),
    WHERE("where");

    override val _value: String
        get() = property

    override val _name: String
        get() = ProfileEvents.PHOTO_ACTION.eventName
}


enum class AmplitudePhotoActionValuesActionType(val property: String) : AmplitudeProperty {
    SAVE("save"),
    PHOTO_CHANGE("photo change"),
    AVATAR_CREATE("avatar create"),
    MAKE_THE_MAIN("make the main"),
    DELETE("delete");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyPhotoAction.ACTION_TYPE.property
}

enum class AmplitudePhotoActionValuesWhose(val property: String) : AmplitudeProperty {
    MY("my"),
    USER("user");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyPhotoAction.WHOSE.property
}

enum class AmplitudePhotoActionValuesWhere(val property: String) : AmplitudeProperty {
    POST("post"),
    AVATAR("avatar"),
    AVATAR_PROFILE("avatar profile"),
    GALLERY("gallery"),
    CHAT("chat");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyPhotoAction.WHERE.property
}

enum class AmplitudePropertyMainPhotoChanges(val property: String) : AmplitudeProperty {
    HOW("how"),
    USER_ID("user id");


    override val _value: String
        get() = property

    override val _name: String
        get() = ProfileEvents.MAIN_PHOTO_CHANGES.eventName
}

enum class AmplitudeMainPhotoChangesValuesHow(val property: String) : AmplitudeProperty {
    DOWNLOAD_NEW_PHOTO("download new photo"),
    AVATAR_CREATE("avatar create"),
    CHOOSE_FROM_ABOUT_ME("choose from about me"),
    CHOOSE_FROM_AVATARS("choose from avatars");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudePropertyAlertPostWithNewAvatar(val property: String) : AmplitudeProperty {
    ACTION_TYPE("action type"),
    FEED_TYPE("feed type"),
    TOGGLE_POSITION("toggle position"),
    USER_ID("user id");

    override val _value: String
        get() = property

    override val _name: String
        get() = ProfileEvents.ALERT_POST_WITH_NEW_AVATAR_ACTION.eventName
}

enum class AmplitudeAlertPostWithNewAvatarValuesActionType(val property: String) :
    AmplitudeProperty {
    PUBLISH("publish"),
    NO_THANKS("no thanks"),
    CLOSE("close");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudeAlertPostWithNewAvatarValuesFeedType(val property: String) : AmplitudeProperty {
    MAIN_FEED("main feed"),
    SELF_FEED("self feed"),
    NO_PUBLISH("no publish");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudeAlertPostWithNewAvatarValuesTogglePosition(val property: String) :
    AmplitudeProperty {
    ON("on"),
    OFF("off");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudePropertyPrivacyPostWithNewAvatarChange(val property: String) :
    AmplitudeProperty {
    WHERE("where"),
    PUBLISH_SETTINGS("publish settings");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudePrivacyPostWithNewAvatarChangeValuesWhere(val property: String) :
    AmplitudeProperty {
    SETTINGS("settings"),
    ALERT("alert");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings(val property: String) :
    AmplitudeProperty {
    MAIN_FEED("main feed"),
    SELF_FEED("self feed"),
    NO_PUBLISH("no publish");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMainPhotoChanges.HOW.property
}

enum class AmplitudeInfluencerProperty(
    val property: String
) : AmplitudeProperty {
    INFLUENCER("influencer"),
    HOT_AUTHOR("hot author"),
    NONE("none");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.INFLUENCER
}

enum class AmplitudeProfileEntranceEventName(
    val event: String
) : AmplitudeName {
    PROFILE_ENTRANCE("profile enterance");

    override val eventName: String
        get() = event
}

enum class AmplitudeUserCardHideWhereProperty(
    private val property: String
) : AmplitudeProperty {
    POSSIBLE_FRIENDS("possible friends"),
    SUGGEST("suggest");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudeUserCardHideSectionProperty(
    private val property: String
) : AmplitudeProperty {
    USER_PROFILE("user profile"),
    MAIN_FEED("main feed"),
    PEOPLE("people"),
    SEARCH("search");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.SECTION
}

enum class AmplitudeFriendRequestPropertyWhere(
    private val property: String
) : AmplitudeProperty {
    SHAKE("shake"),
    INCOMING_APPLICATIONS("incoming applications"),
    NOTIFICATIONS("notifications"),
    OTHER("other");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudeSelfFeedVisibilityChangeWhereProperty(
    private val property: String
) : AmplitudeProperty {
    SETTINGS("settings"),
    MAP("map");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}


enum class AmplitudeProfileEditTapProperty(
    private val property: String
) : AmplitudeProperty {
    PROFILE("profile"),
    SETTINGS("settings"),
    DEEPLINK("deeplink"),
    OTHER("other");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

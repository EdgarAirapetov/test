package com.numplates.nomera3.modules.baseCore.helper.amplitude

import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeEventName(val event: String) {
    TEST_BACK_PRESS("test back press"),
    PROFILE_ENTRANCE("profile enterance"),
    FEED_FILTER_OPEN("feed filter open"),
    SEARCH_MENU_OPEN("search menu open"),
    SEARCH_INPUT("search input"),
    SEARCH_AT_SIGN("@ search"),
    SEARCH_BY_NUMBER_BUTTON_TAP("license plate search press"),
    EMOJI_TAP("emoji panel use"),
    COMMUNITY_CREATE_MENU_OPEN("community create menu open"),
    COMMUNITY_CREATED("community created"),
    COMMUNITY_DELETED("community deleted"),
    COMMUNITY_OPEN("community open"),
    COMMUNITY_SHARE("community share"),
    COMMUNITY_FOLLOW("community follow"),
    COMMUNITY_UNFOLLOW("community unfollow"),
    CHAT_OPEN("chat open"),
    GROUP_CHAT_CREATE("group chat create"),
    GROUP_CHAT_DELETE("group chat delete"),
    MESSAGE_SEND("message send"),
    CHAT_GIF_BUTTON_PRESS("gif button press"),
    PRIVACY_SETTINGS("privacy settings"),
    PUSH_ANSWER_TAP("push answer tap"),
    POST_SHARE_MENU_OPEN("post share menu open"),
    POST_SHARE_MENU_CLOSE("post share menu close"),
    POST_SHARE("post share"),
    POST_SHARE_MENU_SETTINGS_TAP("post share menu settings tap"),
    PUSH_TAP("push tap"),

    // profile
    TRANSPORT_ADD("transport add"),
    UPDATE_BTN_CLICKED("update info tap"),
    UPDATE_BTN_SHOWN("update info showing"),
    SEND_GIFT_BACK_PRESS("gift send back press"),
    SEND_GIFT("gift send"),
    POP_UP_SHOW("pop up showing"),
    NEW_YEAR_CANDY_COLLECT("candy collect"),

    // ------ OLD ------------
    AVATAR_DOWNLOADED("avatar downloaded"),

    REACTION_PANEL_OPEN("reactions panel open"),
    REACTION_TO_COMMENT("reaction to comment"),

    POST_CREATED("post created"),
    POST_EDITED("post change"),
    BUTTON_POST_CREATE_TAP("button post create tap"),
    LIKE("like"),
    DISLIKE("dislike"),

    OPEN_VIDEO_FEED("video feed open"),
    OPEN_SELF_FEED("self feed open"),
    OPEN_MAIN_FEED("main feed open"),
    OPEN_FOLLOW_FEED("follow feed open"),
    PRESS_MORE_POST("more press"),

    PROFILE_SHARE("profile share"),
    OPEN_PICKER_AVATAR_CHANGE("photo install menu open"),
    PHOTO_SCREEN_OPEN("photo screen open"),
    VIP_BUYING("vip buying"),
    CALLS_PERMISSION("call permission"),
    REGISTRATION_COMPLETED("reg finish"),
    LOGIN_FINISHED("reg3 login finished"),
    ONBOARDING("onboarding"),
    FIRST_TIME_OPEN("1-st app open"),
    DELETE_NOTIFICATION("delete notification"),
    DELETE_ALL_NOTIFICATION("delete all notifications"),
    POST_MENU_ACTION("post menu action"),
    SEARCH_BY_NUMBER("license plate search finished"),
    FEED_SCROLL("feed scroll"),
    MUSIC_ADD_PRESS("music add press"),
    MUSIC_PLAY("music play"),
    BOTTOM_BAR_COMMUNICATION("tabbar communication"),
    BOTTOM_BAR_PROFILE("tabbar profile"),
    BOTTOM_BAR_COMMUNITY("tabbar community"),
    BOTTOM_BAR_ROAD("tabbar road"),
    REPORT_SECOND_ACTION("report second action"),
    FRIEND_ADD("friend add"),
    COMPLAIN("profile report finish"),
    FRIEND_DEL("friend del"),
    UNSUBSCRIBE_USER("unfollow"),
    CHANGE_STATUS("change status"),
    ANIMATED_AVATAR_OPENED("avatar constructor opened"),
    ANIMATED_AVATAR_CREATED("avatar created"),
    AVATAR_CREATED("avatar downloaded"),
    PHOTO_SELECTION("photo selection"),
    REGISTRATION_HELP("help press"),
    REGISTRATION_CLOSE("registration close"),
    LOGIN("reg1 login start"),
    CODE_ENTER("reg2 code enter"),
    REGISTRATION_NAME("reg3 nickname"),
    REGISTRATION_BIRTHDAY("reg4 birthday"),
    REGISTRATION_GENDER("reg5 gender"),
    REGISTRATION_LOCATION("reg6 location"),
    REGISTRATION_PHOTO("reg7 photo"),
    RECOGNITION_TAP("recognition tap"),
    PROFILE_DELETE("profile delete"),
    PROFILE_RESTORE("profile restore"),
    HASH_TAG_PRESS("hashtag press"),
    UNDERSTANDABLY_PRESS("understandably press"),
    GROUP_DESCRIPTION_CHANGE("group description change"),
    GROUP_TITLE_CHANGE("group title change"),
    POST_DELETE("post delete"),
    USER_EXIT("exit"),
    BLOCK("block"),
    UNBLOCK("user unblock"),
    CHAT_CREATE("chat create"),
    TOGGLE_PRESS("toggle press"),
    CHAT_UNLOCK("chat unlock"),
    PRIVACY_MAP_CLICK("privacy map visibility tap"),
    PRIVACY_MAP_CHANGED("privacy map visibility change"),
    PRIVACY_MAP_VISIBILITY_NEVER_TAP("privacy map visibility never tap"),
    PRIVACY_MAP_VISIBILITY_ALWAYS_TAP("privacy map visibility always tap"),
    PRIVACY_MAP_DELETE_ALL_PRESS("privacy map delete all press"),
    MESSAGE_FORWARD_TAP("message  forward tap"),
    MESSAGE_FORWARD_SEND("message  forward send"),
    CALL("call"),
    CALL_CANCEL("call cancel"),
    CHAT_REQUEST_ACTION("request action"),
    FB_NEW_TOKEN("firebase new token"),
    FB_NEW_MSG("firebase new msg")
}

enum class AmplitudePropertyWhere(val property: String) : AmplitudeProperty {
    NOTIFICATION("notification"),                       // из уведомлений
    CHAT("chat"),                                       // из чата
    FEED("feed"),                                       // из дороги
    MAP("map"),                                         // из карты
    FEED_PROFILE_WALL("profile wall"),                  // клик на аватар поста из своей дороги
    SEARCH("search"),                                   // из результатов поиска
    YOU_VISITED("you visited"),                         // из вкладки "Вы посещали на странице поиска"
    COMMUNITY("community"),                             // из сообщества
    FRIEND("friend"),                                   // из списка друзей
    FOLLOWERS("followers"),                             // из списка подписчиков
    FOLLOWS("follows"),                                 // из списка подписок
    PROFILE_WALL("profile wall"),                       // из своего профиля
    GIFTS("gifts"),                                     // из списка подарков
    STATISTIC_REACTIONS("statistic reactions"),         // из шторки статистики реакций
    OTHER("other"),                                     // не подпадает под любые критерии выше

    // ----
    REGISTRATION("registration"),
    SETTINGS("settings"),
    PROFILE("profile"),
    USER_PROFILE("user profile"),
    USER_PROFILE_ROAD("user profile road"),
    PHOTO("photo"),
    POST_DETAIL("post detail screen"),
    MUSIC_SEARCH("music_search"),
    OPEN_FEED("open feed"),
    MAIN_FEED("main feed"),
    UNDEFINED_FEED("undefined feed"),
    PROFILE_FEED("profile feed"),
    USER_PROFILE_FEED("user profile feed"),
    SUBSCRIPTION_FEED("follow feed"),
    SELF_FEED("self feed"),
    FOLLOW_FEED("follow feed"),
    COMMUNITY_FEED("community"),
    VIDEO_FEED("video feed"),
    POST("post"),
    MAIN_ROAD("main feed"),
    SELF_ROAD("self feed"),
    USER_ROAD("user feed"),
    FOLLOW_ROAD("follow road"),
    PROFILE_ROAD("profile road"),
    COMMUNICATION("communication"),
    NEW_GROUP_CHAT("new group chat"),
    MY_PROFILE("my profile"),
    ANOTHER_PROFILE("another profile"),
    HASHTAG("hashtag"),
    NOTIFICATIONS("notifications"),
    LINK("link"),
    PUSH("push"),
    ANNOUNCEMENT("post anons"),
    NEW_MESSAGE("new message"),
    MAP_SNIPPET("map snippet"),
    USER_SNIPPET("user snippet"),
    EVENT_SNIPPET("event snippet"),
    POSSIBLE_MEMBER_EVENT("possible member event"),
    MSG_REQUEST("msg request"),
    PROFILE_STATISTICS("profile statistics"),
    USER_FRIENDS("user friends"),
    USER_FOLLOWERS("user followers"),
    USER_FOLLOWS("user follows"),
    MUTUAL_FOLLOWS("mutual follows"),
    SHAKE("shake"),
    SUGGEST_USER_PROFILE("suggest user profile"),
    SUGGEST_MAIN_FEED("suggest main feed"),
    PEOPLE_VIEW_ALL_PEOPLE("people view all people"),
    PEOPLE_VIEW_ALL_SEARCH("people view all search"),
    CONTENT_ADVICE_TO_FOLLOW_PEOPLE("content advice to follow people"),
    CONTENT_ADVICE_TO_FOLLOW_SEARCH("content advice to follow search"),
    ADVICE_TO_FOLLOW_PEOPLE("advice to follow people"),
    ADVICE_TO_FOLLOW_SEARCH("advice to follow search"),
    ADD_FRIENDS_PEOPLE("add friends people"),
    ADD_FRIENDS_SEARCH("add friends search"),
    VIDEO_POST("video post"),
    DEEPLINK("deeplink"),
    PROFILE_SHARE("profile share"),
    MOMENT("moment"),
    MAP_EVENT("map event"),
    MAP_EVENTS_LIST_CREATOR("events list creator");


    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    companion object {
        infix fun from(property: String): AmplitudePropertyWhere? =
            AmplitudePropertyWhere.values().firstOrNull { it.property == property }
    }
}

/**
 * "Отмечаем, откуда пользователь совершил действие
 * moment - момент
 * main feed - главная дорога
 * self feed - моя дорога
 * follow feed - подписочная дорога
 * chat - чат
 * profile - мой профиль
 * user profile - чужой профиль
 * community - сообщество
 * hashtag - хэштег
 * notification - уведомление
 * other - другое"
 */
enum class AmplitudePropertyWhence(val property: String) : AmplitudeProperty {
    MAIN_FEED("main feed"),
    MOMENT("moment"),
    SELF_FEED("self feed"),
    FOLLOW_FEED("follow feed"),
    CHAT("chat"),
    PROFILE("profile"),
    USER_PROFILE("user profile"),
    COMMUNITY("community"),
    HASHTAG("hashtag"),
    NOTIFICATION("notification"),
    MAP("map"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHENCE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyFrom(val property: String) : AmplitudeProperty {
    SELF_PROFILE("self_profile"),
    OTHER_PROFILES("other_profiles");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.FROM
}

enum class AmplitudePropertyOpenType(val property: String) : AmplitudeProperty {
    DEPLOY("deploy"),
    SEPARATE("separate window");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.OPEN_TYPE
}

enum class AmplitudePropertyBackType(val property: String) : AmplitudeProperty {
    SWIPE("swipe"),
    BACK("back");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

enum class AmplitudeCreatePostWhichButton(val property: String) : AmplitudeProperty {
    ICON("icon"),
    LINE("line"),
    BUTTON_IN_STATISTICS("button in statistics");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.CREATE_POST_WHICH_BUTTON
}

enum class AmplitudePropertyPostType(val property: String) : AmplitudeProperty {
    POST("post"),
    REPOST("repost"),
    NONE("0");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.POST_TYPE
}

enum class AmplitudePropertyContentType(val property: String) : AmplitudeProperty {
    SINGLE("single"),
    MULTIPLE("multiple"),
    NONE("none");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.POST_CONTENT_TYPE
}

enum class AmplitudePropertyCommentsSettings(val property: String) : AmplitudeProperty {
    FOR_ALL("for all"),
    FRIENDS("friends"),
    COMMUNITY_MEMBERS("community_members"),
    NOBODY("nobody");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.COMMENTS_SETTINGS
}

enum class AmplitudePropertyCommunityWhere(val property: String) : AmplitudeProperty {
    INSIDE("inside"),
    OUTSIDE("outside"),
    LINK("link");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudePropertyWhereMusicPlay(val property: String) : AmplitudeProperty {
    POST("post"),
    MUSIC_SEARCH("music search");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudePropertyActionPlayStopMusic(val property: String) : AmplitudeProperty {
    PLAY("play"),
    STOP("stop");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

enum class AmplitudePropertyActionType(val property: String) : AmplitudeProperty {
    UPDATE("update"),
    CLOSE("close");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

enum class AmplitudePropertyCommentType(val property: String) : AmplitudeProperty {
    COMMENT("comment"),
    REPLAY("replay");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.COMMENT_TYPE
}

enum class AmplitudePropertyPushPermission(val property: String) : AmplitudeProperty {
    UNKNOWN(UNKNOWN_VALUE),
    GRANTED("granted"),
    NOT_GRANTED("not granted");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.PUSH_PERMISSION
}

enum class AmplitudePropertyPushEnabled(val property: String) : AmplitudeProperty {
    UNKNOWN(UNKNOWN_VALUE),
    TRUE(TRUE_VALUE),
    FALSE(FALSE_VALUE);

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.PUSH_ENABLED
}

enum class AmplitudePropertyGeoEnabled(val property: String) : AmplitudeProperty {
    UNKNOWN(UNKNOWN_VALUE),
    TRUE(TRUE_VALUE),
    FALSE(FALSE_VALUE);

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.GEO_ENABLED
}

enum class AmplitudePropertyUserName(val property: String) : AmplitudeProperty {
    UNKNOWN(UNKNOWN_VALUE),
    DEFAULT("default"),
    CUSTOM("custom");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.USER_NAME
}

enum class AmplitudePropertyChatUserChatStatus(val property: String) : AmplitudeProperty {
    FRIEND("friend"),
    MUTUAL_FOLLOW("mutual follow"),
    FOLLOW("follow"),
    FOLLOWER("follower"),
    NOBODY("nobody");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.STATUS

}

enum class AmplitudePropertyChatMediaKeyboardCategory(val property: String) : AmplitudeProperty {
    RECENT("recent"),
    FAVORITE("favorite"),
    GALLERY("gallery"),
    GIPHY("giphy"),
    RECENT_STICKERS("recent stickers"),
    STICKERPACK("stickerpack"),
    NONE("none");


    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudePropertyNameConst.MP_CATEGORY
}

enum class AmplitudePropertyProfileShare(val property: String) : AmplitudeProperty {
    INSIDE("inside"),
    OUTSIDE("outside"),
    LINK("link");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}


enum class AmplitudePropertySearchType(val property: String) : AmplitudeProperty {
    PEOPLE("people"),
    COMMUNITY("community"),
    HASHTAG("hashtag"),
    FRIENDS("friends"),
    NONE("none");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.SEARCH_TYPE
}

enum class AmplitudePropertyHaveResult(val property: String) : AmplitudeProperty {
    YES("true"),
    NO("false");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HAVE_RESULT
}

enum class AmplitudePropertyWhereCommunitySearch(val property: String) : AmplitudeProperty {
    COMMUNITY("community"),
    NONE("none"),
    FEED("feed");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE_COMMUNITY_SEARCH
}

enum class AmplitudePropertyWhereFriendsSearch(val property: String) : AmplitudeProperty {
    NEW_MESSAGE("new message"),
    GROUP_CHAT_CREATE("group chat create"),
    NONE("none");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE_FRIENDS_SEARCH
}

enum class AmplitudePropertyCommunityType(val property: String) : AmplitudeProperty {
    OPEN("open"),
    CLOSED("closed");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.COMMUNITY_TYPE
}

enum class AmplitudePropertyCanWrite(val property: String) : AmplitudeProperty {
    ALL("all"),
    ADMIN("admin");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.COMMUNITY_CAN_WRITE
}

enum class AmplitudePropertyHavePhoto(val property: String) : AmplitudeProperty {
    YES("true"),
    NO("false");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HAVE_PHOTO
}

enum class AmplitudePropertyHaveMedia(val property: String) : AmplitudeProperty {
    YES("true"),
    NO("false");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HAVE_MEDIA
}

enum class AmplitudePropertyHaveDescription(val property: String) : AmplitudeProperty {
    YES("true"),
    NO("false");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HAVE_DESCRIPTION
}

enum class AmplitudePropertyMenuAction(val property: String) : AmplitudeProperty {
    CHANGE("change"),
    SAVE("save"),
    POST_FOLLOW("post follow"),
    POST_UNFOLLOW("post unfollow"),
    USER_FOLLOW("user follow"),
    USER_UNFOLLOW("user unfollow"),
    HIDE_USER_POSTS("hide user posts"),
    POST_REPORT("post report"),
    PROFILE_REPORT("profile report"),
    DELETE("delete"),
    CANCEL("cancel");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.MENU_ACTION
}

enum class AmplitudePropertyTransportType(val property: String) : AmplitudeProperty {
    CAR("car"),
    MOTO("moto");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.TRANSPORT_TYPE
}

enum class AmplitudePropertyFullness(val property: String) : AmplitudeProperty {
    FULLY("fully"),
    PARTICALLY("partially");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.FULLNESS
}


enum class AmplitudePropertyWhereCommunityOpen(val property: String) : AmplitudeProperty {
    FEED("feed"),
    OWN_PROFILE("own profile"),
    USER_PROFILE("user profile"),
    FRIEND_PROFILE("friend profile"),
    ALL_COMMUNITY("all community"),
    OWN_COMMUNITY("own community"),
    CHAT("chat"),                           // // TODO: Пока не сделано т.к. не реализовано еще поделится сообществом
    NOTIFICATIONS("notifications"),
    DEEPLINK("deeplink"),
    OTHER("other");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudePropertyWhereCommunityFollow(val property: String) : AmplitudeProperty {
    FEED("feed"),
    ALL_COMMUNITY("all community"),
    NOTIFICATIONS("notifications"),
    COMMUNITY("community"),
    UNKNOWN("unknown");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudePropertyChatType(val property: String) : AmplitudeProperty {
    GROUP("group"),
    DEFAULT("default");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.CHAT_TYPE
}

enum class AmplitudePropertyVehicleType(val property: String) : AmplitudeProperty {

    CAR("car"),
    MOTOBIKE("motobike"),
    BICYCLE("bicycle"),
    SCOOTER("scooter"),
    SKATE("skate"),
    ROLLERS("rollers"),
    SNOWMOBILE("snowmobile"),
    JET_SKI("jet ski"),
    AIRPLANE("airplane"),
    BOAT("boat"),
    NONE("none");

    override val _name: String
        get() = AmplitudePropertyNameConst.VEHICLE_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyColor(val property: String) : AmplitudeProperty {
    RED("red"),
    GREEN("green"),
    BLUE("blue"),
    PINK("pink"),
    PURPLE("purple"),
    GOLD("gold"),
    NONE("none");

    override val _name: String
        get() = AmplitudePropertyNameConst.VIP_COLOR

    override val _value: String
        get() = property
}

enum class AmplitudePropertyDuration(val property: String) : AmplitudeProperty {
    WEEK("week"),
    MONTH("month"),
    THREE_MONTH("three month"),
    YEAR("year"),
    NONE("none");

    override val _name: String
        get() = AmplitudePropertyNameConst.DURATION

    override val _value: String
        get() = property
}

enum class AmplitudePropertyHaveVIPBefore(val property: String) : AmplitudeProperty {
    TRUE(TRUE_VALUE),
    FALSE(FALSE_VALUE);

    override val _name: String
        get() = AmplitudePropertyNameConst.HAVE_VIP_BEFORE

    override val _value: String
        get() = property
}

/**
 * Данный эвент отправляет информацию получения VIP аккаунта
 */
enum class AmplitudePropertyWay(
    private val property: String
) : AmplitudeProperty {

    BUY("buy"),
    FRIENDS_INVITATION("friends invitation");

    override val _name: String
        get() = AmplitudePropertyNameConst.WAY

    override val _value: String
        get() = property
}

/**
 * Данный тип обозначает, что с какого экрана добавлен друг
 */
enum class FriendAddAction(
    private val property: String
) : AmplitudeProperty {
    SEARCH("search"),
    INTERESTING_PROFILES("interesting profiles"),
    GLOBAL_FRIEND_SEARCH("global friend search"),
    USER_PROFILE("user profile"),
    MAP_SNIPPET("map snippet"),
    USER_FRIENDS("user friends"),
    USER_FOLLOWERS("user followers"),
    USER_FOLLOWS("user follows"),
    COMMON_FOLLOWS("common follows"),
    SUGGEST_USER_PROFILE("suggest user profile"),
    SUGGEST_MAIN_FEED("suggest main feed"),
    ADD_FRIENDS_PEOPLE("add friends people"),
    ADD_FRIENDS_SEARCH("add friends search"),
    SHAKE("shake"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyCallsSettings(val property: String) : AmplitudeProperty {
    ALL("all"),
    FRIENDS("friends"),
    NOBODY("nobody"),
    NONE("none");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHO_CAN_CALL

    override val _value: String
        get() = property
}

enum class AmplitudePropertyInputType(val property: String) : AmplitudeProperty {
    NUMBER("number"),
    EMAIL("email");

    override val _name: String
        get() = AmplitudePropertyNameConst.INPUT_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyOnboarding(val property: String) : AmplitudeProperty {
    VIEW("view"),
    SKIP("skip");

    override val _name: String
        get() = AmplitudePropertyNameConst.INPUT_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyWhereOpenMap(val property: String) : AmplitudeProperty {
    FEED("feed"),
    MAP("map"),
    MAP_EVENT("map event"),
    PROFILE("profile"),
    USER_PROFILE("user profile"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE_OPEN_MAP

    override val _value: String
        get() = property
}

enum class AmplitudePropertyWhereReaction(val property: String) : AmplitudeProperty {
    COMMENTS("comments"),
    STAT("stat"),
    POST("post"),
    MAP_EVENT("map event"),
    MOMENT("moment");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyReactionType(val property: String) : AmplitudeProperty {
    LIKE("like"),
    HA("ha"),
    WOW("wow"),
    FIRE("fire"),
    OOPS("oops"),
    SAD("sad"),
    OHO("oho"),
    SHIT("shit"),
    MORNING("good morning"),
    NIGHT("good night");

    override val _name: String
        get() = AmplitudePropertyNameConst.REACTION_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePrivacyAboutMeType(val property: String) : AmplitudeProperty {
    FRIENDS("friends"),
    NOBODY("nobody"),
    ALL("all");

    override val _name: String
        get() = AmplitudePropertyNameConst.PRIVACY_ABOUT_ME

    override val _value: String
        get() = property
}

enum class AmplitudePrivacyGarageType(val property: String) : AmplitudeProperty {
    FRIENDS("friends"),
    NOBODY("nobody"),
    ALL("all");

    override val _name: String
        get() = AmplitudePropertyNameConst.PRIVACY_GARAGE

    override val _value: String
        get() = property
}

enum class AmplitudePrivacyGiftsType(val property: String) : AmplitudeProperty {
    FRIENDS("friends"),
    NOBODY("nobody"),
    ALL("all");

    override val _name: String
        get() = AmplitudePropertyNameConst.PRIVACY_GIFTS

    override val _value: String
        get() = property
}

enum class AmplitudePrivacyPersonalRoadType(val property: String) : AmplitudeProperty {
    FRIENDS("friends"),
    NOBODY("nobody"),
    ALL("all");

    override val _name: String
        get() = AmplitudePropertyNameConst.PRIVACY_SELF_ROAD

    override val _value: String
        get() = property
}

enum class AmplitudePrivacyMapType(val property: String) : AmplitudeProperty {
    FRIENDS("friends"),
    NOBODY("nobody"),
    ALL("all");

    override val _name: String
        get() = AmplitudePropertyNameConst.PRIVACY_MAP_VISIBILITY

    override val _value: String
        get() = property
}


enum class AmplitudePropertyRoadType(val property: String) : AmplitudeProperty {
    MAIN_FEED("main feed"),
    FOLLOW_FEED("follow feed"),
    SELF_FEED("self feed");

    override val _name: String
        get() = AmplitudePropertyNameConst.FEED_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyHowWasOpened(val property: String) : AmplitudeProperty {
    AUTOMATICALLY("automatically"),
    MANUALLY("manually");

    override val _name: String
        get() = AmplitudePropertyNameConst.REC_FEED_HOW

    override val _value: String
        get() = property
}

enum class AmplitudePropertyAnimatedAvatarFrom(val property: String) : AmplitudeProperty {
    FROM_REGISTRATION("registration"),
    FROM_SETTINGS("settings"),
    FROM_PROFILE("profile");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyAvatarDownloadFrom(val property: String) : AmplitudeProperty {
    OWN_PROFILE("self profile"),
    OTHER_PROFILE("other profiles");

    override val _name: String
        get() = AmplitudePropertyNameConst.FROM

    override val _value: String
        get() = property
}

enum class AmplitudePropertyAvatarType(val property: String) : AmplitudeProperty {
    ANIMATED_AVATAR("avatar"),
    PHOTO("photo");

    override val _name: String
        get() = AmplitudePropertyNameConst.PHOTO_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyAvatarPhotoType(val property: String) : AmplitudeProperty {
    ANIMATED_AVATAR("avatar"),
    PHOTO("photo");

    override val _name: String
        get() = AmplitudePropertyNameConst.GENERAL_TYPE

    override val _value: String
        get() = property
}

enum class ComplainExtraActions(val property: String) : AmplitudeProperty {
    HIDE("hide"),
    BLOCK("block"),
    NONE("none"),
    NO_THX("no thx");

    override val _name: String
        get() = AmplitudePropertyNameConst.COMPLAIN_EXTRA_ACTION

    override val _value: String
        get() = property
}

enum class AmplitudePropertyCandyCount(val property: String) : AmplitudeProperty {
    CANDY_1("1"),
    CANDY_2("2"),
    CANDY_3("3"),
    CANDY_4("4"),
    CANDY_5("5"),
    CANDY_6("6"),
    CANDY_7("7");

    override val _name: String
        get() = AmplitudePropertyNameConst.NEW_YEAR_CANDY_NUM

    override val _value: String
        get() = property
}

enum class AmplitudePropertyGiftSendBack(val property: String) : AmplitudeProperty {
    TRUE("true"),
    FALSE("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.GIFT_SEND_BACK

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRecognizedTextButton(val property: String) : AmplitudeProperty {
    OPEN("open"),
    CLOSE("close");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.OPEN_TYPE
}

enum class AmplitudePropertyHelpPressedWhere(val property: String) : AmplitudeProperty {
    REGISTRATION("registration"),
    CODE_ENTER("code enter"),
    SETTINGS("settings");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationStep(val property: String) : AmplitudeProperty {
    REG_1("reg1"),
    REG_2("reg2"),
    REG_3("reg3"),
    REG_4("reg4"),
    REG_5("reg5"),
    REG_6("reg6"),
    REG_7("reg7");


    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_CLOSE_STEP

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationBirthdayHide(val property: String) : AmplitudeProperty {
    TRUE("true"),
    FALSE("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_BIRTHDAY_HIDE_AGE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationGender(val property: String) : AmplitudeProperty {
    MALE("Male"),
    FEMALE("Female"),
    NONE("None");

    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_BIRTHDAY_GENDER

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationGenderHide(val property: String) : AmplitudeProperty {
    TRUE("true"),
    FALSE("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_BIRTHDAY_HIDE_GENDER

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationLocationAutocomplete(val property: String) : AmplitudeProperty {
    TRUE("true"),
    FALSE("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_LOCATION_AUTOCOMPLETE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationAvatarPhotoType(val property: String) : AmplitudeProperty {
    PHOTO("photo"),
    AVATAR("avatar"),
    EMPTY("empty");

    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_PHOTO_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRegistrationAvatarHaveReferral(val property: String) : AmplitudeProperty {
    TRUE("true"),
    FALSE("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.REGISTRATION_PHOTO_HAVE_REFERRAL

    override val _value: String
        get() = property
}

enum class AmplitudePropertyDeleteProfileReason(val reasonId: Int, val property: String) : AmplitudeProperty {
    LEAVE_FOR_A_WHILE(1, "leave for a while"),
    DIFFICULT_TO_UNDERSTAND(2, "difficult to understand"),
    NO_SECURITY(3, "no security"),
    UGLY_APP(4, "ugly app"),
    DONT_LIKE_MODERATION_AND_POLITICS(5, "don't like moderation and politics"),
    UNPLEASANT_COMMUNICATION(6, "unpleasant communication"),
    UNINTERESTING_CONTENT(7, "uninteresting content"),
    WHASTING_TIME(8, "whasting time"),
    ANOTHER_REASON(9, "another reason");

    override val _name: String
        get() = AmplitudePropertyNameConst.DELETE_PROFILE_REASON

    override val _value: String
        get() = property
}

enum class AmplitudePropertyChatCallSwitcherPosition(val property: String) : AmplitudeProperty {
    ON("on"),
    OFF("off");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.POSITION
}

enum class AmplitudePropertyBottomSheetCloseMethod(val property: String) : AmplitudeProperty {
    CLOSE("close"),
    CLOSE_SWIPE("close swipe"),
    TAP_ON_SPACE("tap on space"),
    BACK("back");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW
}

enum class AmplitudePropertyWhereSent(val property: String) : AmplitudeProperty {
    CHAT("chat"),
    SELF_FEED("self feed"),
    COMMUNITY("community"),
    OUTSIDE("outside"),
    MOMENT("moment");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE_SENT
}


enum class AmplitudePropertyWhereMapPrivacy(val property: String) : AmplitudeProperty {
    SETTINGS("settings"),
    PROFILE("profile"),
    MAP("map");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudePropertySettingVisibility(val property: String) : AmplitudeProperty {
    NOBODY("nobody"),
    FRIENDS("friends"),
    ALL("all");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.VISIBILITY_SETTINGS
}

enum class AmplitudePropertyMapPrivacyListType(val property: String) : AmplitudeProperty {
    ALWAYS("always"),
    NEVER("never");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.PRIVACY_MAP_LIST_TYPE
}

enum class AmplitudePropertyCallType(val property: String) : AmplitudeProperty {
    INCOMING("incoming"),
    OUTGOING("outgoing");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.CALL_TYPE
}

enum class AmplitudePropertyCallCanceller(val property: String) : AmplitudeProperty {
    CALLER("caller"),
    CALLED("called");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHO_CAN_CALL
}

enum class AmplitudePropertyActionTypeChatRequest(val property: String) : AmplitudeProperty {
    ALLOW("allow"),
    MESSAGE("message"),
    BAN("ban");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

enum class AmplitudePropertyChatCreatedFromWhere(val property: String) : AmplitudeProperty {
    PROFILE("profile"),
    MAP("map"),
    COMMUNICATION("communication"); // needs map snippets feature

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class AmplitudePropertyPublicType(val property: String) : AmplitudeProperty {
    POST("post"),
    VIDEO_POST("video post"),
    MAP_EVENT("map event"),
    MOMENT("moment");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.PUBLIC_TYPE
}

const val UNKNOWN_VALUE = "unknown"
const val NONE_VALUE = "none"
const val TRUE_VALUE = "true"
const val FALSE_VALUE = "false"
const val NO_USER_ID = -1L

object AmplitudePropertyNameConst {
    const val ID = "id"                                             // Универсальный ID
    const val POST_ID = "post id"                                   // Отмечаем id поста
    const val AUTHOR_ID = "author id"                               // Отмечаем id поста
    const val MOMENT_ID = "moment id"                               // Отмечаем id момента
    const val COMMENTOR_ID = "commentor id"                          // Отмечаем id комментатора
    const val POST_TYPE = "post type"                               // Отмечаем, это пост, или репост чужого поста
    const val CREATE_POST_WHICH_BUTTON =
        "which button"                               // на что кликнули на этаже создания поста
    const val FROM =
        "from"                                         // Отмечаем, чей аватар был скачан (свой, в своём профиле, или чужой)
    const val WHERE = "where"
    const val SECTION = "section"
    const val WHOSE = "whose"
    const val WHENCE = "whence"
    const val CONTENT_TYPE =
        "content type"                         // Содержание поста. Если в посте только один тип контента, то проставляем
                                                // single. Если в посте больше одного типа контента, то multiple.
                                                // Если это репост, смотрим на содержание текущего поста, а не репоста
    const val SAVE_TYPE = "save type"                               // Отмечаем, что сохранил пользователь(video, photo,none)
    const val HAVE_TEXT = "have text"                               // true or false
    const val HAVE_ADD_TEXT = "have add text"
    const val HAVE_PIC = "have pic"                                 // true or false
    const val HAVE_VIDEO = "have video"                             // true or false
    const val HAVE_GIF = "have gif"                                 // true or false
    const val VIDEO_DURATION = "video duration"
    const val VIDEO_COUNT = "video count"
    const val IMG_COUNT = "img count"
    const val COMMENT_COUNT = "comment count"
    const val HAVE_AUDIO = "have audio"                             // true or false
    const val HAVE_PHOTO = "have photo"                             // yes or no
    const val HAVE_MEDIA = "have media"                             // yes or no
    const val HAVE_MUSIC = "have music"                             // имеется ли музыка
    const val HAVE_DESCRIPTION = "have description"
    const val HAVE_POSTS = "have posts"
    const val COMMENTS_SETTINGS = "comments settings"               // Какие настройки комментариев у этого поста
    const val PUSH_PERMISSION =
        "push permission"                   // Есть ли системное разрешение на отправку уведомлений. По умолчанию unknown
    const val PUSH_ENABLED =
        "push enabled"                         // Определяет, включены ли в данный момент пуши. Обновляемый параметр
    const val MAIN_PUSH_SETTINGS = "main push settings"
    const val ON = "on"
    const val OFF = "off"
    const val GEO_ENABLED =
        "geo enabled"                           // Определяет, есть ли в данный момент разрешение на гео. Обновляемый параметр
    const val NUM_OF_POSTS = "num of posts"                         // Счётчик, сколько пользователь опубликовал постов
    const val NUM_OF_COMMENTS =
        "num of comments"                   // Счётчик, сколько пользователь отправил комментариев
    const val NUM_OF_FRIENDS = "num of friends"                     // Счётчик кол-ва друзей
    const val NUM_OF_FOLLOWERS = "num of followers"                 // Счётчик кол-ва подписчиков
    const val NUM_OF_FOLLOWS = "num of follow"                      // Счётчик кол-ва подписок
    const val USER_NAME =
        "user name"                               // Определяет, какое имя пользователя установлено -
                                                // по умолчанию (типа id234252), или пользователь его поменял на своё.
                                                // Обновляемый параметр

    const val HAVE_BACKGROUND = "have background"  // Добавлена ли к посту подложка
    const val BACKGROUND_ID = "background id"      // Отмечаем id подложки. Если подложки нет, присылать 0

    const val DATE_OF_BIRTH =
        "date of birth"                       // Проставляется в момент добавления ДР. По умолчанию unknown
    const val FIRST_OPEN_DAY =
        "1-st app open day "                 // Номер дня в году, когда пользователь впервые открыл приложение
                                            //  D1, D2, D3
    const val FIRST_OPEN_WEEK =
        "1-st app open week"                // Номер недели в году, когда пользователь впервые открыл приложение
                                            //  W1, W2, W3
    const val FIRST_OPEN_MONTH =
        "1-st app open month"              // Номер месяца в году, когда пользователь впервые открыл приложение
                                            //  M1, M2, M3
    const val OPEN_TYPE = "type"
    const val ACTION_TYPE = "action type"                           // Тип активности при обновлении прил.

    const val COUNTRY_TYPE = "country type"
    const val CITY_TYPE = "city type"
    const val VISIBILITY_SETTINGS = "visibility settings"
    const val SEARCH_TYPE =
        "type"                                  // Отмечаем, по какой вкладке пользователь осуществил поиск
    const val HAVE_RESULT = "have result"                           // Отмечаем, есть ли результат yes / no
    const val WHERE_COMMUNITY_SEARCH =
        "where community search"     // Отмечаем, откуда пользователь осуществил поиск по сообществам
    const val WHERE_FRIENDS_SEARCH = "where friends search"
    const val EMOJI_TYPE = "emoji type"                             // Отмечаем, на какой именно emoji тапали

    const val COMMUNITY_TYPE =
        "community type"                     // Отмечаем, какой тип сообщества установлен при создании
    const val COMMUNITY_CAN_WRITE = "can write"

    const val CHAT_TYPE = "chat type"                               // Отмечаем, какой тип чата открыт
    const val ANONYMOUS_MESSAGE = "anonim message"                  // Отмечаем, было ли сообщение в анонимный чат
    const val GROUP_CHAT = "group chat"
    const val MESSAGE_DURATION_NONE = 0L

    const val VEHICLE_TYPE = "type"                                 // Тип Т.С.
    const val GIFT_TYPE = "gift type"                               // Тип подарка
    const val GIFT_FROM = "from"                                    // От кого подарок
    const val GIFT_TO = "to"                                        // Кому подарок
    const val GIFT_SEND_BACK =
        "send back"                          // Отмечаем, был ли подарок отправлен в ответ на какой-то подарок
                                            // (соответствующая кнопка в меню).
    const val VIP_COLOR = "color"                                   // Цвет VIP
    const val DURATION = "duration"                                 // Отмечаем на какой срок был приобретен VIP статус
    const val HAVE_VIP_BEFORE =
        "have vip before"                   // Отмечаем, приобретался ли VIP статус ранее (любой и на любой срок)
    const val EXPIRATION_DATE =
        "expiration date"                   // Отмечаем, когда у пользователя закончится VIP статус
    const val COMMENT_TYPE = "comment type"
    const val WHO_CAN_CALL = "who"                                  // Отмечаем, кто может звонить
    const val POST_CONTENT_TYPE = "post content type"
    const val INPUT_TIME =
        "input time"                             // Отмечаем через сколько после отправки первого кода -код был введен
                                                // (любой из отправленных, не обязательно первый)
    const val INCORRECT_COUNT = "incorrect count"                   // Отмечаем были ли неправильные вводы кода
    const val REQUEST_COUNT =
        "request count"                       // Отмечаем сколько раз повторно запрашивался код подтверждения

    const val INPUT_TYPE =
        "type"                                   // Отмечаем каким путем пользователь зарегистрировался
    const val ONBOARDING = "where"                                  // Отмечаем прошел ли пользователь онбординг

    const val HAS_CHANGES =
        "changes"                               // Отмечаем внес ли пользователь какие-то изменения в фильтр
    const val WHERE_OPEN_MAP = "where"                              // Отмечаем откуда пользователь попал в карту

    const val LIKE_ACTION = "action type"                           // Отмечаем какое событие совершил пользователь
    const val MENU_ACTION = "action type"                           // Отмечаем какое событие из меню поста совершил пользователь
    const val TRANSPORT_TYPE = "transport type"                     // Тип ТС
    const val FULLNESS = "fullness"                                 // полнота заполнения данных
    const val COUNTRY = "country"
    const val CHAR_COUNT = "char count"
    const val REACTION_TYPE = "reaction type"                       // тип проставдленной реакции
    const val FEED_TYPE = "feed type"                               // тип дороги
    const val PRIVACY_ABOUT_ME = "privacy about me"                               // тип дороги
    const val PRIVACY_GARAGE = "privacy garage"                               // тип дороги
    const val PRIVACY_GIFTS = "privacy gifts"                               // тип дороги
    const val PRIVACY_SELF_ROAD = "privacy self road"                               // тип дороги
    const val PRIVACY_MAP_VISIBILITY = "privacy map visibility"                               // тип дороги
    const val NEW_YEAR_CANDY_NUM = "num"                            // Количество новогодних леденцов

    const val DEVICE_ID = "device id"
    const val USER_ID = "user id"
    const val POST_SHOWN_COUNT =
        20                                 // отсылаем событие feed scroll после просмотра 20-ти постов каждый раз

    const val COMPLAIN_EXTRA_ACTION =
        "additional measures"         // дополнительное действие при жалобе на пользователя
    const val GENERAL_TYPE = "type"
    const val REPORT_FROM = "report from"
    const val REPORT_TO = "report to"
    const val TO = "to"
    const val EMPTY = "empty"
    const val REPOST_ID = "repost id"
    const val PHOTO_TYPE = "photo type"

    const val HELP_PRESS = "help press"
    const val REGISTRATION_CLOSE_STEP = "step"
    const val REGISTRATION_BIRTHDAY_AGE = "age"
    const val REGISTRATION_BIRTHDAY_HIDE_AGE = "hide"
    const val REGISTRATION_BIRTHDAY_GENDER = "gender"
    const val REGISTRATION_BIRTHDAY_HIDE_GENDER = "hide"
    const val REGISTRATION_LOCATION_AUTOCOMPLETE = "autocomplete"
    const val REGISTRATION_PHOTO_TYPE = "photo type"
    const val REGISTRATION_PHOTO_AVATAR_TIME = "avatar time"
    const val REGISTRATION_PHOTO_HAVE_REFERRAL = "have referal"
    const val AVATAR_CREATION_TIME = "avatar time"
    const val COMMUNITY_ID = "community id"

    const val MESSAGE_ID = "message_id"
    const val MESSAGE_ID_SPACING = "message id"
    const val DELETE_PROFILE_REASON = "reason"
    const val STATUS = "status"
    const val MP_CATEGORY = "mp category"
    const val POSITION = "position"

    const val HOW = "how"
    const val GROUP_COUNT = "group count"
    const val CHAT_COUNT = "chat count"
    const val WHERE_SENT = "where sent"
    const val SEARCH = "search"

    const val COUNT = "count"
    const val ADD_COUNT = "add count"
    const val DELETE_COUNT = "delete count"
    const val PRIVACY_MAP_LIST_TYPE = "list type"

    const val CALL_TYPE = "call type"
    const val CALL = "call"
    const val INFLUENCER = "influencer"
    const val COUNT_MUTUAL_AUDIENCE = "count mutual audience"
    const val COUNT_USER_SHAKE = "count user shake"

    const val WAY = "way"

    //TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    const val FB_TOKEN = "fb token"
    const val FB_MSG = "fb msg"

    const val REC_FEED = "rec feed"
    const val REC_FEED_CHANGE_TYPE = "type"
    const val REC_FEED_HOW= "how"

    const val PUBLIC_TYPE = "public type"

    const val REG_TYPE = "reg type"
    const val COUNTRY_NUMBER = "country number"
    const val NUMBER = "number"
    const val EMAIL = "email"
    const val AGE = "age"
    const val HIDE_AGE = "hide age"
    const val HIDE_GENDER = "hide gender"
    const val UNIQUE_NAME_CHANGE = "unique name change"
    const val INVITER_ID = "inviter id"
    const val SYNC_COUNT = "sync count"

    //Edit post
    const val TEXT_CHANGE = "text change"                 // редактировал ли пользователь текст
    const val PIC_CHANGE = "pic change"                   // редактировал ли пользователь изображение
    const val VIDEO_CHANGE = "video change"               // редактировал ли пользователь видео
    const val MUSIC_CHANGE = "music change"               // редактировал ли пользователь музыку
    const val BACKGROUND_CHANGE = "background change"     // редактировал ли пользователь подложку

}

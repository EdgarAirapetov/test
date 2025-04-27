package com.numplates.nomera3.modules.tracker

enum class ScreenNamesEnum(val value: String) {
    //registration
    REGISTRATION("Registration"),
    CONFIRMATION_CODE("Confirmation code"),
    //road tab
    ROAD_PERSONAL("Road - Personal"),
    ROAD_MAIN("Road - Main"),
    ROAD_SUBSCRIPTIONS("Road - Subscription"),
    SEARCH("Search"),
    ROAD_MAP("Road - Map"),
    ROAD_FILTER("Road - Filter"),
    POST("Post"),
    //map tab
    MAP_MAIN("Map - Main"),
    MAP_FILTER("Map - Filter"),
    //group tab
    MY_GROUPS("Groups - My Groups"),
    ALL_GROUPS("Groups - All Groups"),
    CREATE_GROUP("Groups - Creating a group"),
    EDIT_GROUP("Groups - Editing a group"),
    GROUP_POST("Groups - Post"),
    //chat tab
    CHAT_LIST("Chat - List"),
    NOTIFICATIONS("Chat - Notifications"),
    CHAT_P2P("Chat - P2P"),
    CHAT_GROUP("Chat - Groups"),
    //profile tab
    PROFILE_MY("Profile Me"),
    PROFILE_NOT_MY("Profile - Stranger"),
    FRIENDS("Profile - My Friends"),
    GALLERY("Profile - Gallery"),
    GARAGE("Profile - Garage"),
    MAP_PROFILE("Profile - Map"),
    //purchase
    VIP_SILVER("VIP Silver"),
    VIP_GOLD("VIP Gold"),
    ENHANCE_TO_VIP("Enhance to VIP"),
    SEND_GIFT("Send Gift")
}
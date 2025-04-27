package com.numplates.nomera3.modules.tracker

enum class MyTrackerEvents(val state: String) {
    WRITE_COMMENT("WRITE_COMMENT"),
    BIP("BIP"),
    REPOST_CHAT("REPOST_CHAT"),
    REPOST_ROAD("REPOST_ROAD"),
    REPOST_GROUP ("REPOST_GROUP"),
    GIFT("GIFT"),
    SUBSCRIBE("SUBSCRIBE"),
    TO_FRIEND_REQUEST("TO_FRIEND_REQUEST"),
    TO_FRIEND_ACCEPT("TO_FRIEND_ACCEPT"),
    CREATE_TEXT_POST("CREATE_TEXT_POST"), //создать текстовый пост
    CREATE_IMAGE_POST("CREATE_IMAGE_POST"), // создать пост с картинкой
    CREATE_VIDEO_POST("CREATE_VIDEO_POST"), // создать пост с видео
    CREATE_PERSONAL_POST("CREATE_PERSONAL_POST"), // создать пост в личную дорогу
    CREATE_POST("CREATE_POST") //любой пост создан кроме группы
}
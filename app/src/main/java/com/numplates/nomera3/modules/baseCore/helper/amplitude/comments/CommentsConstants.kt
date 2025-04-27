package com.numplates.nomera3.modules.baseCore.helper.amplitude.comments

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeCommentEventName(
    private val event: String
) : AmplitudeName {
    COMMENT_MENU_OPEN("comment menu open"),
    COMMENT_MENU_ACTION("comment menu action"),
    POST_OPEN("post open"),
    COMMENT_SENT("comment sent");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyCommentMenuAction(val property: String) : AmplitudeProperty {
    REPLY("reply"),
    REACTION("reaction"),
    COPY("copy"),
    REPORT("report"),
    DELETE("comment del"),
    BLOCK("user block"),
    CANCEL("cancel");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHENCE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyCommentWhere(val property: String) : AmplitudeProperty {
    POST("post"),
    MOMENT("moment"),
    VIDEO_POST("video post");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHENCE

    override val _value: String
        get() = property
}

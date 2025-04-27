package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeMapUserSnippetEventName(
    private val event: String
) : AmplitudeName {
    MAP_USER_SNIPPET_OPEN("map snippet open"),
    MAP_USER_SNIPPET_CLOSE("map snippet close");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyMapSnippetOpenType(val property: String) : AmplitudeProperty {
    TAP("tap"),
    SWIPE("swipe"),
    NEARBY("nearby"),
    MY_CREATOR("my creator"),
    MY_MEMBER("my member"),
    ARCHIVE_CREATOR("archive creator"),
    ARCHIVE_MEMBER("archive member");

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapSnippetCloseType(val property: String): AmplitudeProperty {
    CLOSE("close"),
    SWIPE("swipe"),
    TAP("tap"),
    BACK("back");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW
}

enum class AmplitudePropertyMapSnippetType(val property: String) : AmplitudeProperty {
    USER("user"),
    EVENT("event");

    override val _name: String
        get() = AmplitudePropertyMapSnippetConst.SNIPPET_TYPE

    override val _value: String
        get() = property
}

object AmplitudePropertyMapSnippetConst {
    const val SNIPPET_TYPE = "snippet type"
}

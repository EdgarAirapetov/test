package com.numplates.nomera3.modules.baseCore.helper.amplitude.post

import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudePropertyWhosePost(val property: String) : AmplitudeProperty {
    MY("my"),
    USER("user");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHOSE
}

enum class AmplitudePropertyPostWhere(val property: String) : AmplitudeProperty {
    POST("post"),
    MOMENT("moment");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyPostWhence(val property: String) : AmplitudeProperty {
    MAIN_FEED("main feed"),
    SELF_FEED("self feed"),
    FOLLOW_FEED("follow feed"),
    CHAT("chat"),
    PROFILE("profile"),
    USER_PROFILE("user profile"),
    COMMUNITY("community"),
    HASHTAG("hashtag"),
    NOTIFICATION("notification"),
    DEEPLINK("deeplink"),
    OTHER("other");

    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudePropertyNameConst.WHENCE
}

enum class AmplitudePropertySaveType(val property: String) : AmplitudeProperty {
    VIDEO("video"),
    PHOTO("photo"),
    NONE("none");

    override val _name: String
        get() = AmplitudePropertyNameConst.SAVE_TYPE

    override val _value: String
        get() = property
}

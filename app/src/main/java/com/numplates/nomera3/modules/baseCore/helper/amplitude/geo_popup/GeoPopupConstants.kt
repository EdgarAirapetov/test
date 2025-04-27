package com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeGeoPopupEventName(
    private val event: String
) : AmplitudeName {
    GEO_POPUP_ACTION("map pop up action");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyGeoPopupActionType(val property: String) : AmplitudeProperty {
    ENABLE_GEO("enable geo"),
    MISS("miss"),
    CLOSE("close"),
    TAP("tap"),
    BACK("back"),
    SWIPE("swipe");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyGeoPopupConst.ACTION_TYPE
}

enum class AmplitudePropertyGeoPopupWhere(val property: String) : AmplitudeProperty {
    MAP("map"),
    SHOW_ME_BUTTON("show me button"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyGeoPopupConst.WHERE

    override val _value: String
        get() = property
}

object AmplitudePropertyGeoPopupConst {
    const val ACTION_TYPE = "action type"
    const val WHERE = "where"
}

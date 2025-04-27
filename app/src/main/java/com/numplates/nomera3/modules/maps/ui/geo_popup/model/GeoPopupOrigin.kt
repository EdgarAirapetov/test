package com.numplates.nomera3.modules.maps.ui.geo_popup.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudePropertyGeoPopupWhere

enum class GeoPopupOrigin {
    MY_LOCATION, MAP, OTHER
}

fun GeoPopupOrigin.toAmplitudePropertyGeoPopupWhere(): AmplitudePropertyGeoPopupWhere {
    return when (this) {
        GeoPopupOrigin.MY_LOCATION -> AmplitudePropertyGeoPopupWhere.SHOW_ME_BUTTON
        GeoPopupOrigin.MAP -> AmplitudePropertyGeoPopupWhere.MAP
        GeoPopupOrigin.OTHER -> AmplitudePropertyGeoPopupWhere.OTHER
    }
}

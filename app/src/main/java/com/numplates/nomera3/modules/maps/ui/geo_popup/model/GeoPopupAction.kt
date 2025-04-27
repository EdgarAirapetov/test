package com.numplates.nomera3.modules.maps.ui.geo_popup.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup.AmplitudePropertyGeoPopupActionType
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil

sealed class GeoPopupAction {
    object EnableLocation : GeoPopupAction()
    object Skip : GeoPopupAction()
    data class Close(val closeMethod: BottomSheetCloseUtil.BottomSheetCloseMethod) : GeoPopupAction()
}

fun GeoPopupAction.toAmplitudePropertyGeoPopupActionType(): AmplitudePropertyGeoPopupActionType {
    return when (this) {
        is GeoPopupAction.Close -> when (this.closeMethod) {
            BottomSheetCloseUtil.BottomSheetCloseMethod.SWIPE -> AmplitudePropertyGeoPopupActionType.SWIPE
            BottomSheetCloseUtil.BottomSheetCloseMethod.TAP_OUTSIDE -> AmplitudePropertyGeoPopupActionType.TAP
            BottomSheetCloseUtil.BottomSheetCloseMethod.CLOSE_BUTTON -> AmplitudePropertyGeoPopupActionType.CLOSE
            BottomSheetCloseUtil.BottomSheetCloseMethod.BACK_BUTTON -> AmplitudePropertyGeoPopupActionType.BACK
        }
        GeoPopupAction.EnableLocation -> AmplitudePropertyGeoPopupActionType.ENABLE_GEO
        GeoPopupAction.Skip -> AmplitudePropertyGeoPopupActionType.MISS
    }
}

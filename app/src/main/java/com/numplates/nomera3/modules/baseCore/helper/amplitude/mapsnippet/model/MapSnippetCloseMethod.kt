package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetCloseType

enum class MapSnippetCloseMethod {
    SWIPE, TAP, CLOSE_BUTTON, BACK_BUTTON
}

fun MapSnippetCloseMethod.toAmplitudePropertyHow(): AmplitudePropertyMapSnippetCloseType {
    return when (this) {
        MapSnippetCloseMethod.SWIPE -> AmplitudePropertyMapSnippetCloseType.SWIPE
        MapSnippetCloseMethod.TAP -> AmplitudePropertyMapSnippetCloseType.TAP
        MapSnippetCloseMethod.CLOSE_BUTTON -> AmplitudePropertyMapSnippetCloseType.CLOSE
        MapSnippetCloseMethod.BACK_BUTTON -> AmplitudePropertyMapSnippetCloseType.BACK
    }
}

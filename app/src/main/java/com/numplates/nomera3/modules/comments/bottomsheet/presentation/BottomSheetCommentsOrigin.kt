package com.numplates.nomera3.modules.comments.bottomsheet.presentation

import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudePropertyCommentWhere

enum class BottomSheetCommentsOrigin {
    VIDEO_POST,
    MOMENT
}

fun BottomSheetCommentsOrigin.toAmplitudePropertyWhere(): AmplitudePropertyCommentWhere {
    return when (this) {
        BottomSheetCommentsOrigin.VIDEO_POST -> AmplitudePropertyCommentWhere.VIDEO_POST
        BottomSheetCommentsOrigin.MOMENT -> AmplitudePropertyCommentWhere.MOMENT
    }
}

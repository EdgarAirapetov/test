package com.numplates.nomera3.modules.comments.bottomsheet.presentation

import android.os.Parcelable
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import kotlinx.parcelize.Parcelize

@Parcelize
data class BottomSheetCommentsInfoUiModel(
    val contentId: Long,
    val contentUserId: Long,
    val isUserBlackListMe: Boolean,
    val isCommentsEnabled: Boolean,
    val commentsOrigin: BottomSheetCommentsOrigin,
    val baseScreenOrigin: DestinationOriginEnum?
) : Parcelable

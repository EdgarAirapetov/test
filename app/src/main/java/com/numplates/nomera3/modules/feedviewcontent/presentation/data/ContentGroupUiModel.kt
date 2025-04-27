package com.numplates.nomera3.modules.feedviewcontent.presentation.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentGroupUiModel(
    val id: Long,
    val postId: Long,
    val isEventPost: Boolean,
    val isPostSubscribed: Boolean,
    val contentList: List<ContentItemUiModel>
): Parcelable


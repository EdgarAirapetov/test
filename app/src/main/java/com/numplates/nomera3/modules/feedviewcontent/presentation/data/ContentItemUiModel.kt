package com.numplates.nomera3.modules.feedviewcontent.presentation.data

import android.os.Parcelable
import com.numplates.nomera3.modules.feed.ui.entity.UserPost
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentItemUiModel(
    val id: Long,
    val contentUrl: String? = null,
    val user: UserPost? = null,
    val contentType: String? = "Image",
    val postReactions: List<ReactionEntity>? = emptyList(),
    val enableZoomToFit: Boolean = false,
    val aspect: Double
) : Parcelable



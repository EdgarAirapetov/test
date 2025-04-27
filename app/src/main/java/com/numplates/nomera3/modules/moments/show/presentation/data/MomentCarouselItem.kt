package com.numplates.nomera3.modules.moments.show.presentation.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

private const val SHIMMER_MOMENT_CAROUSEL_ITEM_ID = -676982

sealed class MomentCarouselItem(
    val id: Int,
    val displayType: MomentCarouselItemType
): Parcelable {

    @Parcelize
    data class MomentCreateItem(
        val group: MomentGroupUiModel
    ) : MomentCarouselItem(
        id = group.id.toInt(),
        displayType = MomentCarouselItemType.CreateMoment
    )

    @Parcelize
    data class MomentGroupItem(
        val group: MomentGroupUiModel,
        private val type: MomentCarouselItemType
    ) : MomentCarouselItem(
        id = group.id.toInt(),
        displayType = type
    )

    @Parcelize
    data class MiscItem(
        private val itemId: Int,
        private val itemType: MomentCarouselItemType
    ) : MomentCarouselItem(
        id = itemId,
        displayType = itemType
    )

    @Parcelize
    object UserShimmerItem : MomentCarouselItem(
        id = SHIMMER_MOMENT_CAROUSEL_ITEM_ID,
        displayType = MomentCarouselItemType.UserShimmer
    )

    @Parcelize
    object PlaceShimmerItem : MomentCarouselItem(
        id = SHIMMER_MOMENT_CAROUSEL_ITEM_ID,
        displayType = MomentCarouselItemType.PlaceShimmer
    )

    @Parcelize
    object BlankShimmer : MomentCarouselItem(
        id = SHIMMER_MOMENT_CAROUSEL_ITEM_ID,
        displayType = MomentCarouselItemType.BlankShimmer
    )

    companion object {
        fun MomentCarouselItem.isCreateItem(): Boolean {
            return this is MomentCreateItem
        }
    }
}

enum class MomentCarouselItemType(val order: Int) {
    Place(0),
    User(0),
    CreateMoment(99),
    UserShimmer(10),
    PlaceShimmer(10),
    BlankShimmer(10)
}

package com.numplates.nomera3.modules.moments.show.data

import com.meera.db.DataStore
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.data.mapper.MomentsUiMapper
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem.Companion.isCreateItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.util.addToStartOfList
import javax.inject.Inject

private const val CREATE_MOMENT_CAROUSEL_ITEM_ID = -197561L

class CarouselMomentsHelper @Inject constructor(
    private val momentMapper: MomentsUiMapper,
    private val dataStore: DataStore
) {

    suspend fun getMomentsForCarousel(
        momentInfoModel: MomentInfoModel,
        pagingTicket: String?,
        addCreateMomentItem: Boolean,
        isPagingUsed: Boolean
    ): MomentInfoCarouselUiModel? {
        var moments = momentMapper.mapToCarouselUiModel(
            model = momentInfoModel,
            pagingTicket = pagingTicket,
            addCreateMomentItem = addCreateMomentItem,
            isPagingUsed = isPagingUsed
        )
        var carouselList = moments?.momentsCarouselList ?: return moments
        if (carouselList.firstOrNull { it.isCreateItem() } == null && moments.useCreateMomentItem) {
            carouselList = carouselList.addToStartOfList(generateCreateMomentItem())
            moments = moments.copy(momentsCarouselList = carouselList)
        }
        return moments
    }

    private suspend fun generateCreateMomentItem(): MomentCarouselItem {
        val userProfile = dataStore.userProfileDao().getUserProfile()
        return MomentCarouselItem.MomentCreateItem(
            group = MomentGroupUiModel(
                id = CREATE_MOMENT_CAROUSEL_ITEM_ID,
                userId = userProfile?.userId ?: 0,
                moments = listOf(),
                placeholder = userProfile?.avatarBig,
                isMine = true
            )
        )
    }
}

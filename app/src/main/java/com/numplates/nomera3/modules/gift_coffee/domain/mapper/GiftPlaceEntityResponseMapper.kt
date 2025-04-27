package com.numplates.nomera3.modules.gift_coffee.domain.mapper

import com.numplates.nomera3.modules.gift_coffee.data.entity.GiftPlaceEntityResponse
import com.numplates.nomera3.modules.gift_coffee.ui.entity.GiftPlaceEntity

class GiftPlaceEntityResponseMapper {

    fun mapToGiftPlaceEntity(old: List<GiftPlaceEntityResponse?>?): List<GiftPlaceEntity>? {
        return old?.map {
            GiftPlaceEntity(
                    id = it?.id,
                    title = it?.title,
                    address = it?.address,
                    image = it?.image
            )
        }
    }

}
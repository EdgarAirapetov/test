package com.numplates.nomera3.modules.holidays.data.mapper

import com.numplates.nomera3.modules.purchase.data.model.GiftItemDto
import com.numplates.nomera3.modules.holidays.data.entity.ChatRoomEntity
import com.numplates.nomera3.modules.holidays.data.entity.HatsEntity
import com.numplates.nomera3.modules.holidays.data.entity.HolidayInfoEntity
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.holidays.data.entity.MainButtonLinkEntity
import com.numplates.nomera3.modules.holidays.data.entity.OnBoardingEntity
import com.meera.db.models.userprofile.ProductEntity
import com.numplates.nomera3.modules.holidays.domain.entity.ChatRoomModel
import com.numplates.nomera3.modules.holidays.domain.entity.HatsModel
import com.numplates.nomera3.modules.holidays.domain.entity.HolidayInfoModel
import com.numplates.nomera3.modules.holidays.domain.entity.HolidayVisitsModel
import com.numplates.nomera3.modules.holidays.domain.entity.MainButtonLinkModel
import com.numplates.nomera3.modules.holidays.domain.entity.OnBoardingModel
import com.numplates.nomera3.modules.holidays.domain.entity.ProductModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.ProductHolidayModel
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY

fun HolidayInfoEntity.toModel(): HolidayInfoModel {
    val chatModel = this.chatRoomEntity.toModel()
    val hatsModel = this.hatsLink.toModel()
    val mainButtonModel = this.mainButtonLinkEntity.toModel()
    val onboardingModel = this.onBoardingEntity.toModel()
    val productModel = this.productEntity?.toModel()
    return HolidayInfoModel(
        this.id,
        this.holidayCode,
        this.title,
        mainButtonModel,
        this.startTime,
        this.finishTime,
        onboardingModel,
        hatsModel,
        chatModel,
        productModel
    )
}

fun ChatRoomEntity.toModel(): ChatRoomModel {
    return ChatRoomModel(this.type,this.background_dialog,this.background_anon,this.background_group)
}

fun HatsEntity.toModel(): HatsModel {
    return HatsModel(this.general,this.premium,this.vip)
}

fun MainButtonLinkEntity.toModel(): MainButtonLinkModel {
    return MainButtonLinkModel(this.default,this.active)
}

fun OnBoardingEntity.toModel(): OnBoardingModel {
    return OnBoardingModel(this.title,this.description,this.icon,this.buttonText)
}

fun ProductEntity.toModel(): ProductModel {
    return ProductModel(
        this.id,
        this.appleProductId,
        this.customTitle,
        this.description,
        this.imageItem.toModel(),
        this.itunesProductId,
        this.playMarketProductId,
        this.type
    )
}

fun ProductEntity.ImageItemEntity.toModel(): ProductModel.ImageItemModel {
    return ProductModel.ImageItemModel(
        this.link,
        this.linkSmall
    )
}


fun ProductEntity.toGiftItem(): GiftItemDto {
    return GiftItemDto(
        giftId = this.id ?: -1L,
        appleProductId = this.appleProductId ?: "",
        itunesProductId = this.itunesProductId ?: "",
        marketProductId = this.playMarketProductId ?: "",
        smallImage = this.imageItem.linkSmall ?: "",
        image = this.imageItem.link ?: "",
        customTitle = this.customTitle ?: "",
        type = this.type?.toInt() ?: TYPE_GIFT_HOLIDAY,
        customDesc = this.description,
        price = this.price,
    )
}

fun ProductHolidayModel.toGiftItem(): GiftItemDto {
    return GiftItemDto(
        giftId = this.id ?: -1L,
        appleProductId = this.appleProductId ?: "",
        itunesProductId = this.itunesProductId ?: "",
        marketProductId = this.playMarketProductId ?: "",
        smallImage = this.imageItem.linkSmall ?: "",
        image = this.imageItem.link ?: "",
        customTitle = this.customTitle ?: "",
        type = this.type?.toInt() ?: TYPE_GIFT_HOLIDAY,
        customDesc = this.description,
        price = this.price,
    )
}

fun HolidayVisitsEntity.toModel(): HolidayVisitsModel {
    return HolidayVisitsModel(
            goalDays = this.goalDays,
            status = this.status,
            visitDays = this.visitDays
    )
}

package com.numplates.nomera3.modules.holidays.domain.mapper

import com.numplates.nomera3.modules.holidays.domain.entity.ChatRoomModel
import com.numplates.nomera3.modules.holidays.domain.entity.HatsModel
import com.numplates.nomera3.modules.holidays.domain.entity.HolidayInfoModel
import com.numplates.nomera3.modules.holidays.domain.entity.HolidayVisitsModel
import com.numplates.nomera3.modules.holidays.domain.entity.MainButtonLinkModel
import com.numplates.nomera3.modules.holidays.domain.entity.OnBoardingModel
import com.numplates.nomera3.modules.holidays.domain.entity.ProductModel
import com.numplates.nomera3.modules.holidays.ui.entity.Hats
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.modules.holidays.ui.entity.MainButton
import com.numplates.nomera3.modules.holidays.ui.entity.OnBoarding
import com.numplates.nomera3.modules.holidays.ui.entity.Product
import com.numplates.nomera3.modules.holidays.ui.entity.RoomStyle

fun HolidayInfoModel.toUI(): HolidayInfo {
    val chatModel = this.chatRoomEntity.toUI()
    val hatsModel = this.hatsLink.toUI()
    val mainButtonModel = this.mainButtonLinkEntity.toUI()
    val onboardingModel = this.onBoardingEntity.toUI()
    val product = this.productModel?.toUI()
    return HolidayInfo(
        this.id,
        this.code,
        this.title,
        mainButtonModel,
        this.startTime,
        this.finishTime,
        onboardingModel,
        hatsModel,
        chatModel,
        product
    )
}

fun ChatRoomModel.toUI(): RoomStyle {
    return RoomStyle(this.type,this.background_dialog,this.background_anon,this.background_group)
}

fun HatsModel.toUI(): Hats {
    return Hats(this.general,this.premium,this.vip)
}

fun MainButtonLinkModel.toUI(): MainButton {
    return MainButton(this.default,this.active)
}

fun OnBoardingModel.toUI(): OnBoarding {
    return OnBoarding(this.title,this.description,this.icon,this.buttonText)
}

fun ProductModel.toUI(): Product {
    return Product(
        this.id,
        this.appleProductId,
        this.customTitle,
        this.description,
        this.imageItem.toUI(),
        this.itunesProductId,
        this.playMarketProductId,
        this.type
    )
}

fun ProductModel.ImageItemModel.toUI(): Product.ImageItem {
    return Product.ImageItem(
        this.link,
        this.linkSmall
    )
}

fun HolidayVisitsModel.toUI(): HolidayVisits {
    return HolidayVisits(
            goalDays = this.goalDays,
            status = this.status,
            visitDays = this.visitDays
    )
}

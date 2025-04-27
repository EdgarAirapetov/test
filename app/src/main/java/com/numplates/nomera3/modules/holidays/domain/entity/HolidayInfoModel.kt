package com.numplates.nomera3.modules.holidays.domain.entity

data class HolidayInfoModel(
    val id: Long?,
    val code: String?,
    val title: String?,
    val mainButtonLinkEntity: MainButtonLinkModel,
    val startTime: Long,
    val finishTime: Long,
    val onBoardingEntity: OnBoardingModel,
    val hatsLink: HatsModel,
    val chatRoomEntity: ChatRoomModel,
    val productModel: ProductModel?
)

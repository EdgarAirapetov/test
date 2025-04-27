package com.numplates.nomera3.modules.purchase.domain.product

enum class Vip(val id: String) : MarketProduct {
    VIP_WEEK("com.numplates.nomera.vip.week"),
    VIP_MONTH("com.numplates.nomera.vip.month"),
    VIP_THREE_MONTH("com.numplates.nomera.vip.three_months"),
    VIP_ONE_YEAR("com.numplates.nomera.vip.year");

    override fun marketId(): String = id
}

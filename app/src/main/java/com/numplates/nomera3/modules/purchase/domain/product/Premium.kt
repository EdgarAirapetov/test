package com.numplates.nomera3.modules.purchase.domain.product

enum class Premium(val id: String) : MarketProduct {
    PREMIUM_WEEK("com.numplates.nomera.premium.week"),
    PREMIUM_MONTH("com.numplates.nomera.premium.month"),
    PREMIUM_THREE_MONTH("com.numplates.nomera.premium.three_months"),
    PREMIUM_ONE_YEAR("com.numplates.nomera.premium.year");

    override fun marketId(): String = id
}

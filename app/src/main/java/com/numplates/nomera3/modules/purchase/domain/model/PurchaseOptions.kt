package com.numplates.nomera3.modules.purchase.domain.model

data class PurchaseOptions(
    val productId: String,
    val userId: Long?,
    val comment: String?,
    val accountColor: Int?,
    val showSender: Boolean? = null,
)

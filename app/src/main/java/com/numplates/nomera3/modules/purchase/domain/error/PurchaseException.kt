package com.numplates.nomera3.modules.purchase.domain.error

sealed class PurchaseException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    object CancelledByUser : PurchaseException()
    object InternalError : PurchaseException()
}

package com.numplates.nomera3.modules.purchase.ui.send

sealed class SendGiftEvent {
    object GiftSuccess : SendGiftEvent()
    object GiftError : SendGiftEvent()
    object CancelledByUser : SendGiftEvent()
}

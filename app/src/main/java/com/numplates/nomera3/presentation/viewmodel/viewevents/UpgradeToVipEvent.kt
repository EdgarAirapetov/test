package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class UpgradeToVipEvent {
    data class ErrorMarketEvent(val message: Int? = null) : UpgradeToVipEvent()
    object ErrorEvent : UpgradeToVipEvent()
    object SuccessPurchaseEvent : UpgradeToVipEvent()
}

package com.numplates.nomera3.modules.maps.ui.events.model

sealed interface AddressSearchState {
    object Default : AddressSearchState
    object Progress : AddressSearchState
    object Success : AddressSearchState
    object Error : AddressSearchState
}

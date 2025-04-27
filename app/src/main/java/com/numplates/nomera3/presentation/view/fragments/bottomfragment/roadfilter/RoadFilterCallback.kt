package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

interface RoadFilterCallback {

    fun onDismiss()

    fun onCountrySearchClicked()

    interface CallbackOwner {
        var roadFilterCallback: RoadFilterCallback?
    }
}

package com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter

interface FilterCallback {

    fun onSelectCityClick()

    fun onFilterResult(result: FilterResult?) {}

    fun onDismiss() { }

}
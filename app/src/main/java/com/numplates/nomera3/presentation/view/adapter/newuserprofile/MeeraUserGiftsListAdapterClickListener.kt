package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity

interface MeeraUserGiftsListAdapterClickListener {
    fun onLongClick(position: Int, data: UserGiftsUiEntity?)
    fun onBirthdayTextClicked()
}

package com.numplates.nomera3.modules.share.ui

import com.numplates.nomera3.modules.share.ui.entity.UIShareItem

interface ShareItemsCallback {
    fun onChecked(item: UIShareItem, isChecked: Boolean)
    fun canBeChecked(): Boolean = true
}
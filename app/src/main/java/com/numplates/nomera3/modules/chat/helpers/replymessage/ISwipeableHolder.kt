package com.numplates.nomera3.modules.chat.helpers.replymessage

import android.view.View

interface ISwipeableHolder {
    fun getSwipeContainer(): View

    fun canSwipe(): Boolean = true
}
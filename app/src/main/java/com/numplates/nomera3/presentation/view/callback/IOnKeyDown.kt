package com.numplates.nomera3.presentation.view.callback

import android.view.KeyEvent

interface IOnKeyDown {
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
}
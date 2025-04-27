package com.numplates.nomera3.modules.userprofile.ui.fragment

interface CheckScrollPositionListener {
    fun checkVisibilityConnectionButton(isVisible: Boolean)
    fun checkVisibilityUpButton(findFirstVisibleItemPosition: Int, isSwipeUp: Boolean)
}

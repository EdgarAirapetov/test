package com.numplates.nomera3.presentation.view.ui

import android.view.MotionEvent
import android.view.View

/**
 * Используется во время перехвата жестов в [ExtendedGestureOverlayView].
 */
interface GestureEventPasser {
    fun onGesturePassed(view: View?, event: MotionEvent?)
}

package com.meera.core.utils.layouts.intercept

import android.view.MotionEvent

/**
 * Блокирует тач-события и передает их в произвольную вью
 * (используется для блокировки лайаута во время показа бабла реакций)
 */
interface InterceptTouchLayout {
    fun bypassTouches(dispatchEventPasser: ((MotionEvent) -> Boolean)?)
}

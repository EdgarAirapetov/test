package com.numplates.nomera3.presentation.view.ui

import android.view.MotionEvent
import com.numplates.nomera3.presentation.view.ui.swiperefresh.SwipyRefreshLayout

/**
 * Используется во время перехвата onInterceptTouchEvent и onTouchEvent
 * в [BlockTouchesRecyclerView], [BlockTouchesViewPager], [ExtendedGestureOverlayView], [BlockTouchesConstraintLayout] и в
 * [SwipyRefreshLayout].
 */
interface TouchEventPasser {
    fun onTouchPassed(e: MotionEvent?)
}

package com.numplates.nomera3.modules.newroads

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.numplates.nomera3.modules.newroads.fragments.CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import kotlin.math.abs

class BaseRoadRecyclerGestureDetector {
    private var initialY = 0f

    fun onTouchEvent(view: View, e: MotionEvent?): Boolean {
        val event = e ?: return false
        val currentState = NavigationManager.getManager().getTopBehaviour()?.state ?: return false
        if (currentState == BottomSheetBehavior.STATE_SETTLING || currentState == BottomSheetBehavior.STATE_DRAGGING) return false
        val layoutManager = (view as? RecyclerView)?.layoutManager as? LinearLayoutManager ?: return false
        val firstCompletelyVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val currentY = event.rawY
                val deltaY = currentY - initialY
                if (abs(deltaY) > CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD) {
                    if (deltaY > 0
                        && getCurrentBehaviorState() == BottomSheetBehavior.STATE_EXPANDED
                        && firstCompletelyVisiblePosition == 0
                    ) {
                        NavigationManager.getManager().getTopBehaviour()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                    initialY = currentY
                }
            }

            MotionEvent.ACTION_UP -> {
                initialY = 0f
            }
        }
        return false
    }

    private fun getCurrentBehaviorState() = NavigationManager.getManager().getTopBehaviour()?.state
}

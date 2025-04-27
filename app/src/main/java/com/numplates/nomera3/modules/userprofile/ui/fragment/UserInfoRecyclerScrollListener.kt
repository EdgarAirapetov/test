package com.numplates.nomera3.modules.userprofile.ui.fragment

import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.numplates.nomera3.R

class UserInfoRecyclerScrollListener(
    private val bottomBehavior: BottomSheetBehavior<*>?,
    private val layoutManager: LinearLayoutManager,
    private val motionLayout: MotionLayout,
    private val checkScrollPositionListener: CheckScrollPositionListener?,
) : RecyclerView.OnScrollListener() {
    private var movedToEnd = false
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            bottomBehavior?.isDraggable = layoutManager.findFirstCompletelyVisibleItemPosition() == 0
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (layoutManager.findFirstVisibleItemPosition() > 0) {
            checkScrollPositionListener?.checkVisibilityConnectionButton(isVisible = false)
        } else {
            checkScrollPositionListener?.checkVisibilityConnectionButton(isVisible = true)
        }
        checkScrollPositionListener?.checkVisibilityUpButton(layoutManager.findFirstVisibleItemPosition(), dy < 0)

        if (dy > 0) {
            if (layoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                if (movedToEnd) return
                if (motionLayout.currentState != R.id.scene_user_info_end) {
                    movedToEnd = true
                    motionLayout.setTransition(R.id.transition_user_info_middle_to_end)
                    motionLayout.transitionToEnd()
                }
            }
        } else {
            movedToEnd = false
        }
    }
}

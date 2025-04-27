package com.numplates.nomera3.modules.userprofile.ui.fragment

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.meera.core.extensions.isTrue
import com.numplates.nomera3.R
import kotlin.math.abs

class UserInfoGestureDetectorListener(
    private val bottomBehavior: BottomSheetBehavior<*>?,
    private val layoutManager: LinearLayoutManager,
    private val motionLayout: MotionLayout,
    private val isSnippet: Boolean?,
    private val isUserSnippetDataFull: Boolean?
) : SimpleOnGestureListener() {
    private val SWIPE_THRESHOLD: Int = 100
    private val SWIPE_VELOCITY_THRESHOLD: Int = 200

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
    ): Boolean {
        var result = false
        try {
            if (e1?.y == null && bottomBehavior?.state == STATE_HALF_EXPANDED) {
                bottomBehavior.state = STATE_EXPANDED
            }
            val existE1y = e1?.y ?: return result
            val diffY = e2.y - existE1y

            if (abs(diffY.toDouble()) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) onSwipeBottom() else onSwipeTop()
                result = true
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return result
    }

    private fun onSwipeTop() {
        if (bottomBehavior?.state == STATE_HALF_EXPANDED) {
            bottomBehavior.state = STATE_EXPANDED
        }
    }

    private fun onSwipeBottom() {
        if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0 && motionLayout.currentState == R.id.scene_user_info_start) {
            when (bottomBehavior?.state) {
                STATE_EXPANDED -> {
                    when {
                        isSnippet.isTrue() || isUserSnippetDataFull.isTrue() -> {
                            if (motionLayout.progress == 0f) bottomBehavior.state = STATE_COLLAPSED
                        }

                        else -> bottomBehavior.state = STATE_HALF_EXPANDED
                    }
                }

                STATE_HALF_EXPANDED -> {
                    bottomBehavior.state = STATE_HIDDEN
                }

                else -> Unit
            }
        }
    }
}

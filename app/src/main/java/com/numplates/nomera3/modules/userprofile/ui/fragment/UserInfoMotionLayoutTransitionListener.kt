package com.numplates.nomera3.modules.userprofile.ui.fragment

import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionLayout.TransitionListener
import com.numplates.nomera3.R

class UserInfoMotionLayoutTransitionListener: TransitionListener {
    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) = Unit
    override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) =
        Unit

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
    ) = Unit

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        val existMotionLayout = motionLayout ?: return
        if (currentId == R.id.scene_user_info_middle) {
            if (existMotionLayout.progress == 1f) {
                existMotionLayout.setTransition(R.id.transition_user_info_middle_to_end)
                existMotionLayout.transitionToEnd()
            } else {
                existMotionLayout.setTransition(R.id.transition_user_info_start_to_middle)
                existMotionLayout.transitionToStart()
            }
        }
    }
}

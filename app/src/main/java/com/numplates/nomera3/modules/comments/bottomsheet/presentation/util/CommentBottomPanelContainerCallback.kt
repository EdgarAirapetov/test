package com.numplates.nomera3.modules.comments.bottomsheet.presentation.util

import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val BEHAVIOR_SLIDE_OFFSET = -0.2

class BottomPanelContainerCallback(
    private val bottomContainer: ViewGroup,
    private val bottomSheetBehavior: BottomSheetBehavior<out View>,
    private val onBottomSheetStateChanged: (newState: Int) -> Unit
) : BottomSheetBehavior.BottomSheetCallback() {

    private var isBottomContainerVisible = true

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset < BEHAVIOR_SLIDE_OFFSET && isBottomContainerVisible) {
            isBottomContainerVisible = false
            bottomContainer.animateCommentPanelTranslationY(bottomContainer.height.toFloat())
        } else if (slideOffset > BEHAVIOR_SLIDE_OFFSET && !isBottomContainerVisible) {
            isBottomContainerVisible = true
            bottomContainer.animateCommentPanelTranslationY(0f)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        onBottomSheetStateChanged.invoke(newState)
    }
}

package com.numplates.nomera3.modules.maps.ui.snippet.model

import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior

sealed class SnippetState(val isStable: Boolean, val behaviorValue: Int) {

    sealed class StableSnippetState(behaviorValue: Int) : SnippetState(true, behaviorValue)

    object HalfCollapsedPreview : StableSnippetState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED)

    object Preview : StableSnippetState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED)

    object Expanded : StableSnippetState(ViewPagerBottomSheetBehavior.STATE_EXPANDED)

    object Closed : StableSnippetState(ViewPagerBottomSheetBehavior.STATE_HIDDEN)

    sealed class UnstableSnippetState(behaviorValue: Int) : SnippetState(false, behaviorValue)

    object DraggedByUser : UnstableSnippetState(ViewPagerBottomSheetBehavior.STATE_DRAGGING)

    object Transitioning : UnstableSnippetState(ViewPagerBottomSheetBehavior.STATE_SETTLING)

    companion object {
        fun fromBehaviorValue(behaviorValue: Int): SnippetState {
            return when (behaviorValue) {
                ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> Preview
                ViewPagerBottomSheetBehavior.STATE_EXPANDED -> Expanded
                ViewPagerBottomSheetBehavior.STATE_HIDDEN -> Closed
                ViewPagerBottomSheetBehavior.STATE_DRAGGING -> DraggedByUser
                ViewPagerBottomSheetBehavior.STATE_SETTLING -> Transitioning
                else -> throw RuntimeException("No SnippetState for given value: $behaviorValue")
            }
        }
    }
}

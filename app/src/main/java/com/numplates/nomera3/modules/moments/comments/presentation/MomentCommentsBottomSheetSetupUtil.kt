package com.numplates.nomera3.modules.moments.comments.presentation

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MomentCommentsBottomSheetBinding
import com.numplates.nomera3.databinding.MomentCommentsBottomSheetCreateCommentBlockBinding

private val SUGGESTION_EXTRA_SPACE_TAG_DP = 50.dp
private val CREATE_BLOCK_EXTRA_SPACE_TAG_DP = 8.dp
private const val BEHAVIOR_SLIDE_OFFSET = -0.2
private const val SHOW_ANIMATION_DURATION = 150L
private const val HIDE_ANIMATION_DURATION = 100L

/**
 * Класс инкапсулирует реализацию кастомной логики для BottomSheet коментариев моментов
 */
class MomentCommentsBottomSheetSetupUtil {
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var isBottomContainerVisible = true
    private var activity: FragmentActivity? = null

    fun onDispose() {
        keyboardHeightProvider?.release()
        enableWindowAdjust(activity)
    }

    fun setup(
        fragment: MomentCommentsBottomSheetFragment,
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: MomentCommentsBottomSheetBinding,
        bottomDialog: BottomSheetDialog,
        callback: Callback
    ) {
        activity = fragment.activity

        val bottomContainer =
            bottomDialog.findViewById<ViewGroup>(R.id.fl_bottom_container) ?: return
        val bottomSheetView = bottomDialog.findViewById<View>(R.id.design_bottom_sheet) ?: return
        val rootContainer = bottomDialog.findViewById<ViewGroup>(R.id.container) ?: return
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        disableWindowAdjust(
            activity = activity ?: return,
            dialog = bottomDialog
        )

        observeKeyboardHeight(
            bottomContainer = bottomContainer,
            rootContainer = rootContainer,
            bottomDialog = bottomDialog,
            mainBinding = mainBinding,
            createCommentBinding = createCommentBinding,
            callback = callback
        )

        setupBehaviorListener(
            bottomSheetBehavior = bottomSheetBehavior,
            bottomContainer = bottomContainer,
            callback = callback
        )

        extendInputTouchFocus(
            createCommentBinding = createCommentBinding
        )
    }

    private fun extendInputTouchFocus(createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding) {
        createCommentBinding.vgInputLayoutContainer.setThrottledClickListener {
            createCommentBinding.etWriteComment.requestFocus()
        }
    }

    private fun disableWindowAdjust(activity: FragmentActivity, dialog: BottomSheetDialog) {
        activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun enableWindowAdjust(activity: FragmentActivity?) {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun observeKeyboardHeight(
        bottomContainer: ViewGroup,
        rootContainer: ViewGroup,
        bottomDialog: BottomSheetDialog,
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: MomentCommentsBottomSheetBinding,
        callback: Callback
    ) {
        val window = bottomDialog.window ?: return
        val keyboardHeightProvider = KeyboardHeightProvider(window.decorView)

        keyboardHeightProvider.observer = { keyboardHeightPx ->
            adjustRootView(
                keyboardHeight = keyboardHeightPx,
                rootContainer = rootContainer,
                bottomContainer = bottomContainer,
                createCommentBinding = createCommentBinding,
                mainBinding = mainBinding,
                callback = callback,
            )
        }
    }

    private fun getBottomBlockHeight(createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding): Int {
        val bottomBlock = createCommentBinding.vgInputLayoutContainer
        bottomBlock.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        val quickAnswerBlock = createCommentBinding.rvQuickAnswer
        quickAnswerBlock.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        return quickAnswerBlock.measuredHeight + bottomBlock.measuredHeight + CREATE_BLOCK_EXTRA_SPACE_TAG_DP
    }

    private fun adjustRootView(
        keyboardHeight: Int,
        rootContainer: ViewGroup,
        bottomContainer: ViewGroup,
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: MomentCommentsBottomSheetBinding,
        callback: Callback
    ) {
        rootContainer.animate().cancel()

        val isInputFocused = createCommentBinding.etWriteComment.hasFocus()
        val bottomContainerMenuHeight = getBottomBlockHeight(createCommentBinding)
        val suggestionPeekHeight =
            keyboardHeight + bottomContainerMenuHeight + SUGGESTION_EXTRA_SPACE_TAG_DP
        val bottomContainerVerticalOffset = -keyboardHeight

        if (keyboardHeight > 0 && isInputFocused) {
            callback.onChangeSuggestionListPeekHeight(suggestionPeekHeight)

            bottomContainer.animate()
                ?.translationY(bottomContainerVerticalOffset.toFloat())
                ?.setDuration(SHOW_ANIMATION_DURATION)
                ?.start()

            mainBinding.rvMomentComments.setMargins(bottom = keyboardHeight + bottomContainerMenuHeight)
        } else {
            callback.onChangeSuggestionListPeekHeight(suggestionPeekHeight)

            bottomContainer.animate()
                ?.translationY(0f)
                ?.setDuration(HIDE_ANIMATION_DURATION)
                ?.start()

            mainBinding.rvMomentComments.setMargins(bottom = bottomContainerMenuHeight)
        }
    }

    private fun setupBehaviorListener(
        bottomSheetBehavior: BottomSheetBehavior<View>,
        bottomContainer: ViewGroup,
        callback: Callback
    ) {
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < BEHAVIOR_SLIDE_OFFSET && isBottomContainerVisible) {
                    isBottomContainerVisible = false
                    animateBottomContainer(bottomContainer, bottomContainer.height)
                } else if (slideOffset > BEHAVIOR_SLIDE_OFFSET && !isBottomContainerVisible) {
                    isBottomContainerVisible = true
                    animateBottomContainer(bottomContainer, 0)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                callback.onBottomSheetStateChanged(newState)
            }
        })
    }

    private fun animateBottomContainer(bottomContainer: ViewGroup, height: Int) {
        kotlin.runCatching {
            bottomContainer.animate().cancel()
            bottomContainer.animate()
                .translationY(height.toFloat())
                .setDuration(SHOW_ANIMATION_DURATION)
                .start()
        }
    }

    interface Callback {
        fun onChangeSuggestionListPeekHeight(peekHeight: Int)
        fun onBottomSheetStateChanged(newState: Int)
    }
}

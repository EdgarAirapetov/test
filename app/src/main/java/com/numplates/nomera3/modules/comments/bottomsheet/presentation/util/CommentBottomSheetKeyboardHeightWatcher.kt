package com.numplates.nomera3.modules.comments.bottomsheet.presentation.util

import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.setMargins
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.databinding.CommentsBottomSheetBinding
import com.numplates.nomera3.databinding.CommentsBottomSheetCreateCommentBlockBinding
import timber.log.Timber

private val SUGGESTION_EXTRA_SPACE_TAG_DP = 50.dp
private val QUICK_ANSWER_EXTRA_SPACE_MARGIN_DP = 14.dp

class CommentBottomSheetKeyboardHeightWatcher(
    private val onChangeSuggestionListPeekHeight: (peekHeight: Int) -> Unit
) {

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    fun observeKeyboardHeight(
        bottomContainer: ViewGroup,
        rootContainer: ViewGroup,
        bottomSheetDialog: BottomSheetDialog,
        createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: CommentsBottomSheetBinding,
    ) {
        Timber.tag("CommentsBottomSheet").d("window = ${bottomSheetDialog.window}, view = ${bottomSheetDialog.window?.decorView}")
        val window = bottomSheetDialog.window ?: return
        keyboardHeightProvider = KeyboardHeightProvider(window.decorView).also { provider ->
            provider.observer = { keyboardHeightPx ->
                Timber.tag("CommentsBottomSheet").d("keyboard inner observer")
                adjustRootView(
                    keyboardHeight = keyboardHeightPx,
                    rootContainer = rootContainer,
                    bottomContainer = bottomContainer,
                    createCommentBinding = createCommentBinding,
                    mainBinding = mainBinding,
                )
            }
        }
    }

    fun onDispose() {
        keyboardHeightProvider?.release()
        keyboardHeightProvider = null
    }

    private fun getBottomBlockHeight(createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding): Int {
        val bottomBlock = createCommentBinding.vgInputLayoutContainer
        if (bottomBlock.measuredHeight <= 0) bottomBlock.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        val quickAnswerBlock = createCommentBinding.rvQuickAnswer
        val widthSpec = View.MeasureSpec.makeMeasureSpec(getScreenWidth() - QUICK_ANSWER_EXTRA_SPACE_MARGIN_DP, View.MeasureSpec.AT_MOST)
        if (quickAnswerBlock.measuredHeight <= 0) quickAnswerBlock.measure(widthSpec, View.MeasureSpec.UNSPECIFIED)

        return quickAnswerBlock.measuredHeight + bottomBlock.measuredHeight
    }

    private fun adjustRootView(
        keyboardHeight: Int,
        rootContainer: ViewGroup,
        bottomContainer: ViewGroup,
        createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: CommentsBottomSheetBinding,
    ) {
        rootContainer.animate().cancel()

        val isInputFocused = createCommentBinding.etWriteComment.hasFocus()
        val shouldShow = keyboardHeight > 0 && isInputFocused
        val bottomContainerMenuHeight = getBottomBlockHeight(createCommentBinding)
        val suggestionPeekHeight = keyboardHeight + bottomContainerMenuHeight + SUGGESTION_EXTRA_SPACE_TAG_DP

        val desiredTranslationY = if (shouldShow) -keyboardHeight.toFloat() else 0f
        val desiredMargins = if (shouldShow) keyboardHeight + bottomContainerMenuHeight else bottomContainerMenuHeight
        val desiredDuration = if (shouldShow) COMMENT_BOTTOM_SHEET_SHOW_ANIMATION_DURATION else COMMENT_BOTTOM_SHEET_HIDE_ANIMATION_DURATION

        onChangeSuggestionListPeekHeight.invoke(suggestionPeekHeight)
        bottomContainer.animateCommentPanelTranslationY(newTranslation = desiredTranslationY, duration = desiredDuration)
        mainBinding.vgRefreshComments.setMargins(bottom = desiredMargins)
    }

}

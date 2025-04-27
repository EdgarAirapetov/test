package com.numplates.nomera3.modules.moments.comments.presentation

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.dp
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomShareMenuContainerBinding
import com.numplates.nomera3.databinding.MeeraShareBottomSheetBinding

private const val SHOW_ANIMATION_DURATION = 150L
private const val HIDE_ANIMATION_DURATION = 100L
private const val LIST_BOTTOM_PADDING = 230

/**
 * Класс инкапсулирует реализацию кастомной логики для BottomSheet репостинга моментов
 */
class MeeraMomentShareBottomSheetSetupUtil {

    fun setup(
        fragment: Fragment,
        bottomBinding: MeeraBottomShareMenuContainerBinding,
        mainBinding: MeeraShareBottomSheetBinding,
        dialog: BottomSheetDialog
    ) {
        val bottomContainer =
            dialog.findViewById<ViewGroup>(R.id.fl_bottom_container) ?: return
        val bottomSheetView = dialog.findViewById<View>(R.id.design_bottom_sheet) ?: return
        val rootContainer = dialog.findViewById<ViewGroup>(R.id.container) ?: return
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        disableWindowAdjust(
            activity = fragment.activity ?: return,
            dialog = dialog
        )

        observeKeyboardHeight(
            bottomContainer = bottomContainer,
            rootContainer = rootContainer,
            bottomDialog = dialog,
            mainBinding = mainBinding,
            createCommentBinding = bottomBinding,
            bottomSheetBehavior = bottomSheetBehavior
        )
    }

    private fun disableWindowAdjust(activity: FragmentActivity, dialog: BottomSheetDialog) {
        activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun observeKeyboardHeight(
        bottomContainer: ViewGroup,
        rootContainer: ViewGroup,
        bottomDialog: BottomSheetDialog,
        createCommentBinding: MeeraBottomShareMenuContainerBinding,
        mainBinding: MeeraShareBottomSheetBinding,
        bottomSheetBehavior: BottomSheetBehavior<View>
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
                bottomSheetBehavior = bottomSheetBehavior
            )
        }
    }

    private fun adjustRootView(
        keyboardHeight: Int,
        rootContainer: ViewGroup,
        bottomContainer: ViewGroup,
        createCommentBinding: MeeraBottomShareMenuContainerBinding,
        mainBinding: MeeraShareBottomSheetBinding,
        bottomSheetBehavior: BottomSheetBehavior<View>
    ) {
        rootContainer.animate().cancel()
        val isInputFocused = mainBinding.appbarShareSearch.hasFocus()
            || createCommentBinding.vShareInput.hasFocus()
        val bottomContainerVerticalOffset = -keyboardHeight

        if (keyboardHeight > 0 && isInputFocused) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            mainBinding.rvSharePostList.setPaddingBottom(LIST_BOTTOM_PADDING.dp)
            bottomContainer.animate()
                ?.translationY(bottomContainerVerticalOffset.toFloat())
                ?.setDuration(SHOW_ANIMATION_DURATION)
                ?.start()
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mainBinding.rvSharePostList.setPaddingBottom(0)
            bottomContainer.animate()
                ?.translationY(0f)
                ?.setDuration(HIDE_ANIMATION_DURATION)
                ?.start()
        }
    }
}

package com.numplates.nomera3.modules.screenshot.delegate

import androidx.fragment.app.Fragment
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.MeeraScreenshotPopupFragment
import com.numplates.nomera3.modules.screenshot.ui.fragment.MeeraScreenshotPopupFragmentAction
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotPopupFragment
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.CloseTypes

const val SAVING_PICTURE_DELAY = 500L

object ScreenshotPopupController {

    private var fragment: ScreenshotPopupFragment? = null

    var isPopupShowing = false

    fun setListener(listener: BottomSheetDialogEventsListener) {
        fragment?.setListener(object : BottomSheetDialogEventsListener {
            override fun onCreateDialog() {
                listener.onCreateDialog()
            }

            override fun onDismissDialog(closeTypes: CloseTypes?) {
                isPopupShowing = false
                listener.onDismissDialog()
            }
        })
    }

    fun show(
        parentFragment: Fragment,
        screenshotPopupData: ScreenshotPopupData,
        dialogListener: BottomSheetDialogEventsListener? = null
    ) {
        if (screenshotPopupData.isDeleted == true) return
        if (isPopupShowing) return
        this.isPopupShowing = true
        MeeraScreenshotPopupFragment().show(
            fm = parentFragment.childFragmentManager,
            data = screenshotPopupData,
            action = { action ->
                when (action) {
                    is MeeraScreenshotPopupFragmentAction.OnCreateDialog -> dialogListener?.onCreateDialog()
                    is MeeraScreenshotPopupFragmentAction.OnDismissDialog -> {
                        isPopupShowing = false
                        dialogListener?.onDismissDialog()
                    }
                }
            }
        )
    }
}

package com.numplates.nomera3.presentation.view.fragments.meerasettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.meera.core.extensions.animateHeightFromTo
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.input.BottomTextState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraRateUsDialogBinding

const val INPUT_HEIGHT = 100
const val INPUT_HEIGHT_ANIM_DURATION = 250L
private const val DIALOG_TOP_MARGIN = 52
private const val ACTIVATE_INPUT_END_ANIMATION = 300L

interface RateUsDialogAction {
    class OnSendRatingClick(val rating: Int, val comment: String) : RateUsDialogAction
    class OnTextChanged(val text: String) : RateUsDialogAction
    object OnCancelClicked : RateUsDialogAction
}

data class RateUsDialogState(
    val description: String,
    val isRatingSent: Boolean = false,
    val isCancel: Boolean = false
)

class MeeraRateUsDialog : UiKitBottomSheetDialog<MeeraRateUsDialogBinding>() {

    private val viewModel: MeeraRateUsViewModel by viewModels { App.component.getViewModelFactory() }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraRateUsDialogBinding
        get() = MeeraRateUsDialogBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate? {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeState()
        hideInputView()
        setTopMargin()
        setScrollingToBottom()
        setDialogTopTitle()
        setDialogCloseBtnListener()
        setStarsListener()
        setInputTextListener()
        setSendButtonListener()
    }

    private fun setDialogCloseBtnListener() {
        rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener {
            viewModel.onAction(RateUsDialogAction.OnCancelClicked)
        }
    }

    private fun setSendButtonListener() {
        contentBinding?.let { binding ->
            with(binding) {
                btnRateUsSend.setThrottledClickListener {
                    viewModel.onAction(
                        RateUsDialogAction.OnSendRatingClick(
                            rbRateUsStars.rating,
                            inRateUsInput.etInput.text.toString()
                        )
                    )
                }
            }
        }
    }

    private fun setInputTextListener() {
        contentBinding?.inRateUsInput?.doAfterSearchTextChanged { text ->
            viewModel.onAction(RateUsDialogAction.OnTextChanged(text))
        }
    }

    private fun setStarsListener() {
        contentBinding?.let { binding ->
            with(binding) {
                rbRateUsStars.setStarsListener { rating ->
                    if (inRateUsInput.etInput.height == 0) {
                        showInputView()
                    }
                    contentBinding?.btnRateUsSend?.isEnabled = true
                    doDelayed(ACTIVATE_INPUT_END_ANIMATION){
                        contentBinding?.inRateUsInput?.isEnabled = true
                    }
                }
            }
        }
    }

    private fun subscribeState() {
        viewModel.rateUsDialogState.observe(viewLifecycleOwner) { state ->
            when {
                state.isRatingSent -> {
                    dismissAndSendResult()
                }

                state.isCancel -> {
                    dismissDialog()
                }

                else -> {
                    setInputBottomTextState(state)
                }
            }
        }
    }

    private fun dismissAndSendResult() {
        this@MeeraRateUsDialog.dismiss()
        parentFragmentManager.setFragmentResult(KEY_RATE_US_DIALOG_SEND_GRADE, Bundle())
    }

    private fun dismissDialog() = this@MeeraRateUsDialog.dismiss()

    private fun setInputBottomTextState(state: RateUsDialogState) {
        contentBinding?.inRateUsInput?.setBottomTextState(
            BottomTextState.Description(
                descriptionText = state.description,
                showDescriptionIcon = false
            )
        )
    }



    private fun setDialogTopTitle() {
        rootBinding?.apply { tvBottomSheetDialogLabel.text = getString(R.string.meera_rate_us_title) }
    }

    private fun showInputView() {
        contentBinding?.inRateUsInput?.let { inputView ->
            inputView.etInput.animateHeightFromTo(
                initialHeight = 0,
                finalHeight = INPUT_HEIGHT.dp,
                duration = INPUT_HEIGHT_ANIM_DURATION,
                pivot = INPUT_HEIGHT.dp.toFloat()
            )
            inputView.backgroundView.animateHeightFromTo(
                initialHeight = 0,
                finalHeight = INPUT_HEIGHT.dp,
                duration = INPUT_HEIGHT_ANIM_DURATION,
                pivot = INPUT_HEIGHT.dp.toFloat()
            )
        }
    }

    private fun hideInputView() {
        contentBinding?.inRateUsInput?.let { inputView ->
            inputView.etInput.animateHeightFromTo(
                initialHeight = contentBinding?.inRateUsInput?.etInput?.height!!,
                finalHeight = 0,
                duration = 1L,
                pivot = 0f
            )
            inputView.backgroundView.animateHeightFromTo(
                initialHeight = contentBinding?.inRateUsInput?.etInput?.height!!,
                finalHeight = 0,
                duration = 1L,
                pivot = 0f
            )
        }
    }

    private fun setScrollingToBottom() {
        dialog?.window?.decorView?.viewTreeObserver?.addOnGlobalLayoutListener {
            contentBinding?.root?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setTopMargin() = rootBinding?.root?.setMargins(0, DIALOG_TOP_MARGIN.dp, 0, 0)

}


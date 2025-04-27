package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogMapBinding
import com.numplates.nomera3.modules.maps.ui.MapUiActionHandler
import com.numplates.nomera3.modules.maps.ui.model.MapDialogFragmentUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import kotlinx.coroutines.Job


/**
 * We remove and restore window animations to prevent slide animation
 * from happening when returning to screen from another activity
 */
abstract class MeeraMapDialogFragment : UiKitBottomSheetDialog<MeeraDialogMapBinding>() {

    private var windowAnimations: Int = NO_WINDOW_ANIMATIONS
    private var restoreWindowAnimationsJob: Job? = null

    private var confirmed = false

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogMapBinding
        get() = MeeraDialogMapBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        return UiKitBottomSheetDialogParams(
            needShowToolbar = false,
            needShowGrabberView = false,
            horizontalMarginSizeDp = BOTTOMSHEET_MARGIN_HORIZONTAL_DP,
            isLegacy = resources.getBoolean(R.bool.isLegacy),
            dialogStyle = R.style.MapInfoDialog
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as BottomSheetDialog).behavior.apply {
                isHideable = true
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveWindowAnimations(savedInstanceState)
        initUi()
    }

    override fun onResume() {
        super.onResume()
        restoreWindowAnimations()
    }

    override fun onPause() {
        super.onPause()
        cancelWindowAnimations()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_WINDOW_ANIMATIONS, windowAnimations)
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (parentFragment as? MapUiActionHandler)?.apply {
            handleOuterMapUiAction(MapUiAction.MapDialogClosed)
            val confirmMapUiAction = getConfirmMapUiAction(confirmed) ?: return@apply
            handleOuterMapUiAction(confirmMapUiAction)
        }
    }

    abstract fun getUiModel(): MapDialogFragmentUiModel

    open fun getConfirmMapUiAction(confirmed: Boolean): MapUiAction? = null

    private fun initUi() {
        val uiModel = getUiModel()
        bindUiModel(uiModel)
        initListeners()
    }

    private fun bindUiModel(uiModel: MapDialogFragmentUiModel) {
        val binding = contentBinding ?: return
        binding.tvDialogMapTitle.setText(uiModel.titleResId)
        binding.tvDialogMapMessage.setText(uiModel.messageResId)
        binding.tvDialogMapAction.setText(uiModel.actionResId)
        binding.ivDialogMapImage.setImageResource(uiModel.imageResId)
    }

    private fun initListeners() {
        val binding = contentBinding ?: return
        binding.tvDialogMapAction.setThrottledClickListener {
            confirmed = true
            dismiss()
        }
        binding.layoutDialogMapTopbar.ivLayoutMapDialogTopbarClose.setThrottledClickListener {
            dismiss()
        }
    }

    private fun saveWindowAnimations(savedInstanceState: Bundle?) {
        windowAnimations = savedInstanceState
            ?.getInt(KEY_WINDOW_ANIMATIONS, NO_WINDOW_ANIMATIONS)
            ?: dialog?.window?.attributes?.windowAnimations ?: NO_WINDOW_ANIMATIONS
    }

    private fun restoreWindowAnimations() {
        restoreWindowAnimationsJob = doDelayed(100) {
            dialog?.window?.setWindowAnimations(windowAnimations)
        }
    }

    private fun cancelWindowAnimations() {
        restoreWindowAnimationsJob?.cancel()
        dialog?.window?.setWindowAnimations(NO_WINDOW_ANIMATIONS)
    }

    companion object {
        private const val BOTTOMSHEET_MARGIN_HORIZONTAL_DP = 16

        private const val KEY_WINDOW_ANIMATIONS = "KEY_WINDOW_ANIMATIONS"
        private const val NO_WINDOW_ANIMATIONS = -1
    }
}

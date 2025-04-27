package com.numplates.nomera3.modules.contentsharing.ui.loader

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomSheetSharingLoaderBinding
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingAction
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingViewModel
import com.numplates.nomera3.modules.contentsharing.ui.SharingState
import com.numplates.nomera3.modules.contentsharing.ui.TAG_SHARING_LOADER_BOTTOM_SHEET
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val FULL_PROGRESS = 1f
private const val PERCENTAGE_MULTIPLIER_PROGRESS = 100

class MeeraSharingLoaderBottomSheet: UiKitBottomSheetDialog<MeeraBottomSheetSharingLoaderBinding>() {

    private val viewModel by activityViewModels<SharingLoaderViewModel> { App.component.getViewModelFactory() }
    private val activityModel by activityViewModels<ContentSharingViewModel> { App.component.getViewModelFactory() }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetSharingLoaderBinding
        get() = MeeraBottomSheetSharingLoaderBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(
            needShowCloseButton = false,
            needShowToolbar = false
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.IDLE))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        val root = super.onCreateView(inflater, container, state)
        root?.keepScreenOn = true
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        subscribeToChanges()
        subscribeToEvents()
        setupListeners()
        if (savedInstanceState == null) {
            viewModel.handleUIAction(SharingLoaderAction.SendSharingData)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activityModel.handleUIAction(ContentSharingAction.CheckSharingState)
    }

    private fun subscribeToChanges() {
        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleUiState)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    fun show(
        fm: FragmentManager,
    ): MeeraSharingLoaderBottomSheet {
        val dialog = MeeraSharingLoaderBottomSheet()
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, TAG_SHARING_LOADER_BOTTOM_SHEET)
        return dialog
    }

    private fun subscribeToEvents() {
        viewModel.effect
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleUiEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupListeners() {
        rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener {
            viewModel.handleUIAction(SharingLoaderAction.CancelDataUploading)
        }
        contentBinding?.cellMenuCloseShareUpload?.setThrottledClickListener {
            viewModel.handleUIAction(SharingLoaderAction.CancelDataUploading)
        }
    }

    private fun handleUiState(state: SharingLoaderState) {
        val isComplete = state.progress == FULL_PROGRESS && !state.isLoading
        contentBinding?.ivCompleteIcon?.isVisible = isComplete
        contentBinding?.pbLoadProgress?.visibility = if (!isComplete) View.VISIBLE else View.INVISIBLE
        contentBinding?.tvShareUploadProgress?.text = when (isComplete) {
            true -> getString(R.string.general_sent)
            else -> getString(
                R.string.general_progress_percent,
                (state.progress * PERCENTAGE_MULTIPLIER_PROGRESS).toInt()
            )
        }
    }

    private fun handleUiEffect(effect: SharingLoaderEffect) {
        when (effect) {
            SharingLoaderEffect.FinishLoading -> finishLoadingNoError()
            SharingLoaderEffect.ShowWentWrongAlert -> finishLoadingError()
        }
    }

    private fun finishLoadingError() {
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.CloseWithAnError)
    }

    private fun finishLoadingNoError() {
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.CloseWithoutAnError)
    }

}

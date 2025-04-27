package com.numplates.nomera3.modules.contentsharing.ui.loader

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomSheetSharingLoaderBinding
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingAction
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingViewModel
import com.numplates.nomera3.modules.contentsharing.ui.SharingState
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val FULL_PROGRESS = 1f
private const val PERCENTAGE_MULTIPLIER_PROGRESS = 100

class SharingLoaderBottomSheet private constructor() :
    BaseBottomSheetDialogFragment<BottomSheetSharingLoaderBinding>() {

    private val viewModel by activityViewModels<SharingLoaderViewModel> { App.component.getViewModelFactory() }
    private val activityModel by activityViewModels<ContentSharingViewModel> { App.component.getViewModelFactory() }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetSharingLoaderBinding
        get() = BottomSheetSharingLoaderBinding::inflate

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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

        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
        }

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

    private fun subscribeToEvents() {
        viewModel.effect
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleUiEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupListeners() {
        binding?.buttonCancel?.setOnClickListener {
            viewModel.handleUIAction(SharingLoaderAction.CancelDataUploading)
        }
    }

    private fun handleUiState(state: SharingLoaderState) {
        val isComplete = state.progress == FULL_PROGRESS && !state.isLoading
        binding?.ivCompleteIcon?.isVisible = isComplete
        binding?.pbLoadProgress?.isVisible = !isComplete
        binding?.tvInfoTitle?.text = when (isComplete) {
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
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.CloseWithAnError)
    }

    private fun finishLoadingNoError() {
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.CloseWithoutAnError)
    }

    companion object {
        fun newInstance(): SharingLoaderBottomSheet {
            return SharingLoaderBottomSheet()
        }
    }
}

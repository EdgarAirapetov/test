package com.numplates.nomera3.modules.maps.ui.events.navigation

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentEventNavigationBottomsheetBinding
import com.numplates.nomera3.modules.maps.ui.events.navigation.action.EventNavigationUiAction
import com.numplates.nomera3.modules.maps.ui.events.navigation.adapter.EventNavigationItemAdapter
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationUiModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showAboveViewAtStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class EventNavigationBottomsheetDialogFragment :
    BaseBottomSheetDialogFragment<FragmentEventNavigationBottomsheetBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEventNavigationBottomsheetBinding
        get() = FragmentEventNavigationBottomsheetBinding::inflate

    private val viewModel by viewModels<EventNavigationDialogViewModel> { App.component.getViewModelFactory() }

    private var adapter: EventNavigationItemAdapter? = null

    private val addressCopiedTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_copy_address)
    }
    private var addressCopiedTooltipDismissJob: Job? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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
        setupUi()
        parseArguments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    private fun setupUi() {
        adapter = EventNavigationItemAdapter {
            try {
                startActivity(it.navigatorIntent)
                viewModel.handleUiAction(EventNavigationUiAction.AnalyticsUiAction.MapEventToNavigator(it.appName))
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
            }
        }
        binding?.rvEventNavigationItems?.adapter = adapter
        viewModel.liveUiModel.observe(viewLifecycleOwner, ::handleUiModel)
        binding?.tvEventNavigationClose?.setThrottledClickListener {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding?.ivEventNavigationClose?.setThrottledClickListener {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun parseArguments() {
        arguments?.getParcelable<EventNavigationInitUiModel>(KEY_INIT_MODEL)?.let { initUiModel ->
            viewModel.handleUiAction(EventNavigationUiAction.OnInitialized(initUiModel))
        }
    }

    private fun handleUiModel(uiModel: EventNavigationUiModel) {
        adapter?.submitList(uiModel.items)
        binding?.tvEventNavigationAddress?.text = uiModel.address
        binding?.ivEventNavigationCopy?.setOnClickListener {
            copyAddress(uiModel.address)
            showAddressCopiedTooltip()
        }
        val itemsAvailable = uiModel.items.isNotEmpty()
        binding?.vEventNavigationDivider?.isVisible = itemsAvailable
        binding?.nsvEventNavigationContent?.isVisible = itemsAvailable
    }

    private fun copyAddress(address: String) {
        val clipData = ClipData.newPlainText(LABEL_COPY_ADDRESS, address)
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(clipData)
    }

    private fun showAddressCopiedTooltip() {
        val targetView = binding?.ivEventNavigationCopy ?: return
        addressCopiedTooltipDismissJob?.cancel()
        addressCopiedTooltip?.showAboveViewAtStart(
            fragment = this,
            view = targetView,
            offsetX = TOOLTIP_COPY_ADDRESS_OFFSET_X_DP.dp
        )
        addressCopiedTooltipDismissJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(TOOLTIP_COPY_ADDRESS_DISMISS_DELAY_MS)
            addressCopiedTooltip?.dismiss()
        }
    }

    companion object {
        private const val LABEL_COPY_ADDRESS = "event_address"
        private const val KEY_INIT_MODEL = "KEY_INIT_MODEL"
        private const val TOOLTIP_COPY_ADDRESS_OFFSET_X_DP = 44
        private const val TOOLTIP_COPY_ADDRESS_DISMISS_DELAY_MS = 2000L

        @JvmStatic
        fun getInstance(
            initUiModel: EventNavigationInitUiModel
        ) = EventNavigationBottomsheetDialogFragment().apply {
            arguments = bundleOf(
                KEY_INIT_MODEL to initUiModel
            )
        }
    }
}

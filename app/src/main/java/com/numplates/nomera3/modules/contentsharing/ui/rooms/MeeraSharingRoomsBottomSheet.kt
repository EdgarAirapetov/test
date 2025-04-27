package com.numplates.nomera3.modules.contentsharing.ui.rooms

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.empty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomSheetContentSharingBinding
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingAction
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingViewModel
import com.numplates.nomera3.modules.contentsharing.ui.SharingState
import com.numplates.nomera3.modules.contentsharing.ui.TAG_SHARING_ROOMS_BOTTOM_SHEET
import com.numplates.nomera3.modules.contentsharing.ui.loader.MeeraSharingLoaderBottomSheet
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.adapter.MeeraShareItemAdapter
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDividerItemDecorator
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val HEADER_TEXT_PROPORRTION = 0.8f
private const val SPANNABLE_TITLE_START_INDEX = 10

class MeeraSharingRoomsBottomSheet : UiKitBottomSheetDialog<MeeraBottomSheetContentSharingBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetContentSharingBinding
        get() = MeeraBottomSheetContentSharingBinding::inflate

    private val viewModel by viewModels<SharingRoomsViewModel> { App.component.getViewModelFactory() }
    private val activityModel by activityViewModels<ContentSharingViewModel> { App.component.getViewModelFactory() }
    private var adapter = MeeraShareItemAdapter(object : ShareItemsCallback{
        override fun onChecked(item: UIShareItem, isChecked: Boolean) {
            viewModel.handleUIAction(SharingRoomsAction.ChangeSelectedState(item, isChecked))
        }

        override fun canBeChecked(): Boolean {
            return viewModel.canBeCheckedMoreItems()
        }
    })

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_share))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.IDLE))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            setSearchInputTextChangeListener()
            initSharingList()
            subscribeToChanges()
            subscribeToEvents()
            setClickListeners()
            viewModel.handleUIAction(SharingRoomsAction.LoadAvailableChatsToShare)
        }
    }

    fun show(fm: FragmentManager, ): MeeraSharingRoomsBottomSheet {
        val dialog = MeeraSharingRoomsBottomSheet()
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, TAG_SHARING_ROOMS_BOTTOM_SHEET)
        return dialog
    }

    private fun initSharingList() {
        adapter.setHasStableIds(true)
        contentBinding?.apply {
            rvShareUsers.adapter = adapter
            rvShareUsers.addItemDecoration(ShareDividerItemDecorator(requireContext()))
        }
    }

    private fun subscribeToEvents() {
        viewModel.effect
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleUiEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun subscribeToChanges() {
        combine(
            viewModel.state,
            viewModel.shareItems
        ) { state, shareItems -> handleUiState(state, shareItems) }
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setClickListeners() {
        contentBinding?.btnShareSend?.setThrottledClickListener {
            viewModel.handleUIAction(SharingRoomsAction.SendContentToChats)
        }
    }

    private fun handleUiState(state: SharingRoomsState, shareItems: List<UIShareItem>) {
        contentBinding?.noMatchesPlaceholder?.root
            ?.isVisible =  !state.isLoading && !state.isRedirecting && !state.query.isNullOrBlank() && shareItems.isEmpty()
        contentBinding?.pbShareBottomSheet?.isVisible = state.isLoading
        if (!state.isLoading && state.isRedirecting) gotoApp()
        setUserCount(shareItems)
        setupSendButtonState(shareItems.any { it.isChecked })
        adapter.submitList(shareItems)
    }

    private fun setUserCount(shareItems: List<UIShareItem>) {
        val itemCount = shareItems.size
        val countCheckUser = shareItems.count { it.isChecked }
        val checkUserCount = if (itemCount > 0) {
            "  $countCheckUser ${getString(R.string.of)} $itemCount"
        } else {
            String.empty()
        }
        val title = getSpannableTitle(
            checkUserCount = getString(R.string.general_share) + checkUserCount
        )
        rootBinding?.tvBottomSheetDialogLabel?.text = title
    }

    private fun handleUiEffect(effect: SharingRoomsEffect) {
        when (effect) {
            SharingRoomsEffect.SendContentToChats -> sendContentToChats()
            SharingRoomsEffect.SendNetworkError -> showNetworkProblemsAlert()
            SharingRoomsEffect.SendVideoDurationError -> showVideoDurationError()
            SharingRoomsEffect.ShareContentToChats -> shareContentToChats()
            else -> Unit
        }
    }

    private fun sendContentToChats() {
        val text = contentBinding?.vShareInput?.etInput?.text
        viewModel.handleUIAction(SharingRoomsAction.ShareContentToChats(text?.takeIf { it.isNotBlank() }
            ?.toString()))
    }

    private fun showNetworkProblemsAlert() {
        dismiss()
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.ShowNetworkError)
    }

    private fun showVideoDurationError() {
        dismiss()
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.ShowVideoDurationError)
    }

    private fun shareContentToChats() {
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        dismiss()
        MeeraSharingLoaderBottomSheet().show(fm = requireActivity().supportFragmentManager)
    }

    private fun setSearchInputTextChangeListener() {
        contentBinding?.appbarShareSearch?.doAfterSearchTextChanged { text ->
            viewModel.handleUIAction(SharingRoomsAction.QueryShareItems(text))
        }
    }

    private fun gotoApp() {
        val intent = Intent(requireContext(), Act::class.java)
        intent.action = IActionContainer.ACTION_OPEN_AUTHORIZATION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupSendButtonState(isEnabled: Boolean) {
        contentBinding?.btnShareSend?.isEnabled = isEnabled
    }

    private fun getSpannableTitle(
        checkUserCount: String,
        startIndex: Int = SPANNABLE_TITLE_START_INDEX
    ): SpannableString {
        val spannable = SpannableString(checkUserCount)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.uiKitColorForegroundSecondary)),
            startIndex,
            checkUserCount.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        spannable.setSpan(
            RelativeSizeSpan(HEADER_TEXT_PROPORRTION),
            startIndex,
            checkUserCount.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        return spannable
    }

}

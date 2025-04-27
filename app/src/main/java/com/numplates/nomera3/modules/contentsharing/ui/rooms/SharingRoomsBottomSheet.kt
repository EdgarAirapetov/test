package com.numplates.nomera3.modules.contentsharing.ui.rooms

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.dp
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.onSizeChange
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomSheetContentSharingBinding
import com.numplates.nomera3.databinding.ItemInputLayoutBinding
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingAction
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingViewModel
import com.numplates.nomera3.modules.contentsharing.ui.SharingState
import com.numplates.nomera3.modules.contentsharing.ui.TAG_SHARING_ROOMS_BOTTOM_SHEET
import com.numplates.nomera3.modules.contentsharing.ui.loader.SharingLoaderBottomSheet
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.adapter.ShareItemAdapter
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.tags.ui.base.SuggestedTagListMenu
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.EditTextAutoCompletable
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDividerItemDecorator
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

private const val INPUT_TOUCH_EXTRA_SPACE = 32
private const val INPUT_TOUCH_EXTRA_RIGHT_SPACE = 16
private const val SLIDING_OFFSET = -0.2
private const val DURATION_FAST = 150L
private const val DURATION_LONG = 400L
private const val POSITION_FIRST = 1

class SharingRoomsBottomSheet private constructor() :
    BaseBottomSheetDialogFragment<BottomSheetContentSharingBinding>() {

    private var rootView: FrameLayout? = null
    private var mainContainer: FrameLayout? = null
    private var flBottomContainer: FrameLayout? = null
    private var tvShareTitle: TextView? = null
    private var tvShareSubtitle: TextView? = null
    private var ivCloseShare: ImageView? = null
    private var vgShareInput: ViewGroup? = null
    private var etShareInput: EditTextAutoCompletable? = null
    private var btnShareSend: ImageView? = null
    private var btnCommentSettings: ImageView? = null

    private var containerHeight = 0
    private var isBottomContainerVisible = true
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var suggestionsMenu: SuggestedTagListMenu? = null
    private var bottomContainerInputHeight = 0

    private val viewModel by viewModels<SharingRoomsViewModel> { App.component.getViewModelFactory() }
    private val activityModel by activityViewModels<ContentSharingViewModel> { App.component.getViewModelFactory() }
    private val adapter = ShareItemAdapter(object : ShareItemsCallback {
        override fun onChecked(item: UIShareItem, isChecked: Boolean) {
            viewModel.handleUIAction(SharingRoomsAction.ChangeSelectedState(item, isChecked))
        }

        override fun canBeChecked(): Boolean {
            return viewModel.canBeCheckedMoreItems()
        }
    })

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetContentSharingBinding
        get() = BottomSheetContentSharingBinding::inflate

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.IDLE))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        view.post {
            val dialog = dialog as BottomSheetDialog
            rootView = dialog.findViewById(R.id.container)
            mainContainer = dialog.findViewById(R.id.design_bottom_sheet)
            flBottomContainer = dialog.findViewById(R.id.fl_bottom_container)
            tvShareTitle = view.findViewById(R.id.tv_share_title)
            tvShareSubtitle = view.findViewById(R.id.tv_share_subtitle)
            ivCloseShare = view.findViewById(R.id.iv_close_share)
            btnCommentSettings = view.findViewById(R.id.iv_setting_comments)

            createBottomMenu()
            setBehaviorListener()
            setupSharingUi()
            setSearchInputTextChangeListener()
            expandedBottomSheet()
            initSharingList()
            subscribeToChanges()
            subscribeToEvents()
            setClickListeners()

            viewModel.handleUIAction(SharingRoomsAction.LoadAvailableChatsToShare)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activityModel.handleUIAction(ContentSharingAction.CheckSharingState)
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null)
            return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    private fun expandedBottomSheet() {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initSharingList() {
        adapter.setHasStableIds(true)
        binding?.layoutList?.rvSharePostList?.adapter = adapter
        binding?.layoutList?.rvSharePostList
            ?.addItemDecoration(ShareDividerItemDecorator(requireContext()))
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

    private fun handleUiState(state: SharingRoomsState, shareItems: List<UIShareItem>) {
        binding?.layoutList?.appbarShareSearch?.isVisible =
            !state.isRedirecting && !state.isLoading || state.query != null
        binding?.layoutList?.rvSharePostList?.isVisible = !state.isLoading && shareItems.isNotEmpty()
        binding?.layoutList?.tvShareSearchNotFound?.isVisible =
            !state.isLoading && !state.isRedirecting && !state.query.isNullOrBlank() && shareItems.isEmpty()
        binding?.layoutEmpty?.emptyListPlaceholderContainer?.isVisible = !state.isLoading && shareItems.isEmpty()
        binding?.layoutUnauthorized?.logOrSignInContainer?.isVisible = !state.isLoading && state.isRedirecting
        binding?.pbShareBottomSheet?.isVisible = state.isLoading
        binding?.dividerTop?.isVisible = !state.isLoading && state.isRedirecting
        vgShareInput?.isVisible = !state.isRedirecting && !state.isLoading && shareItems.isNotEmpty()
        tvShareSubtitle?.isVisible =
            !state.isRedirecting && !state.isLoading || state.query != null
        tvShareSubtitle?.text =
            "${getString(R.string.selected)} ${shareItems.count { it.isChecked }}/$MAX_SELECTED_ALLOWED"
        setupSendButtonState(shareItems.any { it.isChecked })
        adapter.submitList(shareItems)
    }

    private fun handleUiEffect(effect: SharingRoomsEffect) {
        when (effect) {
            SharingRoomsEffect.SendContentToChats -> sendContentToChats()
            SharingRoomsEffect.SendNetworkError -> showNetworkProblemsAlert()
            SharingRoomsEffect.SendVideoDurationError -> showVideoDurationError()
            SharingRoomsEffect.ShareContentToChats -> shareContentToChats()
            SharingRoomsEffect.ScheduleScrollListToTop -> scrollListToTop()
        }
    }

    private fun setClickListeners() {
        binding?.layoutUnauthorized?.tvLogIn?.setOnClickListener {
            val intent = Intent(requireContext(), Act::class.java)
            intent.action = IActionContainer.ACTION_OPEN_AUTHORIZATION
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            requireActivity().finish()
        }
        ivCloseShare?.setOnClickListener {
            dismiss()
        }
        btnShareSend?.setOnClickListener {
            viewModel.handleUIAction(SharingRoomsAction.SendContentToChats)
        }
    }

    private fun setupSendButtonState(isEnabled: Boolean) {
        val colorRes = if (isEnabled) R.color.colorTransparent else R.color.transparent_white
        btnShareSend?.isEnabled = isEnabled
        btnShareSend?.setColorFilter(context.getColorCompat(colorRes))
    }

    private fun setupSharingUi() {
        (dialog as? BottomSheetDialog)?.let(::initSuggestionsMenu)
        keyboardHeightProvider = KeyboardHeightProvider(dialog?.window!!.decorView)
        keyboardHeightProvider?.observer = {
            adjustRootView(it)
        }
    }

    private fun initSuggestionsMenu(dialog: BottomSheetDialog) {
        dialog.findViewById<View>(R.id.tags_list)?.also { tagListView ->
            tagListView.post {
                tagListView.visible()
                val recyclerTags = tagListView.findViewById<RecyclerView>(R.id.recycler_tags)
                val behavior = BottomSheetBehavior.from(tagListView)
                etShareInput?.let { et ->
                    suggestionsMenu = SuggestedTagListMenu(
                        fragment = this,
                        editText = et,
                        recyclerView = recyclerTags,
                        bottomSheetBehavior = behavior
                    )
                    etShareInput?.suggestionMenu = suggestionsMenu
                }
            }
        }
    }

    private fun createBottomMenu() {
        val shareBinding = ItemInputLayoutBinding.inflate(LayoutInflater.from(context))
        flBottomContainer?.setBackgroundColor(Color.WHITE)
        vgShareInput = shareBinding.root
        vgShareInput?.onSizeChange {
            binding?.layoutList?.rvSharePostList?.setMargins(bottom = vgShareInput?.height ?: 0)
        }
        etShareInput = shareBinding.etShareInput
        btnShareSend = shareBinding.btnShareSend
        setInputTextTouchDelegate(etShareInput)
        flBottomContainer?.addView(vgShareInput)
        binding?.layoutList?.etShareSearch?.hint = getString(R.string.general_search)
        tvShareTitle?.setText(R.string.general_share)
        selectChatTab()
    }

    private fun setInputTextTouchDelegate(editText: EditText?) {
        val parent = editText?.parent as View
        val extraSpace = INPUT_TOUCH_EXTRA_SPACE.dp
        parent.post {
            val touchableArea = Rect()
            editText.getHitRect(touchableArea)
            touchableArea.top -= extraSpace
            touchableArea.bottom += extraSpace
            touchableArea.left -= extraSpace
            touchableArea.right += INPUT_TOUCH_EXTRA_RIGHT_SPACE.dp
            parent.touchDelegate = TouchDelegate(touchableArea, editText)
        }
    }

    private fun setBehaviorListener() {
        val bottomSheetBehavior = BottomSheetBehavior.from(mainContainer!!)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < SLIDING_OFFSET && isBottomContainerVisible) {
                    isBottomContainerVisible = false
                    animateBottomContainer(flBottomContainer?.height ?: 0)
                }
            }
        })
    }

    private fun animateBottomContainer(height: Int) {
        view?.post {
            try {
                flBottomContainer?.animate()?.cancel()
                flBottomContainer?.animate()
                    ?.translationY(height.toFloat())
                    ?.setDuration(DURATION_FAST)
                    ?.start()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun animateRootViewHeight(viewContainerHeight: Int) {
        view?.post {
            try {
                if (mainContainer?.height != viewContainerHeight) {
                    mainContainer.animateHeight(viewContainerHeight, DURATION_LONG)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun adjustRootView(keyboardHeight: Int) {
        flBottomContainer?.animate()?.cancel()
        val isShareInputFocused = etShareInput?.hasFocus() == true
        if (isShareInputFocused && keyboardHeight > 0) {
            suggestionsMenu?.setExtraPeekHeight(keyboardHeight + bottomContainerInputHeight, true)
            flBottomContainer?.animate()
                ?.translationY(keyboardHeight * -1f)
                ?.setDuration(DURATION_FAST)
                ?.start()
        } else {
            suggestionsMenu?.setExtraPeekHeight(bottomContainerInputHeight, true)
            flBottomContainer?.animate()
                ?.translationY(0f)
                ?.setDuration(DURATION_FAST)
                ?.start()
        }
        binding?.layoutList?.rvSharePostList?.setPaddingBottom(keyboardHeight)
    }

    private fun selectChatTab() {
        binding?.layoutList?.tvShareSearchNotFound?.setText(R.string.no_matches)
        binding?.layoutList?.etShareSearch?.setText("")
        tvShareSubtitle?.visible()
        val containerHeight = requireContext().displayHeight -
            context.getStatusBarHeight()
        if (this.containerHeight == 0) this.containerHeight = containerHeight
        animateRootViewHeight(containerHeight)
        binding?.layoutList?.root?.visible()
        binding?.layoutEmpty?.root?.gone()
        binding?.pbShareBottomSheet?.gone()
        btnCommentSettings?.gone()
    }

    private fun setSearchInputTextChangeListener() {
        binding?.layoutList?.etShareSearch?.addTextChangedListener(
            afterTextChanged = { text ->
                binding?.layoutList?.ivShareSearchClear?.isInvisible = text.isNullOrBlank()
                viewModel.handleUIAction(SharingRoomsAction.QueryShareItems(text.toString()))
            }
        )
        binding?.layoutList?.ivShareSearchClear?.setOnClickListener {
            binding?.layoutList?.etShareSearch?.setText("")
            binding?.layoutList?.ivShareSearchClear?.invisible()
        }
    }

    private fun sendContentToChats() {
        viewModel.handleUIAction(SharingRoomsAction.ShareContentToChats(etShareInput?.text?.takeIf { it.isNotBlank() }
            ?.toString()))
    }

    private fun showNetworkProblemsAlert() {
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.ShowNetworkError)
    }

    private fun showVideoDurationError() {
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        activityModel.handleUIAction(ContentSharingAction.ShowVideoDurationError)
    }

    private fun shareContentToChats() {
        activityModel.handleUIAction(ContentSharingAction.UpdateSharingState(SharingState.PROGRESS))
        dismiss()
        SharingLoaderBottomSheet
            .newInstance()
            .show(requireActivity().supportFragmentManager, TAG_SHARING_ROOMS_BOTTOM_SHEET)
    }

    private fun scrollListToTop() {
        adapter.addListener(object : ListListener<UIShareItem> {
            override fun onCurrentListChanged(
                previousList: MutableList<UIShareItem>,
                currentList: MutableList<UIShareItem>
            ) {
                binding?.layoutList?.rvSharePostList?.post {
                    binding?.layoutList?.rvSharePostList?.scrollToPosition(POSITION_FIRST)
                }
                adapter.removeListener(this)
            }
        })
    }

    companion object {
        fun newInstance(): SharingRoomsBottomSheet {
            return SharingRoomsBottomSheet()
        }
    }
}

package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.utils.showCommonError
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMediakeyboardStickersFragmentBinding
import com.numplates.nomera3.modules.chat.MediaKeyboardCallback
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.adapter.MediaKeyboardStickersAdapter
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerAdapterItem
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiAction
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickersEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardWidget
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.viewmodel.MediaKeyboardStickersViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

const val NEED_SHOW_WIDGETS_ARG = "NEED_SHOW_WIDGETS_ARG"

private const val SPAN_COUNT_STICKER = 1
private const val SPAN_COUNT_HEADER = 4
private const val RECYCLER_VIEW_STATE_KEY = "rv_stickers_state"

class MeeraMediaKeyboardStickersFragment : MeeraBaseFragment(layout = R.layout.meera_mediakeyboard_stickers_fragment) {

    private val binding by viewBinding(MeeraMediakeyboardStickersFragmentBinding::bind)
    private val viewModel: MediaKeyboardStickersViewModel by viewModels { App.component.getViewModelFactory() }

    private val needShowWidgets: Boolean
        get() = arguments?.getBoolean(NEED_SHOW_WIDGETS_ARG) ?: false

    private val adapter by lazy {
        MediaKeyboardStickersAdapter(
            this::onStickerClick,
            this::onStickerLongClick,
            this::onRecentStickerClick,
            this::onRecentStickerLongClick,
            this::onWidgetClick,
            this::onClearRecentStickersClick
        )
    }

    private var stickerPackIdToScrollTo: Int? = null

    private val mediaKeyboardCallback: MediaKeyboardCallback?
        get() = parentFragment as? MediaKeyboardCallback?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        savedInstanceState?.let {
            it.getParcelable<Parcelable>(RECYCLER_VIEW_STATE_KEY)
                ?.let { state -> binding.rvStickers.layoutManager?.onRestoreInstanceState(state) }
        }
        initObservers()
    }

    fun tryToScrollToWidgets() {
        if (adapter.currentList.isEmpty()) {
            this.stickerPackIdToScrollTo = null
        } else {
            getGridLayoutManager()?.scrollToPositionWithOffset(0, 0)
        }
    }

    fun tryToScrollToRecentStickers() {
        if (adapter.currentList.isEmpty()) {
            this.stickerPackIdToScrollTo = null
        } else {
            val itemPosition = adapter.currentList.indexOfFirst {
                it is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem
            }
            getGridLayoutManager()?.scrollToPositionWithOffset(itemPosition, 0)
        }
    }

    fun tryToScrollToStickerPackById(stickerPackId: Int) {
        if (adapter.currentList.isEmpty()) {
            this.stickerPackIdToScrollTo = stickerPackId
        } else {
            scrollToStickerPack(stickerPackId)
        }
    }

    private fun scrollToStickerPack(stickerPackId: Int) {
        val itemPosition = adapter.currentList.indexOfFirst {
            it is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem && it.stickerPack.id == stickerPackId
        }
        getGridLayoutManager()?.scrollToPositionWithOffset(itemPosition, 0)
    }

    private fun getGridLayoutManager() = binding.rvStickers.layoutManager as? GridLayoutManager?

    private fun initViews() {
        initContainerBackground()
        initRecyclerView()
        addOnScrollListener()
    }

    private fun initContainerBackground() {
        val colorRes = if (needShowWidgets) {
            R.color.editor_widgets_content
        } else {
            R.color.uiKitColorBackgroundSecondary
        }
        binding.flStickersContainer.setBackgroundResource(colorRes)
    }

    private fun initRecyclerView() {
        val layoutManager = GridLayoutManager(context, SPAN_COUNT_HEADER)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.currentList.getOrNull(position)) {
                    is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem,
                    is MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem,
                    is MediaKeyboardStickerAdapterItem.WidgetsItem -> SPAN_COUNT_HEADER

                    is MediaKeyboardStickerAdapterItem.StickerItem,
                    is MediaKeyboardStickerAdapterItem.RecentStickerItem -> SPAN_COUNT_STICKER

                    else -> SPAN_COUNT_STICKER
                }
            }
        }
        binding.rvStickers.setHasFixedSize(true)
        binding.rvStickers.layoutManager = layoutManager
        binding.rvStickers.adapter = adapter
    }

    private fun addOnScrollListener() {
        binding.rvStickers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val currentPosition = getGridLayoutManager()?.findFirstVisibleItemPosition() ?: return
                val lastPosition = getGridLayoutManager()?.findLastCompletelyVisibleItemPosition() ?: return
                if (currentPosition == RecyclerView.NO_POSITION) return
                val currentItem = adapter.currentList[currentPosition]
                val currentStickerPack = when {
                    lastPosition >= adapter.currentList.size - SPAN_COUNT_STICKER -> {
                        viewModel.mediaKeyboardStickersState.value.stickerPacks.lastOrNull()
                    }

                    currentItem is MediaKeyboardStickerAdapterItem.StickerItem -> {
                        viewModel.mediaKeyboardStickersState.value.stickerPacks.firstOrNull { it.stickers.any { it.id == currentItem.sticker.id } }
                    }

                    currentItem is MediaKeyboardStickerAdapterItem.StickerPackHeaderItem -> {
                        viewModel.mediaKeyboardStickersState.value.stickerPacks.firstOrNull { it.id == currentItem.stickerPack.id }
                    }

                    else -> {
                        null
                    }
                }
                if (currentStickerPack != null) {
                    mediaKeyboardCallback?.onScrollToNewStickerPack(currentStickerPack)
                } else if (currentItem is MediaKeyboardStickerAdapterItem.WidgetsItem) {
                    mediaKeyboardCallback?.onScrollToWidgets()
                } else {
                    mediaKeyboardCallback?.onScrollToRecentStickers()
                }
            }
        })
    }

    private fun initObservers() {
        viewModel.mediaKeyboardStickersState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                val items = getItemsFromPacksAndRecentStickers(
                    stickerPacks = state.stickerPacks,
                    recentStickers = state.recentStickers,
                    widgets = if (needShowWidgets) MediaKeyboardWidget.entries else emptyList()
                )
                adapter.submitList(items) { checkIfNeedToScrollToStickerPack() }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.mediaKeyboardStickersEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(this::handleEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun checkIfNeedToScrollToStickerPack() {
        val stickerPackId = stickerPackIdToScrollTo ?: return
        scrollToStickerPack(stickerPackId)
        stickerPackIdToScrollTo = null
    }

    private fun getItemsFromPacksAndRecentStickers(
        stickerPacks: List<MediaKeyboardStickerPackUiModel>,
        recentStickers: List<MediakeyboardFavoriteRecentUiModel>,
        widgets: List<MediaKeyboardWidget>
    ): List<MediaKeyboardStickerAdapterItem> {
        val adapterItems = mutableListOf<MediaKeyboardStickerAdapterItem>()
        if (widgets.isNotEmpty()) {
            adapterItems.add(MediaKeyboardStickerAdapterItem.WidgetsItem(widgets))
        }
        if (recentStickers.isNotEmpty()) {
            adapterItems.add(MediaKeyboardStickerAdapterItem.RecentStickersHeaderItem)
            adapterItems.addAll(recentStickers.map {
                MediaKeyboardStickerAdapterItem.RecentStickerItem(it)
            })
        }
        stickerPacks.forEach { pack ->
            adapterItems.add(MediaKeyboardStickerAdapterItem.StickerPackHeaderItem(pack))
            adapterItems.addAll(pack.stickers.map {
                MediaKeyboardStickerAdapterItem.StickerItem(it, pack.id)
            })
        }
        return adapterItems
    }

    private fun handleEvent(event: MediaKeyboardStickersEvent) {
        when (event) {
            is MediaKeyboardStickersEvent.OnLoadingStickersError -> {
                showCommonError(getText(R.string.no_internet), requireView())
            }
        }
    }

    private fun onStickerClick(sticker: MediaKeyboardStickerUiModel) {
        mediaKeyboardCallback?.onStickerClicked(sticker, sticker.emoji.firstOrNull())
    }

    private fun onStickerLongClick(sticker: MediaKeyboardStickerUiModel) {
        mediaKeyboardCallback?.onStickerLongClicked(sticker)
    }

    private fun onRecentStickerClick(recentSticker: MediakeyboardFavoriteRecentUiModel) {
        mediaKeyboardCallback?.onFavoriteRecentClicked(recentSticker, MediaPreviewType.RECENT)
    }

    private fun onRecentStickerLongClick(recentSticker: MediakeyboardFavoriteRecentUiModel) {
        mediaKeyboardCallback?.onFavoriteRecentLongClicked(
            favoriteRecent = recentSticker,
            type = MediaPreviewType.RECENT,
            deleteClickListener = {
                viewModel.handleUiAction(
                    MediaKeyboardStickerUiAction.DeleteRecentSticker(
                        recentId = it,
                        stickerId = recentSticker.stickerId,
                        isForMoment = needShowWidgets
                    )
                )
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(RECYCLER_VIEW_STATE_KEY, binding.rvStickers.layoutManager?.onSaveInstanceState())
    }

    private fun onWidgetClick(widget: MediaKeyboardWidget) {
        mediaKeyboardCallback?.onWidgetClicked(widget)
    }

    private fun onClearRecentStickersClick() {
        val menu = MeeraMenuBottomSheet(requireContext())
        menu.addItem(
            title = getString(R.string.clear_recent_stickers),
            icon = R.drawable.ic_delete_from_recents,
            color = R.color.color_reaction_default
        ) {
            viewModel.handleUiAction(MediaKeyboardStickerUiAction.ClearRecentStickers(needShowWidgets))
        }
        menu.show(childFragmentManager)
    }

}

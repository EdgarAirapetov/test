package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.FragmentMediakeyboardFavoritesBinding
import com.numplates.nomera3.modules.chat.MediaKeyboardCallback
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.adapter.MediaKeyboardFavoritesRecentsAdapter
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.viewmodel.MediakeyboardFavoritesViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.BottomSheetSlideOffsetListener
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter.NEED_SHOW_WIDGETS_ARG
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

private const val GRID_SPAN_COUNT = 4

private const val BOTTOM_SHEET_CLOSED_EMPTY_VIEW_POSITION = 64
private const val EMPTY_VIEW_HALF_HEIGHT = 57
private const val DEFAULT_EMPTY_VIEW_POSITION = 350

class MediakeyboardFavoritesFragment : BaseFragmentNew<FragmentMediakeyboardFavoritesBinding>(), BottomSheetSlideOffsetListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMediakeyboardFavoritesBinding
        get() = FragmentMediakeyboardFavoritesBinding::inflate

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        MediaKeyboardFavoritesRecentsAdapter(
            this::onLongClick,
            this::onClick
        )
    }

    private val needShowWidgets: Boolean
        get() = arguments?.getBoolean(NEED_SHOW_WIDGETS_ARG) ?: false

    private val observer = Observer<PagedList<MediakeyboardFavoriteRecentUiModel>> {
        adapter.submitList(it)
        binding?.vgEmptyFavorites?.isVisible = it.isEmpty()
    }

    private val mediaKeyboardFavoritesCallback: MediaKeyboardCallback?
        get() = parentFragment as? MediaKeyboardCallback?

    private val viewModel: MediakeyboardFavoritesViewModel by viewModels { App.component.getViewModelFactory() }

    override fun onResume() {
        super.onResume()
        viewModel.checkNetworkStatus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadFavorites(needShowWidgets)
        initViews()
        subscribeToData()
    }

    override fun onBottomSheetSlide(slideOffset: Float) {
        val bottomSheetOpenedEmptyViewPosition =
            binding?.root?.height?.div(2)?.minus(EMPTY_VIEW_HALF_HEIGHT.dp) ?: DEFAULT_EMPTY_VIEW_POSITION.dp
        val offset = (bottomSheetOpenedEmptyViewPosition - BOTTOM_SHEET_CLOSED_EMPTY_VIEW_POSITION.dp) * slideOffset
        val margin = BOTTOM_SHEET_CLOSED_EMPTY_VIEW_POSITION.dp + offset
        binding?.vgEmptyFavorites?.setMargins(top = margin.roundToInt())
    }

    private fun initViews() {
        binding?.apply {
            rvFavorites.adapter = adapter
            rvFavorites.layoutManager = GridLayoutManager(context, GRID_SPAN_COUNT)
        }
    }

    private fun subscribeToData() {
        viewModel.favoritesLiveData.observe(viewLifecycleOwner, observer)
        viewModel.mediakeyboardFavoritesEventFlow.onEach(this::handleEvent).launchIn(lifecycle.coroutineScope)
    }

    private fun handleEvent(event: MediakeyboardFavoriteEvent) {
        when (event) {
            is MediakeyboardFavoriteEvent.OnPagingInitialized -> {
                viewModel.favoritesLiveData.removeObserver(observer)
                viewModel.favoritesLiveData.observe(viewLifecycleOwner, observer)
            }
            is MediakeyboardFavoriteEvent.OnNetworkStatusReceived -> {
                if (!event.isConnected) {
                    showCommonError()
                }
            }
        }
    }

    private fun onLongClick(item: MediakeyboardFavoriteRecentUiModel) {
        mediaKeyboardFavoritesCallback?.onFavoriteRecentLongClicked(
            favoriteRecent = item,
            type = MediaPreviewType.FAVORITE
        )
    }

    private fun onClick(item: MediakeyboardFavoriteRecentUiModel) {
        mediaKeyboardFavoritesCallback?.onFavoriteRecentClicked(
            favoriteRecent = item,
            type = MediaPreviewType.FAVORITE
        )
    }

}

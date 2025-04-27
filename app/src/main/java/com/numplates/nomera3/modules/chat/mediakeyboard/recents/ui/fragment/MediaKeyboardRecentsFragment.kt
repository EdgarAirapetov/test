package com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.FragmentMediakeyboardRecentsBinding
import com.numplates.nomera3.modules.chat.MediaKeyboardCallback
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.adapter.MediaKeyboardRecentsAdapter
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentsEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentsState
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.viewmodel.MediaKeyboardRecentsViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val GRID_SPAN_COUNT = 4

class MediaKeyboardRecentsFragment : BaseFragmentNew<FragmentMediakeyboardRecentsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMediakeyboardRecentsBinding
        get() = FragmentMediakeyboardRecentsBinding::inflate

    private val viewModel: MediaKeyboardRecentsViewModel by viewModels { App.component.getViewModelFactory() }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        MediaKeyboardRecentsAdapter(
            this::onRecentLongClick,
            this::onRecentClick
        )
    }

    private val mediaKeyboardFavoritesCallback: MediaKeyboardCallback?
        get() = parentFragment as? MediaKeyboardCallback?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecents()
    }

    fun refreshRecents() {
        viewModel.loadRecents()
    }

    private fun initViews() {
        binding?.apply {
            tvClearAll.setThrottledClickListener { viewModel.clearRecents() }
            rvRecents.adapter = adapter
            rvRecents.layoutManager = GridLayoutManager(context, GRID_SPAN_COUNT)
        }
    }

    private fun initListeners() {
        viewModel.mediaKeyboardRecentsState.observe(viewLifecycleOwner, this::handleRecentsState)
        viewModel.mediaKeyboardRecentsEvent
            .onEach(this::handleRecentsEvent)
            .launchIn(lifecycle.coroutineScope)
    }

    private fun handleRecentsState(state: MediaKeyboardRecentsState) {
        when {
            state.recentList.isEmpty() -> {
                binding?.vgEmptyFavorites?.visible()
                binding?.vgFavoritesList?.gone()
            }

            state.recentList.isNotEmpty() -> {
                binding?.vgEmptyFavorites?.gone()
                binding?.vgFavoritesList?.visible()
                adapter.submitList(state.recentList)
            }
        }
    }

    private fun handleRecentsEvent(event: MediaKeyboardRecentsEvent) {
        when (event) {
            is MediaKeyboardRecentsEvent.OnLoadingRecentError -> showCommonError()
        }
    }

    private fun onRecentLongClick(item: MediakeyboardFavoriteRecentUiModel) {
        mediaKeyboardFavoritesCallback?.onFavoriteRecentLongClicked(
            favoriteRecent = item,
            type = MediaPreviewType.RECENT,
            deleteClickListener = viewModel::deleteRecent
        )
    }

    private fun onRecentClick(item: MediakeyboardFavoriteRecentUiModel) {
        mediaKeyboardFavoritesCallback?.onFavoriteRecentClicked(
            favoriteRecent = item,
            type = MediaPreviewType.RECENT,
            deleteRecentListener = viewModel::deleteRecent
        )
    }

}

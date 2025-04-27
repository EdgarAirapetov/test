package com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMediakeyboardRecentsBinding
import com.numplates.nomera3.modules.chat.MediaKeyboardCallback
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.adapter.MediaKeyboardRecentsAdapter
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentsEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentsState
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.viewmodel.MediaKeyboardRecentsViewModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val GRID_SPAN_COUNT = 4

class MeeraMediaKeyboardRecentsFragment : MeeraBaseFragment(layout = R.layout.meera_mediakeyboard_recents_fragment) {

    private val binding by viewBinding(FragmentMediakeyboardRecentsBinding::bind)

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
        refreshRecents()
    }

    fun refreshRecents() {
        viewModel.loadRecents()
    }

    private fun initViews() {
        binding.apply {
            tvClearAll.setThrottledClickListener { viewModel.clearRecents() }
            rvRecents.adapter = adapter
            rvRecents.layoutManager = GridLayoutManager(context, GRID_SPAN_COUNT)
        }
    }

    private fun initListeners() {
        viewModel.mediaKeyboardRecentsState.observe(viewLifecycleOwner, this::handleRecentsState)
        viewModel.mediaKeyboardRecentsEvent
            .onEach(this::handleRecentsEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    @Suppress("KotlinConstantConditions")
    private fun handleRecentsState(state: MediaKeyboardRecentsState) {
        when {
            state.recentList.isEmpty() -> {
                binding.vgEmptyFavorites.visible()
                binding.vgFavoritesList.gone()
            }

            state.recentList.isNotEmpty() -> {
                binding.vgEmptyFavorites.gone()
                binding.vgFavoritesList.visible()
                adapter.submitList(state.recentList)
            }
        }
    }

    private fun handleRecentsEvent(event: MediaKeyboardRecentsEvent) {
        when (event) {
            is MediaKeyboardRecentsEvent.OnLoadingRecentError -> {
                showCommonError(getText(R.string.no_internet), requireView())
            }
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

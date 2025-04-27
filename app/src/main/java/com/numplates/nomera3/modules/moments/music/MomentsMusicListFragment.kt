package com.numplates.nomera3.modules.moments.music

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING
import com.google.gson.Gson
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.media_controller_implementation.presentation.BROADCAST_MEDIA_ACTION
import com.meera.media_controller_implementation.presentation.BROADCAST_MUSIC_EXTRA_JSON
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMusicListBinding
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity
import com.numplates.nomera3.modules.music.ui.adapter.MusicActionCallback
import com.numplates.nomera3.modules.music.ui.adapter.MusicAdapter
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.music.ui.entity.event.UserActionEvent
import com.numplates.nomera3.modules.music.ui.entity.state.MusicSearchScreenState
import com.numplates.nomera3.modules.music.ui.entity.state.Status
import com.numplates.nomera3.modules.music.ui.fragment.CONTAINER_OPEN_ANIMATION_DURATION
import com.numplates.nomera3.modules.music.ui.listener.CentralPositionScrollListener
import com.numplates.nomera3.modules.music.ui.viewmodel.AddMusicViewModel
import com.numplates.nomera3.modules.music.ui.viewmodel.MeeraMusicViewModel
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val PAGE_SIZE = 20
private const val BUFFER_SIZE = 10
private const val SEARCH_DEBOUNCE_TIME_MS = 500L

class MomentsMusicListFragment : BaseBottomSheetDialogFragment<FragmentMusicListBinding>() , MusicActionCallback {

    private var searchJob: Job? = null
    private var isAddingMode: Boolean = true
    private var isMomentMode: Boolean = true

    private val viewModel by viewModels<AddMusicViewModel> { App.component.getViewModelFactory() }

    private val musicViewModel by viewModels<MeeraMusicViewModel>(ownerProducer = { parentFragment ?: this })

    private lateinit var adapter: MusicAdapter

    private var selectedMediaEntity: MediaEntity? = null

    private var mainContainer: FrameLayout? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMusicListBinding
        get() = FragmentMusicListBinding::inflate

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAddingMode = arguments?.getBoolean(MomentsWrapperActivity.IS_ADDING_MUSIC) ?: true
        viewModel.setIsAdding(isAddingMode)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDialog()
        initAdapter()
        setupObservers()
        initView()
    }

    private fun initDialog() {
        dialog?.window?.navigationBarColor = ContextCompat.getColor(
            requireContext(),
            MomentsWrapperActivity.STATUS_BAR_COLOR
        )
        mainContainer = dialog?.findViewById(R.id.design_bottom_sheet)
        mainContainer?.setBackgroundTint(R.color.black)
        animateRootViewHeight()
        mainContainer?.let { bottomSheetBehavior = BottomSheetBehavior.from(it) }
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior?.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (bottomSheetBehavior?.state == STATE_SETTLING) {
                        if (slideOffset > 0) {
                            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                        } else {
                            dismiss()
                        }
                    }
                }
            }
        )
    }

    private fun initView() {
        binding?.etSearchMusic?.setInputBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.uiKitColorForegroundPrimary)
        )
        binding?.etSearchMusic?.setCloseButtonClickedListener {
            view.hideKeyboard()
        }
        binding?.rvMusicContent?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })

        binding?.btnClose?.click { dismiss() }

        showToolbar()
    }

    private fun animateRootViewHeight() {
        view?.post {
            try {
                val totalHeight = requireContext().displayHeight
                if (totalHeight != mainContainer?.height)
                    mainContainer.animateHeight(totalHeight, CONTAINER_OPEN_ANIMATION_DURATION)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun showToolbar() {
        val str = if (isAddingMode) getString(R.string.add_music)
        else getString(R.string.replace_music)
        binding?.tvHeader?.text = str
    }

    private fun hideKeyboard() = binding?.etSearchMusic?.hideKeyboard()

    private fun initAdapter() {
        adapter = MusicAdapter(musicActionCallback = this, isDarkMode = true)
        val layoutManager = LinearLayoutManager(context)
        binding?.rvMusicContent?.layoutManager = layoutManager
        binding?.rvMusicContent?.adapter = adapter
        initPagination(layoutManager)
    }

    private fun initPagination(layoutManager: LinearLayoutManager) {

        binding?.rvMusicContent?.addOnScrollListener(
            object : RecyclerPaginationListener(layoutManager, PAGE_SIZE, BUFFER_SIZE) {
                override fun loadMoreItems() {
                    viewModel.loadMore()
                }

                override fun isLastPage(): Boolean = viewModel.isLastPage()

                override fun isLoading(): Boolean = viewModel.isLoading()
            })

        //Music scrolling
        binding?.rvMusicContent?.addOnScrollListener(object : CentralPositionScrollListener() {
            override fun onMoveCentralItem(range: IntRange) {
                checkMusicCellVisibility(range)
            }
        })
    }

    private fun setupObservers() {
        viewModel.liveState.observe(viewLifecycleOwner) { state ->
            observeState(state)
        }
    }

    private fun observeState(state: MusicSearchScreenState) {
        when (state) {
            is MusicSearchScreenState.RecommendationState ->
                showRecommendations(state)

            is MusicSearchScreenState.SearchResultState ->
                showSearchState(state)

            else -> {}
        }
    }

    private fun setupTextObserver() {
        binding?.etSearchMusic?.doAfterSearchTextChanged { text ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(SEARCH_DEBOUNCE_TIME_MS)
                viewModel.searchMusic(text)
            }
        }
    }

    private fun checkMusicCellVisibility(range: IntRange? = null) {
        val nonNullRange: IntRange = if (range == null) {
            val lm = binding?.rvMusicContent?.layoutManager as? LinearLayoutManager?
            CentralPositionScrollListener.getRange(lm)
        } else {
            range
        }
        val uiItems = adapter.getRangeItems(nonNullRange)
        musicViewModel.handleUIAction(action = UserActionEvent.MoveMusicCell(nonNullRange, uiItems))
    }

    override fun onStart() {
        super.onStart()
        setupTextObserver()
    }

    override fun onStop() {
        super.onStop()
        searchJob?.cancel()
    }

    private fun showSearchState(state: MusicSearchScreenState.SearchResultState) {
        adapter.submitList(state.searchList) {
            checkMusicCellVisibility()
            handleScrollUp(state.needToScrollUp)
        }
        handlePlaceholder(state.status)
    }

    private fun handlePlaceholder(status: Status) {
        when (status) {
            Status.STATUS_NETWORK_ERROR, Status.STATUS_EMPTY_LIST ->
                showErrorPlaceholder()
            Status.STATUS_OK ->
                hidePlaceHolder()
        }
    }

    private fun hidePlaceHolder() {
        binding?.emptyMessageContainer?.gone()
    }

    private fun showErrorPlaceholder() {
        binding?.emptyMessageContainer?.visible()
    }

    private fun showRecommendations(state: MusicSearchScreenState.RecommendationState) {
        adapter.submitList(state.recommendations) {
            checkMusicCellVisibility()
            handleScrollUp(state.needToScrollUp)
        }
        handlePlaceholder(state.status)
    }

    private fun handleScrollUp(needToScrollUp: Boolean) {
        Timber.d("handleScrollUp = $needToScrollUp")
        if (needToScrollUp) binding?.rvMusicContent?.scrollToPosition(0)
    }

    override fun onPause() {
        super.onPause()
        musicViewModel.handleUIAction(action = UserActionEvent.UnSubscribe)
    }

    //adapter callback
    override fun onPlayClicked(
        entity: MusicCellUIEntity,
        audioEventListener: AudioEventListener,
        adapterPosition: Int,
        musicView: View?
    ) {
        musicViewModel.handleUIAction(
            action = UserActionEvent.PlayClicked(
                entity, musicView = musicView
            ),
            audioEventListener = audioEventListener,
            adapterPosition = adapterPosition
        )
    }

    override fun onStopClicked(entity: MusicCellUIEntity, isReset: Boolean) {
        musicViewModel.handleUIAction(UserActionEvent.StopClicked(entity))
    }

    override fun onCellClicked(entity: MusicCellUIEntity) {
        musicViewModel.handleUIAction(action = UserActionEvent.AddClicked(entity))
    }

    override fun onAddClicked(entity: MusicCellUIEntity) {
        musicViewModel.handleUIAction(action = UserActionEvent.AddClicked(entity))
        if (isMomentMode) {
            selectedMediaEntity = entity.mediaEntity
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        sendBroadcast()
        super.onDismiss(dialog)
    }

    private fun sendBroadcast() {
        selectedMediaEntity?.let {
            val intent = Intent()
            intent.action = BROADCAST_MEDIA_ACTION
            intent.setPackage(requireContext().packageName)
            intent.putExtra(BROADCAST_MUSIC_EXTRA_JSON, Gson().toJson(it))
            requireActivity().sendBroadcast(intent)
            activity?.finish()
        } ?: run {
            (activity as MomentsWrapperActivity).onBackPress()
        }
    }

}

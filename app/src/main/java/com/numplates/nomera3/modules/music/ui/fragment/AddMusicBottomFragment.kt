package com.numplates.nomera3.modules.music.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentAddMusicBinding
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.music.ui.adapter.MusicActionCallback
import com.numplates.nomera3.modules.music.ui.adapter.MusicAdapter
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.music.ui.entity.event.UserActionEvent
import com.numplates.nomera3.modules.music.ui.entity.state.MusicSearchScreenState
import com.numplates.nomera3.modules.music.ui.entity.state.Status
import com.numplates.nomera3.modules.music.ui.listener.CentralPositionScrollListener
import com.numplates.nomera3.modules.music.ui.viewmodel.AddMusicViewModel
import com.numplates.nomera3.modules.music.ui.viewmodel.MeeraMusicViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit


private const val SEARCH_DEBOUNCE_TIME_MS = 500L
private const val DEFAULT_ANIMATION_DURATION = 150L
private const val PAGE_SIZE = 20
private const val BUFFER_SIZE = 10

class AddMusicBottomFragment
    : BaseBottomSheetDialogFragment<FragmentAddMusicBinding>(), MusicActionCallback {

    private var mainContainer: FrameLayout? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private val disposables = CompositeDisposable()
    private var isAddingMode: Boolean = true

    private val viewModel by viewModels<AddMusicViewModel> { App.component.getViewModelFactory() }

    private val musicViewModel by viewModels<MeeraMusicViewModel>(ownerProducer = { parentFragment ?: this })

    private lateinit var adapter: MusicAdapter

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAddMusicBinding
        get() = FragmentAddMusicBinding::inflate

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAddingMode = arguments?.getBoolean(DATA_KEY_IS_ADDING_MODE, true) ?: true
        viewModel.setIsAdding(isAddingMode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        setupObservers()
        initView()
    }

    private fun initView() {
        mainContainer = dialog?.findViewById(R.id.design_bottom_sheet)
        animateRootViewHeight()
        mainContainer?.let { bottomSheetBehavior = BottomSheetBehavior.from(it) }

        binding?.etSearchMusic?.click {
            setExpanded()
        }

        binding?.etSearchMusic?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) setExpanded()
        }

        binding?.rvMusicContent?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })

        binding?.btnClose?.click {
            dismiss()
        }

        binding?.btnClearInput?.click {
            binding?.etSearchMusic?.setText("")
        }

        showToolbar()
    }

    private fun showToolbar() {
        val str = if (isAddingMode) getString(R.string.add_music)
        else getString(R.string.replace_music)
        binding?.tvHeader?.text = str
    }

    private fun hideKeyboard() = binding?.etSearchMusic?.hideKeyboard()

    private fun setExpanded() = bottomSheetBehavior?.apply {
        state = BottomSheetBehavior.STATE_EXPANDED
    }


    private fun initAdapter() {
        adapter = MusicAdapter(this)
        val layoutManager = LinearLayoutManager(context)
        binding?.rvMusicContent?.layoutManager = layoutManager
        binding?.rvMusicContent?.adapter = adapter
        initPagination(layoutManager)
    }

    private fun initPagination(layoutManager: LinearLayoutManager) {

        //Pagination
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

    private fun setupTextObserver() {
        binding?.etSearchMusic?.let { editText ->
            disposables.add(
                RxTextView.textChanges(editText)
                    .skip(1) // skip first empty value
                    .map { text ->
                        if (text.trim().isEmpty()) hideClearBtn()
                        else showClearBtn()

                        if (text.isNotEmpty() && bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        text.toString().trim()
                    }
                    .debounce(SEARCH_DEBOUNCE_TIME_MS, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ query ->
                        viewModel.searchMusic(query)
                    })
                    { Timber.e(it) }
            )
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

    private fun hideClearBtn() {
        binding?.btnClearInput?.isEnabled = false
        binding?.btnClearInput
            ?.animate()
            ?.scaleX(0f)
            ?.scaleY(0f)
            ?.setDuration(DEFAULT_ANIMATION_DURATION)
            ?.start()
    }

    private fun showClearBtn() {
        binding?.btnClearInput?.isEnabled = true
        binding?.btnClearInput
            ?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.setDuration(DEFAULT_ANIMATION_DURATION)
            ?.start()
    }

    override fun onStart() {
        super.onStart()
        setupTextObserver()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
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
            Status.STATUS_NETWORK_ERROR ->
                showNetworkErrorPlaceholder()

            Status.STATUS_EMPTY_LIST ->
                showEmptyResultPlaceHolder()

            Status.STATUS_OK ->
                hidePlaceHolder()
        }
    }

    private fun hidePlaceHolder() {
        binding?.emptyMessageContainer?.gone()
    }

    private fun showEmptyResultPlaceHolder() {
        binding?.emptyMessageContainer?.visible()
        binding?.ivEmptyList?.setImageResource(R.drawable.ic_empty_search_noomeera)
    }

    private fun showNetworkErrorPlaceholder() {
        binding?.emptyMessageContainer?.visible()
        binding?.ivEmptyList?.setImageResource(R.drawable.ic_network_placeholder)
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
        dismiss()
    }

    companion object {
        const val DATA_KEY_IS_ADDING_MODE = "IS_ADDING_MODE"


        fun showAddMusicBottomFragment(fm: FragmentManager, isAddingMode: Boolean) {
            val fragment = AddMusicBottomFragment()
            val arg = Bundle()
            arg.putBoolean(DATA_KEY_IS_ADDING_MODE, isAddingMode)
            fragment.arguments = arg
            if (fm.findFragmentByTag(AddMusicBottomFragment::javaClass.name) == null) {
                fragment.show(fm, AddMusicBottomFragment::javaClass.name)
            }
        }
    }
}

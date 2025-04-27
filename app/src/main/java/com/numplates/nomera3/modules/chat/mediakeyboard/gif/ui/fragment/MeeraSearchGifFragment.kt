package com.numplates.nomera3.modules.chat.mediakeyboard.gif.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMediakeyboardGifBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardWhereProperty
import com.numplates.nomera3.modules.chat.MediaKeyboardCallback
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyEmoji
import com.numplates.nomera3.modules.gifservice.ui.adapter.GifEmojiAdapter
import com.numplates.nomera3.modules.gifservice.ui.adapter.GiphyListAdapter
import com.numplates.nomera3.modules.gifservice.ui.entity.GifEmojiEntity
import com.numplates.nomera3.modules.gifservice.ui.entity.GifEmojiItemType
import com.numplates.nomera3.modules.gifservice.ui.entity.GifQueryMode
import com.numplates.nomera3.modules.gifservice.ui.entity.state.GifMenuViewState
import com.numplates.nomera3.modules.gifservice.ui.entity.state.Status
import com.numplates.nomera3.modules.gifservice.ui.viewmodel.GIF_PAGE_SIZE
import com.numplates.nomera3.modules.gifservice.ui.viewmodel.GiphyViewModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment

private const val GIF_LIST_SPAN_COUNT = 3
private const val INFO_SNACKBAR_MARGIN_BOTTOM = 16
private const val RECYCLER_VIEW_GIFS_STATE_KEY = "rv_gifs_state"
private const val RECYCLER_VIEW_EMOJIS_STATE_KEY = "rv_gifs_state"


class MeeraSearchGifFragment : MeeraBaseFragment(layout = R.layout.meera_mediakeyboard_gif_fragment) {

    private val binding by viewBinding(FragmentMediakeyboardGifBinding::bind)
    private val viewModel by viewModels<GiphyViewModel>()

    private var queryMode: GifQueryMode = GifQueryMode.Recent

    private val emojiAdapter = GifEmojiAdapter()
    private val gifAdapter by lazy(LazyThreadSafetyMode.NONE) {
        GiphyListAdapter { id, preview, url, ratio ->
            mediaKeyboardCallback?.onGifLongClicked(
                id = id,
                preview = preview,
                url = url,
                ratio = ratio
            )
        }
    }

    private var gifListScrollListener: RecyclerPaginationListener? = null

    private var searchQuery: String = String.empty()

    private var isOnline = true
    private var isHappenDisconnect = false
    private var isHideKeyboardWhenScrollDown = false

    private val defaultAspectRatio = 1.toDouble()

    private val mediaKeyboardCallback: MediaKeyboardCallback?
        get() = parentFragment as? MediaKeyboardCallback?


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEmojiList()
        initGifSearchResults()
        initSearchField()
        initSearchTextObserver()
        initConnectionHandler()
        savedInstanceState?.let {
            it.getParcelable<Parcelable>(RECYCLER_VIEW_GIFS_STATE_KEY)
                ?.let { state -> binding.rvGifs.layoutManager?.onRestoreInstanceState(state) }

            it.getParcelable<Parcelable>(RECYCLER_VIEW_EMOJIS_STATE_KEY)
                ?.let { state -> binding.rvGifEmojis.layoutManager?.onRestoreInstanceState(state) }
        }
    }

    private fun initConnectionHandler() {
        viewModel.networkStatusProvider.getNetworkStatusLiveData()
            .observe(viewLifecycleOwner) { networkStatus ->
                this.isOnline = networkStatus.isConnected
                if (isOnline && isHappenDisconnect && searchQuery.isNotEmpty()) {
                    isHappenDisconnect = false
                    firstSearchRequest(searchQuery)
                }
            }
    }

    private fun initSearchField() {
        binding?.apply {
            btnClearInput.click {
                etSearchQuery.clearText()
                etSearchQuery.clearFocus()
                etSearchQuery.gravity = Gravity.CENTER
            }
            etSearchQuery.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    mediaKeyboardCallback?.onSearchFieldClicked()
                    etSearchQuery.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
            }
            etSearchQuery.addTextChangedListener {
                if (it.isNullOrEmpty()) {
                    etSearchQuery.gravity = Gravity.CENTER
                    etSearchQuery.clearFocus()
                } else {
                    etSearchQuery.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
            }
            etSearchQuery.setOnKeyListener { _, keyCode, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }
    }

    private fun initEmojiList() {
        val items = mutableListOf(
            GifEmojiEntity(
                itemType = GifEmojiItemType.IMAGE,
                emojiQuery = GiphyEmoji.SEARCH,
                emojiDrawableRes = R.drawable.search,
                isSelected = true
            ),
            GifEmojiEntity(emojiText = "🔝", emojiQuery = GiphyEmoji.TOP),
            GifEmojiEntity(emojiText = "👍", emojiQuery = GiphyEmoji.LIKE),
            GifEmojiEntity(emojiText = "😍", emojiQuery = GiphyEmoji.IN_LOVE),
            GifEmojiEntity(emojiText = "😄", emojiQuery = GiphyEmoji.HAPPY),
            GifEmojiEntity(emojiText = "😡", emojiQuery = GiphyEmoji.ANGRY),
            GifEmojiEntity(emojiText = "😎", emojiQuery = GiphyEmoji.COOL),
            GifEmojiEntity(emojiText = "😂", emojiQuery = GiphyEmoji.LAUGH),
            GifEmojiEntity(emojiText = "😒", emojiQuery = GiphyEmoji.APATHETIC_STARE),
            GifEmojiEntity(emojiText = "🥳", emojiQuery = GiphyEmoji.CELEBRATION),
            GifEmojiEntity(emojiText = "🤒", emojiQuery = GiphyEmoji.SICK),
            GifEmojiEntity(emojiText = "😮", emojiQuery = GiphyEmoji.SHOCK),
            GifEmojiEntity(emojiText = "👎", emojiQuery = GiphyEmoji.DISLIKE),
        )

        binding?.rvGifEmojis?.adapter = emojiAdapter
        emojiAdapter.addItems(items)
        emojiAdapter.clickListener = { item, position ->
            selectEmoji(items, position)
            handleEmojiItemClick(item)
        }
        handleEmojiItemClick(items.first())
    }

    private fun selectEmoji(items: List<GifEmojiEntity>, position: Int) {
        items.forEach { it.isSelected = false }
        items[position].isSelected = true
        emojiAdapter.addItems(items)
    }

    private fun handleEmojiItemClick(emoji: GifEmojiEntity) {
        gifListScrollListener?.release()

        if (emoji.itemType == GifEmojiItemType.TEXT
            && emoji.emojiQuery != GiphyEmoji.TOP
        ) {
            binding?.searchField?.gone()
            queryMode = GifQueryMode.Emoji(emoji.emojiQuery.query)
            firstSearchRequest(emoji.emojiQuery.query)
        } else {
            if (emoji.emojiQuery == GiphyEmoji.SEARCH) {
                queryMode = GifQueryMode.Search(query = searchQuery)
                binding?.searchField?.visible()
                if (searchQuery.isNotEmpty()) {
                    firstSearchRequest(searchQuery)
                } else {
                    viewModel.getRecentGifs()
                }

                binding?.etSearchQuery?.post {
                    if (searchQuery.isNotEmpty()) {
                        binding?.etSearchQuery?.requestFocus()
                    }
                }
            } else {
                binding?.searchField?.gone()
                queryMode = GifQueryMode.Trending(query = GiphyEmoji.TOP.query)
                firstTrendingRequest(GiphyEmoji.TOP.query)
            }
        }
    }

    private fun initGifSearchResults() {
        binding?.rvGifs?.apply {
            setHasFixedSize(false)
            layoutManager = initScrollListener()
            adapter = gifAdapter
            addOnScrollListener(gifListScrollListener as RecyclerPaginationListener)
        }

        viewModel.liveViewState.observe(viewLifecycleOwner, ::handleViewState)

        gifAdapter.clickListener = { giphyEntity ->
            viewModel.setGifToRecent(
                id = giphyEntity?.id ?: String.empty(),
                smallUrl = giphyEntity?.smallUrl ?: String.empty(),
                originalUrl = giphyEntity?.originalUrl ?: String.empty(),
                originalAspectRatio = giphyEntity?.originalAspectRatio
            )
            val gifUrl = if (isOnline) giphyEntity?.originalUrl else giphyEntity?.smallUrl
            val uri = Uri.parse(gifUrl)
            val ratio = giphyEntity?.originalAspectRatio ?: defaultAspectRatio
            val gifSentWhereProp = when {
                queryMode is GifQueryMode.Recent -> AmplitudeMediaKeyboardWhereProperty.GIF_BLOCK
                queryMode is GifQueryMode.Search && searchQuery.isNotEmpty() ->
                    AmplitudeMediaKeyboardWhereProperty.GIF_SEARCH

                queryMode is GifQueryMode.Search -> AmplitudeMediaKeyboardWhereProperty.GIF_BLOCK
                else -> AmplitudeMediaKeyboardWhereProperty.CATEGORIES_GIF
            }
            mediaKeyboardCallback?.onGifClicked(uri, ratio, giphyEntity, gifSentWhereProp)
        }
    }

    private fun initScrollListener(): GridLayoutManager {
        val gridLayoutManager = GridLayoutManager(context, GIF_LIST_SPAN_COUNT)
        gifListScrollListener = object : RecyclerPaginationListener(
            layoutManager = gridLayoutManager,
            pageSize = GIF_PAGE_SIZE,
            bufferSize = GIF_PAGE_SIZE / 2
        ) {
            override fun loadMoreItems() {
                handleLoadMoreItems()
            }

            override fun isLastPage(): Boolean = viewModel.isLastPage

            override fun isLoading(): Boolean = viewModel.isLoading

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleKeyboardVisibilityWhenGifScrollDown(gridLayoutManager)
            }
        }
        return gridLayoutManager
    }

    private fun handleLoadMoreItems() {
        when (queryMode) {
            is GifQueryMode.Recent -> {}
            is GifQueryMode.Search -> moreSearchRequest(
                query = searchQuery,
                offset = gifAdapter.itemCount
            )

            is GifQueryMode.Trending -> moreTrendingRequest(
                query = (queryMode as GifQueryMode.Trending).query,
                offset = gifAdapter.itemCount
            )

            is GifQueryMode.Emoji -> moreSearchRequest(
                query = (queryMode as GifQueryMode.Emoji).query,
                offset = gifAdapter.itemCount
            )
        }
    }

    private fun initSearchTextObserver() {
        binding?.etSearchQuery?.addTextChangedListener { text ->
            handleSearchResults(text.toString())
        }
    }

    private fun handleSearchResults(text: String) {
        this.searchQuery = text
        if (text.isNotEmpty()) {
            queryMode = GifQueryMode.Search(query = searchQuery)
            binding?.btnClearInput?.visible()
            gifListScrollListener?.release()
            firstSearchRequest(text)
        } else {
            queryMode = GifQueryMode.Recent
            binding?.btnClearInput?.gone()
            viewModel.getRecentGifs()
        }
    }

    private fun handleKeyboardVisibilityWhenGifScrollDown(layoutManager: GridLayoutManager) {
        val firstItemVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstItemVisiblePosition > 0 && !isHideKeyboardWhenScrollDown) {
            isHideKeyboardWhenScrollDown = true
        } else if (firstItemVisiblePosition == 0) {
            isHideKeyboardWhenScrollDown = false
        }
    }

    private fun handleViewState(state: GifMenuViewState) {
        when (state) {
            is GifMenuViewState.SearchResultState -> handleSearchResultState(state)
        }
    }

    private fun handleSearchResultState(state: GifMenuViewState.SearchResultState) {
        when (state.status) {
            Status.STATUS_OK -> {
                handleResultsPlaceholder(isVisible = false)
                if (state.isFirstPage) {
                    gifAdapter.clearAndSubmitList(state.resultList)
                } else {
                    gifAdapter.submitList(state.resultList)
                }
            }

            Status.STATUS_EMPTY_SEARCH_GIFS -> defaultRequest()
            Status.STATUS_NETWORK_ERROR -> {
                showNetworkErrorSnackbar()
                isHappenDisconnect = true
                gifAdapter.clearList {
                    handleResultsPlaceholder(isVisible = true)
                }
            }

            Status.STATUS_NETWORK_ERROR_WITH_CACHE -> {
                showNetworkErrorSnackbar()
                isHappenDisconnect = true
                handleResultsPlaceholder(isVisible = false)
                gifAdapter.clearAndSubmitList(state.resultList)
            }

            Status.STATUS_EMPTY_RECENT_GIFS -> defaultRequest()
        }
    }

    private fun defaultRequest() {
        queryMode = GifQueryMode.Emoji(GiphyEmoji.REACTIONS.query)
        firstSearchRequest(GiphyEmoji.REACTIONS.query)
    }

    private fun firstSearchRequest(query: String) {
        viewModel.search(
            query = query,
            limit = GIF_PAGE_SIZE,
            offset = 0
        )
    }

    private fun moreSearchRequest(query: String, offset: Int) {
        viewModel.search(
            query = query,
            limit = GIF_PAGE_SIZE,
            offset = offset
        )
    }

    private fun firstTrendingRequest(query: String) {
        viewModel.getTrending(
            query = query,
            limit = GIF_PAGE_SIZE,
            offset = 0
        )
    }

    private fun moreTrendingRequest(query: String, offset: Int) {
        viewModel.getTrending(
            query = query,
            limit = GIF_PAGE_SIZE,
            offset = offset
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(RECYCLER_VIEW_GIFS_STATE_KEY, binding.rvGifs.layoutManager?.onSaveInstanceState())
        outState.putParcelable(RECYCLER_VIEW_EMOJIS_STATE_KEY, binding.rvGifEmojis.layoutManager?.onSaveInstanceState())
    }

    private fun handleResultsPlaceholder(isVisible: Boolean) {
        binding?.noResultsPlaceholder?.isVisible = isVisible
    }

    private fun showNetworkErrorSnackbar() {
        NSnackbar.with(requireView())
            .typeError()
            .marginBottom(INFO_SNACKBAR_MARGIN_BOTTOM)
            .text(getString(R.string.error_giphy_connect))
            .durationLong()
            .show()
    }
}

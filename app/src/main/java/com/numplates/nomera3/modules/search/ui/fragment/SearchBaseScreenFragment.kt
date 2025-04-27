package com.numplates.nomera3.modules.search.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.stringNullable
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentSearchResultsNewBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.feed.ui.ExtraLinearLayoutManager
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState
import com.numplates.nomera3.modules.search.ui.util.SearchDividerDecoration
import com.numplates.nomera3.modules.search.ui.viewmodel.base.DEFAULT_SEARCH_RESULT_PAGE_SIZE
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchBaseScreenViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import com.numplates.nomera3.presentation.view.utils.NToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

private const val ILLEGAL_STATE_FRAGMENT_BINDING_NOT_CREATED =
    "FragmentSearchResultsBinding should be available"

private const val SNACK_BAR_MARGIN = 64
private const val PLACE_HOLDER_MARGIN_DP = 140
private const val SNACK_BAR_MARGIN_BOTTOM_DP = 16

private val HALF_SCREEN = getScreenHeight() / 2

//const val AT_SIGN = "@"
//const val SHARP_SIGN = "#"

abstract class SearchBaseScreenFragment :
    BaseFragmentNew<FragmentSearchResultsNewBinding>(), SearchScreenContext {

    protected var disposable = CompositeDisposable()

    private var resultObserver: Observer<SearchResultViewState>? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchResultsNewBinding
        get() = FragmentSearchResultsNewBinding::inflate

    protected abstract fun getRecyclerAdapter(): ListDelegationAdapter<List<SearchItem>>

    protected abstract fun getResultScreenViewModel(): SearchResultScreenBaseViewModel
    protected abstract fun getDefaultScreenViewModel(): SearchDefaultScreenBaseViewModel
    /**
     * @param inputData отображаемый список элементов recyclerView
     * @return показывать ли плейсхолдер при списке inputData в состоянии Result
     */
    protected abstract fun isShowResultPlaceholder(inputData: List<SearchItem>): Boolean

    /**
     * @param inputData отображаемый список элементов recyclerView
     * @return показывать ли плейсхолдер при списке inputData в состоянии Default
     */
    protected abstract fun isShowDefaultPlaceholder(inputData: List<SearchItem>): Boolean

    protected abstract fun analyticLogInputSearch(
        searchType: AmplitudePropertySearchType,
        haveResult: AmplitudePropertyHaveResult
    )

    private var undoSnackBar: NSnackbar? = null
    private var messageSnackBar: NSnackbar? = null
    private var subscribedViewModel: SearchBaseScreenViewModel? = null
    private var currentState: SearchScreenContext.ScreenState? = null

    private fun getBinding(): FragmentSearchResultsNewBinding {
        return binding ?: error(ILLEGAL_STATE_FRAGMENT_BINDING_NOT_CREATED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlaceholder()
        initRecycler()
    }

    override fun blankSearch() {
        showAndLoadSearchScreen(String.empty())
    }

    override fun search(query: String) {
        val previousQuery = getResultScreenViewModel().getSearchQuery()

        if (this is SearchHashTagFragment && query == SHARP_SIGN) {
            showAndLoadDefaultScreen()
            return
        }

        if (query != AT_SIGN) {
            searchRequest(query, previousQuery)
        }
    }

    override fun clearCurrentResult() {
        subscribedViewModel?.publishEmptyData()
    }

    private fun searchRequest(newQuery: String, previousQuery: String) {
        val isFilterChanged = getResultScreenViewModel().isFilterChanged()
        if (previousQuery == newQuery && newQuery.isNotEmpty() && !isFilterChanged) {
            setScreenState(SearchScreenContext.ScreenState.Result)
            return
        }

        if (newQuery.isNotEmpty() || isFilterChanged) {
            showAndLoadSearchScreen(newQuery)
        } else {
            showAndLoadDefaultScreen()
        }
    }

    private fun showAndLoadDefaultScreen() {
        setScreenState(SearchScreenContext.ScreenState.Default)
        getDefaultScreenViewModel().reload()
    }

    private fun showAndLoadSearchScreen(query: String) {
        getResultScreenViewModel().search(query)
        setScreenState(SearchScreenContext.ScreenState.Result)
    }

    override fun showAndLoadSearchScreen(numberSearchParameters: NumberSearchParameters) {
        getResultScreenViewModel().searchUserByNumber(numberSearchParameters,0)
        setScreenState(SearchScreenContext.ScreenState.Result)
    }

    override fun setScreenState(state: SearchScreenContext.ScreenState) {
        if (state == currentState) {
            return
        }

        setPlaceHolderType(state)

        subscribedViewModel?.let { unsubscribeViewModel() }
        currentState = state

        when (state) {
            SearchScreenContext.ScreenState.Default -> {
                subscribeViewModel(getDefaultScreenViewModel())
            }
            SearchScreenContext.ScreenState.Result -> {
                subscribeViewModel(getResultScreenViewModel())
            }
        }
    }

    override fun getScreenState(): SearchScreenContext.ScreenState? = currentState


    override fun hideMessages() {
        hideSnackBar()
        undoClearRecent()
    }

    private fun setPlaceHolderType(screenState: SearchScreenContext.ScreenState) {
        when (screenState) {
            SearchScreenContext.ScreenState.Default -> {
                getBinding().ivEmptyList.setImageResource(R.drawable.ic_search_result_soon)
                getBinding().tvEmptyList.setText(R.string.search_result_list_soon)
            }
            SearchScreenContext.ScreenState.Result -> {
                getBinding().ivEmptyList.setImageResource(R.drawable.ic_empty_search_noomeera)
                getBinding().tvEmptyList.setText(R.string.placeholder_empty_search_result)
            }
        }
    }

    private fun hideSnackBar() {
        messageSnackBar?.dismiss()
    }

    private fun undoClearRecent() {
        if (getDefaultScreenViewModel().isClearingRecent()) {
            getDefaultScreenViewModel().forceClearRecent()
            undoSnackBar?.dismiss()
        }
    }

    protected fun subscribeViewModel(viewModel: SearchBaseScreenViewModel) {
        subscribedViewModel = viewModel

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            addRxStream(viewModel)
        }

        resultObserver = Observer<SearchResultViewState> { state -> handleViewState(state) }.apply {
            viewModel.getResultState().observe(viewLifecycleOwner, this)
        }
    }

    protected fun unsubscribeViewModel() {
        if (subscribedViewModel == null) {
            return
        }

        resultObserver?.let { subscribedViewModel!!.getResultState().removeObserver(it) }
        subscribedViewModel = null
        clearRxStream()
    }

    protected open fun addRxStream(viewModel: SearchBaseScreenViewModel) {
        clearRxStream()

        disposable.add(viewModel.getMessageStream()) { event ->
            handleMessageEvent(event)
        }
        disposable.add(viewModel.getEventStream()) { event ->
            handleEvent(event)
        }
    }

    private fun clearRxStream() {
        disposable.clear()
    }

    protected open fun handleEvent(event: SearchBaseViewEvent) {
        when (event) {
            is SearchBaseViewEvent.ShowLoading -> {
                showProgress()
            }
        }
    }

    protected open fun handleMessageEvent(event: SearchMessageViewEvent) {
        when (event) {
            is SearchMessageViewEvent.Error -> {
                showErrorMessage(event.message)

                hideProgress()
            }
            is SearchMessageViewEvent.ClearRecentMessage -> {
                showClearRecentTimerSnackBar(event.message, event.delaySec) {
                    getDefaultScreenViewModel().undoClearRecent()
                }
            }
            else -> {
                showSnackBar(event.message)
            }
        }
    }

    private fun handleViewState(state: SearchResultViewState) {
        hideProgress()

        when (state) {
            is SearchResultViewState.SearchResult -> {
                val newData = state.value

                handleAnalyticLogInputSearch(newData, state.needToLogSearch)

                setShowPlaceholder(isShowResultPlaceholder(newData))
                applyDataToRecycler(newData)
            }
            is SearchResultViewState.DefaultResult -> {
                val newData = state.value

                setShowPlaceholder(isShowDefaultPlaceholder(newData))

                applyDataToRecycler(newData)
            }
            is SearchResultViewState.UpdateSearchResultItem -> {
                updateUserInRecycler(updatedUser = state.updatedUser)
            }
            is SearchResultViewState.Data, SearchResultViewState.SearchStart -> {
                val listData = (state as SearchResultViewState.Data).value
                setShowPlaceholder(false)
                applyDataToRecycler(listData)
            }
        }
    }

    private fun updateUserInRecycler(updatedUser: SearchItem.User) {
        val currentItems = getRecyclerAdapter().items
        for (i in currentItems.indices) {
            val currentItem = currentItems[i] as? SearchItem.User
            if (currentItem != null && currentItem.uid == updatedUser.uid) {
                getRecyclerAdapter().notifyItemChanged(i, updatedUser)
                break
            }
        }
    }

    private fun applyDataToRecycler(data: List<SearchItem>) {
        val diffUtilCallback = object : DiffUtil.Callback() {
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                if (getRecyclerAdapter().items == null) {
                    return false
                }

                return getRecyclerAdapter().items[oldItemPosition] == data[newItemPosition]
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                if (getRecyclerAdapter().items == null) {
                    return false
                }

                return getRecyclerAdapter().items[oldItemPosition] == data[newItemPosition]
            }

            override fun getOldListSize(): Int {
                return getRecyclerAdapter().items?.size ?: 0
            }

            override fun getNewListSize(): Int {
                return data.size
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        diffResult.dispatchUpdatesTo(getRecyclerAdapter())

        getRecyclerAdapter().items = data
    }

    private fun initPlaceholder() {
        val placeHolderPosition = HALF_SCREEN - PLACE_HOLDER_MARGIN_DP.dp
        getBinding().emptyMessageContainer.setMargins(top = placeHolderPosition)
        getBinding().emptyMessageContainer.gone()
    }

    private fun initRecycler() {
        val layoutManager = ExtraLinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val paginationListener = object : RecyclerPaginationListener(
            layoutManager = layoutManager,
            pageSize = DEFAULT_SEARCH_RESULT_PAGE_SIZE,
            bufferSize = DEFAULT_SEARCH_RESULT_PAGE_SIZE / 2
        ) {
            override fun loadMoreItems() {
                getResultScreenViewModel().loadMore()
            }

            override fun isLastPage(): Boolean {
                return getResultScreenViewModel().getPagingProperties().isLastPage
            }

            override fun isLoading(): Boolean {
                return getResultScreenViewModel().getPagingProperties().isLoading
            }
        }

        val divider = SearchDividerDecoration.build(requireContext())

        getBinding().resultsListRecycler.apply {
            (this.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
            this.addItemDecoration(divider)
            this.layoutManager = layoutManager
            this.adapter = getRecyclerAdapter()
            this.addOnScrollListener(paginationListener)
        }
    }

    fun setShowPlaceholder(value: Boolean) {
        getBinding().emptyMessageContainer.setVisible(value)
    }

    fun showProgress() {
        getBinding().progressBar.visible()
    }

    fun hideProgress() {
        getBinding().progressBar.gone()
    }

    fun showSnackBar(@StringRes res: Int?) {
        if (res == null) {
            return
        }

        messageSnackBar = NSnackbar.with(act)
            .typeSuccess()
            .marginBottom(SNACK_BAR_MARGIN)
            .text(getString(res))
            .durationLong()
            .show()
    }

    fun showSuccessMessage(@StringRes text: Int?) {
        text?.let {
            NToast.with(act)
                .text(getString(text))
                .typeSuccess()
                .show()
        }
    }

    fun showErrorMessage(text: String) {
        NToast.with(act)
            .text(text)
            .typeError()
            .show()
    }

    fun showErrorMessage(@StringRes text: Int?) {
        text?.let {
            NToast.with(act)
                .text(getString(text))
                .typeError()
                .show()
        }
    }

    protected fun showClearRecentTimerSnackBar(
        @StringRes message: Int?,
        delaySec: Int,
        undoCallBack: () -> Unit
    ) {
        if (message == null) {
            return
        }

        undoSnackBar?.dismissNoCallbacks()
        undoSnackBar = NSnackbar.with(view)
            .inView(view)
            .text(context.stringNullable(message))
            .description(context.stringNullable(R.string.touch_to_delete))
            .durationIndefinite()
            .button(context.stringNullable(R.string.general_cancel))
            .dismissManualListener { undoCallBack() }
            .timer(delaySec)
            .marginBottom(SNACK_BAR_MARGIN_BOTTOM_DP.dp)
            .show()
    }

    override fun onResume() {
        super.onResume()

        subscribedViewModel?.let {
            addRxStream(it)
        }
    }

    override fun onPause() {
        super.onPause()

        undoClearRecent()
        clearRxStream()
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable.dispose()
    }

    protected fun <Type> CompositeDisposable.add(
        observable: Observable<Type>,
        callback: (Type) -> Unit
    ) {
        add(observable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            callback(it)
        })
    }

    private fun handleAnalyticLogInputSearch(items: List<SearchItem>, needToLogSearch: Boolean) {
        if (!needToLogSearch) return
        var searchType = AmplitudePropertySearchType.PEOPLE
        when(this){
            is SearchUserFragment -> searchType = AmplitudePropertySearchType.PEOPLE
            is SearchGroupFragment -> searchType = AmplitudePropertySearchType.COMMUNITY
            is SearchHashTagFragment -> searchType = AmplitudePropertySearchType.HASHTAG
        }

        if(items.size > 1){
            analyticLogInputSearch(searchType, AmplitudePropertyHaveResult.YES)
        } else {
            analyticLogInputSearch(searchType, AmplitudePropertyHaveResult.NO)
        }
    }
}

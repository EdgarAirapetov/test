package com.numplates.nomera3.modules.search.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.search.ui.adapter.result.meeraSearchHashTagShimmerAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.meeraSearchResultHashTagItemAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.meeraSearchResultTitleAdapterDelegate
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.HashTagSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.hashtag.SearchHashTagDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.hashtag.SearchHashTagResultViewModel
import com.numplates.nomera3.presentation.router.IArgContainer

private const val SHIMMER_COUNT = 4

class MeeraSearchHashTagFragment : MeeraSearchBaseScreenFragment() {

    private val resultScreenViewModel by viewModels<SearchHashTagResultViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val defaultScreenViewModel by viewModels<SearchHashTagDefaultViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var delegateAdapter: ListDelegationAdapter<List<SearchItem>>

    override fun getRecyclerAdapter(): ListDelegationAdapter<List<SearchItem>> {
        return delegateAdapter
    }

    override fun getResultScreenViewModel(): SearchResultScreenBaseViewModel {
        return resultScreenViewModel
    }

    override fun getDefaultScreenViewModel(): SearchDefaultScreenBaseViewModel {
        return defaultScreenViewModel
    }

    override fun exitScreen() {
        defaultScreenViewModel.clearRecentGlobalIfExists()
    }

    override fun getFragmentLifecycle(): Lifecycle = lifecycle

    override fun isShowResultPlaceholder(inputData: List<SearchItem>): Boolean {
        val hasSearchItems = inputData.any { recyclerItem ->
            recyclerItem is SearchItem.HashTag
        }

        return hasSearchItems.not()
    }

    override fun showShimmer() {
        val items = mutableListOf<SearchItem>()
        items.add(SearchItem.Title(R.string.search_result_list_title))
        repeat(SHIMMER_COUNT) {
            items.add(SearchItem.HashtagShimmer)
        }
        applyDataToRecycler(items)
    }

    override fun isShowDefaultPlaceholder(inputData: List<SearchItem>): Boolean {
        val hasRecentItems = inputData.any { recyclerItem ->
            recyclerItem is SearchItem.HashTag
        }

        return hasRecentItems.not()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        delegateAdapter = ListDelegationAdapter(
            meeraSearchResultTitleAdapterDelegate {
                context?.hideKeyboard(requireView())
                defaultScreenViewModel.clearRecent()
            },
            meeraSearchHashTagShimmerAdapterDelegate(),
            meeraSearchResultHashTagItemAdapterDelegate { hashTagItem ->
                when (getScreenState()) {
                    SearchScreenContext.ScreenState.Default -> {
                        defaultScreenViewModel.selectHashTagItem(hashTagItem)
                    }
                    SearchScreenContext.ScreenState.Result -> {
                        resultScreenViewModel.selectHashTagItem(hashTagItem)
                    }
                    else -> {}
                }
            }
        )
    }

    override fun handleEvent(event: SearchBaseViewEvent) {
        super.handleEvent(event)

        when (event) {
            is HashTagSearchViewEvent.OpenHashTag -> {
                resultScreenViewModel.logHashTagPressed()
                openHashtag(event.item.name)
            }
        }
    }

    private fun openHashtag(hashtag: String) {
        hideMessages()
        findNavController().safeNavigate(
            R.id.action_meeraSearchMainFragment_to_meeraHashTagFragment,
            bundleOf(IArgContainer.ARG_HASHTAG to hashtag)
        )
    }

    override fun analyticLogInputSearch(searchType: AmplitudePropertySearchType,
                                        haveResult: AmplitudePropertyHaveResult
    ) {
        resultScreenViewModel.amplitudeHelper.logSearchInput(
            type = searchType,
            haveResult = haveResult,
            whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.NONE
        )
    }

}

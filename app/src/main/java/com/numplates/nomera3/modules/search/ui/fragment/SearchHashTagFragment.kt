package com.numplates.nomera3.modules.search.ui.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.extensions.hideKeyboard
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.search.ui.adapter.result.searchResultHashTagItemAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.searchResultTitleAdapterDelegate
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.HashTagSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.hashtag.SearchHashTagDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.hashtag.SearchHashTagResultViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_HASHTAG

class SearchHashTagFragment : SearchBaseScreenFragment() {

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

    override fun isShowDefaultPlaceholder(inputData: List<SearchItem>): Boolean {
        val hasRecentItems = inputData.any { recyclerItem ->
            recyclerItem is SearchItem.HashTag
        }

        return hasRecentItems.not()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        delegateAdapter = ListDelegationAdapter(
            searchResultTitleAdapterDelegate {
                context?.hideKeyboard(requireView())
                defaultScreenViewModel.clearRecent()
            },
            searchResultHashTagItemAdapterDelegate { hashTagItem ->
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

        checkAppRedesigned(
            isRedesigned = {
            },
            isNotRedesigned = {
                act.addFragment(
                    HashtagFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(ARG_HASHTAG, hashtag)
                )
            }
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

package com.numplates.nomera3.modules.search.ui.fragment

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.extensions.hideKeyboard
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.search.ui.adapter.recent.searchRecentBlockAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.searchResultGroupItemAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.searchResultTitleAdapterDelegate
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.GroupSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.group.SearchGroupDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.group.SearchGroupResultViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID

class SearchGroupFragment : SearchBaseScreenFragment() {

    private val resultScreenViewModel by viewModels<SearchGroupResultViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val defaultScreenViewModel by viewModels<SearchGroupDefaultViewModel>(
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
        val hasSearchItems = inputData.any { recyclerItem -> recyclerItem is SearchItem.Group }

        return hasSearchItems.not()
    }

    override fun isShowDefaultPlaceholder(inputData: List<SearchItem>): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        delegateAdapter = ListDelegationAdapter(
            searchRecentBlockAdapterDelegate(
                { recentGroupItem ->
                    onRecentClick(recentGroupItem)
                },
                {
                    context?.hideKeyboard(requireView())
                    defaultScreenViewModel.clearRecent()
                }
            ),
            searchResultTitleAdapterDelegate(),
            searchResultGroupItemAdapterDelegate(resultScreenViewModel::selectGroupItem) { group ->
                context?.hideKeyboard(requireView())
                resultScreenViewModel.subscribeGroup(group)
            }
        )
    }

    override fun handleEvent(event: SearchBaseViewEvent) {
        super.handleEvent(event)

        when (event) {
            is GroupSearchViewEvent.SelectGroup -> {
                openGroup(event.groupId)
            }
        }
    }

    private fun openGroup(groupId: Int) {
        hideMessages()

        add(
            CommunityRoadFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_GROUP_ID, groupId)
        )
    }

    private fun onRecentClick(item: SearchItem.RecentBlock.RecentBaseItem) {
        if (item is SearchItem.RecentBlock.RecentBaseItem.RecentGroup) {
            defaultScreenViewModel.selectRecentItem(item)
        }
    }

    override fun analyticLogInputSearch(searchType: AmplitudePropertySearchType,
                                        haveResult: AmplitudePropertyHaveResult
    ) {
        resultScreenViewModel.amplitudeHelper.logSearchInput(
            type = searchType,
            haveResult = haveResult,
            whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.FEED
        )
    }

}

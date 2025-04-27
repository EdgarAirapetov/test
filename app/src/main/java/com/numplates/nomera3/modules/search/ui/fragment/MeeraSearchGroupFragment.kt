package com.numplates.nomera3.modules.search.ui.fragment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.search.ui.adapter.recent.meeraSearchRecentBlockAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.meeraSearchGroupShimmerAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.meeraSearchResultGroupItemAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.meeraSearchResultTitleAdapterDelegate
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.GroupSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.group.SearchGroupDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.group.SearchGroupResultViewModel
import com.numplates.nomera3.presentation.router.IArgContainer

private const val SHIMMER_COUNT = 10

class MeeraSearchGroupFragment : MeeraSearchBaseScreenFragment() {

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

    override fun showShimmer() {
        val items = mutableListOf<SearchItem>()
        items.add(SearchItem.Title(R.string.search_result_list_title))
        repeat(SHIMMER_COUNT) {
            items.add(SearchItem.GroupShimmer)
        }
        applyDataToRecycler(items)
    }

    override fun isShowDefaultPlaceholder(inputData: List<SearchItem>): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        delegateAdapter = ListDelegationAdapter(
            meeraSearchRecentBlockAdapterDelegate(
                { recentGroupItem ->
                    onRecentClick(recentGroupItem)
                },
                {
                    context?.hideKeyboard(requireView())
                    defaultScreenViewModel.clearRecent()
                }
            ),
            meeraSearchResultTitleAdapterDelegate(),
            meeraSearchGroupShimmerAdapterDelegate(),
            meeraSearchResultGroupItemAdapterDelegate(resultScreenViewModel::selectGroupItem) { group ->
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
        findNavController().safeNavigate(
            resId = R.id.action_meeraSearchMainFragment_to_communities_graph,
        )
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunitiesListsContainerFragment_to_meeraCommunityRoadFragment,
            bundle = bundleOf(IArgContainer.ARG_GROUP_ID to groupId),
            navBuilder = { builder: NavOptions.Builder ->
                builder.setPopUpTo(
                    destinationId = R.id.meeraCommunitiesListsContainerFragment,
                    inclusive = true,
                    saveState = true
                )
            }
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


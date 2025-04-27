package com.numplates.nomera3.modules.search.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.extensions.hideKeyboard
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.search.ui.adapter.recent.searchRecentBlockAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.searchResultTitleAdapterDelegate
import com.numplates.nomera3.modules.search.ui.adapter.result.searchResultUserItemAdapterDelegate
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.UserSearchViewEvent
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserDefaultViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserResultViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment

@Deprecated("use SearchUserFragmentNew")
class SearchUserFragment : SearchBaseScreenFragment() {

    private val resultScreenViewModel by viewModels<SearchUserResultViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val defaultScreenViewModel by viewModels<SearchUserDefaultViewModel>(
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
        val hasSearchItems = inputData.any { recyclerItem -> recyclerItem is SearchItem.User }

        return hasSearchItems.not()
    }

    override fun isShowDefaultPlaceholder(inputData: List<SearchItem>): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        delegateAdapter = ListDelegationAdapter(
            searchResultTitleAdapterDelegate(),
            searchRecentBlockAdapterDelegate(
                { recentItem ->
                    onRecentClick(recentItem)
                },
                {
                    context?.hideKeyboard(requireView())
                    defaultScreenViewModel.clearRecent()
                }
            ),
            searchResultUserItemAdapterDelegate(
                resultScreenViewModel::selectUserItem,
                resultScreenViewModel::openUserAddDialog,
                resultScreenViewModel::openUserMoments
            )
        )
    }

    override fun handleEvent(event: SearchBaseViewEvent) {
        super.handleEvent(event)

        when (event) {
            is UserSearchViewEvent.AddUser -> Unit
            is UserSearchViewEvent.SelectUser -> {
                openUser(
                    userUid = event.userId,
                    isRecent = event.isRecent
                )
            }

            is UserSearchViewEvent.OpenUserMoments -> {
                openUserMoments(
                    event.userId,
                    event.view,
                    event.hasNewMoments
                )
            }
        }
    }

    private fun openUser(
        userUid: Long,
        isRecent: Boolean
    ) {

        val where = if (isRecent) {
            AmplitudePropertyWhere.YOU_VISITED
        } else {
            AmplitudePropertyWhere.SEARCH
        }

        hideMessages()
        act.addFragment(
            UserInfoFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_USER_ID, userUid),
            Arg(ARG_TRANSIT_FROM, where.property)
        )
    }

    private fun openUserMoments(
        userUid: Long,
        view: View?,
        hasNewMoments:Boolean
    ) {
        hideMessages()
        if ((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false) {
            return
        }
        act.openUserMoments(
            userId = userUid,
            fromView = view,
            openedWhere = AmplitudePropertyMomentScreenOpenWhere.SEARCH,
            viewedEarly = !hasNewMoments
        )
    }

    private fun onRecentClick(item: SearchItem.RecentBlock.RecentBaseItem) {
        if (item is SearchItem.RecentBlock.RecentBaseItem.RecentUser) {
            defaultScreenViewModel.selectRecentItem(item)
        }
    }

    override fun analyticLogInputSearch(
        searchType: AmplitudePropertySearchType,
        haveResult: AmplitudePropertyHaveResult
    ) {
        resultScreenViewModel.amplitudeHelper.logSearchInput(
            type = searchType,
            haveResult = haveResult,
            whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.NONE
        )
    }
}

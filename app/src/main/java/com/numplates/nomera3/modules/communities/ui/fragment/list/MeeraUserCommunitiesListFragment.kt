package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.showCommonSuccessMessage
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.ui.adapter.MeeraUserCommunityListAdapter
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.MeeraCommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityChanges
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoaded
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingProgress
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.UserCommunitiesListViewModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.INPUT_TIMEOUT
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private const val HEADER_LIST_POSITION = 1

/*
* Фрагмент со списком "Мои сообщества", находится внутри экрана CommunitiesListsContainerFragment (внутренний ViewPager)
* */
class MeeraUserCommunitiesListFragment : MeeraCommunitiesListFragmentBase() {

    var openAllCommunitiesList: (() -> Unit)? = null

    private val viewModel by viewModels<UserCommunitiesListViewModel>()
    private val swipeRefreshLayout: SwipeRefreshLayout?
        get() = binding.srRefreshLayoutGroupList

    private val userCommunityRecyclerView: RecyclerView?
        get() = binding.ldlListRecycler

    private var adapter: MeeraUserCommunityListAdapter? = null
    private val shimmerAdapter = MeeraCommunitiesListShimmerAdapter()
    private val listShimmer = List(4) { String.empty() }
    private var undoSnackBar: NSnackbar? = null
    private var searchListEmptyState = false
    private val myCommunityList = mutableSetOf<CommunityListUIModel>()
    private var isSearchActive = false
    private var changeVisibilitySearchField: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initLiveDataObserver()
        initCommunityListLoader()
        initRefreshLayoutListener()
        loadNextData()
    }

    override fun refreshUserGroupsList() {
        onRefreshUserCommunityList()
    }

    fun onRefreshUserCommunityList() {
        if (undoSnackBar?.isVisible == true) {
            undoSnackBar?.dismiss()
        }
        if (!isSearchActive) {
            viewModel.resetUserCommunityListLoader()
        }
        binding.srRefreshLayoutGroupList.isRefreshing = false
    }

    fun refreshList() {
        onRefreshUserCommunityList()
    }

    fun updateVisibilitySearchField(changeVisibilitySearchField: () -> Unit) {
        this.changeVisibilitySearchField = changeVisibilitySearchField
    }

    fun getCountCommunityListSize() = adapter?.itemCount

    @SuppressLint("CheckResult")
    fun searchUserGroup(searchGroupName: String) {
        if (searchGroupName.isEmpty()) {
            isSearchActive = false
            refreshList()
            return
        }
        Observable.just(searchGroupName)
            .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { groupName ->
                isSearchActive = true
                val groupList = mutableListOf<CommunityListUIModel>()
                val resultList = myCommunityList.filter {
                    when (it) {
                        is CommunityListUIModel.Community -> {
                            it.community.name.lowercase().contains(groupName.lowercase())
                        }

                        else -> false
                    }
                }
                resultList?.let {
                    groupList.add(CommunityListUIModel.CommunityListTitle(getString(R.string.search_result_list_title)))
                    groupList.addAll(it)
                }
                searchListEmptyState = groupList.size == HEADER_LIST_POSITION
                if (searchListEmptyState) showPlaceholder()
                adapter?.submitList(groupList)
            }
    }

    private fun loadNextData() {
        viewModel.loadUserCommunityListNext()
    }

    private fun initAdapter() {
        adapter = MeeraUserCommunityListAdapter()

        adapter?.itemClickListener = { selectedUserCommunityListItem: CommunityListItemUIModel? ->
            selectedUserCommunityListItem?.id?.also { communityId: Int ->
                openCommunityFragment(communityId)
            }
        }

        binding.shimmerListGroups.rvListGroupsShimmer.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)
        }

        userCommunityRecyclerView?.also { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            addRecyclerViewPagingHelper(recyclerView)
        }
    }

    private fun openCommunityFragment(id: Int) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunitiesListsContainerFragment_to_meeraCommunityRoadFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, id)
            }
        )
        MeeraCommunityRoadFragment().apply {
            setSubscriptionCallback(object : MeeraCommunityRoadFragment.SubscriptionCallback {
                override fun onCommunitySubscribed(subscribed: Boolean, groupId: Int) {
                    refreshListsCallback?.invoke()
                }
            })
            setEditCallback(object : MeeraCommunityRoadFragment.EditCallback {
                override fun onCommunityEdited() {
                    refreshListsCallback?.invoke()
                }
            })
        }

        viewModel.amplitudeHelper.logCommunityScreenOpened(
            AmplitudePropertyWhereCommunityOpen.OWN_COMMUNITY
        )
    }

    private fun addRecyclerViewPagingHelper(recyclerView: RecyclerView) {
        RecyclerViewPaginator(
            recyclerView = recyclerView,
            onLast = viewModel::isListEndReached,
            isLoading = viewModel::isLoading,
            loadMore = { loadNextData() },
        ).apply {
            endWithAuto = true
        }
    }

    private fun initRefreshLayoutListener() {
        swipeRefreshLayout?.setOnRefreshListener(
            this::onRefreshUserCommunityList
        )
    }

    private fun initCommunityListLoader() {
        viewModel.initUserCommunityListLoader()
    }

    private fun removeCommunityListItem(communityId: Long) {
        myCommunityList.removeIf {
            if (it is CommunityListUIModel.Community) {
                return@removeIf it.community.id?.toLong() == communityId
            } else {
                return@removeIf false
            }
        }
        adapter?.removeItem(communityId, requireContext())
        if (adapter?.isEmptyOrLastItemTitle == true) {
            showPlaceholder()
        }
    }

    private fun restoreRemovedCommunity() {
        hidePlaceholder()
        adapter?.restoreLastRemovedItem(requireContext())
    }

    private fun initLiveDataObserver() {
        viewModel.event.observe(viewLifecycleOwner) { newEvent: CommunityListEvent ->
            when (newEvent) {
                is CommunityListLoaded -> {
                    initCommunities(newEvent)
                }

                is CommunityListLoadingFailed -> showAlertNToastAtScreenTop(R.string.user_community_list_loading_failed)
                is CommunityListLoadingProgress -> {
                    if (newEvent.inProgress) {
                        binding.ldlListRecycler.gone()
                        binding.phMeeraSearchEmptyGroups.llEmptyListContainer.gone()
                        binding.shimmerListGroups.root.visible()
                    } else {
                        binding.shimmerListGroups.root.gone()
                        binding.ldlListRecycler.visible()
                    }
                }

                is CommunityChanges -> handleCommunityChangesEvent(newEvent)
                else -> {}
            }
        }
    }

    private fun handleCommunityChangesEvent(event: CommunityChanges) {
        when (event.communityListEvents) {
            is CommunityListEvents.StartDeletion ->
                removeCommunityListItem(event.communityListEvents.communityId)

            is CommunityListEvents.DeleteSuccess ->

                handleSuccessDelete()

            is CommunityListEvents.CancelDeletion ->
                restoreRemovedCommunity()

            else -> Unit
        }
    }

    private fun handleSuccessDelete() {
        if (adapter?.isEmptyOrLastItemTitle == true) {
            showPlaceholder()
        } else {
            refreshUserGroupsList()
        }
    }

    private fun initCommunities(communities: CommunityListLoaded) {
        val isNewList = communities.isNewList
        val quantity = communities.totalCount
        val list = communities.uiModelList
        val models = mutableListOf<CommunityListUIModel>()
        if (isNewList == true) {
            quantity?.let {
                models.add(
                    CommunityListUIModel.CommunityListTitle(
                        requireContext().pluralString(R.plurals.communities_plural, quantity)
                    )
                )
            }
        }
        list.forEach {
            models.add(CommunityListUIModel.Community(it))
        }
        if (list.isNotEmpty()) {
            hidePlaceholder()
            changeVisibilitySearchField?.invoke()
            myCommunityList.addAll(models)
            adapter?.submitList(models)
        } else {
            showPlaceholder()
        }
    }

    private fun showPlaceholder() {
        binding.ldlListRecycler.gone()
        if (searchListEmptyState) {
            binding.shimmerListGroups.shListFragment.gone()
            binding.phMeeraSearchEmptyGroups.llEmptyListContainer.visible()
        } else {
            binding.phMeeraNoGroups.root.visible()
            binding.phMeeraNoGroups.vSearchGroupBtn.setThrottledClickListener {
                openAllCommunitiesList?.invoke()
            }
        }
    }

    private fun hidePlaceholder() {
        binding.ldlListRecycler.visible()
        if (searchListEmptyState) {
            binding.phMeeraSearchEmptyGroups.llEmptyListContainer.gone()
        } else {
            binding.phMeeraNoGroups.root.gone()
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        showCommonSuccessMessage(getText(stringRes), requireView())
    }
}

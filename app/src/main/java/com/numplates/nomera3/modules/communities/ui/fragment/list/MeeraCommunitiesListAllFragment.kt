package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.communities.ui.adapter.MeeraAllCommunityListAdapter
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityTransitFrom
import com.numplates.nomera3.modules.communities.ui.fragment.MeeraCommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunitiesListViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunitiesSearchViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingProgress
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_COMMUNITY_FROM
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.INPUT_TIMEOUT
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Вкладка "Все Сообщества" на экране "Сообщества"
 * */
class MeeraCommunitiesListAllFragment : MeeraCommunitiesListFragmentBase() {

    private val viewModel by viewModels<CommunitiesListViewModel>()
    private val searchViewModel by viewModels<CommunitiesSearchViewModel>()
    private val shimmerAdapter = MeeraCommunitiesListShimmerAdapter()
    private val listShimmer = List(4) { String.empty() }
    private var adapter: MeeraAllCommunityListAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var isSearchActive = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.srRefreshLayoutGroupList?.setOnRefreshListener {
            if (!isSearchActive) refreshList()
            binding.srRefreshLayoutGroupList.isRefreshing = false
        }

        initAdapter()
        initObservers()
        refreshList()
    }

    override fun setCommunitySubscriptionStatus(position: Int, subscribed: Boolean) {
        adapter?.setSubscribed(position, subscribed)
    }

    override fun refreshAllGroupsList() {
        refreshList()
    }

    fun refreshList() {
        adapter?.clearItemList()
        viewModel.onRefresh()
        isSearchActive = false
    }

    fun logAmplitudeCreateCommunityTap() {
        viewModel.amplitudeHelper.logCommunityCreateMenuOpen()
    }

    @SuppressLint("CheckResult")
    fun searchAllCommunities(searchGroupName: String) {
        Observable.just(searchGroupName)
            .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { groupName ->
                if (groupName.isEmpty()) {
                    isSearchActive = false
                    refreshList()
                } else {
                    isSearchActive = true
                    searchViewModel.setQuery(groupName)
                    binding.ldlListRecycler.gone()
                    binding.phMeeraSearchEmptyGroups.llEmptyListContainer.gone()
                    binding.shimmerListGroups.root.visible()
                }
            }
    }

    private fun initObservers() {
        viewModel.event.observe(viewLifecycleOwner) { newEvent: CommunityListEvent ->
            when (newEvent) {
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

                is CommunityListEvent.CommunityListLoaded -> {
                    binding?.srRefreshLayoutGroupList?.isRefreshing = false
                    if (!isSearchActive) initCommunities(newEvent)
                }

                is CommunityListEvent.CommunityListLoadingFailed -> {
                    binding?.srRefreshLayoutGroupList?.isRefreshing = false
                    showAlertNToastAtScreenTop(R.string.user_community_list_loading_failed)
                }

                else -> Unit
            }
        }
        searchViewModel.liveSearchGroup.observe(viewLifecycleOwner) { list ->
            if (isSearchActive) {
                if (list.isNotEmpty()) {
                    val models = mapCommunityListUIModelToCommunityListUIModelCommunity(list)
                    adapter?.replace(models)
                    binding.phMeeraSearchEmptyGroups.llEmptyListContainer.gone()
                    binding.ldlListRecycler.visible()
                } else {
                    binding.ldlListRecycler.gone()
                    binding.phMeeraSearchEmptyGroups.llEmptyListContainer.visible()
                }
                binding.shimmerListGroups.shListFragment.gone()
            }
        }
    }

    private fun mapCommunityListUIModelToCommunityListUIModelCommunity(
        list: List<CommunityListItemUIModel>
    ): MutableList<CommunityListUIModel> {
        val models = mutableListOf<CommunityListUIModel>()
        models.add(CommunityListUIModel.CommunityListTitle(getString(R.string.search_result_list_title)))
        list.forEach { item ->
            models.add(CommunityListUIModel.Community(item))
        }
        return models
    }

    private fun initCommunities(communities: CommunityListEvent.CommunityListLoaded) {
        val isNewList = communities.isNewList
        val list = communities.uiModelList
        val models = mutableListOf<CommunityListUIModel>()
        if (isNewList == true) {
            models.add(
                CommunityListUIModel.CommunityListTitle(getString(R.string.general_recommendations))
            )
        }
        list.forEach {
            models.add(CommunityListUIModel.Community(it))
        }
        if (list.isNotEmpty()) {
            binding.phMeeraSearchEmptyGroups.root.gone()
            binding.ldlListRecycler.visible()
            adapter?.addItemList(models)
        } else {
            binding.ldlListRecycler.gone()
            binding.phMeeraSearchEmptyGroups.root.visible()
        }
        binding.shimmerListGroups.shListFragment.gone()
    }

    private fun initAdapter() {
        adapter = MeeraAllCommunityListAdapter()
        layoutManager = LinearLayoutManager(context)

        binding.shimmerListGroups.rvListGroupsShimmer.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)
        }

        binding.ldlListRecycler.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
            RecyclerViewPaginator(
                recyclerView = it,
                onLast = viewModel::isListEndReached,
                isLoading = viewModel::isLoading,
                loadMore = { viewModel.loadNext() },
            ).apply {
                endWithAuto = true
            }
        }

        adapter?.itemClickListener = {
            findNavController().safeNavigate(
                resId = R.id.action_meeraCommunitiesListsContainerFragment_to_meeraCommunityRoadFragment,
                bundle = Bundle().apply {
                    putInt(ARG_GROUP_ID, it?.id ?: 0)
                    putInt(ARG_TRANSIT_COMMUNITY_FROM, CommunityTransitFrom.ALL_COMMUNITY.key)
                }
            )
            MeeraCommunityRoadFragment().apply {
                setSubscriptionCallback(object : MeeraCommunityRoadFragment.SubscriptionCallback {
                    override fun onCommunitySubscribed(subscribed: Boolean, groupId: Int) {
                        adapter?.let {
                            it.getIndexByGroupId(groupId)?.let { position ->
                                it.setSubscribed(position, subscribed)
                            }
                        }
                        refreshListsCallback?.invoke()
                    }
                })
            }

            viewModel.amplitudeHelper.logCommunityScreenOpened(
                AmplitudePropertyWhereCommunityOpen.ALL_COMMUNITY
            )
        }
        adapter?.subscriptionClickListener = { model, position ->
            subscribeGroup(model, position)
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        showCommonError(getText(stringRes), requireView())
    }
}

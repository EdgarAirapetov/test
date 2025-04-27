package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.communities.ui.adapter.UserCommunityListAdapter
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityTransitFrom
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunitiesListViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_COMMUNITY_FROM
import com.numplates.nomera3.presentation.view.utils.NToast
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator


/**
 * Вкладка "Все Сообщества" на экране "Сообщества"
 * */
class CommunitiesListAllFragment : CommunitiesListFragmentBase() {

    private val viewModel by viewModels<CommunitiesListViewModel>()

    private lateinit var adapter: UserCommunityListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.srRefreshLayoutGroupList?.setOnRefreshListener {
            refreshList()
        }

        initAdapter()
        initObservers()
        refreshList()
    }

    override fun setCommunitySubscriptionStatus(position: Int, subscribed: Boolean) {
        adapter.setSubscribed(position, subscribed)
    }

    override fun refreshAllGroupsList() {
        refreshList()
    }

    fun refreshList() {
        adapter.clearItemList()
        viewModel.onRefresh()
    }

    fun updateScreen() {
        binding?.ldlListRecycler?.scrollToPosition(0)
        viewModel.onRefresh()
    }

    fun logAmplitudeCreateCommunityTap() {
        viewModel.amplitudeHelper.logCommunityCreateMenuOpen()
    }

    private fun initObservers() {
        viewModel.event.observe(viewLifecycleOwner) { newEvent: CommunityListEvent ->
            when (newEvent) {
                is CommunityListEvent.CommunityListLoaded -> {
                    binding?.srRefreshLayoutGroupList?.isRefreshing = false
                    initCommunities(newEvent)
                }
                is CommunityListEvent.CommunityListLoadingFailed -> {
                    binding?.srRefreshLayoutGroupList?.isRefreshing = false
                    showAlertNToastAtScreenTop(R.string.user_community_list_loading_failed)
                }
                is CommunityListEvent.CommunityListLoadingProgress ->
                    showProgress(newEvent.inProgress)
                else -> {}
            }
        }
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
            binding?.phNoGroups?.root?.gone()
            binding?.ldlListRecycler?.visible()
            adapter.addItemList(models)
        } else {
            binding?.ldlListRecycler?.gone()
            binding?.phNoGroups?.root?.visible()
        }
    }

    private fun initAdapter() {
        adapter = UserCommunityListAdapter()
        layoutManager = LinearLayoutManager(context)

        binding?.ldlListRecycler?.also {
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

        adapter.itemClickListener = {
            val fragment = CommunityRoadFragment().apply {
                setSubscriptionCallback(object : CommunityRoadFragment.SubscriptionCallback {
                    override fun onCommunitySubscribed(subscribed: Boolean, groupId: Int) {
                        adapter.getIndexByGroupId(groupId)?.let { position ->
                            adapter.setSubscribed(position, subscribed)
                        }
                        refreshListsCallback?.invoke()
                    }
                })
            }
            add(fragment, Act.LIGHT_STATUSBAR, Arg(ARG_GROUP_ID, it?.id),
                Arg(ARG_TRANSIT_COMMUNITY_FROM, CommunityTransitFrom.ALL_COMMUNITY.key)
            )

            viewModel.amplitudeHelper.logCommunityScreenOpened(
                AmplitudePropertyWhereCommunityOpen.ALL_COMMUNITY
            )
        }
        adapter.subscriptionClickListener = { model, position ->
            subscribeGroup(model, position)
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        NToast.with(view)
            .text(getString(stringRes))
            .typeAlert()
            .show()

    }

}

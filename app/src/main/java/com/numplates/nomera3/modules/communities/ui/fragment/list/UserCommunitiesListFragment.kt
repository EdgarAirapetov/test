package com.numplates.nomera3.modules.communities.ui.fragment.list

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.ui.adapter.UserCommunityListAdapter
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityChanges
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoaded
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingProgress
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.UserCommunitiesListViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.view.utils.NToast
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator


/*
* Фрагмент со списком "Мои сообщества", находится внутри экрана CommunitiesListsContainerFragment (внутренний ViewPager)
* */
class UserCommunitiesListFragment : CommunitiesListFragmentBase() {

    var openAllCommunitiesList: (() -> Unit)? = null

    private val viewModel by viewModels<UserCommunitiesListViewModel>()

    private val swipeRefreshLayout: SwipeRefreshLayout?
        get() = binding?.srRefreshLayoutGroupList

    private val userCommunityRecyclerView: RecyclerView?
        get() = binding?.ldlListRecycler

    private var adapter: UserCommunityListAdapter? = null

    private var undoSnackBar: NSnackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initLiveDataObserver()
        initPlaceHolder()
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
        binding?.srRefreshLayoutGroupList?.isRefreshing = false
        adapter?.clearItemList()
        viewModel.resetUserCommunityListLoader()
    }

    /*
    * Родительский экран "Сообщества" обновляет список моих
    * групп через этот метод
    * */
    fun updateScreen() {
        onRefreshUserCommunityList()
    }

    fun refreshList() {
        onRefreshUserCommunityList()
    }

    private fun loadNextData() {
        viewModel.loadUserCommunityListNext()
    }

    private fun initAdapter() {
        adapter = UserCommunityListAdapter()

        adapter?.itemClickListener = { selectedUserCommunityListItem: CommunityListItemUIModel? ->
            selectedUserCommunityListItem?.id?.also { communityId: Int ->
                openCommunityFragment(communityId)
            }
        }

        adapter?.onEmptyListListener = {
            binding?.ldlListRecycler?.gone()
            binding?.phNoGroups?.root?.visible()
        }

        userCommunityRecyclerView?.also { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            addRecyclerViewPagingHelper(recyclerView)
        }
    }

    private fun openCommunityFragment(id: Int) {
        add(CommunityRoadFragment().apply {
            setSubscriptionCallback(object : CommunityRoadFragment.SubscriptionCallback {
                override fun onCommunitySubscribed(subscribed: Boolean, groupId: Int) {
                    refreshListsCallback?.invoke()
                }
            })
            setEditCallback(object : CommunityRoadFragment.EditCallback {
                override fun onCommunityEdited() {
                    refreshListsCallback?.invoke()
                }
            })
        }, Act.LIGHT_STATUSBAR, Arg(ARG_GROUP_ID, id))

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
        swipeRefreshLayout?.setOnRefreshListener(this::onRefreshUserCommunityList)
    }

    private fun initCommunityListLoader() {
        viewModel.initUserCommunityListLoader()
    }

    private fun removeCommunityListItem(communityId: Long) {
        adapter?.removeItem(communityId, requireContext())
        if (adapter?.isEmptyOrLastItemTitle == true) {
            adapter?.clearItemList()
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
                is CommunityListLoaded -> initCommunities(newEvent)
                is CommunityListLoadingFailed -> showAlertNToastAtScreenTop(R.string.user_community_list_loading_failed)
                is CommunityListLoadingProgress -> showProgress(newEvent.inProgress)
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
                        requireContext().pluralString(R.plurals.communities_plural, quantity))
                )
            }
        }
        list.forEach {
            models.add(CommunityListUIModel.Community(it))
        }
        if (list.isNotEmpty()) {
            binding?.phNoGroups?.root?.gone()
            binding?.ldlListRecycler?.visible()
            adapter?.addItemList(models)
        } else {
            binding?.ldlListRecycler?.gone()
            binding?.phNoGroups?.root?.visible()
        }
    }

    private fun showPlaceholder() {
        binding?.ldlListRecycler?.gone()
        binding?.phNoGroups?.root?.visible()
    }

    private fun hidePlaceholder() {
        binding?.phNoGroups?.root?.gone()
        binding?.ldlListRecycler?.visible()
    }

    private fun initPlaceHolder() {
        binding?.phNoGroups?.apply {
            tvEmptyList.text = getString(R.string.create_group_placeholder)
            tvEmptyList.gravity = Gravity.CENTER
            ivEmptyList.loadGlide(R.drawable.ic_my_community_list_empty)
            tvButtonEmptyList.text = getString(R.string.find_community)
            tvButtonEmptyList.click {
                openAllCommunitiesList?.invoke()
            }
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        NToast.with(view)
            .text(getString(stringRes))
            .typeAlert()
            .show()

    }
}

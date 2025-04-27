package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMyFriendListFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PAGER_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendsListAction
import com.numplates.nomera3.presentation.view.adapter.newfriends.MeeraFriendsListAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraCellShimmerAdapter
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.viewmodel.MeeraMyFriendListViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val COUNT_SHIMMER_ITEM = 8

class MeeraMyFriendListFragment : MeeraBaseFragment(
    layout = R.layout.meera_my_friend_list_fragment,
), MeeraFriendsHostCallbacks, MeeraPullToRefreshLayout.OnRefreshListener {


    private val binding by viewBinding(MeeraMyFriendListFragmentBinding::bind)

    private val viewModel by viewModels<MeeraMyFriendListViewModel>() {
        App.component.getViewModelFactory()
    }

    private var userID: Long? = null

    private var adapter: MeeraFriendsListAdapter? = null
    private val shimmerAdapter = MeeraCellShimmerAdapter()
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }

    var friendInteractor: IOnMyFriendsInteractor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            args.get(ARG_USER_ID)?.let {
                userID = it as Long
            } ?: kotlin.run {
                userID = viewModel.getUserUid()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initPagination()
        initListeners()
        initObservers()
        initSwipeRefresh()
        viewModel.init(userID)
    }

    private fun initRecycler() {
        binding.apply {
            rvMyFriendsList.layoutManager = LinearLayoutManager(context)
            adapter = MeeraFriendsListAdapter(::initFriendClickListener)
            rvMyFriendsList.adapter = adapter

            rvShimmerFriendsList.layoutManager = LinearLayoutManager(context)
            rvShimmerFriendsList.adapter = shimmerAdapter
            shimmerAdapter.submitList(listShimmer)

            rvShimmerFriendsList.visible()
            rvMyFriendsList.gone()
        }
    }


    private fun initPagination() {
        RecyclerViewPaginator(
            recyclerView = binding.rvMyFriendsList,
            onLast = viewModel::isLastFriend,
            isLoading = viewModel::isLoadingFriend,
            loadMore = { viewModel.loadMoreData(offset = adapter?.itemCount ?: 0) })
    }

    private fun initSwipeRefresh() {
        binding.srMyFriendsList.apply {
            setOnRefreshListener(this@MeeraMyFriendListFragment)
            setRefreshEnable(true)
        }
    }

    private fun initObservers() {
        viewModel.liveRemoveFriend.observe(viewLifecycleOwner, Observer { removeFriend ->
            friendInteractor?.updateFriendsCount()
            adapter?.currentList?.let { currentList ->
                adapter?.submitList(removeFriends(removeFriend, currentList))
            }
            if (adapter?.itemCount == 0) {
                binding.placeholderNoFriendsSearchResult.gone()
                binding.placeholderNoFriends.visible()
            }
        })

        viewModel.liveAllFriends.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it) {
                showPlaceHolderIfEmptyAdapter()
            }
            binding.apply {
                srMyFriendsList.setRefreshing(false)
                rvShimmerFriendsList.gone()
                rvMyFriendsList.visible()
            }
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is FriendsListViewEvents.OnErrorAddFriend -> {
                        showCommonSuccessMessage(getText(R.string.error_try_later), requireView())
                    }

                    is FriendsListViewEvents.OnErrorRemoveFriend -> {
                        showCommonSuccessMessage(getText(R.string.error_try_later), requireView())
                    }

                    is FriendsListViewEvents.HasFriendsEvent -> {
                        binding.placeholderNoFriends.gone()
                    }

                    is FriendsListViewEvents.NoFriendsEvent -> {
                        binding.placeholderNoFriendsSearchResult.gone()
                        binding.placeholderNoFriends.visible()
                    }

                    FriendsListViewEvents.OnFriendRejected -> friendInteractor?.updateFriendsCount()
                    FriendsListViewEvents.HasIncomingRequests -> Unit
                    FriendsListViewEvents.NoIncomigRequests -> Unit
                    FriendsListViewEvents.OnClearSearchAdapter -> Unit
                    FriendsListViewEvents.OnErrorAction -> Unit
                }
            }
        })

        viewModel.liveDeleteFriend.observe(viewLifecycleOwner, Observer { removeFriend ->
            friendInteractor?.updateFriendsCount()
            adapter?.currentList?.let { currentList ->
                adapter?.submitList(removeFriends(removeFriend, currentList)) {
                    showPlaceHolderIfEmptyAdapter()
                }
            }
        })
    }

    private fun initFriendClickListener(action: FriendsListAction) {
        when (action) {
            is FriendsListAction.DeleteUserClick -> {
                MeeraConfirmDialogBuilder().setHeader(R.string.user_info_remove_from_friend_dialog_header)
                    .setDescription(action.model.userModel.name + getString(R.string.remove_from_friend_desc_text_alt))
                    .setTopBtnText(R.string.delete).setTopBtnType(ButtonType.FILLED)
                    .setTopClickListener { viewModel.removeFriend(action.model) }
                    .setBottomBtnText(R.string.meera_user_info_remove_from_friend_dialog_remove_and_unsub)
                    .setBottomBtnType(ButtonType.OUTLINE)
                    .setBottomClickListener { viewModel.removeFriendSaveSubscription(action.model) }
                    .show(childFragmentManager)
            }

            is FriendsListAction.OpenProfileClick -> {
                findNavController().safeNavigate(
                    resId = R.id.action_meeraMyFriendListFragment_to_userInfoFragment, bundle = bundleOf(
                        ARG_USER_ID to action.userId,
                        ARG_PAGER_PROFILE to false,
                        ARG_TRANSIT_FROM to AmplitudePropertyWhere.FRIEND.property
                    )
                )
            }

            else -> Unit
        }
    }

    private fun showPlaceHolderIfEmptyAdapter() {
        binding.apply {
            if (adapter?.itemCount == 0) {
                if (viewModel.isSearch) placeholderNoFriendsSearchResult.visible()
                else placeholderNoFriends.visible()

                tvPlaceholderDescription.text = getString(R.string.friends_list_is_empty)
                btnSearchFriends.visible()
            } else {
                placeholderNoFriends.gone()
                placeholderNoFriendsSearchResult.gone()
            }
        }
    }

    fun search(query: String) {
        viewModel.onStartSearch()
        viewModel.searchFriendNetwork(query)
    }

    override fun onCloseSearch() {
        viewModel.onStopSearch()
        binding.placeholderNoFriendsSearchResult.gone()
        binding.placeholderNoFriends.visible()
    }

    override fun onRefresh() {
        binding.apply {
            rvShimmerFriendsList.gone()
            rvMyFriendsList.gone()
            adapter?.submitList(emptyList())
        }
        viewModel.onRefresh()
    }

    private fun removeFriends(removeFriend: FriendModel, list: List<FriendModel>): MutableList<FriendModel> {
        val removeList = list.toMutableList()
        removeList.removeIf { user ->
            user.userModel.userId == removeFriend.userModel.userId
        }
        return removeList
    }

    private fun initListeners() {
        binding?.btnSearchFriends?.setOnClickListener {
            viewModel.logMyFriendsPeopleSelected()
            findNavController().safeNavigate(
                R.id.action_meeraMyFriendListFragment_to_meeraPeoplesFragment
            )
        }
    }


    companion object {
        fun newInstance(
            userID: Long, openedType: FriendsHostOpenedType = FriendsHostOpenedType.OTHER
        ): MeeraMyFriendListFragment {
            val args = Bundle()
            args.putLong(ARG_USER_ID, userID)
            args.putSerializable(ARG_FRIENDS_HOST_OPENED_FROM, openedType)
            val fragment = MeeraMyFriendListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    interface IOnMyFriendsInteractor {
        fun updateFriendsCount()
        fun onNewFriend() {}
    }
}

package com.numplates.nomera3.modules.userprofile.ui.meerafriends.outgoing

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentFriendsBinding
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.MyFriendsListViewModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.viewmodel.FRIEND_REQUEST_PAGE_LIMIT
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import timber.log.Timber

class MeeraOutgoingFriendsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_friends, behaviourConfigState = ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentFriendsBinding::bind)
    private val argUserId: Long by lazy {
        arguments?.getLong(IArgContainer.ARG_USER_ID) ?: viewModel.getUserUid()
    }
    private val mode: Int by lazy {
        arguments?.getInt(
            IArgContainer.ARG_FRIEND_LIST_MODE
        ) ?: GetFriendsListUseCase.FRIENDS
    }

    private val viewModel: MyFriendsListViewModel by viewModels<MyFriendsListViewModel> {
        App.component.getViewModelFactory()
    }

    private var isSearch = false
    private var searchQueryLen = 0

    private val adapter by lazy {
        MeeraOutgoingFriendsAdapter(
            onItemClicked = ::onItemClicked, onCancelClicked = ::onCancelClicked
        )
    }
    private val searchAdapter by lazy {
        MeeraOutgoingFriendsAdapter(
            onItemClicked = ::onItemClicked, onCancelClicked = ::onCancelClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initObservers()
        initSwipeRefresh()
        initSearch()
        viewModel.init(argUserId, mode)
    }


    private fun initSearch() {
        binding?.uikitInputSearch?.doAfterSearchTextChanged {
            if (it.isEmpty()) {
                onCloseSearch()
            } else {
                onStartSearch()
                search(it)
            }

        }
    }

    private fun showPlaceHolder() {
        if (isSearch) {
            binding?.ivEmptyList?.setImageResource(R.drawable.friends_search_empty)
            binding?.tvEmptyList?.setText(R.string.friends_list_search_is_empty)
            binding?.buttonAddFriend?.text = getText(R.string.find_friend)
        } else {
            binding?.ivEmptyList?.setImageResource(R.drawable.friends_empty_list)
            binding?.tvEmptyList?.setText(R.string.outgoing_friends_list_is_empty)
            binding?.buttonAddFriend?.text = getText(R.string.find_friend)
        }

        binding?.ivEmptyList?.visible()
        binding?.tvEmptyList?.visible()
        binding?.buttonAddFriend?.isGone = isSearch
    }

    private fun hidePlaceHolder() {
        binding?.ivEmptyList?.gone()
        binding?.tvEmptyList?.gone()
        binding?.buttonAddFriend?.gone()
    }


    private fun initRecycler() {
        binding?.rvFriendsList?.layoutManager = LinearLayoutManager(context)
        binding?.rvFriendsList?.adapter = adapter
        initPagination()
    }

    private fun initPagination() {
        if (mode == GetFriendsListUseCase.FRIENDS) {
            binding?.rvFriendsList?.let { rv ->
                RecyclerViewPaginator(recyclerView = rv, onLast = {
                    if (!isSearch) viewModel.isLastFriend()
                    else viewModel.isLastIncoming()
                }, isLoading = {
                    if (!isSearch) viewModel.isLoadingFriend()
                    else viewModel.isLoadingIncoming()
                }, loadMore = {
                    if (!isSearch) viewModel.requestAllFriends(FRIEND_REQUEST_PAGE_LIMIT, adapter.itemCount)
                }).apply {
                    this.threshold = TRESHOLD
                }
            }
        }
    }

    private fun initSwipeRefresh() {
        binding?.srlFriendsList?.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                this@MeeraOutgoingFriendsFragment.onRefresh()
            }
        })
        binding.srlFriendsList.setRefreshEnable(true)
    }

    private fun initObservers() {
        viewModel.liveOutcomingFriends.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) showPlaceHolder() else hidePlaceHolder()

            adapter.submitList(it.map {
                OutgoingFriendsRecyclerData.RecyclerData(it.userModel.userId, it)
            })
            binding?.srlFriendsList?.setRefreshing(false)
        })

        viewModel.liveSearch.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) showPlaceHolder() else hidePlaceHolder()

            searchAdapter.submitList(emptyList())
            searchAdapter.submitList(it.map {
                OutgoingFriendsRecyclerData.RecyclerData(it.userModel.userId, it)
            })
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is FriendsListViewEvents.OnClearSearchAdapter -> {
                        searchAdapter.submitList(emptyList())
                    }

                    is FriendsListViewEvents.OnErrorAddFriend -> {
                        UiKitSnackBar.make(
                            view = requireView(), params = SnackBarParams(
                                snackBarViewState = SnackBarContainerUiState(
                                    messageText = getText(R.string.error_try_later),
                                    avatarUiState = AvatarUiState.ErrorIconState,
                                )
                            )
                        ).show()
                    }

                    is FriendsListViewEvents.OnErrorRemoveFriend -> {
                        UiKitSnackBar.make(
                            view = requireView(), params = SnackBarParams(
                                snackBarViewState = SnackBarContainerUiState(
                                    messageText = getText(R.string.error_try_later),
                                    avatarUiState = AvatarUiState.ErrorIconState,
                                )
                            )
                        ).show()
                    }

                    else -> return@let
                }
            }
        })


    }


    fun search(query: String) {
        searchQueryLen = query.length
        viewModel.search(query)
    }


    private fun onStartSearch() {
        Timber.e("Start search")
        searchAdapter.submitList(emptyList())
        binding?.rvFriendsList?.adapter = searchAdapter
        binding?.srlFriendsList?.isEnabled = false
        viewModel.onStartSearch()
        isSearch = true
    }


    private fun onCloseSearch() {
        hidePlaceHolder()
        binding?.srlFriendsList?.isEnabled = true
        binding?.rvFriendsList?.adapter = adapter
        viewModel.onStopSearch()
        isSearch = false
    }

    fun onRefresh() {
        binding?.srlFriendsList?.setRefreshing(false)
        adapter.submitList(emptyList())
        viewModel.onRefresh()
    }

    // TODO замеиться при редизайне профия
    private fun onItemClicked(friend: FriendModel) {
        friend
//        add(
//            UserInfoFragment(),
//            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//            Arg(IArgContainer.ARG_USER_ID, friend.userModel.userId),
//            Arg(IArgContainer.ARG_PAGER_PROFILE, false),
//            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.FRIEND.property)
//        )
    }


    private fun onCancelClicked(friend: FriendModel) {
        viewModel.cancelOutcomeFriendshipRequest(friend)
    }

    companion object {
        fun getInstance(userId: Long, mode: Int): MeeraOutgoingFriendsFragment {
            return MeeraOutgoingFriendsFragment().apply {
                arguments = bundleOf(
                    IArgContainer.ARG_USER_ID to userId, IArgContainer.ARG_FRIEND_LIST_MODE to mode
                )
            }
        }

        private const val TRESHOLD = 50
    }

}

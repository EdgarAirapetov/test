package com.numplates.nomera3.modules.userprofile.ui.meerafriends.incoming

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
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
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.viewmodel.FRIEND_REQUEST_PAGE_LIMIT
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import timber.log.Timber

private const val THRESHOLD = 50

class MeeraIncomingFriendsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_friends, behaviourConfigState = ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentFriendsBinding::bind)
    private val argUserId: Long by lazy {
        arguments?.getLong(IArgContainer.ARG_USER_ID) ?: viewModel.getUserUid()
    }

    private val argOpenedType: FriendsHostOpenedType by lazy {
        val arg = requireArguments().get(IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM) as? FriendsHostOpenedType
        arg ?: FriendsHostOpenedType.OTHER
    }

    private val viewModel: MyFriendsListViewModel by viewModels<MyFriendsListViewModel> {
        App.component.getViewModelFactory()
    }

    private var isSearch = false
    private var searchQueryLen = 0

    private val adapter by lazy {
        MeeraIncomingFriendsAdapter(
            onItemClicked = ::onItemClicked, onRejectClicked = ::onRejectClicked, onConfirmClicked = ::onConfirmClicked
        )
    }
    private val searchAdapter by lazy {
        MeeraIncomingFriendsAdapter(
            onItemClicked = ::onItemClicked, onRejectClicked = ::onRejectClicked, onConfirmClicked = ::onConfirmClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initObservers()
        initListeners()
        initSearch()
        viewModel.init(argUserId, GetFriendsListUseCase.INCOMING)
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
            binding?.tvEmptyList?.setText(R.string.incoming_friends_list_is_empty)
            binding?.buttonAddFriend?.text = getText(R.string.find_friend)
        }

        binding?.ivEmptyList?.visible()
        binding?.tvEmptyList?.visible()
        binding?.buttonAddFriend?.gone()
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
        binding?.rvFriendsList?.let { rv ->
            RecyclerViewPaginator(recyclerView = rv, onLast = {
                if (!isSearch) viewModel.isLastFriend()
                else viewModel.isLastIncoming()
            }, isLoading = {
                if (!isSearch) viewModel.isLoadingFriend()
                else viewModel.isLoadingIncoming()
            }, loadMore = {
                if (!isSearch) viewModel.requestFriendIncomingList(FRIEND_REQUEST_PAGE_LIMIT, adapter.itemCount)
            }).apply {
                /*
                * https://nomera.atlassian.net/browse/BR-3306
                * при скролле списка друзей начинал тормозить скролл, сделал threshold = 50,
                * чтоб дозагрузка происходила позже. Возможно нужно еще увеличить threshold
                * потому что запрашивается 1000 друзей?
                * */
                this.threshold = THRESHOLD
            }
        }
    }

    private fun initListeners() {
        binding?.srlFriendsList?.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                this@MeeraIncomingFriendsFragment.onRefresh()
            }
        })
        binding.srlFriendsList.setRefreshEnable(true)
    }

    private fun initObservers() {
        viewModel.liveIncomingFriends.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) showPlaceHolder() else hidePlaceHolder()
            adapter.submitList(it.map {
                IncomingFriendsRecyclerData.RecyclerData(it.userModel.userId, it)
            })
            binding?.srlFriendsList?.setRefreshing(false)
        })

        viewModel.liveSearch.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) showPlaceHolder() else hidePlaceHolder()
            searchAdapter.submitList(emptyList())
            searchAdapter.submitList(it.map {
                IncomingFriendsRecyclerData.RecyclerData(it.userModel.userId, it)
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

    fun onRefresh() {
        binding?.srlFriendsList?.setRefreshing(false)
        adapter.submitList(emptyList())
        viewModel.onRefresh()
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

    private fun onRejectClicked(friend: FriendModel) {
        viewModel.rejectFriend(friend = friend, openedType = argOpenedType)
    }

    private fun onConfirmClicked(friend: FriendModel) {
        viewModel.onActionClicked(friend = friend, openedType = argOpenedType)
    }

    companion object {
        fun getInstance(userId: Long, mode: Int): MeeraIncomingFriendsFragment {
            return MeeraIncomingFriendsFragment().apply {
                arguments = bundleOf(
                    IArgContainer.ARG_USER_ID to userId, IArgContainer.ARG_FRIEND_LIST_MODE to mode
                )
            }
        }
    }

}

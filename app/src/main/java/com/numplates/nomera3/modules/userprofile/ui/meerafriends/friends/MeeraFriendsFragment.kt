package com.numplates.nomera3.modules.userprofile.ui.meerafriends.friends

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_ACTION
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_ADD_TO_FRIEND
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_RESULT
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_SUBSCRIBE
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment.Companion.KEY_IS_ME
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout.OnRefreshListener
import com.numplates.nomera3.presentation.viewmodel.FRIEND_REQUEST_PAGE_LIMIT
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MeeraFriendsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_friends,
    behaviourConfigState = ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentFriendsBinding::bind)
    private val argIsMe: Boolean by lazy {
        requireArguments().getBoolean(KEY_IS_ME)
    }
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
        MeeraFriendsAdapter(
            isMe = argIsMe,
            userId = argUserId,
            onItemClicked = ::onItemClicked,
            onRemoveClicked = ::onRemoveClicked,
            onActionClicked = ::onActionClicked
        )
    }

    private val searchAdapter by lazy {
        MeeraFriendsAdapter(
            isMe = argIsMe,
            userId = argUserId,
            onItemClicked = ::onItemClicked,
            onRemoveClicked = ::onRemoveClicked,
            onActionClicked = ::onActionClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initObservers()
        initListeners()
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

    fun onStartSearch() {
        searchAdapter.submitList(emptyList())
        binding?.rvFriendsList?.adapter = searchAdapter
        binding?.srlFriendsList?.isEnabled = false
        viewModel.onStartSearch()
        isSearch = true
    }


    private fun showPlaceHolder() {
        if (isSearch) {
            binding?.ivEmptyList?.setImageResource(R.drawable.friends_search_empty)
            binding?.tvEmptyList?.setText(R.string.friends_list_search_is_empty)
            binding?.buttonAddFriend?.text = getText(R.string.find_friend)
        } else {
            binding?.ivEmptyList?.setImageResource(R.drawable.friends_empty_list)
            binding?.tvEmptyList?.setText(R.string.friends_list_is_empty)
            binding?.buttonAddFriend?.text = getText(R.string.find_friend)
        }

        binding?.ivEmptyList?.visible()
        binding?.tvEmptyList?.visible()
        binding?.buttonAddFriend?.isVisible = isSearch.not() && argIsMe
    }

    private fun hidePlaceHolder() {
        binding?.ivEmptyList?.gone()
        binding?.tvEmptyList?.gone()
        binding?.buttonAddFriend?.gone()
    }


    fun onCloseSearch() {
        binding?.srlFriendsList?.isEnabled = true
        binding?.rvFriendsList?.adapter = adapter
        viewModel.onStopSearch()
        isSearch = false
        hidePlaceHolder()
    }

    fun search(query: String) {
        searchQueryLen = query.length
        if (mode == GetFriendsListUseCase.FRIENDS) viewModel.searchFriendNetwork(query)
        else viewModel.search(query)
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

    private fun initListeners() {
        binding?.srlFriendsList?.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                this@MeeraFriendsFragment.onRefresh()
            }
        })
        binding.srlFriendsList.setRefreshEnable(true)

        setFragmentResultListener(KEY_NOT_MUTUAL_FRIEND_RESULT) { _, bundle ->
            val userId = bundle.getLong(IArgContainer.ARG_USER_ID)
            val action = bundle.getString(KEY_NOT_MUTUAL_FRIEND_ACTION)

            when (action) {
                KEY_NOT_MUTUAL_FRIEND_SUBSCRIBE -> {
                    viewModel.subscribeUser(userId)
                }

                KEY_NOT_MUTUAL_FRIEND_ADD_TO_FRIEND -> {
                    viewModel.addToFriend(userId)
                }
            }
        }
    }

    fun onRefresh() {
        binding?.srlFriendsList?.setRefreshing(false)
        adapter.submitList(emptyList())
        viewModel.onRefresh()
    }

    private fun initObservers() {
        setFragmentResultListener(MeeraDeleteFriendBottomDialogFragment.KEY_DELETE_RESULT) { _, bundle ->
            val deleteOnly = bundle.getBoolean(MeeraDeleteFriendBottomDialogFragment.KEY_DELETE_ONLY)
            val userId = bundle.getLong(IArgContainer.ARG_USER_ID)

            if (deleteOnly) {
                viewModel.removeFriendSaveSubscriptionById(userId)
            } else {
                viewModel.removeFriendById(userId)
            }
        }


        viewModel.liveAllFriends.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty() && isSearch.not()) {
                showPlaceHolder()
            } else {
                hidePlaceHolder()
            }
            adapter.submitList(it.map {
                FriendsRecyclerData.RecyclerData(it.userModel.userId, it)
            })
            binding?.srlFriendsList?.setRefreshing(false)
        })


        viewModel.viewEvent.onEach(::handleEvent).launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.liveSearch.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty() && isSearch) showPlaceHolder() else hidePlaceHolder()

            searchAdapter.submitList(it.map {
                FriendsRecyclerData.RecyclerData(it.userModel.userId, it)
            })
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {

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

    private fun handleEvent(typeEvent: UserSubscriptionViewEvent) {
        when (typeEvent) {
            is UserSubscriptionViewEvent.RefreshUserList -> Unit
            is UserSubscriptionViewEvent.ShowErrorSnackBar -> {
                showSnackBarMessage(getString(typeEvent.errorMessageRes), AvatarUiState.ErrorIconState)
            }

            is UserSubscriptionViewEvent.ShowSuccessSnackBar -> {
                showSnackBarMessage(getString(typeEvent.messageRes), AvatarUiState.SuccessIconState)
            }
        }
    }

    private fun showSnackBarMessage(msg: String?, avatarState: AvatarUiState) {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = msg,
                    avatarUiState = avatarState,
                )
            )
        ).show()
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

    private fun onRemoveClicked(friend: FriendModel) {
        MeeraDeleteFriendBottomDialogFragment.show(
            requireActivity().supportFragmentManager, friend.userModel.userId, friend.userModel.name
        )
    }

    private fun onActionClicked(friend: FriendModel) {
        MeeraNotMutualFriendActionBottomDialogFragment.show(
            fragmentManager = requireActivity().supportFragmentManager, userId = friend.userSimple?.userId ?: 0L
        )
    }

    companion object {
        private const val TRESHOLD = 50
        fun getInstance(isMe: Boolean, userId: Long, mode: Int): MeeraFriendsFragment {
            return MeeraFriendsFragment().apply {
                arguments = bundleOf(
                    KEY_IS_ME to isMe, IArgContainer.ARG_USER_ID to userId, IArgContainer.ARG_FRIEND_LIST_MODE to mode
                )
            }
        }
    }
}

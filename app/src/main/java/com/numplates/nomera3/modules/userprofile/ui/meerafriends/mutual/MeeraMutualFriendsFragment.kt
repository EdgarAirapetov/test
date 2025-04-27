package com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentFriendsBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_ACTION
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_ADD_TO_FRIEND
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_RESULT
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_SUBSCRIBE
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendsFollowersUiState
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MeeraMutualFriendsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_friends,
    behaviourConfigState = ScreenBehaviourState.Full
    ) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentFriendsBinding::bind)
    private val viewModel by viewModels<MutualSubscriptionViewModel> {
        App.component.getViewModelFactory()
    }

    private val userId: Long by lazy { requireArguments().getLong(IArgContainer.ARG_USER_ID) }
    private val adapter by lazy { MeeraMutualFriendsAdapter(::onItemClicked, ::onActionClicked) }
    private var recyclerPagination: RecyclerViewPaginator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initListeners()
        initStateObserver()
        observeViewEvent()
        getMutualFriends()
    }

    private fun initList() {
        binding?.rvFriendsList?.adapter = adapter
        initPagination()
    }

    private fun initListeners() {
        binding?.srlFriendsList?.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                getMutualFriends(isRefreshing = true)
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

    private fun observeViewEvent() {
        viewModel.userMutualSubscriptionViewEvent.onEach(::handleEvent).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleEvent(typeEvent: UserSubscriptionViewEvent) {
        when (typeEvent) {
            is UserSubscriptionViewEvent.RefreshUserList -> {
                viewModel.getUserMutualSubscription(
                    userId = userId, querySearch = viewModel.getSearchInput(), clickedItem = typeEvent.clickedItem
                )
            }

            is UserSubscriptionViewEvent.ShowErrorSnackBar -> showErrorMessage(getString(typeEvent.errorMessageRes))
            is UserSubscriptionViewEvent.ShowSuccessSnackBar -> showSuccessMessage(getString(typeEvent.messageRes))
        }
    }

    private fun showSuccessMessage(msg: String?) {
        NSnackbar.with(requireActivity()).typeSuccess().marginBottom(SNACK_BAR_MARGIN).text(msg)
            .durationLong().show()
    }

    private fun showErrorMessage(errorMessage: String?) {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = errorMessage,
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        ).show()
    }

    private fun getMutualFriends(
        isRefreshing: Boolean = false, querySearch: String? = null, offset: Int = 0
    ) {
        viewModel.getUserMutualSubscription(
            userId = userId ?: 0, isRefreshing = isRefreshing, querySearch = querySearch, offset = offset
        )
    }


    private fun initStateObserver() {
        viewModel.sameSubscriptionState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is UserFriendsFollowersUiState.SuccessGetList -> {
                    hidePlaceHolder()
                    setProgressState(uiState.isShowProgress)
                    setRefreshing(uiState.isRefreshing)
                    adapter.submitList(uiState.friendsList.map {
                        MutualFriendsRecyclerData.RecyclerData(
                            it.userSimple?.userId ?: 0L, it
                        )
                    })
                }

                is UserFriendsFollowersUiState.ListEmpty -> {
                    setProgressState(false)
                    setRefreshing(false)
                    showPlaceHolder(uiState.isSearch)
                    adapter.submitList(emptyList())
                }
            }
        }
    }


    private fun setRefreshing(isRefresh: Boolean) {
        binding?.srlFriendsList?.setRefreshing( isRefresh)
    }

    private fun setProgressState(isShowProgress: Boolean) {
        binding?.srlFriendsList?.setRefreshing( isShowProgress)
    }

    private fun showPlaceHolder(isSearch: Boolean) {
        binding?.apply {
            if (isSearch) {
                ivEmptyList.setImageResource(R.drawable.friends_search_empty)
                tvEmptyList.setText(R.string.friends_list_search_is_empty)
                buttonAddFriend.text = getText(R.string.find_friend)
            } else {
                ivEmptyList.setImageResource(R.drawable.friends_empty_list)
                tvEmptyList.setText(R.string.friends_list_is_empty)
                buttonAddFriend.text = getText(R.string.find_friend)
            }

            ivEmptyList.visible()
            tvEmptyList.visible()
            buttonAddFriend.gone()
        }
    }

    private fun hidePlaceHolder() {
        binding?.apply {
            ivEmptyList.gone()
            tvEmptyList.gone()
            buttonAddFriend.gone()
        }
    }

    private fun initPagination() {
        recyclerPagination = RecyclerViewPaginator(recyclerView = binding?.rvFriendsList ?: return, loadMore = {
            if (!viewModel.isSearch()) {
                getMutualFriends(
                    isRefreshing = false, offset = adapter.itemCount
                )
            }
        }, onLast = {
            viewModel.isLastPage
        }, isLoading = {
            viewModel.isLoading
        }).apply {
            this.threshold = DEFAULT_THRESHOLD
        }
    }
    // TODO замеиться при редизайне профия
    private fun onItemClicked(friend: FriendsFollowersUiModel) {
        friend
//        add(
//            UserInfoFragment(),
//            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//            Arg(IArgContainer.ARG_USER_ID, friend.userSimple?.userId),
//            Arg(IArgContainer.ARG_PAGER_PROFILE, false),
//            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.FRIEND.property)
//        )
    }

    private fun onActionClicked(friend: FriendsFollowersUiModel) {
        MeeraNotMutualFriendActionBottomDialogFragment.show(
            fragmentManager = requireActivity().supportFragmentManager, userId = friend.userSimple?.userId ?: 0L
        )
    }


    companion object {
        private const val DEFAULT_THRESHOLD = 50
        private const val SNACK_BAR_MARGIN = 64
        fun getInstance(userId: Long, mode: Int): MeeraMutualFriendsFragment {
            return MeeraMutualFriendsFragment().apply {
                arguments = bundleOf(
                    IArgContainer.ARG_USER_ID to userId, IArgContainer.ARG_FRIEND_LIST_MODE to mode
                )
            }
        }
    }

}

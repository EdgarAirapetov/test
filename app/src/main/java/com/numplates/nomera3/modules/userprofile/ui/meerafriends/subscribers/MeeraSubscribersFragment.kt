package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribers

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentFriendsBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment.Companion.KEY_IS_ME
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_ACTION
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_ADD_TO_FRIEND
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_RESULT
import com.numplates.nomera3.modules.userprofile.ui.meerafriends.mutual.MeeraNotMutualFriendActionBottomDialogFragment.Companion.KEY_NOT_MUTUAL_FRIEND_SUBSCRIBE
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

private const val REQUEST_LIMIT = 50

class MeeraSubscribersFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_friends,
    behaviourConfigState = ScreenBehaviourState.Full
), MeeraPullToRefreshLayout.OnRefreshListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentFriendsBinding::bind)
    private val argIsMe: Boolean by lazy {
        requireArguments().getBoolean(KEY_IS_ME)
    }

    private val argUserId: Long by lazy {
        requireArguments().getLong(IArgContainer.ARG_USER_ID)
    }
    private var searchQuery: String = ""
    private val subscribersViewModel by viewModels<MeeraSubscribersViewModel> {
        App.component.getViewModelFactory()
    }

    val adapter by lazy {
        MeeraSubscribersAdapter(
            isMe = argIsMe,
            userId = argUserId,
            onItemClicked = ::onItemClicked,
            onRemoveClicked = ::onRemoveClicked,
            onActionClicked = ::onActionClicked
        )
    }
    private val searchAdapter by lazy {
        MeeraSubscribersAdapter(
            isMe = argIsMe,
            userId = argUserId,
            onItemClicked = ::onItemClicked,
            onRemoveClicked = ::onRemoveClicked,
            onActionClicked = ::onActionClicked
        )
    }
    private var isSearchMode = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initRecycler()
        initObservers()
        initSearch()

    }

    private fun showPlaceHolder() {
        if (isSearchMode) {
            binding?.ivEmptyList?.setImageResource(R.drawable.friends_search_empty)
            binding?.tvEmptyList?.setText(R.string.friends_list_search_is_empty)
            binding?.buttonAddFriend?.text = getText(R.string.find_friend)
        } else {
            binding?.ivEmptyList?.setImageResource(R.drawable.friends_empty_list)
            binding?.tvEmptyList?.setText(R.string.subscibers_list_is_empty)
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

    private fun initSearch() {
        binding?.uikitInputSearch?.doAfterSearchTextChanged {
            if (it.isEmpty()) {
                onCloseSearch()
            } else {
                onStartSearch()
                requestSearch(it, 0)
            }

        }
    }


    fun onStartSearch() {
        Timber.e("Start search")
        searchAdapter.submitList(emptyList())
        binding?.rvFriendsList?.adapter = searchAdapter
        binding?.srlFriendsList?.isEnabled = false
        isSearchMode = true
    }


    fun onCloseSearch() {
        hidePlaceHolder()
        binding?.srlFriendsList?.isEnabled = true
        binding?.rvFriendsList?.adapter = adapter
        isSearchMode = false
        requestFreshSubscriptions()
    }


    private fun initListeners() {
        binding?.srlFriendsList?.setOnRefreshListener(this)
        binding.srlFriendsList.setRefreshEnable(true)

        setFragmentResultListener(KEY_NOT_MUTUAL_FRIEND_RESULT) { _, bundle ->
            val userId = bundle.getLong(IArgContainer.ARG_USER_ID)
            val action = bundle.getString(KEY_NOT_MUTUAL_FRIEND_ACTION)

            when (action) {
                KEY_NOT_MUTUAL_FRIEND_SUBSCRIBE -> {
                    subscribersViewModel.subscribeUser(userId)
                }

                KEY_NOT_MUTUAL_FRIEND_ADD_TO_FRIEND -> {
                    subscribersViewModel.addToFriend(userId)
                }
            }
        }
    }

    private fun requestSubscribers() {
        subscribersViewModel.requestSubscribers(
            userId = argUserId,
            limit = REQUEST_LIMIT,
            offset = adapter.itemCount,
        )
    }


    private fun initObservers() {
        subscribersViewModel.liveSubscribers.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                showPlaceHolder()
            } else {
                hidePlaceHolder()
            }

            binding?.srlFriendsList?.setRefreshing(false)
            handleSubscriptions(it)
        })

        subscribersViewModel.liveViewEvent.observe(viewLifecycleOwner, Observer {
            binding?.srlFriendsList?.setRefreshing(false)
            when (it) {
                is SubscriptionViewEvent.ErrorWhileRequestingSubscriptions -> {
                    showListError()
                }

                is SubscriptionViewEvent.ErrorWhileSearchSubscriptions -> {
                    showListError()
                }

                else -> Unit
            }
        })

        subscribersViewModel.viewEvent.onEach(::handleEvent).launchIn(viewLifecycleOwner.lifecycleScope)

        requestFreshSubscriptions()
    }

    private fun handleEvent(typeEvent: UserSubscriptionViewEvent) {
        when (typeEvent) {
            is UserSubscriptionViewEvent.RefreshUserList -> Unit
            is UserSubscriptionViewEvent.ShowErrorSnackBar -> showErrorMessage(getString(typeEvent.errorMessageRes))
            is UserSubscriptionViewEvent.ShowSuccessSnackBar -> showSuccessMessage(getString(typeEvent.messageRes))
        }
    }

    private fun showSuccessMessage(msg: String?) {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = msg,
                    avatarUiState = AvatarUiState.SuccessIconState,
                ), duration = Snackbar.LENGTH_LONG
            )
        ).show()
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


    private fun showListError() {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.error_while_getting_subscribers_list),
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        ).show()
    }

    private fun handleSubscriptions(subscriptions: List<UserSimple?>?) {
        val res = mutableListOf<SubscribersRecyclerData>()
        subscriptions?.forEach { userSimple ->
            userSimple?.let {
                res.add(
                    SubscribersRecyclerData.RecyclerData(
                        userSimple.userId, userSimple
                    )
                )
            }
        }

        if (!isSearchMode) {
            adapter.submitList(res)
        } else {
            searchAdapter.submitList(res)
        }
    }


    private fun initRecycler() {
        binding?.srlFriendsList?.setRefreshing( true)
        binding?.rvFriendsList?.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    val linearLayoutManager = (binding?.rvFriendsList?.layoutManager as? LinearLayoutManager)
                    linearLayoutManager?.scrollToPosition(0)
                }
            }
        })

        RecyclerViewPaginator(recyclerView = binding?.rvFriendsList!!, onLast = {
            if (!isSearchMode) subscribersViewModel.onLastSubscriber()
            else subscribersViewModel.onLastSubscriberSearch()
        }, isLoading = {
            if (!isSearchMode) subscribersViewModel.onLoadingSubscriber()
            else subscribersViewModel.onLoadingSubscriberSearch()
        }, loadMore = {
            if (!isSearchMode) requestSubscribers()
            else requestSearch()
        })
    }

    override fun onRefresh() {
        if (!isSearchMode) {
            adapter.submitList(emptyList())
            requestSubscribers()
        } else {
            searchAdapter.submitList(emptyList())
            requestSearch(offset = 0)
        }
    }
    // TODO замеиться при редизайне профия
    private fun onItemClicked(friend: UserSimple) {
        friend
//        add(
//            UserInfoFragment(),
//            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//            Arg(IArgContainer.ARG_USER_ID, friend.userId),
//            Arg(IArgContainer.ARG_PAGER_PROFILE, false),
//            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.FRIEND.property)
//        )
    }

    private fun onRemoveClicked(userSimple: UserSimple) {
        MeeraDeleteSubscriberBottomDialogFragment.show(
            fragmentManager = requireActivity().supportFragmentManager, userId = userSimple.userId
        )
    }

    private fun onActionClicked(friend: UserSimple) {
        MeeraNotMutualFriendActionBottomDialogFragment.show(
            fragmentManager = requireActivity().supportFragmentManager, userId = friend.userId
        )
    }


    private fun requestFreshSubscriptions() {
        subscribersViewModel.requestSubscribers(
            userId = argUserId,
            limit = REQUEST_LIMIT,
            offset = 0,
        )
    }

    /**
     * Search subscriptions
     * */
    private fun requestSearch(text: String? = null, offset: Int? = null) {
        text?.let { searchQuery = it }

        subscribersViewModel.subscribersSearch(
            userId = argUserId,
            limit = REQUEST_LIMIT,
            offset = offset ?: searchAdapter.itemCount,
            text = text ?: searchQuery,
        )
    }

    companion object {
        fun getInstance(isMe: Boolean, userId: Long): MeeraSubscribersFragment {
            return MeeraSubscribersFragment().apply {
                arguments = bundleOf(
                    KEY_IS_ME to isMe, IArgContainer.ARG_USER_ID to userId
                )
            }
        }
    }
}

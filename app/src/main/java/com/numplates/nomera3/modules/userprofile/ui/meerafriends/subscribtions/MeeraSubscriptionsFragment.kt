package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribtions

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
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
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

private const val REQUEST_LIMIT = 50

class MeeraSubscriptionsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_friends,
    behaviourConfigState = ScreenBehaviourState.Full
),
    MeeraPullToRefreshLayout.OnRefreshListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentFriendsBinding::bind)
    private val argIsMe: Boolean by lazy {
        requireArguments().getBoolean(KEY_IS_ME)
    }

    private val argUserId: Long by lazy {
        requireArguments().getLong(IArgContainer.ARG_USER_ID)
    }

    private val adapter by lazy {
        MeeraSubscriptionsAdapter(
            isMe = argIsMe, userId = argUserId, onItemClicked = ::onItemClicked, onActionClicked = ::onActionClicked
        )
    }
    private val searchAdapter by lazy {
        MeeraSubscriptionsAdapter(
            isMe = argIsMe, userId = argUserId, onItemClicked = ::onItemClicked, onActionClicked = ::onActionClicked
        )
    }
    private var isSearchMode = false


    private val subscriptionViewModel by viewModels<MeeraSubscriptionViewModel> {
        App.component.getViewModelFactory()
    }

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
            binding?.tvEmptyList?.setText(R.string.no_subscription_header)
            binding?.buttonAddFriend?.text = getText(R.string.subscription_list_find_new)
        }
        binding?.buttonAddFriend?.buttonType = ButtonType.FILLED

        binding?.ivEmptyList?.visible()
        binding?.tvEmptyList?.visible()
        binding?.buttonAddFriend?.isVisible = isSearchMode.not() && argIsMe
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
            val action = bundle.getString(KEY_NOT_MUTUAL_FRIEND_ACTION)
            val userId = bundle.getLong(IArgContainer.ARG_USER_ID)

            when (action) {
                KEY_NOT_MUTUAL_FRIEND_SUBSCRIBE -> {
                    subscriptionViewModel.subscribeUser(userId)
                }

                KEY_NOT_MUTUAL_FRIEND_ADD_TO_FRIEND -> {
                    subscriptionViewModel.addToFriend(userId)
                }
            }
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
            if (!isSearchMode) subscriptionViewModel.onLastSubscription()
            else subscriptionViewModel.onLastSubscriptionSearch()
        }, isLoading = {
            if (!isSearchMode) subscriptionViewModel.onLoadingSubscription()
            else subscriptionViewModel.onLoadingSubscriptionSearch()
        }, loadMore = {
            if (!isSearchMode) requestSubscriptions()
            else requestSearch()
        })
    }

    override fun onRefresh() {
        if (!isSearchMode) {
            adapter.submitList(emptyList())
            requestSubscriptions()
        } else {
            searchAdapter.submitList(emptyList())
            requestSearch(offset = 0)
        }
    }

    private fun requestSubscriptions() {
        subscriptionViewModel.requestSubscriptions(
            userId = argUserId,
            limit = REQUEST_LIMIT,
            offset = adapter.itemCount,
        )
    }

    var searchQuery: String = ""
    private fun requestSearch(text: String? = null, offset: Int? = null) {
        text?.let { searchQuery = it }
        subscriptionViewModel.subscriptionsSearch(
            userId = argUserId,
            limit = REQUEST_LIMIT,
            offset = offset ?: searchAdapter.itemCount,
            text = text ?: searchQuery,
        )
    }


    private fun requestFreshSubscriptions() {
        subscriptionViewModel.requestSubscriptions(
            userId = argUserId, limit = REQUEST_LIMIT, offset = 0
        )
    }


    private fun initObservers() {
        subscriptionViewModel.liveSubscriptions.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                showPlaceHolder()
            } else {
                hidePlaceHolder()
            }
            binding?.srlFriendsList?.setRefreshing( false)
            handleSubscriptions(it)
        })

        subscriptionViewModel.viewEvent.onEach(::handleEvent).launchIn(viewLifecycleOwner.lifecycleScope)

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
        NSnackbar.with(requireActivity()).typeSuccess().marginBottom(SNACK_BAR_MARGIN).text(msg).durationLong().show()
    }

    private fun showErrorMessage(errorMessage: String?) {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = errorMessage,
                    avatarUiState = AvatarUiState.SuccessIconState,
                )
            )
        ).show()
    }


    private fun handleSubscriptions(subscriptions: List<UserSimple?>?) {
        val res = mutableListOf<SubscriptionsRecyclerData>()
        subscriptions?.forEach { userSimple ->
            userSimple?.let {
                res.add(SubscriptionsRecyclerData.RecyclerData(userSimple.userId, userSimple))
            }
        }
        adapter.submitList(res)
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

    private fun onActionClicked(friend: UserSimple) {
        MeeraNotMutualFriendActionBottomDialogFragment.show(
            fragmentManager = requireActivity().supportFragmentManager, userId = friend.userId
        )
    }

    companion object {
        private const val SNACK_BAR_MARGIN = 64

        fun getInstance(isMe: Boolean, userId: Long): MeeraSubscriptionsFragment {
            return MeeraSubscriptionsFragment().apply {
                arguments = bundleOf(
                    KEY_IS_ME to isMe, IArgContainer.ARG_USER_ID to userId
                )
            }
        }
    }
}

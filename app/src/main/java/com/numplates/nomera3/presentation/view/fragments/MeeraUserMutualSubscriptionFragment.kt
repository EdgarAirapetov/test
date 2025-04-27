package com.numplates.nomera3.presentation.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.drawable
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserMutualSubscriptionFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.MeeraFriendsFollowersAdapter
import com.numplates.nomera3.presentation.view.adapter.holders.MeeraFriendsFollowerAction
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendsFollowersUiState
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraCellShimmerAdapter
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.view.utils.friendsdialog.MeeraUserFriendActionMenuBottomSheetBuilder
import com.numplates.nomera3.presentation.view.utils.friendsdialog.MeeraUserFriendsDialogAction
import com.numplates.nomera3.presentation.viewmodel.UserFriendActionViewModel
import com.numplates.nomera3.presentation.viewmodel.UserMutualSubscriptionViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val COUNT_SHIMMER_ITEM = 8
private const val DELAY_NOTIFICATION_SEC = 4L

class MeeraUserMutualSubscriptionFragment :
    MeeraBaseFragment(
        layout = R.layout.meera_user_mutual_subscription_fragment,
    ), FriendsSubscribersActionCallback {

    private val binding by viewBinding(MeeraUserMutualSubscriptionFragmentBinding::bind)

    private val viewModel by viewModels<UserMutualSubscriptionViewModel> {
        App.component.getViewModelFactory()
    }

    private val userFriendActionViewModel by viewModels<UserFriendActionViewModel> {
        App.component.getViewModelFactory()
    }
    private var subscriptionAdapter: MeeraFriendsFollowersAdapter? = null
    private val shimmerAdapter = MeeraCellShimmerAdapter()
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }

    private var recyclerPagination: RecyclerViewPaginator? = null
    private var successSnackBar: NSnackbar? = null
    private var pendingDeleteSnackbar: UiKitSnackBar? = null
    private val deleteNotificationState = linkedMapOf<String, NotificationCellUiModel>()
    private var userId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(USER_ID_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initRefreshListener()
        initStateObserver()
        observeViewEvent()
        getMutualFriends()
    }

    override fun onStop() {
        super.onStop()
        dismissSnackBar()
    }

    override fun dismissSuccessSnackBar() {
        dismissSnackBar()
    }

    override fun search(input: String) {
        viewModel.searchOpenStateChanged(true)
        getMutualFriends(
            isRefreshing = false,
            querySearch = input
        )
    }

    override fun logMutualFriendsAmplitude() {
        viewModel.logMutualFriendsAmplitude()
    }

    fun dismissSnackBar() {
        successSnackBar?.dismiss()
    }

    private fun initList() {
        subscriptionAdapter = MeeraFriendsFollowersAdapter(::initUserClickListener)
        binding?.vgUserMutualList?.adapter = subscriptionAdapter

        binding?.vgShimmerList?.adapter = shimmerAdapter
        shimmerAdapter.submitList(listShimmer)

        initPagination()
    }

    private fun initUserClickListener(action: MeeraFriendsFollowerAction) {
        when (action) {
            is MeeraFriendsFollowerAction.AcceptRequestFriendClick -> {
                handleIconIconClicked(action.model)
            }

            is MeeraFriendsFollowerAction.AddFriendsClick -> {
                handleIconIconClicked(action.model)
            }

            is MeeraFriendsFollowerAction.UserClick -> addUserFragment(action.userId)
        }
    }

    private fun initRefreshListener() {
        binding?.vgUserMutualRefresh?.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                getMutualFriends(
                    isRefreshing = true
                )
            }

        })
        binding.vgUserMutualRefresh.setRefreshEnable(true)

    }

    private fun initPagination() {
        recyclerPagination = RecyclerViewPaginator(
            recyclerView = binding?.vgUserMutualList ?: return,
            loadMore = {
                if (!viewModel.isSearch()) {
                    getMutualFriends(
                        isRefreshing = false,
                        offset = subscriptionAdapter?.itemCount ?: 0
                    )
                }
            },
            onLast = {
                viewModel.isLastPage
            },
            isLoading = {
                viewModel.isLoading
            }
        ).apply {
            this.threshold = DEFAULT_THRESHOLD
        }
    }

    private fun getMutualFriends(
        isRefreshing: Boolean = false,
        querySearch: String? = null,
        offset: Int = 0
    ) {
        viewModel.getUserMutualSubscription(
            userId = userId ?: 0,
            isRefreshing = isRefreshing,
            querySearch = querySearch,
            offset = offset
        )
    }

    private fun addUserFragment(userId: Long) {
        dismissSnackBar()
        findNavController().safeNavigate(
            resId = R.id.action_meeraMyFriendListFragment_to_userInfoFragment,
            bundle = bundleOf(
                ARG_USER_ID to userId,
                ARG_TRANSIT_FROM to AmplitudePropertyWhere.MUTUAL_FOLLOWS.property
            )
        )
    }

    private fun initStateObserver() {
        viewModel.sameSubscriptionState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is UserFriendsFollowersUiState.SuccessGetList -> {
                    hidePlaceholder()
                    setProgressState(uiState.isShowProgress)
                    setRefreshing(uiState.isRefreshing)
                    subscriptionAdapter?.submitList(uiState.friendsList)
                }

                is UserFriendsFollowersUiState.ListEmpty -> {
                    setProgressState(false)
                    setRefreshing(false)
                    setPlaceholderByState(uiState.isSearch)
                    subscriptionAdapter?.submitList(emptyList())
                }
            }
        }
    }

    private fun setRefreshing(isRefresh: Boolean) {
        binding?.vgUserMutualRefresh?.setRefreshing(isRefresh)
    }

    private fun setProgressState(isShowProgress: Boolean) {
        if (isShowProgress) {
            binding?.vgUserMutualList?.gone()
            binding?.vgShimmerList?.visible()
        } else {
            binding?.vgUserMutualList?.visible()
            binding?.vgShimmerList?.gone()
        }
    }

    private fun hidePlaceholder() {
        binding?.vgUserMutualPlaceholder?.gone()
    }

    private fun setPlaceholderByState(isSearch: Boolean) {
        binding?.vgUserMutualPlaceholder?.visible()
        if (isSearch) {
            setPlaceholderText(
                placeholderText = getString(R.string.meera_settings_empty_state),
                placeholderIcon = context?.drawable(R.drawable.friends_search_empty) ?: return
            )
        } else {
            setPlaceholderText(
                placeholderText = getString(R.string.mutual_friends_list_empty),
                placeholderIcon = context?.drawable(R.drawable.ic_i_dont_know) ?: return
            )
        }
    }

    private fun setPlaceholderText(
        placeholderText: String,
        placeholderIcon: Drawable
    ) {
        binding?.tvPlaceholder?.text = placeholderText
        binding?.ivPlaceholder?.setImageDrawable(placeholderIcon)
    }

    private fun handleIconIconClicked(model: FriendsFollowersUiModel) {
        val isSubscribed = model.userSimple?.settingsFlags?.subscription_on.toBoolean()

        if (model.userSimple?.settingsFlags?.friendStatus.toBoolean()) {
            MeeraUserFriendActionMenuBottomSheetBuilder()
                .setHeader(R.string.actions)
                .setFirstCellText(R.string.accept_request)
                .setFirstCellIcon(R.drawable.ic_outlined_following_m)
                .setSecondCellText(R.string.user_info_subscribe)
                .setSecondCellIcon(R.drawable.ic_outlined_subscribe_m)
                .setThirdCellText(R.string.reject_friend_request)
                .setThirdCellIcon(R.drawable.ic_outlined_user_delete_m)
                .setClickListener {
                    initUserFriendsDialogClickListener(it, model)
                }
                .show(childFragmentManager)
        } else {
            MeeraUserFriendActionMenuBottomSheetBuilder()
                .setHeader(R.string.actions)
                .setFirstCellText(R.string.add_to_friends)
                .setFirstCellIcon(R.drawable.ic_outlined_user_add_m)
                .setSecondCellText(
                    if (!isSubscribed) R.string.user_info_subscribe else R.string.unsubscribe
                )
                .setSecondCellIcon(
                    if (!isSubscribed) R.drawable.ic_outlined_subscribe_m else R.drawable.ic_outlined_unfollow_m
                )
                .setClickListener {
                    initDialogClickListener(it, isSubscribed, model)
                }
                .show(childFragmentManager)
        }
    }

    private fun initDialogClickListener(
        action: MeeraUserFriendsDialogAction,
        isSubscribed: Boolean,
        selectedUser: FriendsFollowersUiModel
    ) {
        when (action) {
            is MeeraUserFriendsDialogAction.FirstItemClick -> {
                selectedUser.userSimple?.userId?.let { userId ->
                    userFriendActionViewModel.logAddToFriendAmplitude(
                        userId = userId,
                        screenMode = MODE_SHOW_USER_MUTUAL_USERS,
                        approved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple.topContentMaker.toBoolean()
                    )
                    showComplaintInfoSnackbar(R.string.request_send_notiff_on) {
                        userFriendActionViewModel.addToFriendSocket(
                            model = selectedUser,
                            isAcceptFriendRequest = false
                        )
                    }
                }
            }

            is MeeraUserFriendsDialogAction.SecondItemClick -> {
                if (isSubscribed) {
                    userFriendActionViewModel.logUnfollowAmplitudeAction(
                        userId = selectedUser.userSimple?.userId ?: 0,
                        screenMode = MODE_SHOW_USER_MUTUAL_USERS,
                        accountApproved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                    )
                    userFriendActionViewModel.unsubscribeUser(selectedUser)
                    showCommonSuccessMessage(getText(R.string.disabled_new_post_notif), requireView())
                } else {
                    userFriendActionViewModel.logFollowAmplitude(
                        userId = selectedUser.userSimple?.userId ?: 0,
                        screenMode = MODE_SHOW_USER_MUTUAL_USERS,
                        accountApproved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                    )
                    userFriendActionViewModel.subscribeUser(selectedUser)
                    showCommonSuccessMessage(getText(R.string.subscribed_on_user_notif_on), requireView())
                }
            }

            is MeeraUserFriendsDialogAction.ThirdItemClick -> Unit
        }
    }

    private fun initUserFriendsDialogClickListener(
        action: MeeraUserFriendsDialogAction,
        selectedUser: FriendsFollowersUiModel
    ) {
        when (action) {
            is MeeraUserFriendsDialogAction.FirstItemClick -> {
                userFriendActionViewModel.addToFriendSocket(
                    model = selectedUser,
                    isAcceptFriendRequest = true
                )
            }

            is MeeraUserFriendsDialogAction.SecondItemClick -> {
                userFriendActionViewModel.logFollowAmplitude(
                    userId = selectedUser.userSimple?.userId ?: 0,
                    screenMode = MODE_SHOW_USER_MUTUAL_USERS,
                    accountApproved = selectedUser.isAccountApproved,
                    topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                )
                userFriendActionViewModel.subscribeUser(selectedUser)
                showCommonSuccessMessage(getText(R.string.subscribed_on_user_notif_on), requireView())
            }

            is MeeraUserFriendsDialogAction.ThirdItemClick -> {
                userFriendActionViewModel.declineUserFriendRequest(selectedUser)
            }
        }
    }

    private fun observeViewEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.userMutualSubscriptionViewEvent.collect { typeEvent ->
                handleEvent(typeEvent)
            }
        }
    }

    /**
     * Event RefreshUserList будет вызываться тогда, когда меняется состояние иконки
     * "Добавить в друзья/Подписаться/Отписаться". Делается запрос на обновление потому что в бэке
     * идет своя сортировка юзера, а так же сортировка связанная с иконками
     */
    private fun handleEvent(typeEvent: UserSubscriptionViewEvent) {
        when (typeEvent) {
            is UserSubscriptionViewEvent.RefreshUserList -> {
                viewModel.getUserMutualSubscription(
                    userId = userId ?: 0,
                    querySearch = viewModel.getSearchInput(),
                    clickedItem = typeEvent.clickedItem
                )
            }

            else -> Unit
        }
    }

    private fun showComplaintInfoSnackbar(msg: Int, action: () -> Unit) {
        pendingDeleteSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(msg),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_NOTIFICATION_SEC,
                        onTimerFinished = {
                            action.invoke()
                            deleteNotificationState.clear()
                        }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                dismissOnClick = true,
                dismissListeners = DismissListeners(
                    dismissListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }
                )
            )
        )

        pendingDeleteSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        pendingDeleteSnackbar?.show()
    }

    companion object {
        private const val USER_ID_KEY = "userIdKey"
        private const val DEFAULT_THRESHOLD = 50

        @JvmStatic
        fun create(userId: Long): MeeraUserMutualSubscriptionFragment {
            val fragment = MeeraUserMutualSubscriptionFragment()
            val args = bundleOf(USER_ID_KEY to userId)
            fragment.arguments = args
            return fragment
        }
    }
}

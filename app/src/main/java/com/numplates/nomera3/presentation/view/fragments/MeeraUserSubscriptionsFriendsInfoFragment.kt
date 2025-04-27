package com.numplates.nomera3.presentation.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import com.numplates.nomera3.databinding.MeeraUserSubscriptionsFriendsInfoFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.toAmplitudePropertyWhere
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
import com.numplates.nomera3.presentation.viewmodel.UserSubscriptionsFriendsInfoViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val COUNT_SHIMMER_ITEM = 8
private const val DELAY_NOTIFICATION_SEC = 4L

class MeeraUserSubscriptionsFriendsInfoFragment : MeeraBaseFragment(
    layout = R.layout.meera_user_subscriptions_friends_info_fragment
), FriendsSubscribersActionCallback {

    private val binding by viewBinding(MeeraUserSubscriptionsFriendsInfoFragmentBinding::bind)

    private val viewModel by viewModels<UserSubscriptionsFriendsInfoViewModel> {
        App.component.getViewModelFactory()
    }
    private val userFriendActionViewModel by viewModels<UserFriendActionViewModel> {
        App.component.getViewModelFactory()
    }
    private var subscriptionsFriendsAdapter: MeeraFriendsFollowersAdapter? = null
    private val shimmerAdapter = MeeraCellShimmerAdapter()
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }

    private var recyclerPagination: RecyclerViewPaginator? = null
    private var successSnackBar: NSnackbar? = null
    var friendInteractor: IOnSubscribersFriendsInteractor? = null

    private var actionMode: Int? = null
    private var userId: Long? = null

    private var pendingDeleteSnackbar: UiKitSnackBar? = null
    private val deleteNotificationState = linkedMapOf<String, NotificationCellUiModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            actionMode = args.getInt(ACTION_MODE_KEY)
            userId = args.getLong(USER_ID_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initRefreshListener()
        initStateObserver()
        observeViewEvent()
        getUserList(false)
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
        getUserList(
            isRefreshing = false, querySearch = input
        )
    }

    override fun logMutualFriendsAmplitude() {
        viewModel.logMutualFriendsTabChanged(actionMode)
    }

    fun dismissSnackBar() {
        successSnackBar?.dismiss()
    }

    private fun getUserList(isRefreshing: Boolean, querySearch: String? = null) {
        viewModel.getUserList(
            userId = userId, actionMode = actionMode, isRefreshing = isRefreshing, querySearch = querySearch
        )
    }

    private fun initRefreshListener() {
        binding?.vgSubscriptionsRefresh?.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                getUserList(true)
            }
        })
    }

    private fun initRecyclerView() {
        subscriptionsFriendsAdapter = MeeraFriendsFollowersAdapter(::initUserClickListener)
        binding?.vgSubscriptionsList?.adapter = subscriptionsFriendsAdapter

        binding?.vgShimmerList?.adapter = shimmerAdapter
        shimmerAdapter.submitList(listShimmer)

        initPagination()
    }

    private fun addUserFragment(userId: Long) {
        dismissSnackBar()
        val screenMode = actionMode ?: 0
        findNavController().safeNavigate(

            resId = R.id.action_meeraMyFriendListFragment_to_userInfoFragment, bundle = bundleOf(
                ARG_USER_ID to userId, ARG_TRANSIT_FROM to screenMode.toAmplitudePropertyWhere().property
            )
        )
    }

    private fun initPagination() {
        recyclerPagination = RecyclerViewPaginator(recyclerView = binding?.vgSubscriptionsList ?: return, loadMore = {
            if (!viewModel.isSearch()) {
                viewModel.getUserList(
                    userId = userId,
                    actionMode = actionMode,
                    offset = subscriptionsFriendsAdapter?.itemCount ?: 0,
                    isRefreshing = false
                )
            }
        }, onLast = {
            viewModel.isLastPage(actionMode)
        }, isLoading = {
            viewModel.isLoading(actionMode)
        }).apply {
            this.threshold = DEFAULT_THRESHOLD
        }
    }

    private fun initStateObserver() {
        viewModel.userSubscriptionsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserFriendsFollowersUiState.SuccessGetList -> {
                    setIsShowPlaceholder(false)
                    setProgressState(state.isShowProgress)
                    updateRefreshing(state.isRefreshing)
                    if (viewModel.isMe(userId)) friendInteractor?.onSubscribersCount(state.friendsList.size)
                    subscriptionsFriendsAdapter?.submitList(state.friendsList)
                }

                is UserFriendsFollowersUiState.ListEmpty -> {
                    setProgressState(false)
                    updateRefreshing(false)
                    setIsShowPlaceholder(true)
                    subscriptionsFriendsAdapter?.submitList(emptyList())
                    setPlaceholderByState(state.isSearch)
                }
            }
        }
    }

    private fun setPlaceholderByState(isSearch: Boolean) {
        if (isSearch) {
            setPlaceholder(
                placeholderText = getString(R.string.meera_settings_empty_state),
                placeholderIcon = context?.drawable(R.drawable.friends_search_empty) ?: return
            )
        } else {
            setPlaceholderByActionMode()
        }
    }

    private fun setPlaceholderByActionMode() {
        when (actionMode) {
            MODE_SHOW_USER_SUBSCRIPTIONS -> {
                setPlaceholder(
                    placeholderText = getString(R.string.subscription_list_is_empty),
                    placeholderIcon = context?.drawable(R.drawable.ic_i_dont_know) ?: return
                )
            }

            MODE_SHOW_USER_FRIENDS -> {
                setPlaceholder(
                    placeholderText = getString(R.string.friends_list_is_empty),
                    placeholderIcon = context?.drawable(R.drawable.ic_i_dont_know) ?: return
                )
            }

            MODE_SHOW_USER_SUBSCRIBERS -> {
                setPlaceholder(
                    placeholderText = getString(R.string.subscibers_list_is_empty),
                    placeholderIcon = context?.drawable(R.drawable.ic_i_dont_know) ?: return
                )
            }
        }
    }

    private fun setPlaceholder(
        placeholderText: String, placeholderIcon: Drawable
    ) {
        binding?.tvPlaceholder?.text = placeholderText
        binding?.ivPlaceholder?.setImageDrawable(placeholderIcon)
    }

    private fun updateRefreshing(isRefresh: Boolean) {
        binding?.vgSubscriptionsRefresh?.setRefreshing(isRefresh)
        binding.vgSubscriptionsRefresh.setRefreshEnable(true)

    }

    private fun setIsShowPlaceholder(isShow: Boolean) {
        if (binding?.vgSubscriptionsPlaceholder?.isVisible != isShow) {
            binding?.vgSubscriptionsPlaceholder?.isVisible = isShow
        }
    }

    private fun setProgressState(isShowProgress: Boolean) {
        if (isShowProgress) {
            binding?.vgSubscriptionsList?.gone()
            binding?.vgShimmerList?.visible()
        } else {
            binding?.vgSubscriptionsList?.visible()
            binding?.vgShimmerList?.gone()
        }
    }

    private fun observeViewEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.userSubscriptionViewEvent.collect { typeEvent ->
                handleEvent(typeEvent)
            }
        }
    }

    /**
     * Event RefreshUserList будет вызываться тогда, когда меняется состояние иконки
     * "Добавить в друзья/Подписаться/Отписаться". Делается запрос на обновление потому что в бэке
     * идет своя сортировка юзера, а так же сортировка связанная с иконками.
     * Event вызывается в нескольких случаях:
     * 1.В профиле другого юзера, когда мы меняем состояние "Добавить в друзья/Удалить/Отписаться"
     * 2.Когда мы меняем в пределах данного класса, но обновятся 3 instance данного фрагмента, которые имеют состояния:
     *   MODE_SHOW_USER_FRIENDS, MODE_SHOW_USER_SUBSCRIBERS, MODE_SHOW_USER_SUBSCRIPTIONS
     */
    private fun handleEvent(typeEvent: UserSubscriptionViewEvent) {
        when (typeEvent) {
            is UserSubscriptionViewEvent.RefreshUserList -> {
                viewModel.getUserList(
                    userId = userId,
                    actionMode = actionMode,
                    querySearch = viewModel.getSearchInput(),
                    clickedItem = typeEvent.clickedItem
                )
            }

            else -> Unit
        }
    }

    private fun showUserBottomSheetAction(model: FriendsFollowersUiModel) {
        val isSubscribed = model.userSimple?.settingsFlags?.subscription_on.toBoolean()

        if (model.userSimple?.settingsFlags?.friendStatus.toBoolean()) {
            MeeraUserFriendActionMenuBottomSheetBuilder().setHeader(R.string.actions)
                .setFirstCellText(R.string.accept_request).setFirstCellIcon(R.drawable.ic_outlined_following_m)
                .setSecondCellText(R.string.user_info_subscribe).setSecondCellIcon(R.drawable.ic_outlined_subscribe_m)
                .setThirdCellText(R.string.reject_friend_request).setThirdCellIcon(R.drawable.ic_outlined_user_delete_m)
                .setClickListener {
                    initUserFriendsDialogClickListener(it, model)
                }.show(childFragmentManager)
        } else {
            MeeraUserFriendActionMenuBottomSheetBuilder().setHeader(R.string.actions)
                .setFirstCellText(R.string.add_to_friends).setFirstCellIcon(R.drawable.ic_outlined_user_add_m)
                .setSecondCellText(
                    if (!isSubscribed) R.string.user_info_subscribe else R.string.unsubscribe
                ).setSecondCellIcon(
                    if (!isSubscribed) R.drawable.ic_outlined_subscribe_m else R.drawable.ic_outlined_unfollow_m
                ).setClickListener {
                    initDialogClickListener(it, isSubscribed, model)
                }.show(childFragmentManager)
        }
    }

    private fun initDialogClickListener(
        action: MeeraUserFriendsDialogAction, isSubscribed: Boolean, selectedUser: FriendsFollowersUiModel
    ) {
        when (action) {
            is MeeraUserFriendsDialogAction.FirstItemClick -> {
                selectedUser.userSimple?.userId?.let { userId ->
                    userFriendActionViewModel.logAddToFriendAmplitude(
                        userId = userId,
                        screenMode = actionMode ?: MODE_SHOW_USER_FRIENDS,
                        approved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple.topContentMaker.toBoolean()
                    )
                    showComplaintInfoSnackbar(R.string.request_send_notiff_on) {
                        userFriendActionViewModel.addToFriendSocket(
                            model = selectedUser, isAcceptFriendRequest = false
                        )
                    }
                }
            }

            is MeeraUserFriendsDialogAction.SecondItemClick -> {
                if (isSubscribed) {
                    userFriendActionViewModel.logUnfollowAmplitudeAction(
                        userId = selectedUser.userSimple?.userId ?: 0,
                        screenMode = actionMode ?: MODE_SHOW_USER_FRIENDS,
                        accountApproved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                    )
                    userFriendActionViewModel.unsubscribeUser(selectedUser)
                    showCommonSuccessMessage(getText(R.string.disabled_new_post_notif), requireView())
                } else {
                    userFriendActionViewModel.logFollowAmplitude(
                        userId = selectedUser.userSimple?.userId ?: 0,
                        screenMode = actionMode ?: MODE_SHOW_USER_FRIENDS,
                        accountApproved = selectedUser.isAccountApproved,
                        topContentMaker = selectedUser.userSimple?.topContentMaker.toBoolean()
                    )
                    userFriendActionViewModel.subscribeUser(selectedUser)
                    showCommonSuccessMessage(getText(R.string.request_send_notiff_on), requireView())
                }
            }

            is MeeraUserFriendsDialogAction.ThirdItemClick -> Unit
        }
    }

    private fun initUserFriendsDialogClickListener(
        action: MeeraUserFriendsDialogAction, selectedUser: FriendsFollowersUiModel
    ) {
        when (action) {
            is MeeraUserFriendsDialogAction.FirstItemClick -> {
                userFriendActionViewModel.addToFriendSocket(
                    model = selectedUser, isAcceptFriendRequest = true
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

    private fun initUserClickListener(action: MeeraFriendsFollowerAction) {
        when (action) {
            is MeeraFriendsFollowerAction.AcceptRequestFriendClick -> {
                showUserBottomSheetAction(action.model)
            }

            is MeeraFriendsFollowerAction.AddFriendsClick -> {
                showUserBottomSheetAction(action.model)
            }

            is MeeraFriendsFollowerAction.UserClick -> addUserFragment(action.userId)
        }
    }

    private fun showComplaintInfoSnackbar(msg: Int, action: () -> Unit) {
        pendingDeleteSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(msg), loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_NOTIFICATION_SEC, onTimerFinished = {
                            action.invoke()
                            deleteNotificationState.clear()
                        }), buttonActionText = getText(R.string.cancel), buttonActionListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                dismissOnClick = true,
                dismissListeners = DismissListeners(
                    dismissListener = {
                        pendingDeleteSnackbar?.dismiss()
                    })
            )
        )

        pendingDeleteSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        pendingDeleteSnackbar?.show()
    }

    interface IOnSubscribersFriendsInteractor {
        fun onSubscribersCount(count: Int) {}
    }

    companion object {
        private const val ACTION_MODE_KEY = "actionModeKey"
        private const val USER_ID_KEY = "userIdKey"
        private const val DEFAULT_THRESHOLD = 50

        @JvmStatic
        fun create(userId: Long, actionMode: Int): MeeraUserSubscriptionsFriendsInfoFragment {
            val fragment = MeeraUserSubscriptionsFriendsInfoFragment()
            val bundle = bundleOf(
                ACTION_MODE_KEY to actionMode, USER_ID_KEY to userId
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}

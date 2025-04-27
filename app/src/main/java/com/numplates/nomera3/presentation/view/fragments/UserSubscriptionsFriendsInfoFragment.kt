package com.numplates.nomera3.presentation.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.dp
import com.meera.core.extensions.drawable
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserSubscriptionsFriendsInfoBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.toAmplitudePropertyWhere
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.adapter.FriendsFollowersAdapter
import com.numplates.nomera3.presentation.view.adapter.SubscriberFriendActionCallback
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendsFollowersUiState
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.UserFriendActionMenuBottomSheet
import com.numplates.nomera3.presentation.viewmodel.UserSubscriptionsFriendsInfoViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

const val MODE_SHOW_USER_FRIENDS = 1
const val MODE_SHOW_USER_SUBSCRIBERS = 2
const val MODE_SHOW_USER_SUBSCRIPTIONS = 3

class UserSubscriptionsFriendsInfoFragment :
    BaseFragmentNew<FragmentUserSubscriptionsFriendsInfoBinding>(),
    FriendsSubscribersActionCallback {

    private val viewModel by viewModels<UserSubscriptionsFriendsInfoViewModel> {
        App.component.getViewModelFactory()
    }
    private var subscriptionsFriendsAdapter: FriendsFollowersAdapter? = null
    private var recyclerPagination: RecyclerViewPaginator? = null
    private var successSnackBar: NSnackbar? = null

    private var actionMode: Int? = null
    private var userId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            actionMode = args.getInt(ACTION_MODE_KEY)
            userId = args.getLong(USER_ID_KEY)
        }
    }

    override val bindingInflater: (
        LayoutInflater, ViewGroup?, Boolean
    ) -> FragmentUserSubscriptionsFriendsInfoBinding
        get() = FragmentUserSubscriptionsFriendsInfoBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initRefreshListener()
        initStateObserver()
        observeViewEvent()
        getUserList(false)
    }

    override fun onStopFragment() {
        super.onStopFragment()
        dismissSnackBar()
    }

    override fun onStop() {
        super.onStop()
        dismissSnackBar()
    }

    override fun dismissSuccessSnackBar() {
        dismissSnackBar()
    }

    override fun search(input: String) {
        getUserList(
            isRefreshing = false,
            querySearch = input
        )
    }

    override fun logMutualFriendsAmplitude() {
        viewModel.logMutualFriendsTabChanged(actionMode)
    }

    fun dismissSnackBar() {
        successSnackBar?.dismiss()
    }

    fun searchOpen() {
        viewModel.searchOpenStateChanged(true)
    }

    fun searchClosed() {
        viewModel.searchOpenStateChanged(false)
        viewModel.clearSearchList()
        viewModel.replaceSearchListToCurrentList()
    }

    fun setSwipeRefreshEnabled(isEnabled: Boolean) {
        binding?.vgSubscriptionsRefresh?.isEnabled = isEnabled
    }

    private fun getUserList(isRefreshing: Boolean, querySearch: String? = null) {
        viewModel.getUserList(
            userId = userId,
            actionMode = actionMode,
            isRefreshing = isRefreshing,
            querySearch = querySearch
        )
    }

    private fun initRefreshListener() {
        binding?.vgSubscriptionsRefresh?.setOnRefreshListener {
            getUserList(true)
        }
    }

    private fun initList() {
        subscriptionsFriendsAdapter = FriendsFollowersAdapter(AdapterInteractor())
        binding?.vgSubscriptionsList?.adapter = subscriptionsFriendsAdapter
        binding?.vgSubscriptionsList?.addItemDecoration(
            HorizontalLineDivider(
                dividerDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.shared_divider_item_shape
                ) ?: return,
                paddingLeft = DEFAULT_DIVIDER_HORIZONTAL_PADDING.dp,
                paddingRight = DEFAULT_DIVIDER_HORIZONTAL_PADDING.dp
            )
        )
        binding?.vgSubscriptionsList?.itemAnimator = null
        initPagination()
    }

    private fun addUserFragment(model: FriendsFollowersUiModel) {
        dismissSnackBar()
        val screenMode = actionMode ?: 0
        add(
            UserInfoFragment(),
            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, model.userSimple?.userId),
            Arg(ARG_TRANSIT_FROM, screenMode.toAmplitudePropertyWhere().property)
        )
    }

    private fun initPagination() {
        recyclerPagination = RecyclerViewPaginator(
            recyclerView = binding?.vgSubscriptionsList ?: return,
            loadMore = {
                if (!viewModel.isSearch()) {
                    viewModel.getUserList(
                        userId = userId,
                        actionMode = actionMode,
                        offset = subscriptionsFriendsAdapter?.itemCount ?: 0,
                        isRefreshing = false
                    )
                }
            },
            onLast = {
                viewModel.isLastPage(actionMode)
            },
            isLoading = {
                viewModel.isLoading(actionMode)
            }
        ).apply {
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
                    subscriptionsFriendsAdapter?.updateFriends(state.friendsList)
                }
                is UserFriendsFollowersUiState.ListEmpty -> {
                    setProgressState(false)
                    updateRefreshing(false)
                    setIsShowPlaceholder(true)
                    subscriptionsFriendsAdapter?.clearList()
                    setPlaceholderByState(state.isSearch)
                }
            }
        }
    }

    private fun setPlaceholderByState(isSearch: Boolean) {
        if (isSearch) {
            setPlaceholder(
                placeholderText = getString(R.string.friendlist_search_results_empty),
                placeholderIcon = context?.drawable(R.drawable.ic_empty_search_noomeera) ?: return
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
                    placeholderIcon = context?.drawable(R.drawable.ic_placeholder_user) ?: return
                )
            }
            MODE_SHOW_USER_FRIENDS -> {
                setPlaceholder(
                    placeholderText = getString(R.string.friends_list_is_empty),
                    placeholderIcon = context?.drawable(R.drawable.ic_placeholder_user) ?: return
                )
            }
            MODE_SHOW_USER_SUBSCRIBERS -> {
                setPlaceholder(
                    placeholderText = getString(R.string.subscibers_list_is_empty),
                    placeholderIcon = context?.drawable(R.drawable.ic_placeholder_subscription_empty)
                        ?: return
                )
            }
        }
    }

    private fun setPlaceholder(
        placeholderText: String,
        placeholderIcon: Drawable
    ) {
        binding?.tvPlaceholder?.text = placeholderText
        binding?.ivPlaceholder?.setImageDrawable(placeholderIcon)
    }

    private fun updateRefreshing(isRefresh: Boolean) {
        binding?.vgSubscriptionsRefresh?.isRefreshing = isRefresh
    }

    private fun setIsShowPlaceholder(isShow: Boolean) {
        if (binding?.vgSubscriptionsPlaceholder?.isVisible != isShow) {
            binding?.vgSubscriptionsPlaceholder?.isVisible = isShow
        }
    }

    private fun setProgressState(isShowProgress: Boolean) {
        binding?.pbSubscriptions?.isVisible = isShowProgress
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

    private fun showSuccessMessage(msg: String?) {
        successSnackBar = NSnackbar.with(act)
            .typeSuccess()
            .marginBottom(SNACK_BAR_MARGIN)
            .text(msg)
            .durationLong()
            .show()
    }

    private fun showErrorMessage(errorMessage: String?) {
        NToast.with(view)
            .typeError()
            .text(errorMessage)
            .show()
    }

    private fun showUserBottomSheetAction(model: FriendsFollowersUiModel) {
        UserFriendActionMenuBottomSheet(
            selectedUser = model,
            fragment = this,
            screenMode = actionMode ?: MODE_SHOW_USER_FRIENDS,
            successFinishListener = { showSuccessMessage(getString(it)) },
            errorFinishListener = { showErrorMessage(getString(it)) },
        ).show()
    }

    inner class AdapterInteractor : SubscriberFriendActionCallback {
        override fun onUserClicked(model: FriendsFollowersUiModel) =
            addUserFragment(model)

        // Вызывается, когда юзер кликает на иконку "Добавить в друзья/Принять ли заявку"
        override fun onUserActionIconClicked(model: FriendsFollowersUiModel) =
            showUserBottomSheetAction(model)
    }

    companion object {
        private const val ACTION_MODE_KEY = "actionModeKey"
        private const val USER_ID_KEY = "userIdKey"
        private const val DEFAULT_THRESHOLD = 50
        private const val DEFAULT_DIVIDER_HORIZONTAL_PADDING = 20
        private const val SNACK_BAR_MARGIN = 64

        @JvmStatic
        fun create(userId: Long, actionMode: Int): UserSubscriptionsFriendsInfoFragment {
            val fragment = UserSubscriptionsFriendsInfoFragment()
            val bundle = bundleOf(
                ACTION_MODE_KEY to actionMode,
                USER_ID_KEY to userId
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}

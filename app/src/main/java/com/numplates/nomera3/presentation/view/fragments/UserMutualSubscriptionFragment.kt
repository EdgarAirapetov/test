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
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserMutualSubscriptionBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
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
import com.numplates.nomera3.presentation.viewmodel.UserMutualSubscriptionViewModel
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

internal const val MODE_SHOW_USER_MUTUAL_USERS = 4

class UserMutualSubscriptionFragment :
    BaseFragmentNew<FragmentUserMutualSubscriptionBinding>(), FriendsSubscribersActionCallback {

    private val viewModel by viewModels<UserMutualSubscriptionViewModel> {
        App.component.getViewModelFactory()
    }
    private var subscriptionAdapter: FriendsFollowersAdapter? = null
    private var recyclerPagination: RecyclerViewPaginator? = null
    private var successSnackBar: NSnackbar? = null

    private var userId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            userId = args.getLong(USER_ID_KEY)
        }
    }

    override val bindingInflater: (
        LayoutInflater,
        ViewGroup?,
        Boolean
    ) -> FragmentUserMutualSubscriptionBinding
        get() = FragmentUserMutualSubscriptionBinding::inflate

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

    override fun onStopFragment() {
        super.onStopFragment()
        dismissSnackBar()
    }

    override fun dismissSuccessSnackBar() {
        dismissSnackBar()
    }

    override fun search(input: String) {
        getMutualFriends(
            isRefreshing = false,
            querySearch = input
        )
    }

    override fun logMutualFriendsAmplitude() {
        viewModel.logMutualFriendsAmplitude()
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
        binding?.vgUserMutualRefresh?.isEnabled = isEnabled
    }

    fun dismissSnackBar() {
        successSnackBar?.dismiss()
    }

    private fun initList() {
        subscriptionAdapter = FriendsFollowersAdapter(AdapterInteractor())
        binding?.vgUserMutualList?.adapter = subscriptionAdapter
        binding?.vgUserMutualList?.addItemDecoration(
            HorizontalLineDivider(
                dividerDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.shared_divider_item_shape
                ) ?: return,
                paddingLeft = DEFAULT_DIVIDER_HORIZONTAL_PADDING.dp,
                paddingRight = DEFAULT_DIVIDER_HORIZONTAL_PADDING.dp
            )
        )
        binding?.vgUserMutualList?.itemAnimator = null
        initPagination()
    }

    private fun initRefreshListener() {
        binding?.vgUserMutualRefresh?.setOnRefreshListener {
            getMutualFriends(
                isRefreshing = true
            )
        }
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

    private fun addUserFragment(model: FriendsFollowersUiModel) {
        dismissSnackBar()
        add(
            UserInfoFragment(),
            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, model.userSimple?.userId),
            Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.MUTUAL_FOLLOWS.property)
        )
    }

    private fun initStateObserver() {
        viewModel.sameSubscriptionState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is UserFriendsFollowersUiState.SuccessGetList -> {
                    hidePlaceholder()
                    setProgressState(uiState.isShowProgress)
                    setRefreshing(uiState.isRefreshing)
                    subscriptionAdapter?.updateFriends(uiState.friendsList)
                }
                is UserFriendsFollowersUiState.ListEmpty -> {
                    setProgressState(false)
                    setRefreshing(false)
                    setPlaceholderByState(uiState.isSearch)
                    subscriptionAdapter?.clearList()
                }
            }
        }
    }

    private fun setRefreshing(isRefresh: Boolean) {
        binding?.vgUserMutualRefresh?.isRefreshing = isRefresh
    }

    private fun setProgressState(isShowProgress: Boolean) {
        binding?.pbUserMutual?.isVisible = isShowProgress
    }

    private fun hidePlaceholder() {
        binding?.vgUserMutualPlaceholder?.gone()
    }

    private fun setPlaceholderByState(isSearch: Boolean) {
        binding?.vgUserMutualPlaceholder?.visible()
        if (isSearch) {
            setPlaceholderText(
                placeholderText = getString(R.string.friendlist_search_results_empty),
                placeholderIcon = context?.drawable(R.drawable.ic_empty_search_noomeera) ?: return
            )
        } else {
            setPlaceholderText(
                placeholderText = getString(R.string.mutual_friends_list_empty),
                placeholderIcon = context?.drawable(R.drawable.ic_placeholder_user) ?: return
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
        UserFriendActionMenuBottomSheet(
            selectedUser = model,
            fragment = this,
            screenMode = MODE_SHOW_USER_MUTUAL_USERS,
            successFinishListener = { showSuccessMessage(getString(it)) },
            errorFinishListener = { showErrorMessage(getString(it)) }
        ).show()
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

    inner class AdapterInteractor : SubscriberFriendActionCallback {
        override fun onUserClicked(model: FriendsFollowersUiModel) =
            addUserFragment(model)

        override fun onUserActionIconClicked(model: FriendsFollowersUiModel) =
            handleIconIconClicked(model)
    }

    companion object {
        private const val USER_ID_KEY = "userIdKey"
        private const val DEFAULT_DIVIDER_HORIZONTAL_PADDING = 20
        private const val DEFAULT_THRESHOLD = 50
        private const val SNACK_BAR_MARGIN = 64

        @JvmStatic
        fun create(userId: Long): UserMutualSubscriptionFragment {
            val fragment = UserMutualSubscriptionFragment()
            val args = bundleOf(USER_ID_KEY to userId)
            fragment.arguments = args
            return fragment
        }
    }
}

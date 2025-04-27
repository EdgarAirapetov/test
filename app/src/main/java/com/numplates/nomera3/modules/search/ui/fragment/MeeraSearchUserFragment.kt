package com.numplates.nomera3.modules.search.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentSearchUsersBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraPeoplesContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.PeoplesContentDecorator
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getPeopleMainContentPreload
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.search.number.ui.fragment.SearchNumberListener
import com.numplates.nomera3.modules.search.ui.entity.state.UserSearchViewState
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.TOP_PEOPLE_CONTENT_POSITION
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


private const val PAGINATION_BUFFER_SIZE = 15
private const val DELAY_KEYBOARD_OBSERVER = 500L

class MeeraSearchUserFragment : MeeraBaseFragment(R.layout.meera_fragment_search_users),
    SearchScreenContext,
    SearchNumberListener,
    BasePermission by BasePermissionDelegate() {

    companion object {
        fun newInstance(openedFromPeoples: Boolean): MeeraSearchUserFragment {
            val fragment = MeeraSearchUserFragment()
            fragment.arguments = bundleOf(KEY_FROM_PEOPLES to openedFromPeoples)
            return fragment
        }
    }

    private val binding by viewBinding(MeeraFragmentSearchUsersBinding::bind)

    private val viewModel: SearchUserViewModel by viewModels(
        factoryProducer = { App.component.getViewModelFactory() },
        ownerProducer = { requireParentFragment() }
    )

    private var contentAdapter: MeeraPeoplesContentAdapter? = null
    private var recyclerPagination: RecyclerViewPaginator? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy { SyncContactsDialogDelegate(childFragmentManager) }

    private var undoSnackBar: UiKitSnackBar? = null
    private var messageSnackBar: UiKitSnackBar? = null
    private var currentState: SearchScreenContext.ScreenState = SearchScreenContext.ScreenState.Default

    private val showSyncContactsWelcome = false

    private val openedFromPeoples by lazy {
        arguments?.getBoolean(KEY_FROM_PEOPLES, false) ?: false
    }

    override fun onSearchNumberOpened() {
        keyboardHeightProvider?.release()
    }

    override fun onSearchNumberClosed() {
        lifecycleScope.launch {
            doDelayed(DELAY_KEYBOARD_OBSERVER) {
                startKeyboardHeightObserving()
            }
        }
    }

    override fun setScreenState(state: SearchScreenContext.ScreenState) {
        setPlaceHolderType(state)
        currentState = state
    }

    override fun getScreenState(): SearchScreenContext.ScreenState = currentState

    override fun blankSearch() {
        showAndLoadSearchScreen(String.empty())
    }

    override fun clearCurrentResult() = Unit

    override fun search(query: String) {
        val previousQuery = viewModel.getSearchQuery()
        if (query != AT_SIGN) {
            searchRequest(query, previousQuery)
        }
    }

    override fun hideMessages() {
        hideSnackBar()
        undoClearRecent()
    }

    override fun exitScreen() {
        viewModel.clearRecentGlobalIfExists()
    }

    override fun getFragmentLifecycle(): Lifecycle = lifecycle

    override fun showAndLoadSearchScreen(numberSearchParameters: NumberSearchParameters) {
        viewModel.searchUserByNumber(numberSearchParameters, 0)
        setScreenState(SearchScreenContext.ScreenState.Result)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.searchUserState.value?.contentList?.isNotEmpty().isTrue()) {
            viewModel.init(openedFromPeoples)
        }
        observeState()
        initListeners()
        initView()
        initUtils()
    }

    override fun onStart() {
        super.onStart()
        binding.vgPeoplesList.onStart()
        startKeyboardHeightObserving()
        initPeopleWelcome()
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
        binding.vgPeoplesList.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentAdapter = null
        binding.vgPeoplesList.onDestroyView()
        binding.vgPeoplesList.releasePlayer()
    }

    private fun initView() {
        initContentList()
    }

    private fun initUtils() {
        initPermission()
        initKeyboardHeightProvider()
    }

    private fun initRecyclerPagination() {
        recyclerPagination = RecyclerViewPaginator(
            recyclerView = binding.vgPeoplesList,
            onLast = { if (isInDefaultState()) viewModel.isTopContentLast || openedFromPeoples else viewModel.getPagingProperties().isLastPage },
            isLoading = { if (isInDefaultState()) viewModel.isTopContentLoading else viewModel.getPagingProperties().isLoading },
            loadMore = {
                when {
                    isInDefaultState() && contentAdapter?.currentList.isNullOrEmpty().not() -> {
                        viewModel.handleContentAction(FriendsContentActions.GetNextTopUsersAction)
                    }

                    contentAdapter?.currentList.isNullOrEmpty().not() -> {
                        viewModel.loadMore()
                    }
                }
            }
        ).apply {
            threshold = PAGINATION_BUFFER_SIZE
        }
    }

    private fun isInDefaultState(): Boolean = currentState == SearchScreenContext.ScreenState.Default

    private fun initListeners() {
        observeEffect()
        initRefreshListener()
        initRecyclerPagination()
        initPreloadImagesListener()
    }

    private fun initRefreshListener() {
        binding.vgPeoplesRefresh.setOnRefreshListener {
            viewModel.handleContentAction(FriendsContentActions.OnRefreshContentBySwipe)
        }
    }

    private fun initContentList() {
        contentAdapter = MeeraPeoplesContentAdapter(
            actionListener = viewModel::handleContentAction,
            mediaContentScrollListener = { innerPosition, rootPosition ->
                binding.vgPeoplesList.resetStateAndPlayVideo(
                    innerPosition = innerPosition,
                    rootPosition = rootPosition
                )
            },
            recommendedPeoplePaginationHandler = RecommendedPeoplePaginationHandlerImpl()
        )
        binding.vgPeoplesList.adapter = contentAdapter
        binding.vgPeoplesList.itemAnimator = null
        binding.vgPeoplesList.addItemDecoration(PeoplesContentDecorator())
    }

    private fun scrollToPosition(position: Int) {
        binding.vgPeoplesList.scrollToPosition(position)
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.peoplesContentEvent.collect(::handleUiEffect)
                }
            }
        }
    }

    private fun observeState() {
        viewModel.searchUserState.observe(viewLifecycleOwner, ::handleState)
    }

    private fun handleState(state: UserSearchViewState) {
        setRefreshing(state.isRefreshing ?: false)
        setProgressState(state.showProgressBar ?: false)
        binding.vgEmptyMessageContainer.isVisible = state.showPlaceholder ?: false
        val layoutManager = binding.vgPeoplesList.layoutManager as? LinearLayoutManager?
        val previousPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
        contentAdapter?.submitList(state.contentList) {
            if (previousPosition == TOP_PEOPLE_CONTENT_POSITION) {
                layoutManager.scrollToPositionWithOffset(TOP_PEOPLE_CONTENT_POSITION, TOP_PEOPLE_CONTENT_POSITION)
            }
            if (state.contentList?.size == 1) {
                binding.vgEmptyMessageContainer.setMargins(top = MARGIN_PLACEHOLDER_WITH_RECENTS.dp)
            } else {
                binding.vgEmptyMessageContainer.setMargins(top = 0.dp)
            }
        }
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding.vgPeoplesRefresh.isRefreshing = isRefreshing
    }

    private fun setProgressState(needShow: Boolean) {
        if (binding.progressBar.isVisible != needShow) {
            binding.progressBar.isVisible = needShow
        }
    }

    private fun handleUiEffect(effect: PeopleUiEffect) {
        when (effect) {
            is PeopleUiEffect.ShowContactsHasBeenSyncDialogUiEffect -> {
                showContactsHasBeenSyncDialog(effect.syncCount)
            }
            PeopleUiEffect.ShowSyncContactsDialogUiEffect -> {
                showSynchronizeContactsDialog()
            }
            PeopleUiEffect.RequestReadContactsPermissionUiEffect -> {
                requestReadContactsPermission()
            }

            PeopleUiEffect.ShowSyncDialogPermissionDenied -> {
                showSyncDialogPermissionDenied()
            }

            is PeopleUiEffect.ShowErrorToast -> {
                showErrorToast(effect.message)
            }

            is PeopleUiEffect.ShowSuccessToast -> {
                showSuccessToast(effect.message)
            }

            is PeopleUiEffect.OpenUserProfile -> {
                handleOpenProfile(effect.userId)
            }

            is PeopleUiEffect.ScrollToPositionEffect -> {
                scrollToPosition(effect.position)
            }

            is PeopleUiEffect.ClearRelatedUserPageUiEffect -> {
                binding.vgPeoplesList.resetHorizontalListPageByPosition(effect.position)
            }

            is PeopleUiEffect.AddUserFromSearch -> {
                addFriendDialog(effect.user)
            }

            is PeopleUiEffect.ShowClearRecentSnackBar -> {
                showClearRecentTimerSnackBar(effect.delaySec) {
                    viewModel.undoClearRecent()
                }
            }
            else -> Unit
        }
    }

    private fun addFriendDialog(user: UserSearchResultUiEntity) {
        val menu = MeeraMenuBottomSheet(context)

        when (user.friendStatus) {
            FRIEND_STATUS_NONE -> {
                menu.addItem(
                    R.string.add_to_friends,
                    R.drawable.ic_outlined_user_add_m,
                ) {
                    viewModel.addUserToFriend(user)
                }
                addSubscriptionButtonsInFriendDialog(menu, user, true)
            }
            FRIEND_STATUS_INCOMING -> {
                menu.addItem(
                    R.string.accept_request,
                    R.drawable.ic_outlined_following_m,
                ) {
                    viewModel.acceptUserFriendRequest(user)
                }
                addSubscriptionButtonsInFriendDialog(menu, user, false)
                menu.addItem(
                    R.string.reject_friend_request,
                    R.drawable.ic_outlined_user_delete_m,
                    R.color.uiKitColorAccentWrong
                ) {
                    viewModel.declineUserFriendRequest(user)
                }
            }
        }
        menu.show(childFragmentManager)
    }

    private fun addSubscriptionButtonsInFriendDialog(
        menuBottomSheet: MeeraMenuBottomSheet,
        user: UserSearchResultUiEntity,
        showSubscribeButton: Boolean
    ) {
        when {
            !user.isSubscribed && showSubscribeButton -> {
                menuBottomSheet.addItem(
                    R.string.general_subscribe,
                    R.drawable.ic_outlined_follow_m,
                ) {
                    viewModel.subscribeUser(user)
                }
            }
            user.isSubscribed -> {
                menuBottomSheet.addItem(
                    R.string.unsubscribe,
                    R.drawable.ic_outlined_unfollow_m,
                ) {
                    viewModel.unsubscribeUser(user)
                }
            }
        }
    }

    private fun handleOpenProfile(userId: Long) {
        findNavController().safeNavigate(
            R.id.action_meeraSearchMainFragment_to_user_graph,
            bundleOf(IArgContainer.ARG_USER_ID to userId)
        )
    }

    private fun showSuccessToast(@StringRes messageRes: Int) {
        messageSnackBar = UiKitSnackBar.make(
            requireView(), SnackBarParams(
                SnackBarContainerUiState(
                    messageText = getText(messageRes),
                    avatarUiState = AvatarUiState.SuccessIconState
                )
            )
        )
        messageSnackBar?.show()
    }

    private fun showErrorToast(@StringRes errorMessageRes: Int) {
        UiKitSnackBar.make(
            requireView(), SnackBarParams(
                SnackBarContainerUiState(
                    messageText = getText(errorMessageRes),
                    avatarUiState = AvatarUiState.ErrorIconState
                )
            )
        )
    }

    private fun initPreloadImagesListener() {
        val adapter = contentAdapter ?: return
        val preloadListener = getPeopleMainContentPreload(adapter)
        binding.vgPeoplesList.addOnScrollListener(preloadListener)
    }

    private fun initPermission() {
        initPermissionDelegate(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner
        )
    }

    private fun initKeyboardHeightProvider() {
        binding.root.let { root -> keyboardHeightProvider = KeyboardHeightProvider(root) }
    }

    private fun startKeyboardHeightObserving() {
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { height ->
            val isKeyboardOpened = height > 0
            viewModel.handleContentAction(FriendsContentActions.KeyboardVisibilityChanged(isKeyboardOpened))
        }
    }

    private fun showContactsHasBeenSyncDialog(syncCount: Int) {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            positiveButtonAction = { viewModel.handleContentAction(FriendsContentActions.OnSuccessSyncContactsClosedButtonUiAction(syncCount)) },
            closeDialogDismissListener = { viewModel.handleContentAction(FriendsContentActions.OnSuccessSyncContactsClosedUiAction(syncCount)) },
            iconRes = R.drawable.meera_ic_sync_contacts_done,
            isAppRedesigned = true
        )
    }

    private fun showSynchronizeContactsDialog() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.contacts_synchronization_allow_access,
            descriptionRes = R.string.contacts_sync_allow_access_description,
            positiveButtonRes = R.string.allow,
            positiveButtonAction = {
                viewModel.handleContentAction(
                    FriendsContentActions.OnDialogSyncContactsPositiveButtonClickedUiAction(
                        showSyncContactsWelcome
                    )
                )
            },
            negativeButtonRes = R.string.general_later,
            negativeButtonAction = { logSyncContactsDialogClosed() },
            closeDialogDismissListener = { logSyncContactsDialogClosed() },
            iconRes = R.drawable.meera_ic_sync_contacts_dialog,
            isAppRedesigned = true
        )
    }

    private fun logSyncContactsDialogClosed() {
        viewModel.handleContentAction(
            FriendsContentActions.LogSyncContactsDialogClosedUiAction(
                showSyncContactsWelcome
            )
        )
    }

    private fun logSyncContactsDialogGoSettingsClosed() {
        viewModel.handleContentAction(
            FriendsContentActions.LogSyncContactsGoToSettingsClosedUiAction(
                showSyncContactsWelcome
            )
        )
    }

    private fun requestReadContactsPermission() {
        setPermissions(
            permission = Manifest.permission.READ_CONTACTS,
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.handleContentAction(FriendsContentActions.ReadContactsPermissionGrantedUiAction)
                }

                override fun onDenied() {
                    val deniedAndNoRationaleNeededAfterRequest =
                        !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                    viewModel.handleContentAction(
                        FriendsContentActions.ContactsPermissionDeniedUiAction(deniedAndNoRationaleNeededAfterRequest)
                    )
                }

                override fun onError(error: Throwable?) {
                    Timber.e(error)
                }
            }
        )
    }

    private fun showSyncDialogPermissionDenied() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.contacts_synchronization_allow_access,
            descriptionRes = R.string.contacts_sync_allow_in_settings_description,
            positiveButtonRes = R.string.go_to_settings,
            positiveButtonAction = {
                viewModel.handleContentAction(
                    FriendsContentActions.LogSyncContactsGoToSettings(showSyncContactsWelcome)
                )
                sendUserToAppSettings()
            },
            negativeButtonRes = R.string.general_later,
            negativeButtonAction = { logSyncContactsDialogGoSettingsClosed() },
            closeDialogDismissListener = { logSyncContactsDialogGoSettingsClosed() },
            iconRes = R.drawable.meera_ic_sync_contacts_dialog,
            isAppRedesigned = true
        )
    }

    private fun initPeopleWelcome() {
        viewModel.handleContentAction(
            FriendsContentActions.InitPeopleWelcomeUiAction(
                needToShowWelcome = showSyncContactsWelcome,
                isCalledFromBottomNav = false
            )
        )
    }

    private fun hideSnackBar() {
        messageSnackBar?.dismiss()
    }

    private fun undoClearRecent() {
        if (viewModel.isClearingRecent()) {
            viewModel.forceClearRecent()
            undoSnackBar?.dismiss()
        }
    }

    private fun searchRequest(newQuery: String, previousQuery: String) {
        val isFilterChanged = viewModel.isFilterChanged()
        if (previousQuery == newQuery && newQuery.isNotEmpty() && !isFilterChanged) {
            setScreenState(SearchScreenContext.ScreenState.Result)
            return
        }

        if (newQuery.isNotEmpty() || isFilterChanged) {
            showAndLoadSearchScreen(newQuery)
        } else {
            showAndLoadDefaultScreen()
        }
    }

    private fun showAndLoadDefaultScreen() {
        setScreenState(SearchScreenContext.ScreenState.Default)
        viewModel.reload()
    }

    private fun showAndLoadSearchScreen(query: String) {
        setScreenState(SearchScreenContext.ScreenState.Result)
        viewModel.search(query)
    }

    private fun setPlaceHolderType(screenState: SearchScreenContext.ScreenState) {
        when (screenState) {
            SearchScreenContext.ScreenState.Default -> {
                binding.ivEmptyList.setImageResource(R.drawable.ic_search_people_placeholder)
                binding.tvEmptyList.setText(R.string.search_result_list_soon)
                binding.tvSearchResults.gone()
            }

            SearchScreenContext.ScreenState.Result -> {
                binding.ivEmptyList.setImageResource(R.drawable.ic_search_people_empty)
                binding.tvEmptyList.setText(R.string.we_didnt_find_anyone)
                binding.tvSearchResults.visible()
            }
        }
    }

    private fun showClearRecentTimerSnackBar(
        delaySec: Int,
        @StringRes message: Int? = R.string.meera_search_recent_list_clear_timer,
        undoCallBack: () -> Unit
    ) {
        if (message == null) {
            return
        }

        undoSnackBar?.handleSnackBarActions(UiKitSnackBarActions.DismissNoCallbacksAction)
        undoSnackBar = UiKitSnackBar.make(
            requireView(),
            SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(message),
                    loadingUiState = SnackLoadingUiState.DonutProgress(delaySec.toLong()),
                    buttonActionText = getText(R.string.general_cancel),
                    buttonActionListener = {
                        undoCallBack.invoke()
                        undoSnackBar?.handleSnackBarActions(UiKitSnackBarActions.DismissNoCallbacksAction)
                    }
                ),
                duration = TimeUnit.SECONDS.toMillis(delaySec.toLong()).toInt()
            )
        )
        undoSnackBar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        undoSnackBar?.show()
    }

    private inner class RecommendedPeoplePaginationHandlerImpl : RecommendedPeoplePaginationHandler {
        override fun loadMore(offsetCount: Int, rootAdapterPosition: Int) {
            viewModel.handleContentAction(
                FriendsContentActions.GetNextRelatedUsers(
                    offsetCount = offsetCount,
                    rootAdapterPosition = rootAdapterPosition
                )
            )
        }

        override fun onLast(): Boolean = viewModel.isRecommendationLast

        override fun isLoading(): Boolean = viewModel.isRecommendationLoading
    }

}

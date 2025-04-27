package com.numplates.nomera3.modules.search.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.stringNullable
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.NSnackbar
import com.meera.referrals.ui.ReferralFragment
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentSearchUsersBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.PeoplesContentDecorator
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getPeopleMainContentPreload
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.search.ui.AddFriendBottomSheet
import com.numplates.nomera3.modules.search.ui.entity.state.UserSearchViewState
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserViewModel
import com.numplates.nomera3.modules.search.ui.viewmodel.user.TOP_PEOPLE_CONTENT_POSITION
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import com.numplates.nomera3.presentation.view.utils.NToast
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.Job
import timber.log.Timber

private const val SNACK_BAR_MARGIN_BOTTOM_DP = 16

private const val MIN_BACK_STACK_COUNT = 1
private const val PAGINATION_BUFFER_SIZE = 15

class SearchUserFragmentNew : BaseFragmentNew<FragmentSearchUsersBinding>(), SearchScreenContext {

    companion object {
        fun newInstance(openedFromPeoples: Boolean): SearchUserFragmentNew {
            val fragment = SearchUserFragmentNew()
            fragment.arguments = bundleOf(KEY_FROM_PEOPLES to openedFromPeoples)
            return fragment
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchUsersBinding
        get() = FragmentSearchUsersBinding::inflate

    private val viewModel: SearchUserViewModel by viewModels(
        factoryProducer = { App.component.getViewModelFactory() },
        ownerProducer = { requireParentFragment() }
    )

    private var contentAdapter: PeoplesContentAdapter? = null
    private var recyclerPagination: RecyclerViewPaginator? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy { SyncContactsDialogDelegate(childFragmentManager) }

    private var undoSnackBar: NSnackbar? = null
    private var messageSnackBar: NSnackbar? = null
    private var currentState: SearchScreenContext.ScreenState = SearchScreenContext.ScreenState.Default

    private val showSyncContactsWelcome = false
    private var effectJob: Job? = null

    private val openedFromPeoples by lazy {
        arguments?.getBoolean(KEY_FROM_PEOPLES, false) ?: false
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
        viewModel.init(openedFromPeoples)
        observeState()
        initListeners()
        initView()
        initUtils()
    }

    override fun onStart() {
        super.onStart()
        binding?.vgPeoplesList?.onStart()
        startKeyboardHeightObserving()
        observeEffect()
        initPeopleWelcome()
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
        effectJob?.cancel()
        binding?.vgPeoplesList?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentAdapter = null
        binding?.vgPeoplesList?.onDestroyView()
        binding?.vgPeoplesList?.releasePlayer()
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
            recyclerView = binding?.vgPeoplesList ?: return,
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
        initRefreshListener()
        initRecyclerPagination()
        initPreloadImagesListener()
    }

    private fun initRefreshListener() {
        binding?.vgPeoplesRefresh?.setOnRefreshListener {
            viewModel.handleContentAction(FriendsContentActions.OnRefreshContentBySwipe)
        }
    }

    private fun initContentList() {
        contentAdapter = PeoplesContentAdapter(
            actionListener = viewModel::handleContentAction,
            mediaContentScrollListener = { innerPosition, rootPosition ->
                binding?.vgPeoplesList?.resetStateAndPlayVideo(
                    innerPosition = innerPosition,
                    rootPosition = rootPosition
                )
            },
            recommendedPeoplePaginationHandler = RecommendedPeoplePaginationHandlerImpl()
        )
        binding?.vgPeoplesList?.adapter = contentAdapter
        binding?.vgPeoplesList?.itemAnimator = null
        binding?.vgPeoplesList?.addItemDecoration(PeoplesContentDecorator())
    }

    private fun scrollToPosition(position: Int) {
        binding?.vgPeoplesList?.scrollToPosition(position)
    }

    private fun isBackStackNotEmpty(): Boolean {
        val adapter = act.getNavigationAdapter() ?: return false
        return adapter.getFragmentsCount() > MIN_BACK_STACK_COUNT
    }

    private fun observeEffect() {
        effectJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.peoplesContentEvent.collect(::handleUiEffect)
        }
    }

    private fun observeState() {
        viewModel.searchUserState.observe(viewLifecycleOwner, ::handleState)
    }

    private fun handleState(state: UserSearchViewState) {
        setRefreshing(state.isRefreshing ?: false)
        setProgressState(state.showProgressBar ?: false)
        binding?.emptyMessageContainer?.isVisible = state.showPlaceholder ?: false
        val layoutManager = binding?.vgPeoplesList?.layoutManager as? LinearLayoutManager?
        val previousPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
        contentAdapter?.submitList(state.contentList) {
            if (previousPosition == TOP_PEOPLE_CONTENT_POSITION) {
                layoutManager.scrollToPositionWithOffset(TOP_PEOPLE_CONTENT_POSITION, TOP_PEOPLE_CONTENT_POSITION)
            }
        }
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding?.vgPeoplesRefresh?.isRefreshing = isRefreshing
    }

    private fun setProgressState(needShow: Boolean) {
        if (binding?.progressBar?.isVisible != needShow) {
            binding?.progressBar?.isVisible = needShow
        }
    }

    private fun handleUiEffect(effect: PeopleUiEffect) {
        when (effect) {
            PeopleUiEffect.OpenSearch -> {
                add(
                    SearchMainFragment(),
                    Act.LIGHT_STATUSBAR,
                )
            }

            PeopleUiEffect.OpenReferralScreen -> {
                add(ReferralFragment(), Act.LIGHT_STATUSBAR)
            }

            PeopleUiEffect.ShowShakeDialog -> {
                handleShowShakeDialogAction()
            }

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
                handleOpenProfile(
                    userId = effect.userId,
                    postId = effect.postId,
                    where = effect.where
                )
            }

            is PeopleUiEffect.ScrollToPositionEffect -> {
                scrollToPosition(effect.position)
            }

            is PeopleUiEffect.ClearRelatedUserPageUiEffect -> {
                binding?.vgPeoplesList?.resetHorizontalListPageByPosition(effect.position)
            }

            is PeopleUiEffect.AddUserFromSearch -> {
                addFriendDialog(effect.user)
            }

            is PeopleUiEffect.ShowClearRecentSnackBar -> {
                showClearRecentTimerSnackBar(R.string.search_recent_list_clear_timer, effect.delaySec) {
                    viewModel.undoClearRecent()
                }
            }

            is PeopleUiEffect.OpenMomentsProfile -> {
                if((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == true) {
                    act.openUserMoments(userId = effect.userId, fromView = effect.view)
                }
            }

            else -> Unit
        }
    }

    private fun addFriendDialog(user: UserSearchResultUiEntity) {
        AddFriendBottomSheet(user, viewModel, requireContext()).show(
            childFragmentManager
        )
    }

    private fun handleShowShakeDialogAction() = act.showShakeOrLocationDialogByClick()

    private fun handleOpenProfile(
        userId: Long,
        postId: Long?,
        where: AmplitudePropertyWhere
    ) {
        if (postId == null) {
            add(
                UserInfoFragment(),
                Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, where.property)
            )
        } else {
            add(
                UserInfoFragment(),
                Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_POST_ID, postId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, where.property)
            )
        }
    }

    private fun showSuccessToast(@StringRes messageRes: Int) {
        NToast.with(act)
            .typeSuccess()
            .text(getString(messageRes))
            .show()
    }

    private fun showErrorToast(@StringRes errorMessageRes: Int) {
        NToast.with(act)
            .typeError()
            .text(getString(errorMessageRes))
            .show()
    }

    private fun initPreloadImagesListener() {
        val adapter = contentAdapter ?: return
        val preloadListener = getPeopleMainContentPreload(adapter)
        binding?.vgPeoplesList?.addOnScrollListener(preloadListener)
    }

    private fun initPermission() {
        initPermissionDelegate(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner
        )
    }

    private fun initKeyboardHeightProvider() {
        binding?.root?.let { root -> keyboardHeightProvider = KeyboardHeightProvider(root) }
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
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_done else R.drawable.ic_sync_contacts_done
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
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_dialog else R.drawable.ic_sync_contacts_dialog
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
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_dialog else R.drawable.ic_sync_contacts_dialog
        )
    }

    private fun initPeopleWelcome() {
        viewModel.handleContentAction(
            FriendsContentActions.InitPeopleWelcomeUiAction(
                needToShowWelcome = showSyncContactsWelcome,
                isCalledFromBottomNav = isBackStackNotEmpty().not()
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
                binding?.ivEmptyList?.setImageResource(R.drawable.ic_search_result_soon)
                binding?.tvEmptyList?.setText(R.string.search_result_list_soon)
            }

            SearchScreenContext.ScreenState.Result -> {
                binding?.ivEmptyList?.setImageResource(R.drawable.ic_empty_search_noomeera)
                binding?.tvEmptyList?.setText(R.string.placeholder_empty_search_result)
            }
        }
    }

    private fun showClearRecentTimerSnackBar(
        @StringRes message: Int?,
        delaySec: Int,
        undoCallBack: () -> Unit
    ) {
        if (message == null) {
            return
        }

        undoSnackBar?.dismissNoCallbacks()
        undoSnackBar = NSnackbar.with(view)
            .inView(view)
            .text(context.stringNullable(message))
            .description(context.stringNullable(R.string.touch_to_delete))
            .durationIndefinite()
            .button(context.stringNullable(R.string.general_cancel))
            .dismissManualListener { undoCallBack() }
            .timer(delaySec)
            .marginBottom(SNACK_BAR_MARGIN_BOTTOM_DP.dp)
            .show()
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

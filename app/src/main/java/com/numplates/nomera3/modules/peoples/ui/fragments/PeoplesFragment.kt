package com.numplates.nomera3.modules.peoples.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.referrals.ui.ReferralFragment
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPeoplesBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.action.RefreshPeopleHandler
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.PeoplesContentDecorator
import com.numplates.nomera3.modules.peoples.ui.content.holder.RecommendedPeopleListHolder
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getPeopleMainContentPreload
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiStates
import com.numplates.nomera3.modules.peoples.ui.onboarding.PeopleOnboardingFragment
import com.numplates.nomera3.modules.peoples.ui.utils.PeopleCommunitiesNavigator
import com.numplates.nomera3.modules.peoples.ui.viewmodel.PeoplesViewModel
import com.numplates.nomera3.modules.search.ui.fragment.KEY_FROM_PEOPLES
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_SWITCHER
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_SYNC_CONTACTS_WELCOME
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.Job
import timber.log.Timber

private const val PEOPLES_TAB_SELECTED = true
private const val PEOPLES_TAB_NOT_SELECTED = false
private const val MIN_BACK_STACK_COUNT = 1
private const val ARROW_DOWN_PADDING = 2
private const val PAGINATION_BUFFER_SIZE = 15
private const val EMPTY_UID_FROM_PUSH = -1L

class PeoplesFragment : BaseFragmentNew<FragmentPeoplesBinding>(),
    IOnBackPressed,
    RefreshPeopleHandler,
    BasePermission by BasePermissionDelegate() {

    private val viewModel by viewModels<PeoplesViewModel> {
        App.component.getViewModelFactory()
    }
    private var contentAdapter: PeoplesContentAdapter? = null
    private var recyclerPagination: RecyclerViewPaginator? = null

    private val showSelectFragmentButton by lazy {
        arguments?.getBoolean(ARG_SHOW_SWITCHER, true) ?: true
    }
    private val showSyncContactsWelcome by lazy {
        arguments?.getBoolean(ARG_SHOW_SYNC_CONTACTS_WELCOME, false) ?: false
    }
    private var userIdFromPush: Long? = null

    private val tooltipSelectCommunityHere: PopupWindow? by lazy {
        createTooltip(
            context = context,
            layoutResId = R.layout.tooltip_select_people_community
        )
    }
    private var tooltipJob: Job? = null
    private var effectJob: Job? = null

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy { SyncContactsDialogDelegate(childFragmentManager) }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPeoplesBinding
        get() = FragmentPeoplesBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        initListeners()
        initView()
        initUtils()
    }

    override fun onStart() {
        super.onStart()
        binding?.vgPeoplesList?.onStart()
        userIdFromPush = arguments?.getLong(ARG_USER_ID)
        if (userIdFromPush == 0L || userIdFromPush == EMPTY_UID_FROM_PUSH) userIdFromPush = null
        viewModel.init(userIdFromPush)
        setNavBarTabSelected(PEOPLES_TAB_SELECTED)
    }

    override fun onStop() {
        super.onStop()
        binding?.vgPeoplesList?.onStop()
        setNavBarTabSelected(PEOPLES_TAB_NOT_SELECTED)
        clearTooltip()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        observeEffect()
        initPeopleWelcome()
        checkIfNeedToScrollToUserFromPush()
    }

    override fun onStopFragment() {
        cancelEffectJob()
        super.onStopFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentAdapter = null
        binding?.vgPeoplesList?.onDestroyView()
        binding?.vgPeoplesList?.releasePlayer()
    }

    override fun onBackPressed(): Boolean {
        val isSelectFragmentMenuVisible = binding?.layoutSelectFragmentAction?.root?.isVisible ?: false
        if (isSelectFragmentMenuVisible) {
            hideSelectTabMenu()
            return true
        }
        return false
    }

    override fun onRefreshPeopleContent() {
        viewModel.handleContentAction(FriendsContentActions.OnRefreshContentByTabBarAction)
    }

    private fun cancelEffectJob() {
        effectJob?.cancel()
    }

    private fun setNavBarTabSelected(isSelected: Boolean) {
        binding?.nbvPeople?.setPeoples(isSelected)
    }

    private fun initNavBar() {
        binding?.nbvPeople?.let { barView ->
            onActivityInteraction?.onGetNavigationBar(barView)
        }
    }

    private fun initView() {
        initNavBar()
        initContentList()
        initStatusBar()
        initNavBarState()
        initToolBarState()
    }

    private fun initUtils() {
        initPermission()
    }

    private fun initStatusBar() {
        val layoutParamsStatusBar =
            binding?.vStatusBarFriends?.layoutParams as? AppBarLayout.LayoutParams
        layoutParamsStatusBar?.height = requireContext().getStatusBarHeight()
        binding?.vStatusBarFriends?.layoutParams = layoutParamsStatusBar
    }

    private fun initRefreshListener() {
        binding?.vgPeoplesRefresh?.setOnRefreshListener {
            viewModel.handleContentAction(FriendsContentActions.OnRefreshContentBySwipe)
        }
    }

    private fun initClickListeners() {
        binding?.ibPeoplesBack?.setThrottledClickListener {
            act.onBackPressed()
        }
    }

    private fun initRecyclerPagination() {
        recyclerPagination = RecyclerViewPaginator(
            recyclerView = binding?.vgPeoplesList ?: return,
            onLast = { viewModel.isTopContentLast },
            isLoading = { viewModel.isTopContentLoading },
            loadMore = {
                viewModel.handleContentAction(FriendsContentActions.GetNextTopUsersAction)
            }
        ).apply {
            threshold = PAGINATION_BUFFER_SIZE
        }
    }

    private fun initListeners() {
        initClickListeners()
        initRefreshListener()
        initRecyclerPagination()
        initPreloadImagesListener()
    }

    private fun initNavBarState() {
        val isBackStackNotEmpty = isBackStackNotEmpty()
        binding?.ibPeoplesBack?.isVisible = isBackStackNotEmpty
        binding?.nbvPeople?.isVisible = !isBackStackNotEmpty
    }

    private fun initToolBarState() {
        initSelectFragmentMenu()
    }

    private fun initSelectFragmentMenu() {
        binding?.tvPeoplesToolbarLabel?.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            if (showSelectFragmentButton) R.drawable.ic_arrow_down_black_16 else 0,
            0
        )
        binding?.tvPeoplesToolbarLabel?.compoundDrawablePadding = ARROW_DOWN_PADDING.dp
        binding?.tvPeoplesToolbarLabel?.click {
            if (!showSelectFragmentButton) return@click
            val isSelectTabVisible = binding?.layoutSelectFragmentAction?.root?.isVisible ?: false
            if (!isSelectTabVisible) {
                showSelectTabMenu()
            } else {
                hideSelectTabMenu()
            }
        }
        binding?.layoutSelectFragmentAction?.tvSelectPeopleTab?.setThrottledClickListener {
            hideSelectTabMenu()
        }
        binding?.layoutSelectFragmentAction?.tvSelectCommunityTab?.setThrottledClickListener {
            selectCommunities()
            hideSelectTabMenu()
        }
        binding?.layoutSelectFragmentAction?.vShadow?.click {
            hideSelectTabMenu()
        }
    }

    private fun showSelectTabMenu() {
        binding?.layoutSelectFragmentAction?.root?.visibleAppearAnimate()
    }

    private fun hideSelectTabMenu() {
        binding?.layoutSelectFragmentAction?.root?.gone()
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
        viewModel.peoplesContentState.observe(viewLifecycleOwner, ::handleState)
    }

    private fun handleState(state: PeopleUiStates) {
        when (state) {
            is PeopleUiStates.LoadingState -> {
                contentAdapter?.submitList(state.contentList)
                setRefreshing(state.isRefreshing)
            }
            is PeopleUiStates.PeoplesContentUiState -> {
                contentAdapter?.submitList(state.contentList)
                setRefreshing(state.isRefreshing)
                setProgressState(state.showProgressBar)
            }
        }
    }

    private fun checkIfNeedToScrollToUserFromPush() {
        viewModel.handleContentAction(FriendsContentActions.CheckIfNeedToScrollToUserFromPush(userIdFromPush, contentAdapter?.currentList))
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding?.vgPeoplesRefresh?.isRefreshing = isRefreshing
    }

    private fun setProgressState(needShow: Boolean) {
        if (binding?.pbPeople?.isVisible != needShow) {
            binding?.pbPeople?.isVisible = needShow
        }
    }

    private fun handleUiEffect(effect: PeopleUiEffect) {
        when (effect) {
            PeopleUiEffect.OpenSearch -> {
                add(
                    SearchMainFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(KEY_FROM_PEOPLES, true)
                )
            }
            PeopleUiEffect.OpenReferralScreen -> {
                viewModel.handleContentAction(FriendsContentActions.LogInviteFriendAction)
                add(ReferralFragment(), Act.LIGHT_STATUSBAR)
            }
            PeopleUiEffect.ShowShakeDialog -> {
                handleShowShakeDialogAction()
            }
           is  PeopleUiEffect.ShowContactsHasBeenSyncDialogUiEffect -> {
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
            is PeopleUiEffect.OpenMomentsProfile -> {
                if((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == true) {
                    act.openUserMoments(
                        userId = effect.userId,
                        fromView = effect.view,
                        openedWhere = AmplitudePropertyMomentScreenOpenWhere.PEOPLE_REC,
                        viewedEarly = effect.hasNewMoments?.not()
                    )
                }
            }
            is PeopleUiEffect.ShowOnboardingEffect -> {
                showOnboarding(effect.isShowOnboardingFirstTime)
            }
            is PeopleUiEffect.ScrollToPositionEffect -> {
                scrollToPosition(effect.position)
            }
            is PeopleUiEffect.ClearRelatedUserPageUiEffect -> {
                binding?.vgPeoplesList?.resetHorizontalListPageByPosition(effect.position)
            }
            is PeopleUiEffect.ScrollToRecommendedUser -> {
                scrollToRecommendedUser(effect.position, effect.userId)
            }
            else -> Unit
        }
    }

    private fun handleShowShakeDialogAction() = act.showShakeOrLocationDialogByClick()

    private fun showOnboarding(isShowOnboardingFirstTime: Boolean) {
        userIdFromPush?.let { if (it > 0) return }
        val actionMode = if (isShowOnboardingFirstTime) {
            PeopleOnboardingFragment.ONBOARDING_SHOW_FIRST_TIME_ACTION
        } else {
            PeopleOnboardingFragment.JUST_SHOW_ONBOARDING_ACTION
        }
        val fragment = PeopleOnboardingFragment.create(actionMode)
        fragment.show(childFragmentManager, PeopleOnboardingFragment.simpleName)
    }

    private fun handleOpenProfile(
        userId: Long,
        postId: Long?,
        where: AmplitudePropertyWhere) {
        if (postId == null) {
            add(
                UserInfoFragment(),
                Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(ARG_USER_ID, userId),
                Arg(ARG_TRANSIT_FROM, where.property)
            )
        } else {
            add(
                UserInfoFragment(),
                Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(ARG_USER_ID, userId),
                Arg(ARG_POST_ID, postId),
                Arg(ARG_TRANSIT_FROM, where.property)
            )
        }
    }

    private fun scrollToRecommendedUser(position: Int, userId: Long) {
        val holder = binding?.vgPeoplesList?.findViewHolderForAdapterPosition(position) as? RecommendedPeopleListHolder?
        holder?.scrollToUser(userId)
        if (isBackStackNotEmpty().not()) {
            userIdFromPush = null
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

    private fun clearTooltip() {
        tooltipJob?.cancel()
        tooltipSelectCommunityHere?.dismiss()
    }

    private fun selectCommunities() {
        viewModel.handleContentAction(FriendsContentActions.LogCommunitySectionUiAction)
        (requireParentFragment() as? PeopleCommunitiesNavigator)?.selectCommunities()
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

    private fun showContactsHasBeenSyncDialog(syncCount: Int) {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            positiveButtonAction = {
                viewModel.handleContentAction(FriendsContentActions.OnSuccessSyncContactsClosedButtonUiAction(syncCount))
            },
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_done else R.drawable.ic_sync_contacts_done,
            closeDialogDismissListener = {
                viewModel.handleContentAction(FriendsContentActions.OnSuccessSyncContactsClosedUiAction(syncCount))
            }
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
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_dialog else R.drawable.ic_sync_contacts_dialog,
            closeDialogDismissListener = { logSyncContactsDialogClosed() },
            negativeButtonRes = R.string.general_later,
            negativeButtonAction = { logSyncContactsDialogClosed() }
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
            closeDialogDismissListener = { logSyncContactsDialogGoSettingsClosed() },
            negativeButtonRes = R.string.general_later,
            negativeButtonAction = { logSyncContactsDialogGoSettingsClosed() },
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

package com.numplates.nomera3.modules.peoples.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.simpleName
import com.meera.core.permission.PermissionDelegate
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentPeoplesBinding
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.adapter.MeeraPeoplesContentAdapter
import com.numplates.nomera3.modules.peoples.ui.content.decorator.PeoplesContentDecorator
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecommendedPeopleListHolder
import com.numplates.nomera3.modules.peoples.ui.content.preloader.getPeopleMainContentPreload
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiStates
import com.numplates.nomera3.modules.peoples.ui.onboarding.MeeraPeopleOnboardingFragment
import com.numplates.nomera3.modules.peoples.ui.viewmodel.MeeraPeoplesViewModel
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.search.ui.fragment.KEY_FROM_PEOPLES
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.launch
import timber.log.Timber

private const val PAGINATION_BUFFER_SIZE = 15
private const val EMPTY_UID_FROM_PUSH = -1L

private const val PROGRESS_START_FRAME = 42
private const val PROGRESS_END_FRAME = 78

private const val DELAY_INIT_VIEW_BY_DATA_MS = 500L

class MeeraPeoplesFragment : MeeraBaseDialogFragment(R.layout.meera_fragment_peoples, ScreenBehaviourState.Full),
    IOnBackPressed,
    BasePermission by BasePermissionDelegate() {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val viewModel by viewModels<MeeraPeoplesViewModel> {
        App.component.getViewModelFactory()
    }
    private var contentAdapter: MeeraPeoplesContentAdapter? = null
    private var recyclerPagination: RecyclerViewPaginator? = null

    private val showSyncContactsWelcome by lazy {
        arguments?.getBoolean(IArgContainer.ARG_SHOW_SYNC_CONTACTS_WELCOME, false) ?: false
    }
    private var userIdFromPush: Long? = null

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy {
        SyncContactsDialogDelegate(
            childFragmentManager
        )
    }

    private val binding by viewBinding(MeeraFragmentPeoplesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        observeEffect()
        observeState()
        initListeners()
        initView()
        initUtils()
        initPeopleWelcome()

        (view.parent as? View)?.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userIdFromPush = arguments?.getLong(IArgContainer.ARG_USER_ID)
        if (userIdFromPush == 0L || userIdFromPush == EMPTY_UID_FROM_PUSH) userIdFromPush = null
        viewModel.init(userIdFromPush)
        checkIfNeedToScrollToUserFromPush()
    }

    override fun onStart() {
        super.onStart()
        doDelayed(DELAY_INIT_VIEW_BY_DATA_MS) {
            binding.vgPeoplesList.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.vgPeoplesList.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentAdapter = null
        binding.vgPeoplesList.onDestroyView()
        binding.vgPeoplesList.releasePlayer()
    }

    override fun onBackPressed(): Boolean = false

    private fun initView() {
        initContentList()
        initProgressBar()
        initStatusBar()
    }

    private fun initUtils() {
        initPermission()
    }

    private fun initRefreshListener() {
        binding.vgPeoplesRefresh.setOnRefreshListener {
            viewModel.handleContentAction(FriendsContentActions.OnRefreshContentBySwipe)
        }
    }

    private fun initClickListeners() {
        binding.nvPeoples.backButtonClickListener = {
            findNavController().popBackStack()
        }
    }

    private fun initRecyclerPagination() {
        recyclerPagination = RecyclerViewPaginator(
            recyclerView = binding.vgPeoplesList,
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

    private fun initStatusBar() {
        binding.nvPeoples.addScrollableView(binding.vgPeoplesList)
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
        binding.vgPeoplesList.apply {
            adapter = contentAdapter
            itemAnimator = null
            isNestedScrollingEnabled = true
            addItemDecoration(PeoplesContentDecorator())
        }
    }

    private fun initProgressBar() {
        binding.lavPeoples.setMinAndMaxFrame(PROGRESS_START_FRAME, PROGRESS_END_FRAME)
    }

    private fun scrollToPosition(position: Int) {
        binding.vgPeoplesList.scrollToPosition(position)
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.peoplesContentEvent.collect(::handleUiEffect)
            }
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
        viewModel.handleContentAction(
            FriendsContentActions.CheckIfNeedToScrollToUserFromPush(
                userIdFromPush,
                contentAdapter?.currentList
            )
        )
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding.vgPeoplesRefresh.isRefreshing = isRefreshing
    }

    private fun setProgressState(needShow: Boolean) {
        binding.lavPeoples.isVisible = needShow
        if (needShow) {
            binding.lavPeoples.playAnimation()
        } else {
            binding.lavPeoples.cancelAnimation()
        }
    }

    private fun handleUiEffect(effect: PeopleUiEffect) {
        when (effect) {
            PeopleUiEffect.OpenSearch -> {
                findNavController().safeNavigate(
                    R.id.action_peoplesFragment_to_meeraSearchMainFragment,
                    bundleOf(KEY_FROM_PEOPLES to true)
                )
            }

            PeopleUiEffect.OpenReferralScreen -> {
//                viewModel.handleContentAction(FriendsContentActions.LogInviteFriendAction)
//                add(ReferralFragment(), Act.LIGHT_STATUSBAR)
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
                handleOpenProfile(effect.userId, effect.postId)
            }

            is PeopleUiEffect.OpenMomentsProfile -> {
                if ((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == true) {
                    findNavController().safeNavigate(
                        R.id.action_peoplesFragment_to_meeraViewMomentFragment,
                        bundleOf(
                            KEY_USER_ID to effect.userId,
                            KEY_MOMENT_CLICK_ORIGIN to MomentClickOrigin.fromUserAvatar()
                        )
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
                binding.vgPeoplesList.resetHorizontalListPageByPosition(effect.position)
            }

            is PeopleUiEffect.ScrollToRecommendedUser -> {
                scrollToRecommendedUser(effect.position, effect.userId)
            }

            else -> Unit
        }
    }

    private fun handleShowShakeDialogAction() {
        (activity as? MeeraAct?)?.showShakeOrLocationDialogByClick()
    }

    private fun showOnboarding(isShowOnboardingFirstTime: Boolean) {
        userIdFromPush?.let { if (it > 0) return }
        val actionMode = if (isShowOnboardingFirstTime) {
            MeeraPeopleOnboardingFragment.ONBOARDING_SHOW_FIRST_TIME_ACTION
        } else {
            MeeraPeopleOnboardingFragment.JUST_SHOW_ONBOARDING_ACTION
        }
        val fragment = MeeraPeopleOnboardingFragment.create(actionMode)
        fragment.show(childFragmentManager, MeeraPeopleOnboardingFragment.simpleName)
    }

    private fun handleOpenProfile(
        userId: Long,
        postId: Long?
    ) {
        findNavController().safeNavigate(
            R.id.action_peoplesFragment_to_userInfoFragment,
            bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_POST_ID to postId
            )
        )
    }

    private fun scrollToRecommendedUser(position: Int, userId: Long) {
        val holder =
            binding.vgPeoplesList.findViewHolderForAdapterPosition(position) as? MeeraRecommendedPeopleListHolder?
        holder?.scrollToUser(userId)
    }

    private fun showSuccessToast(@StringRes messageRes: Int) {
        UiKitSnackBar.make(
            requireView(), SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(messageRes),
                    avatarUiState = AvatarUiState.SuccessIconState,
                )
            )
        ).show()
    }

    private fun showErrorToast(@StringRes errorMessageRes: Int) {
        UiKitSnackBar.make(
            requireView(), SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(errorMessageRes),
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        ).show()
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

    private fun showContactsHasBeenSyncDialog(syncCount: Int) {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            positiveButtonAction = {
                viewModel.handleContentAction(FriendsContentActions.OnSuccessSyncContactsClosedButtonUiAction(syncCount))
            },
            iconRes = R.drawable.meera_ic_sync_contacts_done,
            closeDialogDismissListener = {
                viewModel.handleContentAction(FriendsContentActions.OnSuccessSyncContactsClosedUiAction(syncCount))
            },
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
            iconRes = R.drawable.meera_ic_sync_contacts_dialog,
            closeDialogDismissListener = { logSyncContactsDialogClosed() },
            negativeButtonRes = R.string.general_later,
            negativeButtonAction = { logSyncContactsDialogClosed() },
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
            closeDialogDismissListener = { logSyncContactsDialogGoSettingsClosed() },
            negativeButtonRes = R.string.general_later,
            negativeButtonAction = { logSyncContactsDialogGoSettingsClosed() },
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

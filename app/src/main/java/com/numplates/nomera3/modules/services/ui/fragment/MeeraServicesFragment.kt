package com.numplates.nomera3.modules.services.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.safeNavigate
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.navigation.BottomType
import com.meera.uikit.widgets.navigation.NavigationBarActions
import com.meera.uikit.widgets.navigation.NavigationBarViewContract
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentServicesBinding
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraEmptyMapFragment.Companion.ARG_MEERA_FROM_SERVICE
import com.numplates.nomera3.modules.redesign.util.NavTabItem
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.NavigationUiSetter
import com.numplates.nomera3.modules.services.ui.adapter.MeeraServicesAdapter
import com.numplates.nomera3.modules.services.ui.decorator.MeeraServicesDecorator
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiEffect
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiModel
import com.numplates.nomera3.modules.services.ui.viewmodel.MeeraServicesViewModel
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.launch


private const val SCROLL_TOP = -1

class MeeraServicesFragment :
    MeeraBaseDialogFragment(R.layout.fragment_services, ScreenBehaviourState.ScrollableFull(false)) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override val isBottomNavBarVisibility: UiKitNavigationBarViewVisibilityState
        get() = UiKitNavigationBarViewVisibilityState.VISIBLE

    private val bottomNavListener = object : NavigationBarViewContract.NavigatonBarListener {
        override fun onClickProfile() {
            val layoutManager = binding.rvServices.layoutManager as? LinearLayoutManager? ?: return
            smoothScroller?.targetPosition = 0
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }

    private var smoothScroller: RecyclerView.SmoothScroller? = null

    private val binding by viewBinding(FragmentServicesBinding::bind)

    private val viewModel by viewModels<MeeraServicesViewModel> {
        App.component.getViewModelFactory()
    }

    private var clearRecentSnackbar: UiKitSnackBar? = null

    private val servicesAdapter: MeeraServicesAdapter by lazy {
        MeeraServicesAdapter(
            viewModel::handleUiAction,
            RecommendedPeoplePaginationHandlerImpl(),
            CommunitiesPaginationHandlerImpl(),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
    }

    override fun onStart() {
        super.onStart()
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().showLogo = true
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.servicesContentState.collect(::handleContentState)
                }
                launch {
                    viewModel.servicesUiEffect.collect(::handleUiEffect)
                }
            }
        }
    }

    private fun handleContentState(contentState: List<MeeraServicesUiModel>) {
        binding.pbServices.isVisible = contentState.isEmpty()
        servicesAdapter.submitList(contentState)
    }

    private fun handleUiEffect(uiEffect: MeeraServicesUiEffect) {
        when (uiEffect) {
            MeeraServicesUiEffect.NavigateToPeoples -> {
                findNavController().safeNavigate(R.id.action_mainServiceFragment_to_peoplesFragment)
            }

            is MeeraServicesUiEffect.ShowErrorToast -> showErrorToast(uiEffect.stringId)
            is MeeraServicesUiEffect.ShowSuccessToast -> showSuccessToast(uiEffect.stringId)
            is MeeraServicesUiEffect.ShowClearRecentsToast -> showClearRecentsToast(uiEffect.delaySeconds)
            is MeeraServicesUiEffect.NavigateToSettings -> {
                findNavController().safeNavigate(R.id.action_servicesFragment_to_meeraProfileSettingsFragment)
            }

            MeeraServicesUiEffect.NavigateToCommunities -> {
                findNavController().safeNavigate(R.id.action_servicesFragment_to_meeraCommunitiesListsContainerFragment)
            }

            is MeeraServicesUiEffect.NavigateToCommunity -> {
                findNavController().safeNavigate(
                    resId = R.id.action_servicesFragment_to_meeraCommunitiesListsContainerFragment,
                )
                findNavController().safeNavigate(
                    resId = R.id.action_meeraCommunitiesListsContainerFragment_to_meeraCommunityRoadFragment,
                    bundle = bundleOf(IArgContainer.ARG_GROUP_ID to uiEffect.communityId),
                    navBuilder = { builder: NavOptions.Builder ->
                        builder.setPopUpTo(
                            destinationId = R.id.meeraCommunitiesListsContainerFragment,
                            inclusive = true,
                            saveState = true
                        )
                    }
                )
            }

            is MeeraServicesUiEffect.NavigateToUserProfile -> {
                NavigationManager.getManager().mainMapFragment.isQuasiMap = true
                findNavController().safeNavigate(
                    R.id.action_mainServiceFragment_to_userInfoFragment,
                    bundleOf(IArgContainer.ARG_USER_ID to uiEffect.userId)
                )
            }

            MeeraServicesUiEffect.NavigateToEvents -> {
                NavigationManager.getManager().mainMapFragment.navigatingFromServices = true
                NavigationManager.getManager().isMapMode = true
                NavigationUiSetter.onNavDesSelectedNew(NavTabItem.MAP_TAB_ITEM.itemNav, findNavController())
                NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView()
                    .setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Peoples))
                findNavController().safeNavigate(
                    R.id.emptyMapFragment, bundleOf(
                        ARG_MEERA_FROM_SERVICE to true
                    )
                )
            }

            is MeeraServicesUiEffect.NavigateToMoments -> findNavController().navigate(
                R.id.action_global_meeraViewMomentFragment,
                bundleOf(
                    KEY_USER_ID to uiEffect.userId,
                    KEY_MOMENT_CLICK_ORIGIN to MomentClickOrigin.fromUserAvatar()
                )
            )
        }
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

    private fun showClearRecentsToast(delaySeconds: Int) {
        clearRecentSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = delaySeconds.toLong()
                    ),
                    messageText = getText(R.string.list_cleared),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        viewModel.handleUiAction(MeeraServicesUiAction.CancelClearingRecentUsersClick)
                        clearRecentSnackbar?.dismiss()
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            )
        )
        clearRecentSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        clearRecentSnackbar?.show()
    }

    override fun onResume() {
        super.onResume()
        NavigationManager.getManager().isMapMode = false
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().addListener(bottomNavListener)
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().clearListeners()
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().hasSecondButton = true
        viewModel.loadPageContent()
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        NavigationManager.getManager().mainMapFragment.initNavigationButtonsListeners(fromMap = false)
    }

    override fun onPause() {
        super.onPause()
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().removeListener(bottomNavListener)
        smoothScroller = null
    }

    private fun initViews() {
        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = servicesAdapter
            addItemDecoration(MeeraServicesDecorator())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    isShowShadow = recyclerView.canScrollVertically(SCROLL_TOP)
                }
            })
        }
    }

    private inner class RecommendedPeoplePaginationHandlerImpl : RecommendedPeoplePaginationHandler {
        override fun loadMore(offsetCount: Int, rootAdapterPosition: Int) {
            viewModel.handleUiAction(
                MeeraServicesUiAction.LoadNextRecommendedUsers(
                    offsetCount = offsetCount,
                    rootAdapterPosition = rootAdapterPosition
                )
            )
        }

        override fun onLast(): Boolean = viewModel.isRecommendationLast

        override fun isLoading(): Boolean = viewModel.isRecommendationLoading
    }

    private inner class CommunitiesPaginationHandlerImpl : RecommendedPeoplePaginationHandler {
        override fun loadMore(offsetCount: Int, rootAdapterPosition: Int) {
            viewModel.handleUiAction(
                MeeraServicesUiAction.LoadNextCommunities(
                    offset = offsetCount,
                    rootAdapterPosition = rootAdapterPosition
                )
            )
        }

        override fun onLast(): Boolean = viewModel.isCommunitiesLast

        override fun isLoading(): Boolean = viewModel.isCommunitiesLoading
    }
}

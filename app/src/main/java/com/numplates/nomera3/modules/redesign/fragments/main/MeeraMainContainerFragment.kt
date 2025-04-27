package com.numplates.nomera3.modules.redesign.fragments.main

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.navHost
import com.meera.core.extensions.orFalse
import com.meera.core.utils.text.limitCounterText
import com.meera.uikit.widgets.nav.UiKitToolbarView
import com.meera.uikit.widgets.navigation.BottomType
import com.meera.uikit.widgets.navigation.NavigationBarActions
import com.meera.uikit.widgets.navigation.NavigationBarViewContract
import com.meera.uikit.widgets.navigation.UiKitNavigationBarView
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMainContainerFragmentBinding
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.deeplink.DeeplinkNavigation
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.main.actions.MeeraMainContainerActions
import com.numplates.nomera3.modules.redesign.util.NavTabItem
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.NavigationUiSetter
import com.numplates.nomera3.modules.redesign.util.isHiddenState
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.presentation.utils.viewModels
import com.numplates.nomera3.presentation.view.fragments.RECOVERY_SERVICE_REQUEST_KEY
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

interface ToolbarNavbarInteraction {

    fun getToolbar(): UiKitToolbarView

    fun getNavigationView(): UiKitNavigationBarView
}

private const val ALPHA_NAV_VIEW = 0.95F

class MeeraMainContainerFragment : MeeraBaseFragment(R.layout.meera_main_container_fragment), ToolbarNavbarInteraction {

    @Inject
    lateinit var deeplinkNavigation: DeeplinkNavigation

    private val binding by viewBinding(MeeraMainContainerFragmentBinding::bind)
    private val viewModel by viewModels<MeeraMainContainerViewModel>()

    private var navHostFragment: NavHostFragment? = null

    private val act by lazy { activity as MeeraAct }

    init {
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initNavigation()
        initObservers()
        viewModel.handleAction(MeeraMainContainerActions.PreloadPeopleContent)
        // TODO: FIX in https://nomera.atlassian.net/browse/BR-31809

        initAuthStateListener()
        activity?.supportFragmentManager?.setFragmentResultListener(
            RECOVERY_SERVICE_REQUEST_KEY,
            this
        ) { requestKey, _ ->
            if (RECOVERY_SERVICE_REQUEST_KEY == requestKey) {
                navigateServiceFragment()
                activity?.supportFragmentManager?.clearFragmentResult(RECOVERY_SERVICE_REQUEST_KEY)
            }
        }

        viewModel.deeplinkController
            .deeplinkSharedFlow
            .flowWithLifecycle(lifecycle)
            .onEach { action ->
                deeplinkNavigation.handleTransition(act, action)

                viewModel.deeplinkController.clearDeeplinkCache()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NavigationManager.build(requireActivity() as MeeraAct, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        NavigationManager.clear()
    }

    fun setToolbarOverlayVisibility(isVisible: Boolean) {
        binding.vMapEventsToolbarOverlay.isVisible = isVisible
    }

    private fun initViews() {
        (activity as? MeeraAct)?.setStatusBarColor()

        val size = activity.getStatusBarHeight()

        val lpBar = binding.barLayout.layoutParams
            as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams

        lpBar.setMargins(0, size, 0, 0)
        binding.barLayout.layoutParams = lpBar

        val lpStatusBar = binding.statusBarView.layoutParams
            as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams

        lpStatusBar.height = size
        binding.statusBarView.layoutParams = lpStatusBar

        binding.barLayout.outlineProvider = null
        getNavigationView().apply {
            alpha = ALPHA_NAV_VIEW
            outlineProvider = null
        }

        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().isChangeStateWithAnimation = true
    }

    private fun initNavBackPressed(navController: NavController) {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val current = navController.currentDestination
                when (current?.id) {
                    R.id.mainChatFragment,
                    R.id.emptyMapFragment,
                    R.id.mainRoadFragment,
                    R.id.servicesFragment -> requireActivity().finish()

                    else -> {
                        val result = navController.popBackStack()
                        if (result.not()) requireActivity().finish()
                    }
                }
            }
        }.also {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }
    }

    private fun initNavigation() {
        navHostFragment = navHost(R.id.fragment_first_container_view)
        val navController = navHostFragment?.findNavController() ?: return

        initNavBackPressed(navController)

        val navGraph = navController.navInflater.inflate(R.navigation.main_flow_graph)

        navController.graph = navGraph

        getNavigationView().setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Map))

        getNavigationView().addListener(object : NavigationBarViewContract.NavigatonBarListener {

            // 1 position
            override fun onClickMap() {
                NavigationManager.getManager().isRoadOpen = true
                getNavigationView().setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Map))
                navController.onNavDestinationSelected(NavTabItem.ROAD_TAB_ITEM)
                NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().showLogo = true
                NavigationManager.getManager().isMapMode = false
            }

            // 2 position
            override fun onClickPeoples() {
                val isSetMapMode = NavigationManager.getManager().isMapMode
                if (isSetMapMode.not()) NavigationManager.getManager().isMapMode = true
                NavigationManager.getManager().mainMapFragment.isQuasiMap = false
                getNavigationView().setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Peoples))
                navController.onNavDestinationSelected(NavTabItem.MAP_TAB_ITEM)
            }

            // 3 position
            override fun onClickChat() {
                needAuthToNavigate {
                    getNavigationView().setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Messenger))
                    navController.onNavDestinationSelected(NavTabItem.CHAT_TAB_ITEM)
                    NavigationManager.getManager().isMapMode = false
                }
            }

            // 4 position
            override fun onClickProfile() {
                needAuthToNavigate {
                    if (NavigationManager.getManager().getTopBehaviour()?.isHiddenState().orFalse()) {
                        NavigationManager.getManager().getTopBehaviour()?.state = BottomSheetBehavior.STATE_EXPANDED
                    }

                    getNavigationView().setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Profile))
                    navController.onNavDestinationSelected(NavTabItem.SERVICE_TAB_ITEM)
                    NavigationManager.getManager().isMapMode = false
                }
            }

            // 0 position - middle tab
            override fun onClickMain() {
                navController.onNavDestinationSelected(NavTabItem.SERVICE_TAB_ITEM) {
                    Timber.e("Должна быть обработка перехода в PRIVATE")
                }
            }
        })
    }

    private fun initObservers() {
        viewModel.notificationCounterFlow
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach { count ->
                binding.toolbar.notificationsCount = count
            }
            .launchIn(lifecycleScope)

        viewModel.totalChatUnreadCounter
            .distinctUntilChanged()
            .observe(viewLifecycleOwner) { unreadCount -> showAnimatedChatBadgeWithViewCheck(unreadCount) }

        viewModel.bottomNavState.observe(viewLifecycleOwner) { state ->
            getNavigationView().setState(state.navigationBarUiState)
        }
    }

    override fun getToolbar(): UiKitToolbarView = binding.toolbar

    override fun getNavigationView(): UiKitNavigationBarView =
        binding.navBarUi

    private fun NavController.onNavDestinationSelected(
        navTabItem: NavTabItem,
        actionWhenUndefined: (() -> Unit)? = null
    ) {
        if (navTabItem.itemNav.ids == currentDestination?.id) {
            if (NavigationManager.getManager().getTopBehaviour()?.state == BottomSheetBehavior.STATE_HIDDEN) {
                when (navTabItem) {
                    NavTabItem.ROAD_TAB_ITEM -> NavigationManager.getManager().getTopBehaviour()?.state =
                        BottomSheetBehavior.STATE_HALF_EXPANDED

                    NavTabItem.MAP_TAB_ITEM -> Unit
                    NavTabItem.SWITCH_TAB_ITEM -> Unit

                    NavTabItem.CHAT_TAB_ITEM -> NavigationManager.getManager().getTopBehaviour()?.state =
                        BottomSheetBehavior.STATE_EXPANDED

                    else -> NavigationManager.getManager().getTopBehaviour()?.state =
                        BottomSheetBehavior.STATE_EXPANDED
                }
            } else if (NavigationManager.getManager().getTopBehaviour()?.state == BottomSheetBehavior.STATE_EXPANDED) {
                when (navTabItem) {
                    NavTabItem.ROAD_TAB_ITEM -> NavigationManager.getManager().getTopBehaviour()?.state =
                        BottomSheetBehavior.STATE_HALF_EXPANDED

                    else -> Unit
                }
            }
        } else if (NavTabItem.checkIfIdUndefined(navTabItem)) {
            Timber.d("NavTabItem doesn't have id for transition")
            actionWhenUndefined?.invoke()
        } else {
            NavigationUiSetter.onNavDesSelectedNew(navTabItem.itemNav, this)
        }
    }

    private fun navigateServiceFragment() {
        navHostFragment = navHost(R.id.fragment_first_container_view)
        val navController = navHostFragment?.findNavController() ?: return

        NavigationManager.getManager().logOutDoPassAndSetState(
            destinationId = R.id.servicesNavGraph,
            selectedNavigationTab = BottomType.Profile,
            action = {
                navController.onNavDestinationSelected(NavTabItem.SERVICE_TAB_ITEM)
                NavigationManager.getManager().isMapMode = false
            }
        )
    }

    private fun initAuthStateListener() {
        val authNavigation = (requireActivity() as MeeraAct).getMeeraAuthNavigation()
        authNavigation.authStatusLive.observe(viewLifecycleOwner) { authStatus ->
            if (authStatus is AuthStatus.Authorized) {
                initNotificationCounterObserver()
            } else {
                clearNotificationCountOnBadge()
            }
        }
    }

    private fun initNotificationCounterObserver() {
        viewModel.handleAction(MeeraMainContainerActions.InitNotificationCounter)
    }

    private fun clearNotificationCountOnBadge() {
        binding.toolbar.notificationsCount = 0
    }

    private fun showAnimatedChatBadgeWithViewCheck(unreadCount: Int?) {
        if (unreadCount == null) return

        view?.apply {
            if (isAttachedToWindow) {
                showAnimatedChatBadge(unreadCount)
            } else {
                post { if (isAttachedToWindow) showAnimatedChatBadge(unreadCount) }
            }
        }
    }

    private fun showAnimatedChatBadge(unreadCount: Int) {
        getNavigationView().setNavigationBarAction(
            NavigationBarActions.ShowAnimatedChatBadge(
                isShow = unreadCount > 0,
                count = limitCounterText(unreadCount)
            )
        )
    }
}

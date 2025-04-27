package com.numplates.nomera3.modules.redesign.fragments.main

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.invisible
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.meera.uikit.widgets.navigation.BottomType
import com.meera.uikit.widgets.navigation.NavigationBarActions
import com.meera.uikit.widgets.navigation.NavigationBarViewContract
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewSizeState
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMainRoadFragmentBinding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.bump.ui.ShakeRegisterUiHandler
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.MainRoadAppBarAnimationHelper
import com.numplates.nomera3.modules.newroads.MainPostRoadsEvent
import com.numplates.nomera3.modules.newroads.MeeraMainPostRoadsViewModel
import com.numplates.nomera3.modules.newroads.MeeraRoadsViewPagerAdapter
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.fragments.MeeraBaseRoadsFragment
import com.numplates.nomera3.modules.newroads.fragments.MeeraMainRoadItemFragment
import com.numplates.nomera3.modules.newroads.fragments.MeeraSubscriptionsRoadItemFragment
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.onboarding.OnBoardingFinishListener
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavTabItem
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.NavigationUiSetter
import com.numplates.nomera3.modules.redesign.util.isAuthorized
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigateWithResult
import com.numplates.nomera3.modules.redesign.util.setHiddenState
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.filters.ui.fragment.MeeraFilterCitiesBottomSheet
import com.numplates.nomera3.presentation.birthday.ui.BirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.MeeraRoadFilterBottomSheet
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.MeeraRoadFilterSubscriptionsBottomSheet
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.MeeraRoadFilterSubscriptionsCallback
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterCallback
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_DELAY = 1000L
private const val MAIN_ROAD_VIEW_PAGER_INDEX = 0
private const val SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX = 1
private const val THROTTLE_DURATION_MS = 1000

private const val START_CURRENT_ITEM = MAIN_ROAD_VIEW_PAGER_INDEX

const val SUBSCRIPTION_ROAD_REQUEST_KEY = "SUBSCRIPTION_ROAD_VIEW_PAGER_REQUEST_KEY"

private const val SLIDE_OFFSET_TABS_ACTION_THRESHOLD = 0.4f
private const val SLIDE_OFFSET_HALF_EXPANDED_MAX_SETTLING_OFFSET = -0.5f
private const val DEFAULT_STATE_TABS_VISIBILITY = -1

class MainRoadFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_main_road_fragment,
    behaviourConfigState = ScreenBehaviourState.ScrollableHalfMain
), IAuthStateObserver,
    RoadFilterCallback.CallbackOwner,
    OnBoardingFinishListener,
    ShakeRegisterUiHandler,
    ScreenshotTakenListener {

    private val binding by viewBinding(MeeraMainRoadFragmentBinding::bind)

    private val act by lazy { activity as MeeraAct }

    private var lastThrottledActionTime = 0L

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun ignoreSlide(): Boolean = !isResumed

    override val isBottomNavBarVisibility: UiKitNavigationBarViewVisibilityState
        get() = UiKitNavigationBarViewVisibilityState.VISIBLE

    override var roadFilterCallback: RoadFilterCallback? = null

    private val viewModel by viewModels<MeeraMainPostRoadsViewModel>() {
        App.component.getViewModelFactory()
    }

    private val toolbarMapOverlay by lazy { NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar() }

    private var actualExpandState: Int? = null
    private var actualNotStableState: Int? = null

    private var appBarAnimationHelper: MainRoadAppBarAnimationHelper? = null

    //TODO ROAD_FIX
//    private var onBoardingController: MeeraOnBoardingViewController? = null

    @Inject
    lateinit var filterSettingsProvider: FilterSettingsProvider

    private var currentRoadFragment: MeeraBaseRoadsFragment<*>? = null
    private var mainRoadFragment: MeeraMainRoadItemFragment? = null
    private var subscriptionsRoadFragment: MeeraSubscriptionsRoadItemFragment? = null

    private var roadFilter: MeeraRoadFilterBottomSheet? = null
    private val viewPagerAdapter: MeeraRoadsViewPagerAdapter by lazy {
        MeeraRoadsViewPagerAdapter(childFragmentManager)
    }

    private var groupId: Int = 0

    private var selectedPagePosition = MAIN_ROAD_VIEW_PAGER_INDEX
    private var currentFragmentListener: OnParentFragmentActionsListener? = null
    private var onViewPagerSwipeStateChangeListener: OnViewPagerSwipeStateChangeListener? = null
    private var onRoadScrollListener: OnRoadScrollListener? = null

    //    private var roadFilterTooltipJob: Job? = null
    private val roadFilterTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_open_road_filter)
    }

    //TODO ROAD_FIX
//    private var isAddNewPostTooltipShowable: Boolean = true
//    private var addNewPostTooltipJob: Job? = null

    private var authObserver: AuthStatusObserver? = null

    private var mainRoadMode: MainRoadMode = MainRoadMode.POSTS
//    private var mapOpenPayload: MainMapOpenPayload? = null

    private var needToShowOnboardingWhenFragmentStartedInRoadMode: Boolean = false

    private var isRefreshed = true

    /*
    *   tooltipWasCalled нужен так как при холодном запуске вызывается сначала onStartFragment, а затем
    *   onCreate.
    * */
    private var tooltipWasCalled = true

    private var bottomNavControllerCallback: BottomSheetBehavior.BottomSheetCallback? = null

    private val bottomNavListener = object : NavigationBarViewContract.NavigatonBarListener {
        override fun onClickMap() {
            mainRoadFragment?.onRefresh(false)
            subscriptionsRoadFragment?.onRefresh(false)
        }
    }

    init {
        App.component.inject(this)
    }

    /**
     * когда надо будет убрать диалог с новым годом,
     * то замените if (!tooltipWasCalled) showHappyNewYearDialog()
     * на if (!tooltipWasCalled) handleTooltipOnStartFragment()
     *
     * сейчас handleTooltipOnStartFragment заменен на showHappyNewYearDialog,
     * чтоб показать диалог, а потом вызвать показ подсказок
     * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("Bazaleev.a: onCreate called")
        // когда происходит холодный запуск onStartFragment вызывается до onCreate
        // и подсказки не показываются, для этого нужна эта строчка

        if (!tooltipWasCalled) handleTooltipOnStartFragment()

        initMainRoadMode()
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putSerializable(MainPostRoadsFragment.KEY_MODE, getMode())
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiveArguments()
        setupViewPager()
        setupViewsClickListener()
        initStateObservables()
        setupOnBoarding()

        authObserver = initAuthObserver()
        initAuthTabInterceptor()
        restoreTabsExpandState()
        updateRoadFilterCounter(FilterSettingsProvider.FilterType.Main)

        addBottomControllerListener()
        setupTouchListenerForHeader()
        tapToolbarHandler()
    }

    private fun tapToolbarHandler() {
        toolbarMapOverlay.setThrottledClickListener {
            NavigationUiSetter.onNavDesSelectedNew(NavTabItem.MAP_TAB_ITEM.itemNav, findNavController())
        }
    }

    private fun restoreTabsExpandState() {
        actualExpandState?.let { handleBottomSheetState(it) }
    }

    override fun onScreenshotTaken() {
        (childFragmentManager.fragments.firstOrNull() as? ScreenshotTakenListener?)?.onScreenshotTaken()
    }

    override fun initAuthObserver() = object : AuthStatusObserver(act, this) {

        override fun onAuthState() {
            enableViewPagerSwipe(true)
            refreshAllRoads()
        }

        override fun onNotAuthState() {
            enableViewPagerSwipe(false)
            isRefreshed = false
        }

        private fun enableViewPagerSwipe(value: Boolean) {
            binding.roadsViewPager.isSwipeEnabled = value
        }
    }

    override fun onBoardingFinished() = viewModel.onBoardingFinished()

    override fun onRegistrationFinished() {
        needToShowOnboardingWhenFragmentStartedInRoadMode = true
    }

    override fun getRoadMode(): MainRoadMode? = getMode()

    override fun registerShake() {
        //TODO ROAD_FIX
//        updateShakeState()
    }

    fun navigateToEventOnMap(post: PostUIEntity) {
        isOpeningEvent = true
        NavigationManager.getManager().getForceUpdatedTopBehavior()?.setHiddenState()
        NavigationManager.getManager().mainMapFragment.openEventFromAnotherScreen(post, true)
    }

    fun getCurrentFragment(): MeeraBaseRoadsFragment<*>? {
        return currentRoadFragment
    }

    fun getMode(): MainRoadMode? {
        return try {
            viewModel.liveRoadMode.value
        } catch (e: Exception) {
            Timber.e("Get road mode failed ${e.message}")
            mainRoadMode
        }
    }


    //TODO ROAD_FIX
//    fun updateShakeState() {
//        when (onBoardingController?.getOnBoardingState()) {
//            BottomSheetBehavior.STATE_EXPANDED -> viewModel.setNeedToRegisterShakeEvent(false)
//            else -> viewModel.setNeedToRegisterShakeEvent(true)
//        }
//    }

    //TODO ROAD_FIX
//    fun actionsIfTabAlreadySelected(needToScrollUpWithRefresh: Boolean) {
//        scrollUpToFirstPost(needToScrollUpWithRefresh)
//
//        binding.rtlMainRoadsTabLayout.let { tabLayout ->
//            currentRoadFragment?.onUpdateMomentsClicked()
//        }
//    }

    //TODO ROAD_FIX
//    private fun dismissTooltips() {
//        roadFilterTooltip?.dismiss()
//    }

//    private fun cancelTooltipsJobs() {
//        roadFilterTooltipJob?.cancel()
//        addNewPostTooltipJob?.cancel()
//    }

    private fun setupOnBoarding() {
        //TODO ROAD_FIX
//        binding?.bsOnBoarding?.let { BottomSheetBehavior.from(it).isGestureInsetBottomIgnored = true }
//        if (viewModel.isNeedToShowOnBoarding()) {
//            onBoardingController = OnBoardingViewController(
//                binding,
//                childFragmentManager,
//                viewModel
//            )
//            doDelayed(50) { onBoardingController?.showOnBoarding() }
//        }
    }

    private fun initMainRoadMode() {
        lifecycleScope.launch {
            viewModel.setRoadMode(mainRoadMode)
        }
    }

    private fun refreshAllRoads() {
        if (isRefreshed) return
        try {
            doDelayed(DEFAULT_DELAY) {
                subscriptionsRoadFragment?.onRefresh(false)
            }
            isRefreshed = true
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    private fun initAuthTabInterceptor() {
        val tabLayouts: ArrayList<TabLayout> = arrayListOf(binding.rtlMainRoadsTabLayout, binding.tlMainRoadsTabs)
        tabLayouts.forEach { setupTouchListenersForTabs(it) }
    }

    private fun throttleAction(action: () -> Unit = {}) {
        val time = SystemClock.elapsedRealtime()
        if (time - lastThrottledActionTime > THROTTLE_DURATION_MS) {
            action.invoke()
            lastThrottledActionTime = time
        }
    }

    private fun setupTouchListenersForTabs(tabLayout: TabLayout) {
        val tabStrip = tabLayout.getChildAt(0) as LinearLayout
        for (index in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(index).setOnTouchListener { view, motionEvent ->
                if (isAuthorized()) {
                    view.performClick()
                    false
                } else {
                    when (index) {
                        MAIN_ROAD_VIEW_PAGER_INDEX -> {
                            view.performClick()
                        }

                        SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> {
                            throttleAction {
                                needAuthToNavigateWithResult(requestKey = SUBSCRIPTION_ROAD_REQUEST_KEY) {
                                    (binding.tlMainRoadsTabs.children.first() as ViewGroup).children.last().performClick()
                                }
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListenerForHeader() {
        binding.clMainRoadsHeader.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    setSwipeRefreshEnable(enable = false)
                }

                MotionEvent.ACTION_UP -> {
                    val state =
                        NavigationManager.getManager().getTopBehaviour()?.state ?: return@setOnTouchListener false
                    setupSwipeRefresh(screenExpandState = state)
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun receiveArguments() {
        arguments?.let { bundle ->
            groupId = if (bundle.containsKey(IArgContainer.ARG_GROUP_ID)) {
                bundle.getInt(IArgContainer.ARG_GROUP_ID)
            } else 0
        } ?: kotlin.run {
            groupId = 0
        }
    }

    private fun setupViewPager() {
        viewPagerAdapter.addTitleOfFragment(getString(R.string.road_type_main_posts))
        viewPagerAdapter.addTitleOfFragment(getString(R.string.road_type_subscriptions_posts))

        currentFragmentListener = object : OnParentFragmentActionsListener {
            override fun requestCurrentFragment(): Int {
                return when (binding.roadsViewPager.currentItem) {
                    MAIN_ROAD_VIEW_PAGER_INDEX -> BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index
                    SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index
                    else -> 4 // unknown type
                }
            }

            override fun getParentBehaviorState(): Int? = actualExpandState
            override fun expandAppBar(expand: Boolean, showBigTabs: Boolean) = expandAppBarActions(expand, showBigTabs)
        }

        onViewPagerSwipeStateChangeListener = object : OnViewPagerSwipeStateChangeListener {
            override fun onMomentsBlockPositionChanged(
                momentsBlockCoords: Rect?,
                currentRoadType: MeeraBaseRoadsFragment.RoadTypeEnum
            ) {
                if (currentRoadFragment?.getRoadType() == currentRoadType) {
                    binding.roadsViewPager.unallowedCoords = momentsBlockCoords
                }
            }

            override fun requestCalculateMomentsBlockPosition(requiredRoadType: MeeraBaseRoadsFragment.RoadTypeEnum) {
                if (currentRoadFragment?.getRoadType() == requiredRoadType) {
                    currentRoadFragment?.calculateMomentsBlockPosition()
                }
            }

            override fun onMultimediaPostsCoordsChanged(multimediaPostsCoords: ArrayList<Rect>) {
                binding.roadsViewPager.unallowedCoordsList = multimediaPostsCoords
            }
        }

        onRoadScrollListener = object : OnRoadScrollListener {
            override fun onTopOverScroll() {
                handleOnTopOverScroll()
            }
        }

        if (mainRoadFragment == null) mainRoadFragment = MeeraMainRoadItemFragment()
        mainRoadFragment?.apply {
            bindListeners(
                currentFragmentListener = currentFragmentListener,
                onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener,
                onRoadScrollListener = onRoadScrollListener
            )
        }
        if (subscriptionsRoadFragment == null) subscriptionsRoadFragment = MeeraSubscriptionsRoadItemFragment()
        subscriptionsRoadFragment?.apply {
            bindListeners(
                currentFragmentListener = currentFragmentListener,
                onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener,
                onRoadScrollListener = onRoadScrollListener
            )
        }

        val mainRoadItemFragment = mainRoadFragment ?: return
        val subscriptionsRoadItemFragment = subscriptionsRoadFragment ?: return

        val roadFragments =
            mutableListOf<MeeraBaseFragment>(
                mainRoadItemFragment,
                subscriptionsRoadItemFragment
            )
        viewPagerAdapter.addFragment(roadFragments)

        binding.roadsViewPager.offscreenPageLimit = 2
        binding.roadsViewPager.adapter = viewPagerAdapter
        binding.rtlMainRoadsTabLayout.setupWithViewPager(binding.roadsViewPager)
        binding.tlMainRoadsTabs.setupWithViewPager(binding.roadsViewPager)
        binding.tlMainRoadsTabsDublicate.setupWithViewPager(binding.roadsViewPager)
        binding.roadsViewPager.currentItem = START_CURRENT_ITEM
        currentRoadFragment = mainRoadFragment

        binding.roadsViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                val isChildTouchesEnabled = state == ViewPager.SCROLL_STATE_IDLE
                binding.roadsViewPager.needCheckUnallowedCoords = isChildTouchesEnabled
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageSelected(position: Int) {
                //TODO ROAD_FIX
//                (activity as? ActivityToolsProvider)?.getStatusToastViewController()
//                    ?.hideStatusToast()
                selectedPagePosition = position

                currentRoadFragment = roadFragments[position] as MeeraBaseRoadsFragment<*>

                when (position) {
                    MAIN_ROAD_VIEW_PAGER_INDEX -> {
                        mainRoadFragment?.startVideoIfExist()
                        viewModel.logScreenForFragment(mainRoadFragment?.javaClass?.simpleName.orEmpty())

                        subscriptionsRoadFragment?.stopVideoIfExist()
                        subscriptionsRoadFragment?.stopAudio()

                        updateRoadFilterCounter(FilterSettingsProvider.FilterType.Main)

                        viewModel.logOpenMainFeed()
                    }

                    SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> {
                        mainRoadFragment?.stopVideoIfExist()
                        mainRoadFragment?.stopAudio()

                        subscriptionsRoadFragment?.startVideoIfExist()
                        subscriptionsRoadFragment?.refreshIfFirstRun()
                        viewModel.logScreenForFragment(subscriptionsRoadFragment?.javaClass?.simpleName.orEmpty())

                        showSubscriptionsRoadFilterCounter()

                        val hasPosts = subscriptionsRoadFragment?.viewModel?.livePosts?.value?.isNotEmpty().isTrue()
                        viewModel.logOpenFollowFeed(hasPosts)
                        viewModel.clearMomentIndicator()
                    }
                }

                mainRoadFragment?.enableNestedScroll(enable = position == MAIN_ROAD_VIEW_PAGER_INDEX)
                subscriptionsRoadFragment?.enableNestedScroll(enable = position == SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX)
                currentRoadFragment?.calculateMomentsBlockPosition()
            }
        })
    }

    private fun expandAppBarActions(expand: Boolean, showBigTabs: Boolean) {
        binding.apply {
            val isBigTabsVisible = tlMainRoadsTabs.isVisible
            if (showBigTabs && !isBigTabsVisible) {
                tlMainRoadsTabs.visible()
                tlMainRoadsTabsDublicate.visible()
                clMainRoadsHeader.invisible()
            }

            if (appBarAnimationHelper?.isAnimating().isTrue()) return

            appBarAnimationHelper = MainRoadAppBarAnimationHelper().also { helper ->
                helper.startAnimate(
                    tabs = ablMainRoadsAppbar,
                    smallTabs = tlMainRoadsTabsDublicate,
                    shouldExpand = expand,
                    onAnimationFinished = { updateNavigationViewSize() }
                )
            }
        }
    }

    private fun updateNavigationViewSize() {
        val isAppbarExpanded = binding.ablMainRoadsAppbar.translationY == 0f
        val isBottomSheetExpanded =
            NavigationManager.getManager().getForceUpdatedTopBehavior()?.state == BottomSheetBehavior.STATE_EXPANDED
        val bottomNavigationState =
            if (isBottomSheetExpanded && !isAppbarExpanded) UiKitNavigationBarViewSizeState.MIN else UiKitNavigationBarViewSizeState.MAX
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateSize = bottomNavigationState
    }

    private fun handleOnTopOverScroll() {
        if (NavigationManager.getManager().getForceUpdatedTopBehavior()?.state == BottomSheetBehavior.STATE_EXPANDED) {
            NavigationManager.getManager().getForceUpdatedTopBehavior()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    override fun onStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_SETTLING) {
            actualNotStableState = newState
            return
        }

        val isProfile =
            NavigationManager.getManager().topNavController.currentDestination?.id == R.id.userInfoFragment

        actualNotStableState = null

        setupAppbarAppearance(newState)
        subscriptionsRoadFragment?.setNewPostBtnMarginTop(newState)

        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                NavigationManager.getManager().getTopBehaviour()?.isHideable = false
            }

            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                mainRoadFragment?.scrollToTop()
                subscriptionsRoadFragment?.scrollToTop()

                //при открытии профиля из дороги isHideable должен быть false, чтобы профиль не закрывался полностью
                if (!isProfile)
                    NavigationManager.getManager().getTopBehaviour()?.isHideable = true
            }

            BottomSheetBehavior.STATE_HIDDEN -> {
                if (isOpeningEvent) {
                    return
                }
                NavigationManager.getManager().isMapMode = true
                NavigationManager.getManager().mainMapFragment.isQuasiMap = false
                NavigationUiSetter.onNavDesSelectedNew(NavTabItem.MAP_TAB_ITEM.itemNav, findNavController())
                NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView()
                    .setNavigationBarAction(NavigationBarActions.SelectItem(BottomType.Peoples))
            }
        }

        setupSwipeRefresh(newState)
        updateNavigationViewSize()

        actualExpandState = newState
        super.onStateChanged(newState)
    }

    override fun onSlide(offset: Float) {
        if (offset < SLIDE_OFFSET_TABS_ACTION_THRESHOLD) {
            setupAppbarAppearance(DEFAULT_STATE_TABS_VISIBILITY)
        }

        if (actualNotStableState == BottomSheetBehavior.STATE_SETTLING
            && actualExpandState == BottomSheetBehavior.STATE_EXPANDED
            && offset < SLIDE_OFFSET_HALF_EXPANDED_MAX_SETTLING_OFFSET
            && NavigationManager.getManager().getTopBehaviour()?.state != BottomSheetBehavior.STATE_HALF_EXPANDED
        ) {
            NavigationManager.getManager().getTopBehaviour()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        if (actualNotStableState == null
            && actualExpandState == BottomSheetBehavior.STATE_EXPANDED
            && NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state == UiKitToolbarViewState.EXPANDED
            && NavigationManager.getManager().getTopBehaviour()?.state != BottomSheetBehavior.STATE_HALF_EXPANDED
        ) {
            NavigationManager.getManager().getTopBehaviour()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        super.onSlide(offset)
    }

    private fun setupAppbarAppearance(screenExpandState: Int) {
        val isExpanded = screenExpandState == BottomSheetBehavior.STATE_EXPANDED

        binding.apply {
            tlMainRoadsTabs.isInvisible = !isExpanded
            tlMainRoadsTabsDublicate.isInvisible = !isExpanded
            clMainRoadsHeader.isInvisible = isExpanded
            arrayListOf(root, roadsViewPager, ablMainRoadsAppbar).forEach { view ->
                view.apply {
                    if (isExpanded) {
                        setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.uiKitColorBackgroundPrimary))
                    } else {
                        setBackgroundResource(R.drawable.default_ui_kit_bottom_sheet_dialog_background)
                    }
                }
            }
        }

        if (!isExpanded) {
            appBarAnimationHelper?.resetAnimations()
        }
    }

    private fun handleBottomSheetState(screenExpandState: Int) {
        setupAppbarAppearance(screenExpandState)
        setupSwipeRefresh(screenExpandState)
    }

    private fun setupSwipeRefresh(screenExpandState: Int) {
        val isHalfExpanded = screenExpandState == BottomSheetBehavior.STATE_HALF_EXPANDED
        setSwipeRefreshEnable(enable = isHalfExpanded)
    }

    private fun setSwipeRefreshEnable(enable: Boolean) {
        mainRoadFragment?.enableSwipeRefresh(enable)
        subscriptionsRoadFragment?.enableSwipeRefresh(enable)
    }

    private fun updateRoadFilterCounter(filterSettingsType: FilterSettingsProvider.FilterType) {
        val changesCount = filterSettingsProvider.get(filterSettingsType).getFilterChangesCount()
        val isCounterVisible = changesCount > 0
        binding.ivMainRoadsFilterBadge.isVisible = isCounterVisible
    }

    private fun showSubscriptionTabNewPostIndicator(value: Boolean) {
        binding.rtlMainRoadsTabLayout.setSmallBadgeVisible(SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX, value)
        binding.tlMainRoadsTabs.setSmallBadgeVisible(SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX, value)
    }

    private fun isRoadFilterEnabled() = viewModel.isRoadFilterEnabled()

    private fun setupViewsClickListener() {
        binding.btnMainRoadsFilter.setThrottledClickListener {
            openFilters()
            stopAudioInFragments()
        }
    }

    fun openFilters() {
        viewModel.logScreenForFragment("RoadFilter")
        requireActivity().vibrate()
        when (selectedPagePosition) {
            MAIN_ROAD_VIEW_PAGER_INDEX -> showRoadFilter()
            SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> showSubscriptionsRoadFilter()
        }
    }

    private fun showRoadFilter() {
        val filterSettings = filterSettingsProvider.get(FilterSettingsProvider.FilterType.Main)
        val oldData = filterSettings.data.clone()

        roadFilterCallback = object : RoadFilterCallback {
            override fun onDismiss() {
                if (oldData != filterSettings.data) {
                    mainRoadFragment?.onRefresh(false)

                    if (oldData.isRecommended != filterSettings.data.isRecommended) {
                        viewModel.logFilterMainRoadRecChange(filterSettings)
                    }
                }

                viewModel.logFilterMainRoad(filterSettings)

                updateRoadFilterCounter(FilterSettingsProvider.FilterType.Main)
            }

            override fun onCountrySearchClicked() {
                MeeraFilterCitiesBottomSheet.newInstance(
                    saveResult = true,
                    filterType = FilterSettingsProvider.FilterType.Main
                ).apply {
                    this.setOnDismissListener(object : MeeraFilterCitiesBottomSheet.DismissListener {
                        override fun onDismiss() {
                            roadFilter?.onCitySearchComplete()
                        }
                    })
                }.show(childFragmentManager, "CityFilterBottomSheet")
            }
        }

        roadFilter = MeeraRoadFilterBottomSheet.newInstance(FilterSettingsProvider.FilterType.Main)
        roadFilter?.show(childFragmentManager, MeeraRoadFilterBottomSheet.TAG)
    }

    private fun showSubscriptionsRoadFilter() {
        val filterFragment = MeeraRoadFilterSubscriptionsBottomSheet().apply {
            callback = object : MeeraRoadFilterSubscriptionsCallback {
                override fun onApply() {
                    showSubscriptionsRoadFilterCounter()
                    subscriptionsRoadFragment?.onRefresh(false)
                }
            }
        }
        filterFragment.show(childFragmentManager, MeeraRoadFilterSubscriptionsBottomSheet.TAG)
    }

    private fun showSubscriptionsRoadFilterCounter() {
        when {
            selectedPagePosition != SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> return
            !isRoadFilterEnabled() -> {
                binding.ivMainRoadsFilterBadge.visible()
            }

            else -> binding.ivMainRoadsFilterBadge.gone()
        }
    }

    private fun initStateObservables() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.hasNewSubscriptionPostOrMoment.collect { post ->
                showSubscriptionTabNewPostIndicator(post.hasNew)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.mainPostRoadsEvents.collect { event ->
                when (event) {
                    MainPostRoadsEvent.CloseOnBoarding -> {
                        //TODO ROAD_FIX
//                        onBoardingController?.hideOnBoarding()
//                        onBoardingController = null
                        act.getHolidayInfo()
                        viewModel.onBoardingClosed()
                    }

                    MainPostRoadsEvent.OnBoardingCollapsed -> {
                        handleTooltipOnStartFragment()
                    }

                    MainPostRoadsEvent.ShowOnBoardingWelcome -> {
                        needToShowOnboardingWhenFragmentStartedInRoadMode = true
                        //TODO ROAD_FIX
//                        onBoardingController = MeeraOnBoardingViewController(
//                            binding,
//                            childFragmentManager,
//                            viewModel
//                        )
//                        onBoardingController?.showOnBoarding(withWelcomeScreen = true, isHidden = true)
                    }

                    is MainPostRoadsEvent.ShowBirthdayDialog -> {
                        val actionType =
                            if (event.isBirthdayToday) BirthdayBottomDialogFragment.ACTION_TODAY_IS_BIRTHDAY
                            else BirthdayBottomDialogFragment.ACTION_YESTERDAY_IS_BIRTHDAY
                        showBirthdayDialog(actionType)
                    }

                    MainPostRoadsEvent.ShowSubscribersPrivacyDialog -> {
                        //TODO ROAD_FIX
//                        DialogNavigator(act).showFriendsSubscribersPrivacyDialog()
                    }

                    is MainPostRoadsEvent.CheckHolidays -> {
                        act.getHolidayInfo()
                    }
                }
            }
        }
    }

    private fun showBirthdayDialog(actionType: String) {
        act.showBirthdayDialog(
            actionType = actionType,
            dismissListener = {
                viewModel.updateBirthdayDialogShown()
            }
        )
    }

    private fun addBottomControllerListener() {
        bottomNavControllerCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED -> {
                        stopMediaInFragments()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> playVideoInCurrentFragment()
                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        }
        bottomNavControllerCallback?.let { NavigationManager.getManager().getBottomBehaviour()?.addBottomSheetCallback(it) }
    }

    private fun removeBottomControllerListener() {
        bottomNavControllerCallback?.let { NavigationManager.getManager().getBottomBehaviour()?.removeBottomSheetCallback(it) }
            .also { bottomNavControllerCallback = null }
    }

    private fun stopMediaInFragments() {
        stopAudioInFragments()

        listOf(mainRoadFragment, subscriptionsRoadFragment).forEach { fragment ->
            fragment?.stopVideoIfExist()
        }
    }

    private fun stopAudioInFragments() {
        listOf(mainRoadFragment, subscriptionsRoadFragment).forEach { fragment ->
            fragment?.stopAudio()
        }
    }

    private fun playVideoInCurrentFragment() {
        currentRoadFragment?.forcePlayVideoFromStart()
    }

    override fun onResume() {
        super.onResume()
        NavigationManager.getManager().isRoadOpen = true
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().setOnClickListener {
            NavigationManager.getManager().isMapMode = true
            NavigationManager.getManager().getForceUpdatedTopBehavior()?.setHiddenState()
            NavigationManager.getManager().mainMapFragment.isQuasiMap = false
        }
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().hasSecondButton = true
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().addListener(bottomNavListener)
    }

    override fun onPause() {
        super.onPause()
        NavigationManager.getManager().isRoadOpen = false
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().removeListener(bottomNavListener)
        roadFilterTooltip?.dismiss()
        roadFilterTooltip?.contentView = null
    }

    //TODO ROAD_FIX
//    private fun showOnboardingIfNeeded() {
//        doDelayed(MainPostRoadsFragment.ONBOARDING_CHECK_DELAY_MS) {
//            if (mainRoadMode != MainRoadMode.POSTS) return@doDelayed
//            if (needToShowOnboardingWhenFragmentStartedInRoadMode) {
//                needToShowOnboardingWhenFragmentStartedInRoadMode = false
//                onBoardingController?.expandOnBoarding()
//            }
//        }
//    }

    private fun handleTooltipOnStartFragment() {
        //TODO ROAD_FIX
//        if (isVisible
//            && viewModel.isUserRegistered
//            && viewModel.isOpenRoadFilterTooltipWasShownTimes()
//            && isOnBoardingCollapsed()
//            && mainRoadMode == MainRoadMode.POSTS
//        ) {
//            roadFilterTooltipJob = lifecycleScope.launch {
//                delay(TooltipDuration.COMMON_START_DELAY)
//
//                binding?.ivFilterRoad?.let { filterRoad ->
//                    roadFilterTooltip?.showAboveViewAtStart(
//                        fragment = this@MainPostRoadsFragment,
//                        view = filterRoad,
//                        offsetX = 30.dp,
//                        offsetY = -(8.dp)
//                    )
//                }
//
//                roadFilterTooltip?.setOnDismissListener {
//                    roadFilterTooltipJob?.cancel()
//                }
//
//                delay(TooltipDuration.OPEN_ROAD_FILTER)
//
//                roadFilterTooltip?.dismiss()
//            }
//
//            roadFilterTooltipJob?.invokeOnCompletion {
//                viewModel.incOpenRoadFilterTooltipWasShown()
//
//                if (isMainRoadFragmentActive() && isRecycleViewIdle() && isAddNewPostTooltipShowable) {
//                    if (viewModel.isUserRegistered && viewModel.isAddNewPostTooltipWasShownTimes()) {
//                        addNewPostTooltipJob = mainRoadFragment.showAddNewPostTooltip()
//                        addNewPostTooltipJob?.invokeOnCompletion {
//                            viewModel.incAddNewPostTooltipWasShown()
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!viewModel.isOpenRoadFilterTooltipWasShownTimes()
//            && isMainRoadFragmentActive()
//            && isRecycleViewIdle()
//            && viewModel.isUserRegistered
//            && viewModel.isAddNewPostTooltipWasShownTimes()
//            && mainRoadMode == MainRoadMode.POSTS
//        ) {
//            addNewPostTooltipJob = mainRoadFragment.showAddNewPostTooltip()
//            addNewPostTooltipJob?.invokeOnCompletion {
//                viewModel.incAddNewPostTooltipWasShown()
//            }
//        }
    }

    interface OnParentFragmentActionsListener {
        fun requestCurrentFragment(): Int
        fun getParentBehaviorState(): Int?
        fun expandAppBar(expand: Boolean, showBigTabs: Boolean)
    }

    interface OnViewPagerSwipeStateChangeListener {
        fun onMomentsBlockPositionChanged(
            momentsBlockCoords: Rect?,
            currentRoadType: MeeraBaseRoadsFragment.RoadTypeEnum
        )

        fun requestCalculateMomentsBlockPosition(requiredRoadType: MeeraBaseRoadsFragment.RoadTypeEnum)

        fun onMultimediaPostsCoordsChanged(multimediaPostsCoords: ArrayList<Rect>)
    }

    interface OnRoadScrollListener {
        fun onTopOverScroll()
    }

//TODO ROAD_FIX
//    private fun isOnBoardingCollapsed(): Boolean {
//        return onBoardingController == null ||
//            onBoardingController?.getOnBoardingState() == BottomSheetBehavior.STATE_COLLAPSED
//    }

    override fun onDestroyView() {
        removeBottomControllerListener()
        viewPagerAdapter.clearResources()
        binding.roadsViewPager.unallowedCoords = null
        binding.roadsViewPager.adapter = null
        currentFragmentListener = null
        onViewPagerSwipeStateChangeListener = null
        onRoadScrollListener = null
        authObserver = null
        mainRoadFragment?.clearListeners()
        subscriptionsRoadFragment?.clearListeners()
        toolbarMapOverlay.setOnClickListener(null)
        appBarAnimationHelper?.clear()
        super.onDestroyView()
    }
}

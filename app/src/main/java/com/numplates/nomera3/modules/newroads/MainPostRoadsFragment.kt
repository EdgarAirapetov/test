package com.numplates.nomera3.modules.newroads

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.Behavior.DragCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.observeOnce
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.updatePadding
import com.meera.core.extensions.visible
import com.meera.core.network.utils.LocaleManager
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPostRoadsLayoutBinding
import com.numplates.nomera3.modules.appDialogs.ui.DialogNavigator
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.bump.ui.ShakeRegisterUiHandler
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.maps.domain.model.isDark
import com.numplates.nomera3.modules.maps.ui.model.MainMapOpenPayload
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.newroads.fragments.CustomRoadFragment
import com.numplates.nomera3.modules.newroads.fragments.MainRoadFragment
import com.numplates.nomera3.modules.newroads.fragments.SubscriptionsRoadFragment
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.newroads.util.HackedTouchDelegate
import com.numplates.nomera3.modules.onboarding.OnBoardingFinishListener
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.birthday.ui.BirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.slidinguplayout.SlidingUpPanelLayout
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.CityFilterBottomSheet
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.CityFilterBottomSheet.DismissListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterBottomSheetNew
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterCallback
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterSubscriptionsBottomSheet
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.RoadFilterSubscriptionsCallback
import com.numplates.nomera3.presentation.view.utils.PermissionManager
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration.COMMON_START_DELAY
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration.OPEN_ROAD_FILTER
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showAboveViewAtStart
import com.numplates.nomera3.presentation.view.widgets.NavigationBarView
import com.numplates.nomera3.presentation.view.widgets.VipView
import com.numplates.nomera3.presentation.view.widgets.VipView.Companion.TYPE_MAIN_ROAD_AVATAR
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow

private const val DEFAULT_DELAY = 1000L
private const val CUSTOM_ROAD_VIEW_PAGER_INDEX = 0
private const val MAIN_ROAD_VIEW_PAGER_INDEX = 1
private const val SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX = 2
private const val FILTER_SIZE_DP = 14
private const val DISABLE_FILTER_BUTTON_ALPHA = 0.65f
private const val ENABLE_FILTER_BUTTON_ALPHA = 1f

private const val START_CURRENT_ITEM = MAIN_ROAD_VIEW_PAGER_INDEX
private val START_FILTER_TYPE = FilterSettingsProvider.FilterType.Main

class MainPostRoadsFragment : BaseFragmentNew<FragmentPostRoadsLayoutBinding>(),
    IOnBackPressed,
    IAuthStateObserver,
    RoadFilterCallback.CallbackOwner,
    OnBoardingFinishListener,
    ShakeRegisterUiHandler,
    ScreenshotTakenListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPostRoadsLayoutBinding
        get() = FragmentPostRoadsLayoutBinding::inflate

    override var roadFilterCallback: RoadFilterCallback? = null

    private val viewModel by viewModels<MainPostRoadsViewModel>()

    var appBarScrollController: MainPostRoadsFragmentAppBarScrollController? = null
    private var onBoardingController: OnBoardingViewController? = null

    private var currentRoadFragment: BaseRoadsFragment<*>? = null
    private lateinit var mainRoadFragment: MainRoadFragment
    private lateinit var customRoadFragment: CustomRoadFragment
    private lateinit var subscriptionsRoadFragment: SubscriptionsRoadFragment

    private var mapFragment: MapFragment? = null
    private var roadFilter: RoadFilterBottomSheetNew? = null
    private var viewPagerAdapter: RoadsViewPagerAdapter? = null

    private var nbBar: NavigationBarView? = null
    private var groupId: Int = 0

    var onPanelOpen: Boolean = false
    private var selectedPagePosition = MAIN_ROAD_VIEW_PAGER_INDEX
    private var currentFragmentListener: OnCurrentFragmentListener? = null
    private var onViewPagerSwipeStateChangeListener: OnViewPagerSwipeStateChangeListener? = null

    private var roadFilterTooltipJob: Job? = null
    private val roadFilterTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_open_road_filter)
    }

    private var isAddNewPostTooltipShowable: Boolean = true
    private var addNewPostTooltipJob: Job? = null

    private var authObserver: AuthStatusObserver? = null

    private var userAvatarBitmap: Bitmap? = null

    private var mainRoadMode: MainRoadMode = MainRoadMode.POSTS
    private var mapOpenPayload: MainMapOpenPayload? = null

    private var needToShowOnboardingWhenFragmentStartedInRoadMode: Boolean = false

    @Inject
    lateinit var filterSettingsProvider: FilterSettingsProvider
    /***
     * Конченый костыль чтобы отрисовыввать индикатор новых уведомлений
     * Удалить после редизайна
     * Решался баг - https://nomera.atlassian.net/browse/BR-30073
     */
    @Inject
    lateinit var localeManager: LocaleManager


    private var isRefreshed = true

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

        requestLocationPermission()
        initMainRoadMode()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(KEY_MODE, getMode())
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiveArguments()
        setupViews(view)
        setupViewPager()
        setupViewsClickListener()
        initStateObservables()
        setupSlidingLayout()
        onRefreshAvatar()
        createMapFragment()
        setupOnBoarding()
        act?.onCallDialogFragmentDismissedEvent?.observe(viewLifecycleOwner, Observer {
            doDelayed(200) {
                handleTooltipOnStartFragment()
            }
        })
        onAppSettingsRequestFinished.observeOnce(viewLifecycleOwner) {
            handleTooltipOnStartFragment()
        }

        authObserver = initAuthObserver()
        initAuthTabInterceptor()
        initInsets()

        val argsMode = arguments?.getSerializable(KEY_MODE) as? MainRoadMode
        val savedMode = savedInstanceState?.getSerializable(KEY_MODE) as? MainRoadMode
        if ((savedInstanceState == null && argsMode == MainRoadMode.MAP) || savedMode == MainRoadMode.MAP) {
            setMode(MainRoadMode.MAP)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (mainRoadMode == MainRoadMode.MAP) {
            mapFragment?.setOpenInTab(!hidden)
        }
    }

    override fun onBackPressed(): Boolean {
        return mapFragment is IOnBackPressed && (mapFragment as IOnBackPressed).onBackPressed()
    }

    override fun onScreenshotTaken() {
        (childFragmentManager.fragments.firstOrNull() as? ScreenshotTakenListener?)?.onScreenshotTaken()
    }

    override fun initAuthObserver() = object : AuthStatusObserver(act, this) {

        override fun onAuthState() {
            enableViewPagerSwipe(true)
            setRealAvatar()
            setAuthProfileButton()
            refreshAllRoads()
        }

        override fun onNotAuthState() {
            enableViewPagerSwipe(false)
            setAnonAvatar()
            setAnonProfileButton()
            isRefreshed = false
        }

        private fun setAnonProfileButton() {
            nbBar?.ivProfileBtn?.setImageResource(R.drawable.selector_button_profile_anon)
        }

        private fun setAuthProfileButton() {
            nbBar?.ivProfileBtn?.setImageResource(R.drawable.selector_button_profile)
        }

        private fun setRealAvatar() {
            userAvatarBitmap?.let { bitmap ->
                binding?.ivAvatar?.setImageBitmap(bitmap)
            }
        }

        private fun setAnonAvatar() {
            val drawable =
                ContextCompat.getDrawable(
                    this@MainPostRoadsFragment.requireContext(),
                    R.drawable.ic_anon
                )

            binding?.ivAvatar?.setImageDrawable(drawable)
        }

        private fun enableViewPagerSwipe(value: Boolean) {
            binding?.roadsViewPager?.isSwipeEnabled = value
        }
    }

    override fun onBoardingFinished() = viewModel.onBoardingFinished()

    override fun onRegistrationFinished() {
        needToShowOnboardingWhenFragmentStartedInRoadMode = true
    }

    override fun getRoadMode(): MainRoadMode = getMode()

    override fun onHideHints() {
        super.onHideHints()
        dismissTooltips()
        cancelTooltipsJobs()
    }

    override fun registerShake() {
        updateShakeState()
    }

    fun getCurrentFragment(): BaseRoadsFragment<*>? {
        return currentRoadFragment
    }

    /**
     * Can be called before Fragment is attached and there is a ViewModel available
     * in which case we return locally-stored MainRoadMode
     */
    fun getMode(): MainRoadMode {
        return try {
            viewModel.liveRoadMode.value!!
        } catch (e: Exception) {
            Timber.e("Get road mode failed ${e.message}")
            mainRoadMode
        }
    }


    fun setMode(mode: MainRoadMode) {
        setMode(mode = mode, mapOpenPayload = null)
        this.currentRoadFragment?.setRoadMode(mode)
    }

    fun setMapModeWithPayload(mapOpenPayload: MainMapOpenPayload) {
        setMode(mode = MainRoadMode.MAP, mapOpenPayload = mapOpenPayload)
    }

    /**
     * Can be called before Fragment is attached and there is a ViewModel available
     * so we store MainRoadMode locally and pass it to ViewModel in OnCreate()
     */
    private fun setMode(mode: MainRoadMode, mapOpenPayload: MainMapOpenPayload?) {
        if (mode == MainRoadMode.MAP && this.mainRoadMode != MainRoadMode.MAP) {
            val whereOpenMap = when {
                mapOpenPayload != null -> AmplitudePropertyWhereOpenMap.MAP_EVENT
                binding?.slidingLayout?.panelState == SlidingUpPanelLayout.PanelState.EXPANDED ->
                    AmplitudePropertyWhereOpenMap.MAP
                else -> AmplitudePropertyWhereOpenMap.FEED
            }
            viewModel.logOpenMap(whereOpenMap)
        }
        this.mapOpenPayload = mapOpenPayload
        try {
            viewModel.setRoadMode(mode)
        } catch (e: Exception) {
            Timber.e(e)
        }
        this.mainRoadMode = mode
        showOnboardingIfNeeded()
    }

    fun resetMap() {
        mapFragment?.resetGlobalMap()
    }

    fun updateShakeState() {
        when (onBoardingController?.getOnBoardingState()) {
            BottomSheetBehavior.STATE_EXPANDED -> viewModel.setNeedToRegisterShakeEvent(false)
            else -> viewModel.setNeedToRegisterShakeEvent(true)
        }
    }

    fun actionsIfTabAlreadySelected(needToScrollUpWithRefresh: Boolean) {
        scrollUpToFirstPost(needToScrollUpWithRefresh)

        binding?.roadTypeTabLayout?.let { tabLayout ->
            if (tabLayout.selectedTabPosition != CUSTOM_ROAD_VIEW_PAGER_INDEX) {
                currentRoadFragment?.onUpdateMomentsClicked()
            }
        }
    }

    private fun dismissTooltips() {
        roadFilterTooltip?.dismiss()
    }

    private fun cancelTooltipsJobs() {
        roadFilterTooltipJob?.cancel()
        addNewPostTooltipJob?.cancel()
    }

    private fun setupOnBoarding() {
        binding?.bsOnBoarding?.let { BottomSheetBehavior.from(it).isGestureInsetBottomIgnored = true }
        if (viewModel.isNeedToShowOnBoarding()) {
            onBoardingController = OnBoardingViewController(
                binding,
                childFragmentManager,
                viewModel
            )
            doDelayed(50) { onBoardingController?.showOnBoarding() }
        }
    }

    /**
     * while using fitsSystemWindows = true we might have problems with status bar (it disappears)
     * instead of using fitsSystemWindows it's better to work directly with insets
     * */
    private fun initInsets() {
        binding?.appbar?.setOnApplyWindowInsetsListener { appBar, insets ->
            val systemWindowInsetTop = insets.systemWindowInsetTop
            if (systemWindowInsetTop > 0) {
                appBar.updatePadding(paddingTop = systemWindowInsetTop)
                return@setOnApplyWindowInsetsListener insets.consumeSystemWindowInsets()
            }
            insets
        }
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
                customRoadFragment.onRefresh()
                subscriptionsRoadFragment.onRefresh()
            }
            isRefreshed = true
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    private fun requestLocationPermission() {
        act.requestRuntimePermission(
            PermissionManager.PERMISSION_LOCATION_CODE, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun initAuthTabInterceptor() {
        val tabLayout = binding?.roadTypeTabLayout!!
        val tabStrip = tabLayout.getChildAt(0) as LinearLayout
        for (index in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(index).setOnTouchListener { view, motionEvent ->
                if (act.getAuthenticationNavigator().isAuthorized()) {
                    view.performClick()
                    false
                } else {
                    when (index) {
                        MAIN_ROAD_VIEW_PAGER_INDEX -> {
                            view.performClick()
                        }
                        SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX,
                        CUSTOM_ROAD_VIEW_PAGER_INDEX -> needAuth {
                            view.performClick()
                        }
                    }
                    true
                }
            }
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

    private fun setupViews(view: View) {
        appBarScrollController = binding?.let(::MainPostRoadsFragmentAppBarScrollController)
        nbBar = view.findViewById(R.id.nbBar)
        nbBar?.let {
            onActivityInteraction?.onGetNavigationBar(it)
        }

        binding?.appbar?.apply {
            addOnOffsetChangedListener(appBarScrollController)
            outlineProvider = null
            invalidateOutline()

            val behavior = AppBarLayout.Behavior()
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = behavior
            behavior.setDragCallback(object : DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            })
        }

        updateRoadFilterCounter(START_FILTER_TYPE)
    }

    private fun setupViewPager() {
        viewPagerAdapter = RoadsViewPagerAdapter(parentFragmentManager)
        viewPagerAdapter?.addTitleOfFragment(getString(CustomRoadFragment.ConfigConst.TITLE_RES_ID))
        viewPagerAdapter?.addTitleOfFragment(getString(R.string.road_type_main_posts))
        viewPagerAdapter?.addTitleOfFragment(getString(R.string.road_type_subscriptions_posts))

        currentFragmentListener = object : OnCurrentFragmentListener {
            override fun requestCurrentFragment(): Int {
                return when (binding?.roadsViewPager?.currentItem) {
                    CUSTOM_ROAD_VIEW_PAGER_INDEX -> BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index
                    MAIN_ROAD_VIEW_PAGER_INDEX -> BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index
                    SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index
                    else -> 4 // unknown type
                }
            }
        }

        onViewPagerSwipeStateChangeListener = object : OnViewPagerSwipeStateChangeListener {
            override fun onMomentsBlockPositionChanged(momentsBlockCoords: Rect?, currentRoadType: BaseRoadsFragment.RoadTypeEnum) {
                if (currentRoadFragment?.getRoadType() == currentRoadType) {
                    binding?.roadsViewPager?.unallowedCoords = momentsBlockCoords
                }
            }

            override fun requestCalculateMomentsBlockPosition(requiredRoadType: BaseRoadsFragment.RoadTypeEnum) {
                if (currentRoadFragment?.getRoadType() == requiredRoadType) {
                    currentRoadFragment?.calculateMomentsBlockPosition()
                }
            }

            override fun onMultimediaPostsCoordsChanged(multimediaPostsCoords: ArrayList<Rect>) {
                binding?.roadsViewPager?.unallowedCoordsList = multimediaPostsCoords
            }
        }

        customRoadFragment = CustomRoadFragment()
        customRoadFragment.currentFragment = currentFragmentListener
        customRoadFragment.onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener
        mainRoadFragment = MainRoadFragment()
        mainRoadFragment.currentFragment = currentFragmentListener
        mainRoadFragment.onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener
        subscriptionsRoadFragment = SubscriptionsRoadFragment()
        subscriptionsRoadFragment.currentFragment = currentFragmentListener
        subscriptionsRoadFragment.onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener

        viewPagerAdapter?.addFragment(
            mutableListOf(
                customRoadFragment,
                mainRoadFragment,
                subscriptionsRoadFragment
            )
        )
        val roadFragments =
            mutableListOf<BaseFragment>(
                customRoadFragment,
                mainRoadFragment,
                subscriptionsRoadFragment
            )
        viewPagerAdapter?.addFragment(roadFragments)

        binding?.roadsViewPager?.offscreenPageLimit = 3
        binding?.roadsViewPager?.adapter = viewPagerAdapter
        binding?.roadTypeTabLayout?.setupWithViewPager(binding?.roadsViewPager)
        binding?.roadTypeTabLayoutDuplicateIndicator?.setupWithViewPager(binding?.roadsViewPager)
        binding?.roadsViewPager?.currentItem = START_CURRENT_ITEM
        currentRoadFragment = mainRoadFragment

        binding?.roadsViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                val isChildTouchesEnabled = state == ViewPager.SCROLL_STATE_IDLE
                binding?.roadsViewPager?.needCheckUnallowedCoords = isChildTouchesEnabled
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
            override fun onPageSelected(position: Int) {
                (activity as? ActivityToolsProvider)?.getStatusToastViewController()
                    ?.hideStatusToast()

                currentRoadFragment?.isFragmentStarted = false
                selectedPagePosition = position

                currentRoadFragment = roadFragments[position] as BaseRoadsFragment<*>
                currentRoadFragment?.isFragmentStarted = true

                when (position) {
                    CUSTOM_ROAD_VIEW_PAGER_INDEX -> {
                        customRoadFragment.startVideoIfExist()
                        viewModel.logScreenForFragment(customRoadFragment.javaClass.simpleName)

                        mainRoadFragment.stopVideoIfExist()
                        mainRoadFragment.stopAudio()

                        subscriptionsRoadFragment.stopVideoIfExist()
                        subscriptionsRoadFragment.stopAudio()

                        if (CustomRoadFragment.ConfigConst.IS_FILTER_ENABLE) {
                            updateRoadFilterCounter(FilterSettingsProvider.FilterType.Custom)
                        } else {
                            disableFilterButton()
                        }

                        viewModel.amplitudeHelper.logOpenCustomFeed()
                    }

                    MAIN_ROAD_VIEW_PAGER_INDEX -> {
                        customRoadFragment.stopVideoIfExist()
                        customRoadFragment.stopAudio()

                        mainRoadFragment.startVideoIfExist()
                        viewModel.logScreenForFragment(mainRoadFragment.javaClass.simpleName)

                        subscriptionsRoadFragment.stopVideoIfExist()
                        subscriptionsRoadFragment.stopAudio()

                        updateRoadFilterCounter(FilterSettingsProvider.FilterType.Main)

                        viewModel.logOpenMainFeed()
                    }

                    SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> {
                        customRoadFragment.stopVideoIfExist()
                        customRoadFragment.stopAudio()

                        mainRoadFragment.stopVideoIfExist()
                        mainRoadFragment.stopAudio()

                        subscriptionsRoadFragment.startVideoIfExist()
                        subscriptionsRoadFragment.refreshIfFirstRun()
                        viewModel.logScreenForFragment(subscriptionsRoadFragment.javaClass.simpleName.orEmpty())

                        showSubscriptionsRoadFilterCounter()

                        val hasPosts = subscriptionsRoadFragment.viewModel.livePosts.value?.isNotEmpty().isTrue()
                        viewModel.logOpenFollowFeed(hasPosts)
                        viewModel.clearMomentIndicator()
                    }
                }

                currentRoadFragment?.calculateMomentsBlockPosition()
                appBarScrollController?.checkIfAppBarCorrespondScrollPosition(currentRoadFragment)
            }
        })

        increaseTabViewHitRect()
    }

    private fun disableFilterButton() {
        binding?.clFilterRoadContainer?.apply {
            isEnabled = false
            alpha = DISABLE_FILTER_BUTTON_ALPHA
        }
    }

    override fun onPause() {
        super.onPause()
        roadFilterTooltip?.dismiss()
    }

    private fun enableFilterButton() {
        binding?.clFilterRoadContainer?.apply {
            isEnabled = true
            alpha = ENABLE_FILTER_BUTTON_ALPHA
        }
    }

    private fun updateRoadFilterCounter(filterSettingsType: FilterSettingsProvider.FilterType) {
        enableFilterButton()
        val changesCount = filterSettingsProvider.get(filterSettingsType).getFilterChangesCount()
        val isCounterVisible = changesCount > 0

        binding?.filterChangesCounter?.text = changesCount.toString()
        binding?.filterChangesCounter?.setVisible(isCounterVisible)

        setFilterSize(FILTER_SIZE_DP)
    }

    private fun setFilterSize(size: Int) {
        val params = binding?.filterChangesCounter?.layoutParams
        params?.width = size.dp
        params?.height = size.dp
        binding?.filterChangesCounter?.layoutParams = params
    }

    private fun showSubscriptionTabNewPostIndicator(value: Boolean) {
//        if (localeManager.isRusLanguage()){
//            println("rus")
//            binding?.subscriptionPostIndicator?.setMargins(0, 8.dp, 16.dp, 0)
//        } else {
//            println("eng")
//            binding?.subscriptionPostIndicator?.setMargins(0, 8.dp, 8.dp, 0)
//        }
        binding?.subscriptionPostIndicator?.setVisible(value)
    }

    private fun increaseTabViewHitRect() {
        binding?.roadTypeTabLayout?.post {
            binding?.roadTypeTabLayout?.children?.forEach { tabView ->
                val tabViewParent = tabView.parent as? View
                if (tabViewParent != null) {
                    val tabViewIncreasedHitRect = Rect()
                        .apply { tabView.getHitRect(this) }
                        .also { it.top += 6.dp }

                    tabViewParent.touchDelegate =
                        HackedTouchDelegate(tabViewIncreasedHitRect, tabView)
                }
            }
        }
    }

    fun isRoadFilterEnabled() = viewModel.isRoadFilterEnabled()

    private fun setupViewsClickListener() {
        // Show filter dialog
        binding?.clFilterRoadContainer?.setOnClickListener {
            viewModel.logScreenForFragment("RoadFilter")
            when (selectedPagePosition) {
                CUSTOM_ROAD_VIEW_PAGER_INDEX -> showRoadFilter(FilterSettingsProvider.FilterType.Custom)
                MAIN_ROAD_VIEW_PAGER_INDEX -> showRoadFilter(FilterSettingsProvider.FilterType.Main)
                SUBSCRIPTION_ROAD_VIEW_PAGER_INDEX -> showSubscriptionsRoadFilter()
            }
        }

        // Search
        binding?.svSearch?.setOnClickListener {
            viewModel.logOpenMainSearch(currentFragmentListener?.requestCurrentFragment())
            openSearch()
        }
    }

    private fun openSearch() = needAuth {
        add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
    }

    private fun showRoadFilter(filterSettingsType: FilterSettingsProvider.FilterType) {
        val filterSettings = filterSettingsProvider.get(filterSettingsType)
        val oldData = filterSettings.data.clone()

        roadFilterCallback = object : RoadFilterCallback {
            override fun onDismiss() {
                if (oldData != filterSettings.data) {
                    when (filterSettingsType) {
                        FilterSettingsProvider.FilterType.Main -> mainRoadFragment.onRefresh()
                        FilterSettingsProvider.FilterType.Custom -> customRoadFragment.onRefresh()
                    }

                    if (oldData.isRecommended != filterSettings.data.isRecommended) {
                        viewModel.logFilterMainRoadRecChange(filterSettings)
                    }
                }

                viewModel.logFilterMainRoad(filterSettings)

                updateRoadFilterCounter(filterSettingsType)
            }

            override fun onCountrySearchClicked() {
                CityFilterBottomSheet(
                    filterSettingsType = filterSettingsType
                ).apply {
                    this.setOnDismissListener(object : DismissListener {
                        override fun onDismiss() {
                            roadFilter?.onCitySearchComplete()
                        }
                    })
                }.show(childFragmentManager, "CityFilterBottomSheet")
            }
        }

        roadFilter = RoadFilterBottomSheetNew.newInstance(filterSettingsType)
        roadFilter?.show(childFragmentManager, RoadFilterBottomSheetNew.TAG)
    }

    private fun showSubscriptionsRoadFilter() {
        val filterFragment =
            RoadFilterSubscriptionsBottomSheet(object : RoadFilterSubscriptionsCallback {
                override fun onDismiss() {
                    showSubscriptionsRoadFilterCounter()
                    subscriptionsRoadFragment.onRefresh()
                }
            })
        filterFragment.show(childFragmentManager, "RoadFilterSubscriptionsBottomSheet")
    }

    private fun showSubscriptionsRoadFilterCounter() {
        enableFilterButton()

        when {
            selectedPagePosition != 2 -> return
            !isRoadFilterEnabled() -> {
                setFilterSize(8)
                binding?.filterChangesCounter?.text = " "
                binding?.filterChangesCounter?.visible()
            }

            else -> binding?.filterChangesCounter?.gone()
        }
    }

    private fun setupSlidingLayout() {
        var isCollapsed = false
        binding?.slidingLayout?.addPanelSlideListener(object :
            SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset < 0.98f && !isCollapsed) {
                    onPanelOpen = true
                    isCollapsed = true
                    viewModel.logScreenForFragment(mapFragment?.javaClass?.simpleName.orEmpty())
                } else if (slideOffset > 0.98f && isCollapsed) {
                    onPanelOpen = false
                    isCollapsed = false
                }
                binding?.coordinator?.alpha = if (slideOffset <= 0.6f) (slideOffset / 0.6f).pow(2) else 1f

                currentRoadFragment?.calculateMomentsBlockPosition()
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: SlidingUpPanelLayout.PanelState,
                newState: SlidingUpPanelLayout.PanelState,
            ) {
                when (newState) {
                    SlidingUpPanelLayout.PanelState.COLLAPSED -> {
                        setMode(MainRoadMode.MAP)
                    }

                    SlidingUpPanelLayout.PanelState.EXPANDED -> {
                    }

                    SlidingUpPanelLayout.PanelState.DRAGGING -> {
                        onBoardingController?.onPanelDragging()
                    }

                    SlidingUpPanelLayout.PanelState.ANCHORED -> {
                    }

                    SlidingUpPanelLayout.PanelState.HIDDEN -> {
                    }
                }
                onSlidePanelStateChanged(newState)
            }
        })

        binding?.slidingLayout?.setFadeOnClickListener(null)

        binding?.slidingLayout?.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    private fun requestCurrentFragment(): BaseFragment? {
        return when (currentFragmentListener?.requestCurrentFragment()) {
            BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index -> customRoadFragment
            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index -> mainRoadFragment
            BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index -> subscriptionsRoadFragment
            else -> null
        }
    }

    fun requestCurrentFragmentEnum(): BaseRoadsFragment.RoadTypeEnum? {
        return when (currentFragmentListener?.requestCurrentFragment()) {
            BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index -> BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD
            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index -> BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD
            BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index -> BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD
            else -> null
        }
    }

    private fun onSlidePanelStateChanged(state: SlidingUpPanelLayout.PanelState) {
        val fragment = requestCurrentFragment()
        fragment?.let {
            val currentFragment = it as? BaseFragment
            when (state) {
                SlidingUpPanelLayout.PanelState.EXPANDED -> {
                    (it as? MainRoadFragment)?.isMapCollapsed = true
                    currentFragment?.onReturnTransitionFragment()
                }

                SlidingUpPanelLayout.PanelState.COLLAPSED -> {
                    (it as? MainRoadFragment)?.isMapCollapsed = false
                    if (currentFragment?.isFragmentStarted == true) currentFragment.onStopFragment() else Unit
                }

                else -> Unit
            }

        }
    }

    private fun initStateObservables() {
        viewModel.liveRoadMode.observe(viewLifecycleOwner, ::displayRoadMode)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.hasNewSubscriptionPostOrMoment.collect { post ->
                showSubscriptionTabNewPostIndicator(post.hasNew)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.mainPostRoadsEvents.collect { event ->
                when (event) {
                    MainPostRoadsEvent.CloseOnBoarding -> {
                        onBoardingController?.hideOnBoarding()
                        onBoardingController = null
                        act.getHolidayInfo()
                        viewModel.onBoardingClosed()
                    }
                    MainPostRoadsEvent.OnBoardingCollapsed -> {
                        handleTooltipOnStartFragment()
                    }
                    MainPostRoadsEvent.ShowOnBoardingWelcome -> {
                        needToShowOnboardingWhenFragmentStartedInRoadMode = true
                        onBoardingController = OnBoardingViewController(
                            binding,
                            childFragmentManager,
                            viewModel
                        )
                        onBoardingController?.showOnBoarding(withWelcomeScreen = true, isHidden = true)
                    }
                    is MainPostRoadsEvent.ShowBirthdayDialog -> {
                        val actionType =
                            if (event.isBirthdayToday) BirthdayBottomDialogFragment.ACTION_TODAY_IS_BIRTHDAY
                            else BirthdayBottomDialogFragment.ACTION_YESTERDAY_IS_BIRTHDAY
                        showBirthdayDialog(actionType)
                    }
                    MainPostRoadsEvent.ShowSubscribersPrivacyDialog -> {
                        DialogNavigator(act).showFriendsSubscribersPrivacyDialog()
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

    private fun onRefreshAvatar() {
        lifecycleScope.launchWhenResumed {
            viewModel.getUserProfileFlow().collect(::handleProfileForVipView)
        }
    }

    private fun handleProfileForVipView(user: UserProfileModel?) {
        if (user != null) {
            val testVipView = VipView(act)
            testVipView.init(VipView.SIZE52)
            testVipView.setPadding(
                dpToPx(10),
                dpToPx(10),
                dpToPx(10),
                dpToPx(10)
            )
            testVipView.type = TYPE_MAIN_ROAD_AVATAR
            testVipView.setUp(
                act,
                user.avatarSmall ?: "",
                user.accountType ?: 0,
                user.accountColor ?: 0,
                hasShadow = false
            )
            testVipView.onImageReady = {
                doAsync({
                    try {
                        val avatar = NGraphics.getBitmapView(testVipView)
                        var shadowColor = NGraphics.getColorResourceId(
                            accountType = viewModel.getAccountType(),
                            accountColor = viewModel.getAccountColor()
                        )
                        if (shadowColor == R.color.ui_white)
                            shadowColor = R.color.ui_text_gray
                        val shadow = NGraphics.addShadowToBitmap(
                            avatar, avatar.height, avatar.width,
                            ContextCompat.getColor(act, shadowColor),
                            dpToPx(6),
                            0,
                            dpToPx(8)
                        )
                        return@doAsync shadow
                    } catch (e: Exception) {
                        Timber.e("Test vip view create shadow failed ${e.message}")
                        return@doAsync null
                    }
                }, {
                    if (it != null) {
                        userAvatarBitmap = it as Bitmap
                        authObserver?.forceUpdate()
                    }
                })
            }
        }
    }

    private fun createMapFragment() {
        mapFragment = MapFragment()
        mapFragment?.let { fragment ->
            childFragmentManager.beginTransaction()
                .add(R.id.flMapContainer, fragment, MapFragment::class.java.name)
                .runOnCommit {
                    if (mainRoadMode == MainRoadMode.MAP) {
                        mapFragment?.setOpenInTab(true)
                    }
                }
                .commitAllowingStateLoss()
        }
    }

    fun scrollUpToFirstPost(needToScrollUpWithRefresh: Boolean = true) {
        binding?.roadsViewPager?.let { viewPager ->
            val fragment = viewPagerAdapter?.getItem(viewPager.currentItem)
            if (fragment is BaseRoadsFragment<*> && needToScrollUpWithRefresh) {
                fragment.onFeedBtnClicked()
            }
            if (needToScrollUpWithRefresh) binding?.appbar?.setExpanded(true)
        }
    }

    private fun displayRoadMode(mode: MainRoadMode) {
        when (mode) {
            MainRoadMode.POSTS -> setModePosts()
            MainRoadMode.MAP -> setModeMap()
        }
    }

    private fun setModeMap() {
        val consumedPayload = mapOpenPayload
        mapOpenPayload = null
        mapFragment?.setOpenInTab(isOpen = true, mapOpenPayload = consumedPayload)
        val withAnimation = appBarScrollController?.currentState == AppBarScrollState.OPEN
        binding?.slidingLayout?.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED, withAnimation)
        nbBar?.selectRoad(false)
        nbBar?.selectMap(true)
        setTransparentStatusBar()
        currentRoadFragment?.stopVideoIfExist()
    }

    private fun setModePosts() {
        mapFragment?.setOpenInTab(false)
        val withAnimation = appBarScrollController?.currentState == AppBarScrollState.OPEN
        binding?.slidingLayout?.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED, withAnimation)
        nbBar?.selectRoad(true)
        nbBar?.selectMap(false)
        appBarScrollController?.currentState?.let(::updateStatusBar)
        currentRoadFragment?.startVideoIfExist()
    }

    override fun onStopFragment() {
        mapFragment?.onStopFragment()
        when (currentFragmentListener?.requestCurrentFragment()) {
            BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index -> {
                customRoadFragment.onStopFragment()
            }

            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index -> {
                mainRoadFragment.onStopFragment()
            }

            BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index -> {
                subscriptionsRoadFragment.onStopFragment()
            }
        }
    }

    /*
    *   tooltipWasCalled нужен так как при холодном запуске вызывается сначала onStartFragment, а затем
    *   onCreate.
    * */
    private var tooltipWasCalled = true
    override fun onStartFragment() {
        super.onStartFragment()
        mapFragment?.onStartFragment()
        viewModel.logScreenForFragment(this.javaClass.simpleName)
        Timber.d("Bazaleev.a: onStartFragment isAdded = $isAdded")
        when (currentFragmentListener?.requestCurrentFragment()) {
            BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index -> {
                customRoadFragment.onStartFragment()
            }

            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index -> {
                mainRoadFragment.lifecycleScope.launchWhenStarted {
                    mainRoadFragment.onStartFragment()
                }
            }

            BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index -> {
                subscriptionsRoadFragment.onStartFragment()
            }
        }

        if (isAdded) {
            handleTooltipOnStartFragment()

            doDelayed(200) {
                handleTooltipOnStartFragment()
            }
        } else {
            tooltipWasCalled = false
        }
        showOnboardingIfNeeded()
    }

    private fun showOnboardingIfNeeded() {
        doDelayed(ONBOARDING_CHECK_DELAY_MS) {
            if (mainRoadMode != MainRoadMode.POSTS) return@doDelayed
            if (needToShowOnboardingWhenFragmentStartedInRoadMode) {
                needToShowOnboardingWhenFragmentStartedInRoadMode = false
                onBoardingController?.expandOnBoarding()
            }
        }
    }

    private fun handleTooltipOnStartFragment() {
        if (isVisible
            && viewModel.isUserRegistered
            && viewModel.isOpenRoadFilterTooltipWasShownTimes()
            && isOnBoardingCollapsed()
            && mainRoadMode == MainRoadMode.POSTS
        ) {
            roadFilterTooltipJob = lifecycleScope.launch {
                delay(COMMON_START_DELAY)

                binding?.ivFilterRoad?.let { filterRoad ->
                    roadFilterTooltip?.showAboveViewAtStart(
                        fragment = this@MainPostRoadsFragment,
                        view = filterRoad,
                        offsetX = 30.dp,
                        offsetY = -(8.dp)
                    )
                }

                roadFilterTooltip?.setOnDismissListener {
                    roadFilterTooltipJob?.cancel()
                }

                delay(OPEN_ROAD_FILTER)

                roadFilterTooltip?.dismiss()
            }

            roadFilterTooltipJob?.invokeOnCompletion {
                viewModel.incOpenRoadFilterTooltipWasShown()

                if (isMainRoadFragmentActive() && isRecycleViewIdle() && isAddNewPostTooltipShowable) {
                    if (viewModel.isUserRegistered && viewModel.isAddNewPostTooltipWasShownTimes()) {
                        addNewPostTooltipJob = mainRoadFragment.showAddNewPostTooltip()
                        addNewPostTooltipJob?.invokeOnCompletion {
                            viewModel.incAddNewPostTooltipWasShown()
                        }
                    }
                }
            }
        }

        if (!viewModel.isOpenRoadFilterTooltipWasShownTimes()
            && isMainRoadFragmentActive()
            && isRecycleViewIdle()
            && viewModel.isUserRegistered
            && viewModel.isAddNewPostTooltipWasShownTimes()
            && mainRoadMode == MainRoadMode.POSTS
        ) {
            addNewPostTooltipJob = mainRoadFragment.showAddNewPostTooltip()
            addNewPostTooltipJob?.invokeOnCompletion {
                viewModel.incAddNewPostTooltipWasShown()
            }
        }
    }

    private fun isMainRoadFragmentActive(): Boolean =
        currentFragmentListener?.requestCurrentFragment() ==
            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index

    private fun isRecycleViewIdle(): Boolean =
        currentRoadFragment?.isRecyclerViewIdle ?: false

    override fun onReturnTransitionFragment() {
        when (currentFragmentListener?.requestCurrentFragment()) {
            BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index -> {
                customRoadFragment.onReturnTransitionFragment()
            }

            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD.index -> {
                mainRoadFragment.onReturnTransitionFragment()
            }

            BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD.index -> {
                subscriptionsRoadFragment.onReturnTransitionFragment()
            }
        }
    }

    fun setNavbarVisible(visible: Boolean) {
        binding?.let {
            if (visible) {
                appBarScrollController?.showBottomBar()
            } else {
                appBarScrollController?.hideBottomBar()
            }
        }
    }

    fun updateStatusBar(state: AppBarScrollState) {
        when (state) {
            AppBarScrollState.OPEN -> {
                setTransparentStatusBar()
            }

            AppBarScrollState.HALF_OPEN -> {
                setWhiteNonTransparentStatusBar()
            }

            AppBarScrollState.CLOSE -> {
                setWhiteNonTransparentStatusBar()
            }
        }
    }

    private fun setWhiteNonTransparentStatusBar() {
        act?.setLightStatusBarNotTransparent()
        act?.changeStatusBarState(Act.LIGHT_STATUSBAR_NOT_TRANSPARENT)
    }

    private fun setTransparentStatusBar() {
        if (viewModel.mapSettings.mapMode.isDark()) {
            act.setColorStatusBarNavLight()
            act.changeStatusBarState(Act.COLOR_STATUSBAR_LIGHT_NAVBAR)
        } else {
            act.setLightStatusBar()
            act.changeStatusBarState(Act.LIGHT_STATUSBAR)
        }
    }

    interface OnCurrentFragmentListener {
        fun requestCurrentFragment(): Int
    }

    interface OnViewPagerSwipeStateChangeListener {
        fun onMomentsBlockPositionChanged(momentsBlockCoords: Rect?, currentRoadType: BaseRoadsFragment.RoadTypeEnum)

        fun requestCalculateMomentsBlockPosition(requiredRoadType: BaseRoadsFragment.RoadTypeEnum)

        fun onMultimediaPostsCoordsChanged(multimediaPostsCoords: ArrayList<Rect>)
    }

    companion object {
        private const val TOOL_TIP_SHOW_RATIO = 0.1f
        const val SHOW_HIDE_TOP_BOTTOM_PANEL_TIME = 150L
        const val ONBOARDING_CHECK_DELAY_MS = 200L

        private const val KEY_MODE = "KEY_MODE"

        fun newInstance(mode: MainRoadMode): MainPostRoadsFragment {
            val args = Bundle()
            args.putSerializable(KEY_MODE, mode)
            val fragment = MainPostRoadsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    enum class AppBarScrollState(val range: ClosedFloatingPointRange<Float>) {
        OPEN(0.0f..0.7f),
        HALF_OPEN(0.7f..0.95f),
        CLOSE(0.95f..1f);

        companion object {
            fun getState(value: Float): AppBarScrollState? {
                return values().find { checkedState ->
                    checkedState.range.contains(value)
                }
            }
        }
    }

    inner class MainPostRoadsFragmentAppBarScrollController(
        private val binding: FragmentPostRoadsLayoutBinding
    ) : AppBarLayout.OnOffsetChangedListener {

        var currentState: AppBarScrollState? = null

        fun checkIfAppBarCorrespondScrollPosition(fragment: BaseRoadsFragment<*>?) {
            if (fragment?.isCurrentScrollPositionIsTop() == false && currentState == AppBarScrollState.OPEN
            ) {
                hideAppBar()
            }
        }

        /**
         * Вызывается когда скроллится дорога
         */
        fun onRoadScroll() = Unit

        /**
         * Вызывается когда меняется позиция шапки AppBarLayout
         * (не вызывается когда шапка скрыта и происходит обычный скролл дороги)
         */
        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
            val maxScroll = appBarLayout.totalScrollRange.toFloat()
            val percentage = abs(verticalOffset) / maxScroll

            if (percentage > TOOL_TIP_SHOW_RATIO) {
                isAddNewPostTooltipShowable = false
                addNewPostTooltipJob?.cancel()
            }

            val newState = AppBarScrollState.getState(percentage)

            if (newState != currentState) {
                when (newState) {
                    AppBarScrollState.OPEN -> {
                        onAppBarOpen()
                    }

                    AppBarScrollState.HALF_OPEN -> {
                        onAppBarOpenPart()
                    }

                    AppBarScrollState.CLOSE -> {
                        onAppBarClose()
                    }

                    else -> {}
                }
                binding.slidingLayout.isTouchEnabled = newState == AppBarScrollState.OPEN
                newState?.let { updateStatusBar(newState) }
            }

            currentState = newState
        }

        private fun hideAppBar() {
            val appBarLayout: AppBarLayout = act.findViewById(R.id.appbar)
            appBarLayout.setExpanded(false)
        }

        private fun onAppBarOpenPart() {
            showBottomBar()
            onBoardingController?.onAppBarOpen()
            binding.roadTypeTabLayout.visible()
        }

        private fun onAppBarOpen() {
            binding.flMapContainer.visible()
            binding.roadTypeTabLayout.visible()
        }

        private fun onAppBarClose() {
            hideBottomBar()
            onBoardingController?.onAppBarClosed()
            binding.flMapContainer.invisible()
            binding.roadTypeTabLayout.invisible()
        }

        fun showBottomBar() {
            binding.bottomBarPostlist.animate()
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator()).duration =
                SHOW_HIDE_TOP_BOTTOM_PANEL_TIME
        }

        fun hideBottomBar() {
            binding.bottomBarPostlist
                .animate()
                .translationY(100.dp.toFloat())
                .setInterpolator(DecelerateInterpolator()).duration =
                SHOW_HIDE_TOP_BOTTOM_PANEL_TIME
        }

        fun updateStatusBar(state: AppBarScrollState) {
            when (state) {
                AppBarScrollState.OPEN -> {
                    setTransparentStatusBar()
                }

                AppBarScrollState.HALF_OPEN -> {
                    setWhiteNonTransparentStatusBar()
                }

                AppBarScrollState.CLOSE -> {
                    setWhiteNonTransparentStatusBar()
                }
            }
        }

        private fun setWhiteNonTransparentStatusBar() {
            act?.setLightStatusBarNotTransparent()
            act?.changeStatusBarState(Act.LIGHT_STATUSBAR_NOT_TRANSPARENT)
        }

        private fun setTransparentStatusBar() {
            if (viewModel.isDarkMapStyle()) {
                act.setColorStatusBarNavLight()
                act.changeStatusBarState(Act.COLOR_STATUSBAR_LIGHT_NAVBAR)
            } else {
                act.setLightStatusBar()
                act.changeStatusBarState(Act.LIGHT_STATUSBAR)
            }
        }

        private fun BaseRoadsFragment<*>.isCurrentScrollPositionIsTop(): Boolean {
            return getRoadVerticalScrollPosition() == 0
        }

    }


    private fun isOnBoardingCollapsed(): Boolean {
        return onBoardingController == null ||
            onBoardingController?.getOnBoardingState() == BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        nbBar = null
        mapFragment = null
    }
}

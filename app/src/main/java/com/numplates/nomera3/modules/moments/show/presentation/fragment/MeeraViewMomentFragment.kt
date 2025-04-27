package com.numplates.nomera3.modules.moments.show.presentation.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentViewMomentContainerBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowFlipped
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.data.ARG_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentViewModel
import com.numplates.nomera3.modules.moments.show.presentation.adapter.ViewMomentAdapter
import com.numplates.nomera3.modules.moments.show.presentation.custom.CubeTransform
import com.numplates.nomera3.modules.moments.show.presentation.custom.SwipeDirection
import com.numplates.nomera3.modules.moments.show.presentation.custom.ViewMomentGestures
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupPositionType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentNavigationType
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsExoPlayerManager
import com.numplates.nomera3.modules.moments.show.presentation.viewevents.ViewMomentEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.ViewMomentState
import com.numplates.nomera3.modules.moments.util.isSmallScreen
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener

class MeeraViewMomentFragment : MeeraBaseDialogFragment(
    layout = R.layout.fragment_view_moment_container,
    behaviourConfigState = ScreenBehaviourState.FullScreenMoment
), ViewMomentPagerParent, ScreenshotTakenListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(FragmentViewMomentContainerBinding::bind)

    private val viewModel by viewModels<ViewMomentViewModel> { App.component.getViewModelFactory() }

    private val ANIMATION_CONTAINER_SCALE = 0.6f
    private val NEW_PAGE_REQUESTING_POSITION = 5
    private val DEFAULT_MOMENTS_PAGE_LIMIT = 10

    private var lastWatchedMomentGroupId: Long = -1L
    private var singleMomentId: Long? = null
    private var startMomentGroupId: Long? = null
    private var targetMomentId: Long? = null
    private var targetCommentId: Long? = null
    private var preventMomentAnimation: Boolean = false
    private var userId: Long? = null
    private var adapter: ViewMomentAdapter? = null
    private var openedFrom: MomentClickOrigin? = null
    private var openedFromViewPosition: IntArray? = null
    private var viewMomentGestures: ViewMomentGestures? = null
    private var momentsPlayerHandler: MomentsExoPlayerManager? = null

    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }

    private val onDefaultFinishAction: () -> Unit = { findNavController().popBackStack() }

    fun close(onFinishAction: (() -> Unit)? = null) {
        onFinishAction?.invoke() ?: onDefaultFinishAction.invoke()
    }

    fun getState(): MomentsFragmentClosingAnimationState = viewModel.getClosingAnimationState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openedFrom = arguments?.getParcelable(KEY_MOMENT_CLICK_ORIGIN)
        singleMomentId = arguments?.getLong(ARG_MOMENT_ID)?.takeIf { it > 0 }
        startMomentGroupId = arguments?.getLong(KEY_START_GROUP_ID)?.takeIf { it > -1 }
        targetMomentId = arguments?.getLong(KEY_MOMENT_TARGET_ID)?.takeIf { it > -1 }
        targetCommentId = arguments?.getLong(KEY_MOMENT_COMMENT_ID)?.takeIf { it > -1 }
        preventMomentAnimation = arguments?.getBoolean(KEY_MOMENT_PREVENT_ANIMATION)?: false
        userId = arguments?.getLong(KEY_USER_ID)
        openedFromViewPosition = arguments?.getIntArray(KEY_OPENED_FROM_VIEW_POSITION)
        momentsPlayerHandler = MomentsExoPlayerManager(requireContext())
    }

    override fun onStart() {
        super.onStart()
        if (isSmallScreen()) {
            binding.vStatusBar.gone()
            return
        }
        binding.vStatusBar.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onUiReady()
        openedFromViewPosition?.let { prepareContainerForAnimate() }
    }

    override fun onScreenshotTaken() {
        binding.vpViewMomentContainer.getCurrentFragment()?.onScreenshotTaken()
    }

    private fun onUiReady() {
        initViews()
        observeMomentEvents()
        viewModel.init(startMomentGroupId = startMomentGroupId, roadType = openedFrom?.toRoadType())
        viewModel.onTriggerViewEvent(
            ViewMomentEvent.FetchMoments(
                momentsSource = clickOriginToMomentSource(openedFrom),
                userId = userId,
                targetMomentId = targetMomentId,
                singleMomentId = singleMomentId
            )
        )
        viewMomentGestures = ViewMomentGestures().apply {
            initGesturesInterceptor(
                extendedGestureOverlayView = binding.govViewMomentGestureInterceptor,
                viewPager2 = binding.vpViewMomentContainer
            )
        }
        setMomentGroupChangeListener()
        setMomentGroupInvalidateListener()
        setMomentGesturesAvailabilityListener()
        viewMomentGestures?.onDragStart = { isHorizontal ->
            binding.vpViewMomentContainer.getCurrentFragment()?.toggleTouchEvents(enable = !isHorizontal)
        }
        viewMomentGestures?.onDragEnd = { swipeDirection ->
            binding.vpViewMomentContainer.getCurrentFragment()?.toggleTouchEvents(enable = true)
            leftIfFirstOrLastMoment(swipeDirection)
        }
    }

    private fun prepareContainerForAnimate() {
        binding.apply {
            clViewMomentRootContainer.scaleX = ANIMATION_CONTAINER_SCALE
            clViewMomentRootContainer.scaleY = ANIMATION_CONTAINER_SCALE
            root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val openFromPoint = getCorrectXYForContainer()
                    openedFromViewPosition = openFromPoint
                    clViewMomentRootContainer.x = openFromPoint[0].toFloat()
                    clViewMomentRootContainer.y = openFromPoint[1].toFloat()
                }
            })
        }
    }

    private fun getCorrectXYForContainer(): IntArray {
        val initialX = (openedFromViewPosition?.get(0) ?: 0).toFloat()
        val initialY = (openedFromViewPosition?.get(1) ?: 0).toFloat()
        val containerHeight = binding.clViewMomentRootContainer.height
        val point = IntArray(2)
        val commonTranslation = (getScreenWidth() * (1 - ANIMATION_CONTAINER_SCALE))
        point[0] = (initialX - commonTranslation).toInt()
        point[1] = (initialY - commonTranslation - ((containerHeight * ANIMATION_CONTAINER_SCALE) / 2)).toInt()
        return point
    }

    private fun leftIfFirstOrLastMoment(swipeDirection: SwipeDirection?) {
        if (swipeDirection == null) return
        val currentList = adapter?.getCurrentList() ?: return
        val currentIndex = binding.vpViewMomentContainer.currentItem
        if (currentIndex == 0 && swipeDirection == SwipeDirection.LEFT) {
            close()
            return
        }
        if (currentIndex == currentList.size - 1 && swipeDirection == SwipeDirection.RIGHT) {
            close()
            return
        }
        val howFlipped = if (swipeDirection == SwipeDirection.RIGHT) {
            AmplitudePropertyMomentHowFlipped.NEXT_SWIPE
        } else {
            AmplitudePropertyMomentHowFlipped.BACK_SWIPE
        }
        logFlipMoment(howFlipped)
    }

    private fun logFlipMoment(howFlipped: AmplitudePropertyMomentHowFlipped) {
        viewModel.logAmplitudeFlipMoment(howFlipped)
    }

     fun onStartFragment() {
            val positionFragment = binding.vpViewMomentContainer.getCurrentFragment()
            val dialogsCreated = positionFragment?.isDialogsCreated() ?: false
            if (!dialogsCreated) positionFragment?.resumeMoment()
            positionFragment?.registerComplaintListener()
            if (isSmallScreen()) hideSystemUi()
    }

    fun onStopFragment() {
        val positionFragment = binding.vpViewMomentContainer.getCurrentFragment()
        positionFragment?.unregisterComplaintListener()
        act.getReactionBubbleViewController().hideReactionBubble()
        if (isSmallScreen()) showSystemUi()
        sendEventsBeforeClose()
    }

    override fun onDestroyView() {
        saveCurrentInfoBeforeClose()
        viewMomentGestures?.destroyGesturesInterceptor()
        binding.vpViewMomentContainer.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        momentsPlayerHandler?.releasePlayer()
        momentsPlayerHandler = null
        val key = when (openedFrom) {
            MomentClickOrigin.Subscriptions -> KEY_SUBSCRIPTION_ROAD_WATCHED_MOMENT_GROUP
            else -> KEY_MAIN_ROAD_WATCHED_MOMENT_GROUP
        }
        setFragmentResult(
            key, bundleOf(
                KEY_MOMENT_GROUP_ID to lastWatchedMomentGroupId,
                KEY_MOMENT_CLICK_ORIGIN to openedFrom
            )
        )
    }

    override fun isCurrentItem(groupId: Long): Boolean {
        val currentItem = binding.vpViewMomentContainer.currentItem
        val currentGroupId = adapter?.getItemId(currentItem) ?: return false
        return currentGroupId == groupId
    }

    override fun onDismissDialog() {
        binding.vpViewMomentContainer.getCurrentFragment()?.onDismissDialog()
    }

    override fun onCreateDialog() {
        binding.vpViewMomentContainer.getCurrentFragment()?.onCreateDialog()
    }

    override fun detectPositionType(groupId: Long): MomentGroupPositionType? {
        val currentItemPosition = binding.vpViewMomentContainer.currentItem
        val pastItemPosition = adapter?.getItemPositionFromId(groupId)
        if (pastItemPosition == null || pastItemPosition == -1) return null
        return when {
            currentItemPosition > pastItemPosition -> MomentGroupPositionType.BEHIND
            currentItemPosition < pastItemPosition -> MomentGroupPositionType.FRONT
            else -> null
        }
    }

    override fun getPlayerHandler() = momentsPlayerHandler

    private fun saveCurrentInfoBeforeClose() {
        val currentFragment = binding.vpViewMomentContainer.getCurrentFragment() ?: return
        val currentItemPosition = binding.vpViewMomentContainer.currentItem
        startMomentGroupId = adapter?.getItemId(currentItemPosition) ?: return
        targetMomentId = currentFragment.getCurrentItemId()
    }

    private fun sendEventsBeforeClose() {
        adapter?.getItem(lastWatchedMomentGroupId)?.userId?.let { userId ->
            if (openedFrom == MomentClickOrigin.fromUserProfile()) {
                viewModel.updateProfileUserMomentsState(userId)
            }
        }
    }

    private fun setMomentGesturesAvailabilityListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_MOMENT_GESTURES,
            viewLifecycleOwner
        ) { _, bundle ->
            val availability = bundle.getBoolean(KEY_MOMENT_GESTURES_AVAILABILITY)
            viewMomentGestures?.toggleGesturesAvailability(availability)
        }
    }

    private fun setMomentGroupChangeListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_MOMENT_GROUP_CHANGE,
            viewLifecycleOwner
        ) { _, bundle ->
            val id = bundle.getLong(KEY_MOMENT_GROUP_CHANGE_ID)
            val direction = bundle.getInt(KEY_MOMENT_GROUP_CHANGE_DIRECTION)
            val invalidateGroup = bundle.getBoolean(KEY_MOMENT_GROUP_CURRENT_INVALIDATE)
            val howUserFlipMoment = bundle
                .getSerializable(KEY_MOMENT_GROUP_THE_WAY_HOW_USER_FLIP) as AmplitudePropertyMomentHowFlipped?
            val fetchMomentsCallback = FetchMomentsOnFinishedScroll()
            if (invalidateGroup) binding.vpViewMomentContainer.registerOnPageChangeCallback(fetchMomentsCallback)
            val groupChangeResult = handleMomentGroupChange(
                currentGroupId = id,
                direction = direction,
                howUserFlipMoment
            )
            handleGroupChangeResult(
                navigationHandlingResult = groupChangeResult,
                invalidateGroup = invalidateGroup,
                fetchMomentsCallback = fetchMomentsCallback
            )
        }
    }

    private fun setMomentGroupInvalidateListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_MOMENT_GROUP_INVALIDATE_ONLY,
            viewLifecycleOwner
        ) { _, _ ->
            viewModel.onTriggerViewEvent(
                ViewMomentEvent.FetchMoments(
                    momentsSource = clickOriginToMomentSource(openedFrom),
                    userId = userId
                )
            )
        }
    }

    private fun handleMomentGroupChange(
        currentGroupId: Long,
        direction: Int,
        howUserFlipMoment : AmplitudePropertyMomentHowFlipped?
    ): MomentNavigationHandling {
        return when (direction) {
            MomentNavigationType.NEXT.direction -> selectNextGroup(currentGroupId, howUserFlipMoment)
            MomentNavigationType.PREVIOUS.direction -> selectPrevGroup(currentGroupId, howUserFlipMoment)
            else -> error("Couldn't handle group change. Invalid direction = $direction")
        }
    }

    private fun handleGroupChangeResult(
        navigationHandlingResult: MomentNavigationHandling,
        invalidateGroup: Boolean,
        fetchMomentsCallback: FetchMomentsOnFinishedScroll,
    ) {
        when (navigationHandlingResult) {
            MomentNavigationHandling.NOT_HANDLED -> {
                adapter?.getCurrentList()?.let { momentGroupList ->
                    val momentsCount = momentGroupList.sumOf { it.momentsCount }
                    viewModel.logEndMoments(momentsCount)
                }
                if (invalidateGroup) binding.vpViewMomentContainer.unregisterOnPageChangeCallback(fetchMomentsCallback)
                close()
            }

            MomentNavigationHandling.PAGE_CHANGED_ALREADY -> {
                if (invalidateGroup) {
                    binding.vpViewMomentContainer.unregisterOnPageChangeCallback(fetchMomentsCallback)
                    viewModel.onTriggerViewEvent(
                        ViewMomentEvent.FetchMoments(
                            momentsSource = clickOriginToMomentSource(openedFrom),
                            userId = userId
                        )
                    )
                }
            }
            else -> Unit
        }
    }

    /**
     * Tries to select the next group ahead of [currentGroupId]
     * @return NOT_HANDLED when we can't navigate between groups
     *
     * PAGE_CHANGED if we set a new current item with smoothScroll
     *
     * PAGE_CHANGED_ALREADY if index of page change request is different
     * from current index, meaning user changed pages themselves
     */
    private fun selectNextGroup(
        currentGroupId: Long,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped?
    ): MomentNavigationHandling {
        val currentList = adapter?.getCurrentList() ?: return MomentNavigationHandling.NOT_HANDLED
        val currentIndex = binding.vpViewMomentContainer.currentItem
        val calledFromIndex = currentList.indexOfFirst { it.id == currentGroupId }
        if (currentIndex != calledFromIndex) return MomentNavigationHandling.PAGE_CHANGED_ALREADY
        return when (currentIndex) {
            in 0 until currentList.size - 1 -> setCurrentViewPagerItem(currentIndex + 1, howUserFlipMoment)
            else -> MomentNavigationHandling.NOT_HANDLED
        }
    }

    /**
     * Tries to select the group before [currentGroupId]
     * @return NOT_HANDLED when we can't navigate between groups
     *
     * PAGE_CHANGED if we set a new current item with smoothScroll
     *
     * PAGE_CHANGED_ALREADY if index of page change request is different
     * from current index, meaning user changed pages themselves
     */
    private fun selectPrevGroup(
        currentGroupId: Long,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped?
    ): MomentNavigationHandling {
        val currentList = adapter?.getCurrentList() ?: return MomentNavigationHandling.NOT_HANDLED
        val currentIndex = binding.vpViewMomentContainer.currentItem
        val calledFromIndex = currentList.indexOfFirst { it.id == currentGroupId }
        if (currentIndex != calledFromIndex) return MomentNavigationHandling.PAGE_CHANGED_ALREADY
        return when (currentIndex) {
            in 1 until currentList.size -> setCurrentViewPagerItem(currentIndex - 1, howUserFlipMoment)
            else -> MomentNavigationHandling.NOT_HANDLED
        }
    }

    private fun setCurrentViewPagerItem(
        index: Int,
        howUserFlipMoment: AmplitudePropertyMomentHowFlipped?
    ): MomentNavigationHandling {
        toggleOffscreenPageLimit()
        if (binding.vpViewMomentContainer.isFakeDragging) binding.vpViewMomentContainer.endFakeDrag()
        binding.vpViewMomentContainer.setCurrentItem(index, true)
        howUserFlipMoment?.let { logFlipMoment(it) }
        return MomentNavigationHandling.PAGE_CHANGED
    }

    private fun observeMomentEvents() {
        viewModel.viewMomentState.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ViewMomentState.MomentsDataReceived -> {
                    if (event.momentGroups.isEmpty()) {
                        close()
                    } else {
                        adapter?.submitList(event.momentGroups)
                        initFirstMoment()
                    }
                }

                is ViewMomentState.MomentsPaginatedDataReceived -> {
                    adapter?.submitList(event.momentGroups)
                }

                else -> Unit
            }
        }
    }

    private fun initFirstMoment() {
        val groupId = startMomentGroupId ?: return
        adapter?.getCurrentList()?.indexOfFirst { it.id == groupId }?.let { index ->
            binding.vpViewMomentContainer.setCurrentItem(index, false)
        }
        startMomentGroupId = null
    }

    private fun initViews() {
        tuneOffscreenPageLimit()
        if (adapter == null) {
            adapter = ViewMomentAdapter(
                fragment = this,
                momentsSource = clickOriginToMomentSource(openedFrom),
                targetMomentId = targetMomentId,
                targetCommentId = targetCommentId,
                singleMomentId = singleMomentId
            )
        }
        binding.vpViewMomentContainer.apply {
            adapter = this@MeeraViewMomentFragment.adapter
            isSaveEnabled = false
            setPageTransformer(CubeTransform())
            registerOnPageChangeCallback(LastMomentGroupWatcher())
        }
    }

    /**
     * Регулирует прогрузку следующих / предыдущих фрагментов.
     * Исправляет баг фриза первого свайпа вправо переключения групп моментов.
     */
    private fun tuneOffscreenPageLimit() {
        binding.vpViewMomentContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    SCROLL_STATE_IDLE -> 2
                    SCROLL_STATE_DRAGGING -> 1
                    else -> null
                }?.let { toggleOffscreenPageLimit(it) }
                super.onPageScrollStateChanged(state)
            }
        })
        toggleOffscreenPageLimit(2)
    }

    private fun toggleOffscreenPageLimit(limit: Int = 1) {
        if (binding.vpViewMomentContainer.offscreenPageLimit == limit) return
        binding.vpViewMomentContainer.offscreenPageLimit = limit
    }

    private fun MomentClickOrigin.toRoadType(): RoadTypesEnum? {
        return when (this) {
            MomentClickOrigin.Main -> RoadTypesEnum.MAIN
            MomentClickOrigin.Subscriptions -> RoadTypesEnum.SUBSCRIPTION
            else -> null
        }
    }

    /**
     * Находит текущий фрагмент просмотра момента.
     * @return MeeraViewMomentPositionFragment - если текущий фрагмент является фрагментом просмотра момента.
     * @throws NullPointerException - Если [MeeraViewMomentPositionFragment] не найден.
     */
    private fun ViewPager2.getCurrentFragment(): MeeraViewMomentPositionFragment? {
        val currentItemId = adapter?.getItemId(currentItem)
        val positionFragment = childFragmentManager.fragments.filterIsInstance<MeeraViewMomentPositionFragment>()
            .firstOrNull { it.arguments?.getLong(ARG_MOMENT_GROUP_ID) == currentItemId }
        return positionFragment
    }

    private fun clickOriginToMomentSource(momentClickOrigin: MomentClickOrigin?): GetMomentDataUseCase.MomentsSource {
        return when (momentClickOrigin) {
            is MomentClickOrigin.Main -> GetMomentDataUseCase.MomentsSource.Main
            is MomentClickOrigin.Subscriptions -> GetMomentDataUseCase.MomentsSource.Subscription
            is MomentClickOrigin.User,
            is MomentClickOrigin.Profile -> GetMomentDataUseCase.MomentsSource.User
            else -> GetMomentDataUseCase.MomentsSource.User
        }
    }

    private inner class FetchMomentsOnFinishedScroll : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.vpViewMomentContainer.unregisterOnPageChangeCallback(this)
            viewModel.onTriggerViewEvent(
                ViewMomentEvent.FetchMoments(
                    momentsSource = clickOriginToMomentSource(openedFrom),
                    userId = userId
                )
            )
        }
    }

    private inner class LastMomentGroupWatcher : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            adapter?.getItem(position)?.let { momentGroup ->
                lastWatchedMomentGroupId = momentGroup.id
            }
            controlRequestingNewMomentsGroupsPage(position)
        }
    }

    private fun controlRequestingNewMomentsGroupsPage(watchingGroupPosition: Int) {
        if (watchingGroupPosition % NEW_PAGE_REQUESTING_POSITION == 0
            && watchingGroupPosition % DEFAULT_MOMENTS_PAGE_LIMIT != 0
        ) {
            viewModel.requestNewMomentsGroupsPage()
        }
    }

    private fun showSystemUi() {
        val insetsController = ViewCompat.getWindowInsetsController(act.window.decorView) ?: return
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        act.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun hideSystemUi() {
        val insetsController = ViewCompat.getWindowInsetsController(act.window.decorView) ?: return
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        act.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}

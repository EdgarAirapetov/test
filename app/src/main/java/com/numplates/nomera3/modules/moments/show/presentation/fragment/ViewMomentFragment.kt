package com.numplates.nomera3.modules.moments.show.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.FragmentViewMomentContainerBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowFlipped
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
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
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.presentation.router.BaseFragmentNew

const val KEY_MAIN_ROAD_WATCHED_MOMENT_GROUP = "KEY_MAIN_ROAD_WATCHED_MOMENT_GROUP"
const val KEY_SUBSCRIPTION_ROAD_WATCHED_MOMENT_GROUP = "KEY_SUBSCRIPTION_ROAD_WATCHED_MOMENT_GROUP"
const val KEY_USER_ID = "KEY_USER_ID"
const val KEY_START_GROUP_ID = "KEY_START_GROUP_ID"
const val KEY_MOMENT_GROUP_ID = "KEY_MOMENT_GROUP_ID"
const val KEY_MOMENT_TARGET_ID = "KEY_MOMENT_TARGET_ID"
const val KEY_MOMENT_CLICK_ORIGIN = "KEY_MOMENT_CLICK_ORIGIN"
const val KEY_MOMENT_GROUP_CHANGE = "KEY_MOMENT_GROUP_CHANGE"
const val KEY_MOMENT_GROUP_INVALIDATE_ONLY = "KEY_MOMENT_GROUP_INVALIDATE_ONLY"
const val KEY_MOMENT_GROUP_CHANGE_ID = "KEY_MOMENT_GROUP_CHANGE_ID"
const val KEY_MOMENT_GROUP_CHANGE_DIRECTION = "KEY_MOMENT_GROUP_CHANGE_DIRECTION"
const val KEY_MOMENT_GROUP_CURRENT_INVALIDATE = "KEY_MOMENT_GROUP_CURRENT_INVALIDATE"
const val KEY_MOMENT_GROUP_THE_WAY_HOW_USER_FLIP = "KEY_MOMENT_GROUP_THE_WAY_HOW_USER_FLIP"
const val KEY_MOMENT_GESTURES = "KEY_MOMENT_GESTURES"
const val KEY_MOMENT_GESTURES_AVAILABILITY = "KEY_MOMENT_GESTURES_AVAILABILITY"
const val KEY_OPENED_FROM_VIEW_POSITION = "KEY_FROM_VIEW_POSITION"
const val KEY_MOMENT_LAST_REACTION_TYPE = "KEY_MOMENT_LAST_REACTION_TYPE"
const val KEY_MOMENT_PUSH_INFO = "KEY_MOMENT_PUSH_INFO"
const val KEY_MOMENT_COMMENT_ID = "KEY_MOMENT_COMMENT_ID"
const val KEY_MOMENT_PREVENT_ANIMATION = "KEY_MOMENT_PREVENT_ANIMATION"

class ViewMomentFragment : BaseFragmentNew<FragmentViewMomentContainerBinding>(), ViewMomentPagerParent, ScreenshotTakenListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewMomentContainerBinding
        get() = FragmentViewMomentContainerBinding::inflate

    private val viewModel by viewModels<ViewMomentViewModel> { App.component.getViewModelFactory() }

    private val ANIMATION_CONTAINER_SCALE = 0.6f
    private val OPEN_ANIMATION_DELAY = 200L
    private val OPEN_ANIMATION_DURATION = 300L
    private val CLOSE_ANIMATION_DURATION = 200L
    private val NEW_PAGE_REQUESTING_POSITION = 5
    private val DEFAULT_MOMENTS_PAGE_LIMIT = 10

    private var lastWatchedMomentGroupId: Long = -1L
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
    private var openCloseAnimation: ViewPropertyAnimator? = null

    private val onDefaultFinishAction: () -> Unit = { act.onBackPressed() }

    fun close(onFinishAction: (() -> Unit)? = null) {
        animateContainerViewForClose(endAction = onFinishAction ?: onDefaultFinishAction)
    }

    fun getState(): MomentsFragmentClosingAnimationState = viewModel.getClosingAnimationState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openedFrom = arguments?.getParcelable(KEY_MOMENT_CLICK_ORIGIN)
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
            binding?.vStatusBar?.gone()
            return
        }
        binding?.vStatusBar?.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onUiReady()
        openedFromViewPosition?.let { prepareContainerForAnimate() }
    }

    override fun onScreenshotTaken() {
        binding?.vpViewMomentContainer?.getCurrentFragment()?.onScreenshotTaken()
    }

    private fun onUiReady() {
        initViews()
        observeMomentEvents()
        viewModel.init(startMomentGroupId = startMomentGroupId, roadType = openedFrom?.toRoadType())
        viewModel.onTriggerViewEvent(
            ViewMomentEvent.FetchMoments(
                momentsSource = clickOriginToMomentSource(openedFrom),
                userId = userId,
                targetMomentId = targetMomentId
            )
        )
        viewMomentGestures = ViewMomentGestures().apply {
            initGesturesInterceptor(
                extendedGestureOverlayView = binding?.govViewMomentGestureInterceptor,
                viewPager2 = binding?.vpViewMomentContainer
            )
        }
        setMomentGroupChangeListener()
        setMomentGroupInvalidateListener()
        setMomentGesturesAvailabilityListener()
        viewMomentGestures?.onDragStart = { isHorizontal ->
            binding?.vpViewMomentContainer?.getCurrentFragment()?.toggleTouchEvents(enable = !isHorizontal)
        }
        viewMomentGestures?.onDragEnd = { swipeDirection ->
            binding?.vpViewMomentContainer?.getCurrentFragment()?.toggleTouchEvents(enable = true)
            leftIfFirstOrLastMoment(swipeDirection)
        }
    }

    private fun prepareContainerForAnimate() {
        binding?.apply {
            clViewMomentRootContainer.scaleX = ANIMATION_CONTAINER_SCALE
            clViewMomentRootContainer.scaleY = ANIMATION_CONTAINER_SCALE
            root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val openFromPoint = getCorrectXYForContainer()
                    openedFromViewPosition = openFromPoint
                    clViewMomentRootContainer.x = openFromPoint[0].toFloat()
                    clViewMomentRootContainer.y = openFromPoint[1].toFloat()
                    animateContainerViewForOpen()
                }
            })
        }
    }

    private fun animateContainerViewForOpen() {
        binding?.apply {
            if (!preventMomentAnimation) {
                openCloseAnimation = clViewMomentRootContainer.animate().scaleX(1f).scaleY(1f)
                    .x(0f).y(0f).alpha(1f).setDuration(OPEN_ANIMATION_DURATION)
                    .setStartDelay(OPEN_ANIMATION_DELAY).withEndAction {
                        act.setStatusBar()
                    }
                openCloseAnimation?.start()
            } else {
                clViewMomentRootContainer.apply {
                    scaleX = 1f
                    scaleY = 1f
                    x = 0f
                    y = 0f
                    alpha = 1f
                }
                act.setStatusBar()
            }
        }
    }

    private fun animateContainerViewForClose(endAction: () -> Unit) {
        viewModel.onTriggerViewEvent(ViewMomentEvent.ChangedClosingState(MomentsFragmentClosingAnimationState.IN_PROGRESS))
        if (!preventMomentAnimation) {
            act.setNavigationMomentsPageTransformer()
            openCloseAnimation?.cancel()

            binding?.apply {
                val targetX = (openedFromViewPosition?.get(0) ?: 0).toFloat()
                val targetY = (openedFromViewPosition?.get(1) ?: 0).toFloat()

                openCloseAnimation =
                    clViewMomentRootContainer.animate().scaleX(ANIMATION_CONTAINER_SCALE)
                        .scaleY(ANIMATION_CONTAINER_SCALE)
                        .x(targetX).y(targetY).alpha(0f).setDuration(CLOSE_ANIMATION_DURATION).withEndAction {
                            viewModel.onTriggerViewEvent(
                                ViewMomentEvent.ChangedClosingState(
                                    MomentsFragmentClosingAnimationState.FINISHED
                                )
                            )
                            endAction.invoke()
                        }
                openCloseAnimation?.start()
            }
        } else {
            viewModel.onTriggerViewEvent(
                ViewMomentEvent.ChangedClosingState(
                    MomentsFragmentClosingAnimationState.FINISHED
                )
            )
            endAction.invoke()
        }
    }

    private fun getCorrectXYForContainer(): IntArray {
        val initialX = (openedFromViewPosition?.get(0) ?: 0).toFloat()
        val initialY = (openedFromViewPosition?.get(1) ?: 0).toFloat()
        val containerHeight = binding?.clViewMomentRootContainer?.height ?: 0
        val point = IntArray(2)
        val commonTranslation = (getScreenWidth() * (1 - ANIMATION_CONTAINER_SCALE))
        point[0] = (initialX - commonTranslation).toInt()
        point[1] = (initialY - commonTranslation - ((containerHeight * ANIMATION_CONTAINER_SCALE) / 2)).toInt()
        return point
    }

    private fun leftIfFirstOrLastMoment(swipeDirection: SwipeDirection?) {
        if (swipeDirection == null) return
        val currentList = adapter?.getCurrentList() ?: return
        val currentIndex = binding?.vpViewMomentContainer?.currentItem
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

    private fun setSwipeState(lockSwipe: Boolean) {
        act.navigatorViewPager.let { if (lockSwipe) it.lockSwipe() else it.unlockSwipe() }
    }

    override fun onStartFragment() {
        if (act.getCurrentFragment() is ViewMomentFragment) {
            val positionFragment = binding?.vpViewMomentContainer?.getCurrentFragment()
            val dialogsCreated = positionFragment?.isDialogsCreated() ?: false
            if (!dialogsCreated) positionFragment?.resumeMoment()
            positionFragment?.registerComplaintListener()
            setSwipeState(lockSwipe = true)
            if (isSmallScreen()) hideSystemUi()
        }
        super.onStartFragment()
    }

    override fun onStopFragment() {
        val positionFragment = binding?.vpViewMomentContainer?.getCurrentFragment()
        positionFragment?.pauseMoment()
        if (act.getCurrentFragment() is ViewMomentFragment) {
            positionFragment?.unregisterComplaintListener()
        }
        preventSwipeForAuth()
        act.getReactionBubbleViewController().hideReactionBubble()
        if (isSmallScreen()) showSystemUi()
        sendEventsBeforeClose()
        super.onStopFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewMomentGestures?.destroyGesturesInterceptor()
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
        val currentItem = binding?.vpViewMomentContainer?.currentItem ?: return false
        val currentGroupId = adapter?.getItemId(currentItem) ?: return false
        return currentGroupId == groupId
    }

    override fun onDismissDialog() {
        binding?.vpViewMomentContainer?.getCurrentFragment()?.onDismissDialog()
    }

    override fun onCreateDialog() {
        binding?.vpViewMomentContainer?.getCurrentFragment()?.onCreateDialog()
    }

    override fun detectPositionType(groupId: Long): MomentGroupPositionType? {
        val currentItemPosition = binding?.vpViewMomentContainer?.currentItem ?: return null
        val pastItemPosition = adapter?.getItemPositionFromId(groupId)
        if (pastItemPosition == null || pastItemPosition == -1) return null
        return when {
            currentItemPosition > pastItemPosition -> MomentGroupPositionType.BEHIND
            currentItemPosition < pastItemPosition -> MomentGroupPositionType.FRONT
            else -> null
        }
    }

    override fun getPlayerHandler() = momentsPlayerHandler

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
            if (invalidateGroup) binding?.vpViewMomentContainer?.registerOnPageChangeCallback(fetchMomentsCallback)
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
                if (invalidateGroup) binding?.vpViewMomentContainer?.unregisterOnPageChangeCallback(fetchMomentsCallback)
                close()
            }

            MomentNavigationHandling.PAGE_CHANGED_ALREADY -> {
                if (invalidateGroup) {
                    binding?.vpViewMomentContainer?.unregisterOnPageChangeCallback(fetchMomentsCallback)
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
        val currentIndex = binding?.vpViewMomentContainer?.currentItem
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
        val currentIndex = binding?.vpViewMomentContainer?.currentItem
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
        if (binding?.vpViewMomentContainer?.isFakeDragging == true) binding?.vpViewMomentContainer?.endFakeDrag()
        binding?.vpViewMomentContainer?.setCurrentItem(index, true)
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
            binding?.vpViewMomentContainer?.setCurrentItem(index, false)
        }
        startMomentGroupId = null
    }

    private fun initViews() {
        tuneOffscreenPageLimit()
        adapter = ViewMomentAdapter(
            fragment = this,
            momentsSource = clickOriginToMomentSource(openedFrom),
            targetMomentId = targetMomentId,
            targetCommentId = targetCommentId
        )
        binding?.vpViewMomentContainer?.apply {
            adapter = this@ViewMomentFragment.adapter
            setPageTransformer(CubeTransform())
            registerOnPageChangeCallback(LastMomentGroupWatcher())
        }
    }

    /**
     * Регулирует прогрузку следующих / предыдущих фрагментов.
     * Исправляет баг фриза первого свайпа вправо переключения групп моментов.
     */
    private fun tuneOffscreenPageLimit() {
        binding?.vpViewMomentContainer?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
        if (binding?.vpViewMomentContainer?.offscreenPageLimit == limit) return
        binding?.vpViewMomentContainer?.offscreenPageLimit = limit
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
     * @return ViewMomentPositionFragment - если текущий фрагмент является фрагментом просмотра момента.
     * @throws NullPointerException - Если [ViewMomentPositionFragment] не найден.
     */
    private fun ViewPager2.getCurrentFragment(): ViewMomentPositionFragment? {
        val currentItemId = adapter?.getItemId(currentItem)
        val positionFragment = childFragmentManager.fragments.filterIsInstance<ViewMomentPositionFragment>()
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

    /**
     * Stops the swipe from being explicitly enabled for [RegistrationContainerFragment] when leaving this fragment.
     *
     * [RegistrationContainerFragment] itself disables swipes, and behaves incorrectly with them enabled.
     *
     * **Problematic behavior:** If we leave [RegistrationContainerFragment] with a swipe, the auth flow will not complete properly.
     * This causes the inability to open Auth Screen from this fragment instance.
     *
     * @see [ViewMomentPositionFragment.preventSwipeForAuth]
     */
    private fun preventSwipeForAuth() {
        if (act.getCurrentFragment() !is RegistrationContainerFragment) {
            setSwipeState(lockSwipe = false)
        }
    }

    private inner class FetchMomentsOnFinishedScroll : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding?.vpViewMomentContainer?.unregisterOnPageChangeCallback(this)
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

enum class MomentNavigationHandling {
    /**
     * Navigation between groups couldn't be handled, should leave moment viewing
     */
    NOT_HANDLED,

    /**
     * Navigation between groups succeeded, changing groups ourselves
     */
    PAGE_CHANGED,

    /**
     * Navigation between groups was done by user manually swiping to another group
     */
    PAGE_CHANGED_ALREADY
}

enum class MomentsFragmentClosingAnimationState {
    /**
     * Animation of closing container in progress
     */
    NOT_STARTED,

    /**
     * Animation of closing container in progress
     */
    IN_PROGRESS,

    /**
     * Animation of closing container ended
     */
    FINISHED
}

interface ViewMomentPagerParent {
    fun isCurrentItem(groupId: Long): Boolean
    fun detectPositionType(groupId: Long): MomentGroupPositionType?
    fun getPlayerHandler(): MomentsExoPlayerManager?
    fun onDismissDialog()
    fun onCreateDialog()
}

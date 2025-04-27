package com.numplates.nomera3.modules.redesign.fragments.base

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.extensions.animateHorizontalMargins
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.orFalse
import com.meera.core.extensions.setMargins
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraEmptyMapFragment
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import timber.log.Timber


private const val TAG = "MEERA_BASE_DIALOG_FRAGMENT"
private const val SLIDE_THRESHOLD = 0.95F
private const val HALF_CHANGE_PERCENT = 0.86F
private const val HALF_BOTTOM_SCREEN_PERCENT = 0.6F
private const val FULLSCREEN_PERCENT = 1f

/**
 * Значения от 0 до 1
 * */
private const val DEF_PERCENT_CONTAINER_HEIGHT = 0.5f
private const val DEF_HEIGHT_SNIPPET_CONTAINER = 440
private const val DEF_HEIGHT_EVENT_SNIPPET_CONTAINER = 700
private const val DEF_HEIGHT_EVENTLIST_CONTAINER = 460
const val DEF_HEIGHT_USERINFO_SNIPPET_CONTAINER = 430
private const val DEF_HORIZONTAL_MARGIN_SNIPPET_CONTAINER = 16
private const val MAX_HEIGHT_NO_MAX_SIZE = -1

sealed class ScreenBehaviourState {
    data object Empty : ScreenBehaviourState()
    data object MapTransparent : ScreenBehaviourState()
    data object Full : ScreenBehaviourState()
    data class ScrollableFull(val isScrollable: Boolean = false) : ScreenBehaviourState()
    data object ScrollableHalf : ScreenBehaviourState()
    data object ScrollableHalfProfile : ScreenBehaviourState()
    data object ScrollableHalfMain : ScreenBehaviourState()
    data object CommunitiesTransparent : ScreenBehaviourState()

    data class Snippet(
        val height: Int = DEF_HEIGHT_SNIPPET_CONTAINER,
        val horizontalMargin: Int = DEF_HORIZONTAL_MARGIN_SNIPPET_CONTAINER,
        @FloatRange(0.0, 1.0) val percentHeight: Float = DEF_PERCENT_CONTAINER_HEIGHT,
        val isCollapsedInit: Boolean = true
    ) : ScreenBehaviourState()

    data class EventSnippet(
        val height: Int = DEF_HEIGHT_EVENT_SNIPPET_CONTAINER,
        val horizontalMargin: Int = DEF_HORIZONTAL_MARGIN_SNIPPET_CONTAINER,
        @FloatRange(0.0, 1.0) val percentHeight: Float = DEF_PERCENT_CONTAINER_HEIGHT
    ) : ScreenBehaviourState()

    data object EventList : ScreenBehaviourState()

    data class BottomScreens(
        @FloatRange(0.0, 1.0) val percentHeight: Float = DEF_PERCENT_CONTAINER_HEIGHT,
        val isFullWidth: Boolean = false,
        val isDraggable: Boolean = true
    ) : ScreenBehaviourState()

    data class BottomScreenHalfExpanded(
        @FloatRange(0.0, 1.0) val percentHeight: Float = DEF_PERCENT_CONTAINER_HEIGHT
    ) : ScreenBehaviourState()

    data object BottomScreensWrapContent : ScreenBehaviourState()

    data object FullScreenMoment : ScreenBehaviourState()

    data object EventParticipants : ScreenBehaviourState()

    data object Authorizes : ScreenBehaviourState()

    data object Calls : ScreenBehaviourState()
}

const val SPEED_ANIMATION_CHANGING_HEIGHT_MLS = 200L

abstract class MeeraBaseDialogFragment(
    @LayoutRes layout: Int = R.layout.empty_layout, private val behaviourConfigState: ScreenBehaviourState
) : BottomSheetDialogFragment(layout) {

    private val bnManager by lazy { NavigationManager.getManager() }

    private val childClass = this.javaClass.simpleName

    private val statusBarHeight by lazy { context.getStatusBarHeight() }

    private val toolbarHeight get() = bnManager.toolbarAndBottomInteraction.getToolbar().heightWithoutShadow

    private var sheetState: Int = STATE_HALF_EXPANDED

    private var halfMainSavedState: Int = STATE_HALF_EXPANDED
    private var halfProfileSavedState: Int = STATE_HALF_EXPANDED
    private var halfMainActionBarState: UiKitToolbarViewState = UiKitToolbarViewState.EXPANDED
    private var halfProfileActionBarState: UiKitToolbarViewState = UiKitToolbarViewState.EXPANDED
    private var isSettling = false
    protected var isOpeningEvent = false
    var notFromMap = true

    var isShowShadow: Boolean? = null
        set(value) {
            field = value
            bnManager.toolbarAndBottomInteraction.getToolbar().showShadow = value.orFalse()
        }

    // Fix for map fragments
    var isFullDraggable = false

    var isApplyNavigationConfig: Boolean = true

    var altSheetBehaviour: ScreenBehaviourState? = null

    var isRecreated = false

    @get:IdRes
    abstract val containerId: Int

    open val isBottomNavBarVisibility: UiKitNavigationBarViewVisibilityState =
        UiKitNavigationBarViewVisibilityState.GONE

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("$TAG ON_VIEW_CREATED: $childClass, savedInstanceState: $savedInstanceState")

        if (isApplyNavigationConfig) initSheetBehaviour(altSheetBehaviour)

        activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
            BottomSheetBehavior.from(containerView).apply {
                behaviourCallback?.let {
                    addBottomSheetCallback(it)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("$TAG ON_START: $childClass")

        applyConfigToToolbar()

        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
            isBottomNavBarVisibility
    }

    protected fun getContainerFragmentId(): Int {
        return if (parentFragment?.id == R.id.fragment_first_container_view || parentFragment?.id == R.id.fragment_second_container_view) {
            parentFragment?.id ?: containerId
        } else {
            containerId
        }
    }

    private fun applyConfigToToolbar() {
        val toolbar = bnManager.toolbarAndBottomInteraction.getToolbar()

        when (behaviourConfigState) {
            is ScreenBehaviourState.Full -> toolbar.state = UiKitToolbarViewState.COLLAPSED
            ScreenBehaviourState.EventList -> toolbar.state = UiKitToolbarViewState.COLLAPSED
            ScreenBehaviourState.FullScreenMoment -> toolbar.state = UiKitToolbarViewState.COLLAPSED
            ScreenBehaviourState.MapTransparent -> toolbar.state = UiKitToolbarViewState.COLLAPSED
            is ScreenBehaviourState.ScrollableFull -> toolbar.state = UiKitToolbarViewState.EXPANDED
            ScreenBehaviourState.ScrollableHalf -> toolbar.state = UiKitToolbarViewState.EXPANDED
            ScreenBehaviourState.ScrollableHalfMain -> toolbar.state = halfMainActionBarState
            is ScreenBehaviourState.CommunitiesTransparent -> toolbar.state = UiKitToolbarViewState.COLLAPSED
            ScreenBehaviourState.ScrollableHalfProfile -> toolbar.state = if (altSheetBehaviour == null) {
                halfProfileActionBarState
            } else {
                UiKitToolbarViewState.COLLAPSED
            }

            ScreenBehaviourState.Authorizes,
            ScreenBehaviourState.Calls,
            is ScreenBehaviourState.Snippet -> toolbar.state = UiKitToolbarViewState.COLLAPSED

            is ScreenBehaviourState.EventSnippet -> toolbar.state = UiKitToolbarViewState.COLLAPSED

            is ScreenBehaviourState.BottomScreenHalfExpanded,
            is ScreenBehaviourState.BottomScreens,
            is ScreenBehaviourState.BottomScreensWrapContent,
            ScreenBehaviourState.Empty -> Unit

            ScreenBehaviourState.EventParticipants -> toolbar.state = UiKitToolbarViewState.COLLAPSED
        }
    }

    protected open fun ignoreSlide() = false

    /** The initSheetBehaviourForTop function initializes the behavior of a BottomSheet within your Android
     *  application. It configures various properties of the BottomSheet based on the current behaviourConfigState.
     *  Purpose:
     * This function is responsible for setting up the desired behavior of a BottomSheet in your application.
     * It handles various aspects of the BottomSheet's appearance and interaction based on the
     * configured state and settings.
     * Usage:
     * You would typically call this function during the initialization phase of your activity or fragment to configure
     * the BottomSheet's behavior before it's displayed to the user. The specific configurations applied depend on
     * the behaviourConfigState and the settings defined in behaviourConfigState.config.*/

    private var behaviourCallback: BottomSheetBehavior.BottomSheetCallback? = null

    private fun initStatusAndNavBarElementsColor(isDark: Boolean) {
        val window = requireActivity().window

        window.navigationBarColor = ContextCompat.getColor(
            requireContext(), if (isDark) R.color.uiKitColorPrimaryBlack else R.color.uiKitColorForegroundInvers
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        } else {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    private fun initSheetBehaviour(altSheetBehaviour: ScreenBehaviourState? = null) {
        val behaviourConfigState = altSheetBehaviour ?: behaviourConfigState
        activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
            BottomSheetBehavior.from(containerView).apply {

                if (isShowShadow == null) bnManager.toolbarAndBottomInteraction.getToolbar().showShadow = false

                when (behaviourConfigState) {
                    ScreenBehaviourState.Empty -> Unit

                    ScreenBehaviourState.MapTransparent -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)

                        expandedOffset = statusBarHeight
                        isDraggable = true

                        maxHeight = getContainerHeight()

                        doDelayed(SPEED_ANIMATION_CHANGING_HEIGHT_MLS) {
                            state = STATE_HIDDEN
//                            bnManager.toolbarAndBottomInteraction.getToolbar().state = UiKitToolbarViewState.EXPANDED
                        }
                    }

                    is ScreenBehaviourState.Full -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        expandedOffset = statusBarHeight
                        isDraggable = false || isFullDraggable || !notFromMap

                        if (!notFromMap) {
                            skipCollapsed = true
                        }
                        expandedOffset = statusBarHeight
                        maxHeight = getContainerHeight()

                        doDelayed(SPEED_ANIMATION_CHANGING_HEIGHT_MLS) {
                            state = STATE_EXPANDED
                            updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        }


                        if (notFromMap) {
                            (activity as? MeeraAct)?.setStatusBarColor(R.color.ui_white)
                        }

                        initStatusAndNavBarElementsColor(isDark = false)
                    }

                    is ScreenBehaviourState.EventParticipants -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        expandedOffset = 0
                        isDraggable = false || isFullDraggable

                        expandedOffset = 0
                        maxHeight = MAX_HEIGHT_NO_MAX_SIZE

                        state = STATE_EXPANDED
                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        initStatusAndNavBarElementsColor(isDark = false)
                    }

                    is ScreenBehaviourState.FullScreenMoment -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        expandedOffset = 0
                        isDraggable = false || isFullDraggable

                        expandedOffset = 0
                        maxHeight = MAX_HEIGHT_NO_MAX_SIZE

                        state = STATE_EXPANDED
                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        (activity as? MeeraAct)?.setStatusBarColor(R.color.transparent)
                        initStatusAndNavBarElementsColor(isDark = true)
                    }

                    is ScreenBehaviourState.Authorizes -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        expandedOffset = 0
                        isDraggable = false

                        expandedOffset = 0
                        maxHeight = MAX_HEIGHT_NO_MAX_SIZE

                        state = STATE_EXPANDED
                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        (activity as? MeeraAct)?.setStatusBarColor(R.color.transparent)
                        initStatusAndNavBarElementsColor(isDark = true)
                    }

                    is ScreenBehaviourState.Calls -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        expandedOffset = 0
                        isDraggable = false

                        expandedOffset = 0
                        maxHeight = MAX_HEIGHT_NO_MAX_SIZE

                        state = STATE_EXPANDED
                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        (activity as? MeeraAct)?.setStatusBarColor(R.color.transparent)
                        initStatusAndNavBarElementsColor(isDark = true)
                    }

                    is ScreenBehaviourState.ScrollableFull -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.setMargins(
                            start = 0,
                            end = 0
                        )

                        expandedOffset = toolbarHeight + statusBarHeight
                        isDraggable = behaviourConfigState.isScrollable
                        skipCollapsed = true
                        isHideable = true

                        maxHeight = getContainerHeight()
                        isFitToContents = false

                        halfExpandedRatio = HALF_CHANGE_PERCENT

                        state = STATE_EXPANDED
                        updateToolbarState(UiKitToolbarViewState.EXPANDED)
                        val shouldWhiteStatusBar = state == STATE_EXPANDED
                        val statusBarColor = if (shouldWhiteStatusBar) {
                            R.color.uiKitColorForegroundInvers
                        } else {
                            R.color.transparent
                        }
                        (activity as? MeeraAct)?.setStatusBarColor(statusBarColor)
                    }

                    ScreenBehaviourState.ScrollableHalf -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        state = STATE_HALF_EXPANDED

                        expandedOffset = toolbarHeight + statusBarHeight
                        maxHeight = getContainerHeight()
                        isFitToContents = false
                        isDraggable = true
                        skipCollapsed = true
                        isHideable = true

                        doDelayed(SPEED_ANIMATION_CHANGING_HEIGHT_MLS) {
                            state = STATE_EXPANDED
                        }
                        updateToolbarState(UiKitToolbarViewState.EXPANDED)
                    }

                    is ScreenBehaviourState.Snippet -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        if (behaviourConfigState.isCollapsedInit) {
                            state = STATE_COLLAPSED
                        }

                        containerView.setBackgroundColor(requireContext().getColor(R.color.transparent))
                        isFitToContents = true
                        isDraggable = true
                        skipCollapsed = false
                        isHideable = true

                        peekHeight = behaviourConfigState.height.dp

                        bnManager.toolbarAndBottomInteraction.getToolbar().state = UiKitToolbarViewState.COLLAPSED

                        if (isRecreated) {
                            maxHeight = if (behaviourConfigState.isCollapsedInit) {
                                behaviourConfigState.height.dp
                            } else {
                                (getContainerHeight() * 0.95).toInt()
                            }
                        } else {
                            maxHeight = getContainerHeight()
                        }
                    }

                    is ScreenBehaviourState.EventSnippet -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        state = STATE_EXPANDED
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)
                        containerView.setBackgroundColor(requireContext().getColor(R.color.transparent))
                        isFitToContents = true
                        isDraggable = false
                        skipCollapsed = true
                        isHideable = false

                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)

                        doDelayed(SPEED_ANIMATION_CHANGING_HEIGHT_MLS * 2) {
                            if (behaviourConfigState.percentHeight == FULLSCREEN_PERCENT) {
                                maxHeight = behaviourConfigState.height.dp
                            } else {
                                maxHeight = ((getContainerHeight()) * behaviourConfigState.percentHeight).toInt()
                                containerView.setBackgroundColor(requireContext().getColor(R.color.transparent))
                            }
                        }
                    }

                    is ScreenBehaviourState.EventList -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)

                        containerView.background = ContextCompat.getDrawable(
                            requireContext(), R.drawable.bg_rectangle_rad_top_8
                        )

                        isFitToContents = true
                        isDraggable = true
                        skipCollapsed = true
                        isHideable = true

                        state = STATE_COLLAPSED
                        maxHeight = DEF_HEIGHT_EVENTLIST_CONTAINER.dp
                        peekHeight = DEF_HEIGHT_EVENTLIST_CONTAINER.dp

                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        containerView.setMargins(
                            start = DEF_HORIZONTAL_MARGIN_SNIPPET_CONTAINER.dp,
                            end = DEF_HORIZONTAL_MARGIN_SNIPPET_CONTAINER.dp
                        )
                    }

                    is ScreenBehaviourState.BottomScreens -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        state = STATE_EXPANDED

                        skipCollapsed = true

                        isHideable = true
                        isDraggable = behaviourConfigState.isDraggable

                        isFitToContents = true

                        peekHeight = DEF_HEIGHT_USERINFO_SNIPPET_CONTAINER.dp
                        maxHeight = ((getContainerHeight()) * behaviourConfigState.percentHeight).toInt()

                        if (behaviourConfigState.isFullWidth) {
                            containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)
                        }
                    }

                    is ScreenBehaviourState.BottomScreensWrapContent -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.WRAP_CONTENT)
                        state = STATE_EXPANDED

                        skipCollapsed = true

                        isHideable = true
                        isDraggable = true

                        isFitToContents = true

                        maxHeight = MAX_HEIGHT_NO_MAX_SIZE
                    }

                    is ScreenBehaviourState.BottomScreenHalfExpanded -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        val containerHeight = getContainerHeight()
                        state = STATE_COLLAPSED

                        skipCollapsed = false

                        isHideable = true
                        isDraggable = true

                        isFitToContents = true

                        halfExpandedRatio = HALF_BOTTOM_SCREEN_PERCENT
                        peekHeight = (HALF_BOTTOM_SCREEN_PERCENT * containerHeight).toInt()
                        maxHeight = (containerHeight * behaviourConfigState.percentHeight).toInt()
                    }

                    ScreenBehaviourState.ScrollableHalfProfile -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        val containerHeight = getContainerHeight()

                        state = halfProfileSavedState

                        expandedOffset = statusBarHeight
                        maxHeight = containerHeight

                        isFitToContents = false
                        isDraggable = true
                        skipCollapsed = true
                        isHideable = false

                        peekHeight = DEF_HEIGHT_USERINFO_SNIPPET_CONTAINER.dp
                        doDelayed(SPEED_ANIMATION_CHANGING_HEIGHT_MLS) {
                            val shouldWhiteStatusBar = halfProfileSavedState == STATE_EXPANDED
                            val statusBarColor = if (shouldWhiteStatusBar) {
                                R.color.uiKitColorForegroundInvers
                            } else {
                                R.color.transparent
                            }
                            (activity as? MeeraAct)?.setStatusBarColor(statusBarColor)
                            initStatusAndNavBarElementsColor(isDark = false)
                            val state = if (shouldWhiteStatusBar) {
                                UiKitToolbarViewState.COLLAPSED
                            } else {
                                UiKitToolbarViewState.EXPANDED
                            }
                            updateToolbarState(state)
                        }
                    }

                    ScreenBehaviourState.ScrollableHalfMain -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        val containerHeight = getContainerHeight()

                        state = halfMainSavedState

                        expandedOffset = statusBarHeight
                        maxHeight = containerHeight

                        isFitToContents = false
                        isDraggable = true
                        skipCollapsed = false
                        isHideable = true

                        halfExpandedRatio = HALF_CHANGE_PERCENT
                        peekHeight = (HALF_CHANGE_PERCENT * (containerHeight + statusBarHeight)).toInt()
                        val shouldWhiteStatusBar = halfMainSavedState == STATE_EXPANDED
                        val statusBarColor = if (shouldWhiteStatusBar) {
                            R.color.uiKitColorForegroundInvers
                        } else {
                            R.color.transparent
                        }
                        (activity as? MeeraAct)?.setStatusBarColor(statusBarColor)
                        initStatusAndNavBarElementsColor(isDark = false)
                        updateToolbarState(halfMainActionBarState)
                    }

                    is ScreenBehaviourState.CommunitiesTransparent -> {
                        initContainerViewHeight(containerView, ViewGroup.LayoutParams.MATCH_PARENT)
                        containerView.animateHorizontalMargins(0.dp, SPEED_ANIMATION_CHANGING_HEIGHT_MLS)

                        expandedOffset = 0
                        isDraggable = false

                        expandedOffset = 0
                        maxHeight = MAX_HEIGHT_NO_MAX_SIZE
                        notFromMap = false
                        state = STATE_EXPANDED
                        updateToolbarState(UiKitToolbarViewState.COLLAPSED)
                        (activity as? MeeraAct)?.setStatusBarColor(R.color.transparent)
                    }
                }

                behaviourCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Timber.d(
                            """MeeraBaseDialogFragment type=$behaviourConfigState.config.typeDialog
                            | state=${state.convertToState()}, newState = ${newState.convertToState()}""".trimMargin()
                        )

                        if (behaviourConfigState is ScreenBehaviourState.BottomScreens
                            || behaviourConfigState is ScreenBehaviourState.BottomScreenHalfExpanded
                        ) {
                            if (newState == STATE_HIDDEN && sheetState != newState) onHidden()
                        }

                        val isProfile =
                            NavigationManager.getManager().topNavController.currentDestination?.id == R.id.userInfoFragment

                        val isSearchNumberFragment = behaviourConfigState is ScreenBehaviourState.BottomScreensWrapContent

                        if (newState == STATE_HIDDEN && this@MeeraBaseDialogFragment !is MeeraEmptyMapFragment
                            && notFromMap && !isOpeningEvent && isProfile.not() && isSearchNumberFragment.not()) {
                            bnManager.toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                                UiKitNavigationBarViewVisibilityState.VISIBLE
                        }

                        if (behaviourConfigState is ScreenBehaviourState.Snippet){
                            when (newState) {
                                STATE_EXPANDED -> {
                                    ValueAnimator.ofInt(
                                        behaviourConfigState.height.dp,
                                        ((getContainerHeight()) * behaviourConfigState.percentHeight).toInt()
                                    ).apply {
                                        addUpdateListener {
                                            maxHeight = it.animatedValue as Int
                                        }
                                        duration = SPEED_ANIMATION_CHANGING_HEIGHT_MLS
                                        start()
                                    }
                                }
                                STATE_SETTLING -> {
                                    changeHeightToSnippet()
                                    isSettling = true
                                }
                                STATE_COLLAPSED -> {
                                    if (!isSettling) {
                                        changeHeightToSnippet()
                                    }
                                    isSettling = false
                                }
                                STATE_HALF_EXPANDED -> {
                                    state = STATE_COLLAPSED
                                }
                            }
                        }

                        Timber.d(
                            """fargment.hashCode() = ${this@MeeraBaseDialogFragment.hashCode()},
                            | newState = $newState, config = $behaviourConfigState""".trimMargin()
                        )

                        if (behaviourConfigState == ScreenBehaviourState.ScrollableHalfMain) {
                            if ((newState == STATE_EXPANDED || newState == STATE_HALF_EXPANDED) && isResumed) {
                                halfMainSavedState = newState
                                saveCurrentScrollableHalfMainToolbarState()
                            }

                            if (newState == STATE_COLLAPSED) {
                                state = STATE_HALF_EXPANDED
                            }
                        }

                        if (behaviourConfigState == ScreenBehaviourState.ScrollableHalfProfile) {
                            if ((newState == STATE_EXPANDED || newState == STATE_HALF_EXPANDED) && isResumed) {
                                halfProfileSavedState = newState
                                halfProfileActionBarState = bnManager.toolbarAndBottomInteraction.getToolbar().state
                            }
                        }

                        sheetState = newState
                        onStateChanged(newState)
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        Timber.d("""MeeraBaseDialogFragment onSlide slideOffset=$slideOffset""".trimMargin())

                        if (behaviourConfigState !is ScreenBehaviourState.BottomScreens
                            && behaviourConfigState !is ScreenBehaviourState.BottomScreenHalfExpanded
                            && behaviourConfigState !is ScreenBehaviourState.BottomScreensWrapContent
                        ) {
                            val isTransparent = if (NavigationManager.getManager().isMapMode.not() && notFromMap) {
                                (slideOffset >= SLIDE_THRESHOLD).not()
                            } else {
                                true
                            }

                            bnManager.toolbarAndBottomInteraction.getToolbar().transparentBackground = isTransparent
                            val isFullscreenMoment = behaviourConfigState is ScreenBehaviourState.FullScreenMoment
                            val isCall = behaviourConfigState is ScreenBehaviourState.Calls
                            val isMap = behaviourConfigState is ScreenBehaviourState.MapTransparent

                            val statusBarColor = if(isCall || isFullscreenMoment || isMap || isTransparent) {
                                R.color.colorTransparent
                            } else {
                                R.color.ui_white
                            }
                            (activity as? MeeraAct)?.setStatusBarColor(statusBarColor)
                            initStatusAndNavBarElementsColor(isDark = isFullscreenMoment)
                        }
                        if (behaviourConfigState is ScreenBehaviourState.ScrollableHalfProfile) {
                            val isTransparent = (slideOffset >= SLIDE_THRESHOLD).not()
                            if (ignoreSlide()) return

                            val state = if (isTransparent && !isOpeningEvent) {
                                UiKitToolbarViewState.EXPANDED
                            } else {
                                UiKitToolbarViewState.COLLAPSED
                            }
                            updateToolbarState(state)
                        }
                        if (behaviourConfigState is ScreenBehaviourState.ScrollableHalfMain) {
                            val isTransparent = (slideOffset >= SLIDE_THRESHOLD).not()
                            if (ignoreSlide()) return

                            val state = if (isTransparent) {
                                UiKitToolbarViewState.EXPANDED
                            } else {
                                UiKitToolbarViewState.COLLAPSED
                            }
                            updateToolbarState(state)
                        }

                        this@MeeraBaseDialogFragment.onSlide(slideOffset)
                    }

                    private fun changeHeightToSnippet(){
                        behaviourConfigState as ScreenBehaviourState.Snippet
                        ValueAnimator.ofInt(maxHeight, behaviourConfigState.height.dp).apply {
                            addUpdateListener {
                                maxHeight = it.animatedValue as Int
                            }
                            duration = SPEED_ANIMATION_CHANGING_HEIGHT_MLS
                            start()
                        }
                    }
                }
            }
        }
    }

    private fun saveCurrentScrollableHalfMainToolbarState() {
        if (behaviourConfigState == ScreenBehaviourState.ScrollableHalfMain) {
            halfMainActionBarState = bnManager.toolbarAndBottomInteraction.getToolbar().state
        }
    }

    private fun initContainerViewHeight(containerView: View, newHeight: Int) {
        if (containerView.layoutParams.height == newHeight) return
        containerView.layoutParams = containerView.layoutParams.apply {
            height = newHeight
        }
    }

    private fun updateToolbarState(state: UiKitToolbarViewState) {
        if (state != bnManager.toolbarAndBottomInteraction.getToolbar().state) {
            bnManager.toolbarAndBottomInteraction.getToolbar().state = state
        }
    }

    /** This code defines a private extension function named convertToState for the Int type. Its purpose is
     *  to convert an integer representing a state into a human-readable string.
    Functionality:
    Extension Function: It extends the Int class, allowing you to call this function directly on integer values as if
    it were a built-in method.
    when Expression: It uses a when expression to match the input integer (this) against predefined constants
    (presumably STATE_DRAGGING, STATE_SETTLING, etc.).
    State Mapping: Each branch of the when expression maps a specific integer value
    to its corresponding string representation.
    Default Case: The else branch handles any integer values that don't match the defined states,
    returning "STATE_UNKNOWN".
    Usage:
    This function is likely used for debugging, logging, or displaying the state of a component in
    a user-friendly format.
    For example, if you have a UI element with different states (dragging, settling, expanded, etc.),
    you can use this function to convert the integer state value into a descriptive string for display
    or logging purposes. */
    private fun Int.convertToState(): String = when (this) {
        STATE_DRAGGING -> "STATE_DRAGGING"
        STATE_SETTLING -> "STATE_SETTLING"
        STATE_EXPANDED -> "STATE_EXPANDED"
        STATE_COLLAPSED -> "STATE_COLLAPSED"
        STATE_HIDDEN -> "STATE_HIDDEN"
        else -> "STATE_UNKNOWN"
    }

    @Suppress("DEPRECATION", "UNUSED_VARIABLE")
    private fun getContainerHeight(): Int {
        val size = Point()
        val display = requireActivity().windowManager.defaultDisplay
        display.getSize(size)

        val realSize = Point()
        val realDisplay = requireActivity().windowManager.defaultDisplay
        realDisplay.getRealSize(realSize)

        var maxWidth = size.x
        var maxHeight = size.y

        if (realSize.y - context.getNavigationBarHeight() == size.y || realSize.y == size.y) {
            maxHeight = size.y - context.getStatusBarHeight()
        }
        return maxHeight
    }

    @Suppress("DEPRECATION", "UNUSED_VARIABLE")
    private fun getContainerWidth(): Int {
        val size = Point()
        val display = requireActivity().windowManager.defaultDisplay
        display.getSize(size)

        val realSize = Point()
        val realDisplay = requireActivity().windowManager.defaultDisplay
        realDisplay.getRealSize(realSize)

        var maxWidth = size.x
        var maxHeight = size.y

        if (realSize.x == size.x || realSize.x == size.x) {
            maxWidth = size.x
        }
        return maxWidth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("$TAG ON_CREATE: $childClass")
    }

    override fun onResume() {
        super.onResume()
        isOpeningEvent = false
        Timber.d("$TAG ON_RESUME: $childClass")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("$TAG ON_PAUSE: $childClass")
    }

    override fun onStop() {
        saveCurrentScrollableHalfMainToolbarState()
        super.onStop()
        Timber.d("$TAG ON_STOP: $childClass")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("$TAG ON_DESTROY_VIEW: $childClass")

        activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
            BottomSheetBehavior.from(containerView).apply {
                behaviourCallback?.let {
                    removeBottomSheetCallback(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("$TAG ON_DESTROY: $childClass")
    }

    open fun onStateChanged(newState: Int) = Unit

    open fun onSlide(offset: Float) = Unit

    open fun onHidden() {
        Timber.d("$TAG ON_HIDDEN: $childClass")
        if (
            behaviourConfigState is ScreenBehaviourState.BottomScreens ||
            behaviourConfigState is ScreenBehaviourState.BottomScreenHalfExpanded ||
            altSheetBehaviour is ScreenBehaviourState.BottomScreens
            ) {
            bnManager.clearState()
        }
    }
}

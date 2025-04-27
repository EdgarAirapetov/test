package com.numplates.nomera3.modules.maps.ui.events

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.debouncedAction1
import com.meera.core.extensions.invisible
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventConfigurationBinding
import com.numplates.nomera3.modules.maps.ui.events.adapter.EventDateAdapter
import com.numplates.nomera3.modules.maps.ui.events.adapter.EventTypeAdapter
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationMarkerState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed

class EventConfigurationWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), IOnBackPressed {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_configuration, this, false)
        .apply(::addView)
        .let(ViewEventConfigurationBinding::bind)
    private var behavior: BottomSheetBehavior<*>? = null
    private var eventTypeAdapter: EventTypeAdapter? = null
    private var eventDateAdapter: EventDateAdapter? = null

    private var onEvent: ((EventConfigurationEvent) -> Unit)? = null
    private var state: EventConfigurationState = EventConfigurationState.Closed

    private var debouncedAnimatedPeekHeightUpdate: ((Int) -> Unit)? = null

    init {
        invisible()
        binding.ivMapEventsMyLocation.setThrottledClickListener {
            binding.ivMapEventsMyLocation.clickAnimate()
            onEvent?.invoke(EventConfigurationEvent.MyLocationClicked)
        }
        binding.vMapEventsOverlay.setThrottledClickListener {
            close()
        }
        binding.layoutMapEventsOnboarding.ibMapEventsOnboardingClose.setThrottledClickListener {
            close()
        }
        binding.ibMapEventsConfigurationClose.setThrottledClickListener {
            close()
        }
        binding.layoutMapEventsOnboarding.tvMapEventsOnboardingCreate.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.CreateEventClicked)
        }
        eventTypeAdapter = EventTypeAdapter { item ->
            onEvent?.invoke(EventConfigurationEvent.EventTypeItemSelected(item))
        }
        binding.layoutMapEventsOnboarding.rvMapEventsOnboardingTypes.apply {
            adapter = eventTypeAdapter
            itemAnimator = null
        }
        binding.layoutMapEventsConfiguration.rvMapEventsConfigurationTypes.apply {
            adapter = eventTypeAdapter
            itemAnimator = null
        }
        binding.layoutMapEventsConfiguration.rvMapEventsConfigurationDates.apply {
            eventDateAdapter = EventDateAdapter { item ->
                onEvent?.invoke(EventConfigurationEvent.EventDateItemSelected(item))
            }
            adapter = eventDateAdapter
            itemAnimator = null
        }
        binding.layoutMapEventsConfiguration.tvMapEventsConfigurationTime.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.SelectTimeClicked)
        }
        binding.layoutMapEventsConfiguration.tvMapEventsConfigurationContinue.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.ConfigurationFinished)
        }
        binding.layoutMapEventsError.tvMapErrorStubAction.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.RetryClicked)
        }
        binding.ecmvMapEventsMarker.setOnAddressClickListener {
            onEvent?.invoke(EventConfigurationEvent.SearchPlaceClicked)
        }
        binding.layoutMapEventsConfiguration.tvMapEventsConfigurationAbout.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.EventsAboutClicked)
        }
        binding.layoutMapEventsOnboarding.eivEventsOnboardingAboutInfo.rulesOpenListener = {
            onEvent?.invoke(EventConfigurationEvent.RulesOpen)
        }
        onMeasured {
            behavior = createBottomSheetBehavior()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        debouncedAnimatedPeekHeightUpdate = binding.root.findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?.debouncedAction1(PEEK_HEIGHT_DEBOUNCE_DURATION_MS) { peekHeight ->
                behavior?.setPeekHeight(peekHeight, true)
            }
    }

    override fun onBackPressed(): Boolean {
        return if (state != EventConfigurationState.Closed) {
            close()
            true
        } else {
            false
        }
    }

    fun setEventListener(onEvent: (EventConfigurationEvent) -> Unit) {
        this.onEvent = onEvent
    }

    fun getState(): EventConfigurationState = state

    fun setState(newState: EventConfigurationState) {
        if (state == newState) return
        when (newState) {
            is EventConfigurationState.Onboarding -> {
                visible()
                eventTypeAdapter?.submitList(newState.eventTypeItems)
                showOnboarding()
            }

            EventConfigurationState.Closed -> {
                behavior?.state = BottomSheetBehavior.STATE_HIDDEN
                if (state !is EventConfigurationState.Onboarding) {
                    animateTopBar(false)
                    animateControlsUi(false)
                }
            }

            is EventConfigurationState.Configuration -> {
                isInvisible = newState.isHidden
                binding.layoutMapEventsConfiguration.tvMapEventsConfigurationTime.text = newState.selectedTime
                eventTypeAdapter?.submitList(newState.eventTypeItems)
                eventDateAdapter?.submitList(newState.eventDateItems)
                if (eventDateAdapter?.currentList?.firstOrNull() != newState.eventDateItems.firstOrNull()) {
                    binding.layoutMapEventsConfiguration.rvMapEventsConfigurationDates.post {
                        binding.layoutMapEventsConfiguration.rvMapEventsConfigurationDates
                            .scrollToPosition(newState.eventDateItems.indexOfFirst { it.selected })
                    }
                }
                binding.ecmvMapEventsMarker.setState(newState.markerState)
                binding.layoutMapEventsConfiguration.tvMapEventsConfigurationContinue.isEnabled =
                    newState.isContinueEnabled
                if (state !is EventConfigurationState.Configuration) {
                    animateConfigurationUiEnter(state is EventConfigurationState.Onboarding)
                }
                if (newState.markerState == EventConfigurationMarkerState.Error) {
                    showError()
                } else {
                    showConfiguration()
                }
                (state as? EventConfigurationState.Configuration)?.let { oldState ->
                    if (newState.markerState.isLevitating != oldState.markerState.isLevitating) {
                        animateTopBar(!newState.markerState.isLevitating)
                        val height = if (newState.markerState.isLevitating) {
                            0
                        } else {
                            resources.getDimensionPixelSize(R.dimen.map_events_configuration_bottomsheet_height)
                        }
                        binding.vgMapEventsConfigurationBottomsheet.post {
                            debouncedAnimatedPeekHeightUpdate?.invoke(height)
                        }
                        val enter = !newState.markerState.isLevitating
                        if ((enter || binding.ivMapEventsMyLocation.isVisible) && oldState.isMyLocationActive.not()) {
                            animateMyLocation(enter)
                        }
                    }
                }
                if (newState.markerState !is EventConfigurationMarkerState.Progress) {
                    setMyLocationActive(newState.isMyLocationActive)
                }
            }
            else -> Unit
        }
        state = newState
    }

    fun getEventMarkerPositionRelative(parentView: View): Point =
        binding.ecmvMapEventsMarker.getTipPositionRelative(parentView)

    private fun createBottomSheetBehavior(): BottomSheetBehavior<*> {
        var draggedByUser = false
        return BottomSheetBehavior.from(findViewById(R.id.vg_map_events_configuration_bottomsheet)).apply {
            isHideable = true
            peekHeight = findViewById<View>(R.id.layout_map_events_onboarding).height
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        invisible()
                        if (draggedByUser) {
                            onEvent?.invoke(EventConfigurationEvent.UiCloseInitiated)
                        }
                    }
                    if (newState != BottomSheetBehavior.STATE_SETTLING) {
                        draggedByUser = newState == BottomSheetBehavior.STATE_DRAGGING
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            })
            state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun close() {
        onEvent?.invoke(EventConfigurationEvent.UiCloseInitiated)
    }

    private fun showOnboarding() {
        behavior?.peekHeight = findViewById<View>(R.id.layout_map_events_onboarding).height
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior?.isDraggable = true
        binding.vMapEventsOverlay.visible()
        binding.vgMapEventsTopBar.invisible()
        binding.layoutMapEventsOnboarding.root.visible()
        binding.layoutMapEventsConfiguration.root.invisible()
        binding.layoutMapEventsError.root.invisible()
        binding.vgMapEventsControlsContainer.invisible()
    }

    private fun animateConfigurationUiEnter(fromOnboarding: Boolean) {
        behavior?.isDraggable = false
        binding.vMapEventsOverlay.invisible()
        animateTopBar(true)
        animateControlsUi(true)
        if (fromOnboarding) {
            animateOnboardingUiExit()
            animateConfigurationUiEnter()
            binding.vgMapEventsConfigurationBottomsheet.post {
                behavior?.setPeekHeight(
                    resources.getDimensionPixelSize(R.dimen.map_events_configuration_bottomsheet_height),
                    true
                )
                behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            val passedPosition = (state as? EventConfigurationState.Onboarding)
                ?.eventTypeItems
                ?.indexOfFirst { it.selected }
                ?: 0
            binding.layoutMapEventsConfiguration.rvMapEventsConfigurationTypes.scrollToPosition(passedPosition)
        } else {
            binding.layoutMapEventsConfiguration.rvMapEventsConfigurationTypes.scrollToPosition(0)
            binding.layoutMapEventsConfiguration.rvMapEventsConfigurationDates.scrollToPosition(0)
            showConfiguration()
            behavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.map_events_configuration_bottomsheet_height)
            behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun showConfiguration() {
        binding.layoutMapEventsOnboarding.root.invisible()
        binding.layoutMapEventsConfiguration.root.visible()
        binding.layoutMapEventsError.root.invisible()
    }

    private fun showError() {
        binding.layoutMapEventsOnboarding.root.invisible()
        binding.layoutMapEventsConfiguration.root.invisible()
        binding.layoutMapEventsError.root.visible()
    }

    private fun animateTopBar(enter: Boolean) {
        val from = if (enter) -binding.vgMapEventsTopBar.height.toFloat() else 0f
        val to = if (enter) 0f else -binding.vgMapEventsTopBar.height.toFloat()
        ObjectAnimator.ofFloat(binding.vgMapEventsTopBar, "y", from, to)
            .apply {
                interpolator = LinearInterpolator()
                duration = ANIMATION_DURATION_MS
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator) = Unit
                    override fun onAnimationCancel(p0: Animator) = Unit
                    override fun onAnimationStart(p0: Animator) {
                        binding.vgMapEventsTopBar.visible()
                    }
                })
            }
            .start()
    }

    private fun animateOnboardingUiExit() {
        animateAlpha(
            view = binding.layoutMapEventsConfiguration.root,
            enter = false
        ) {
            binding.layoutMapEventsOnboarding.root.invisible()
            binding.layoutMapEventsOnboarding.root.alpha = 1f
        }
    }

    private fun animateConfigurationUiEnter() {
        animateAlpha(
            view = binding.layoutMapEventsConfiguration.root,
            enter = true
        )
    }

    private fun animateControlsUi(enter: Boolean) {
        animateAlpha(
            view = binding.vgMapEventsControlsContainer,
            enter = enter
        )
    }

    private fun animateMyLocation(enter: Boolean) {
        animateAlpha(
            view = binding.ivMapEventsMyLocation,
            enter = enter
        )
    }

    private fun animateAlpha(view: View, enter: Boolean, onEnd: () -> Unit = {}) {
        val from = if (enter) 0f else 1f
        val to = if (enter) 1f else 0f
        ObjectAnimator.ofFloat(view, "alpha", from, to)
            .apply {
                interpolator = LinearInterpolator()
                duration = ANIMATION_DURATION_MS
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator) {
                        onEnd()
                    }

                    override fun onAnimationCancel(p0: Animator) = Unit
                    override fun onAnimationStart(p0: Animator) {
                        view.visible()
                    }
                })
            }
            .start()
    }

    private fun setMyLocationActive(active: Boolean) {
        if (active) {
            binding.ivMapEventsMyLocation.clearAnimation()
        }
        binding.ivMapEventsMyLocation.isInvisible = active
    }

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
        private const val PEEK_HEIGHT_DEBOUNCE_DURATION_MS = 100L
    }
}

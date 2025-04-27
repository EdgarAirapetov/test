package com.numplates.nomera3.modules.redesign.fragments.main.map.layers

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentMapLayersBinding
import com.numplates.nomera3.databinding.TooltipLayersBinding
import com.numplates.nomera3.modules.maps.ui.layers.MapLayersDialogViewModel
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersTooltip
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersUiEffect
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersUiModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraBaseBottomSheetDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraLocationDelegate
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showAboveViewAtCenter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


/**
 * We remove and restore window animations to prevent slide animation
 * from happening when returning to screen from another activity
 */
class MeeraMapLayersDialogFragment : MeeraBaseBottomSheetDialogFragment<MeeraFragmentMapLayersBinding>() {

    private val viewModel: MapLayersDialogViewModel by viewModels { App.component.getViewModelFactory() }
    private lateinit var locationContract: MeeraLocationDelegate
    private var windowAnimations: Int = NO_WINDOW_ANIMATIONS
    private var restoreWindowAnimationsJob: Job? = null
    private var deniedAndNoRationaleNeededBeforeRequest = false

    private val tooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.meera_tooltip_layers)
    }
    private var tooltipDismissJob: Job? = null

    override fun getTheme(): Int = R.style.LayersBottomSheetDialogTheme

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentMapLayersBinding
        get() = MeeraFragmentMapLayersBinding::inflate

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as BottomSheetDialog).behavior.apply {
                isHideable = true
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setDimAmount(0f)
        saveWindowAnimations(savedInstanceState)
        setupLocationContract()
        updateLocationAvailability()
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        updateLocationAvailability()
        restoreWindowAnimations()
    }

    override fun onPause() {
        super.onPause()
        cancelWindowAnimations()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.applyUserVisibilityOnMapSetting()
        viewModel.logFilterApply()
        viewModel.logFiltersClosed()
        (parentFragment as? MapFragment)?.applyFilters()
        (parentFragment as? MainMapFragment)?.applyFilters()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_WINDOW_ANIMATIONS, windowAnimations)
        super.onSaveInstanceState(outState)
    }

    private fun setupLocationContract() {
        val permissionDelegate = PermissionDelegate(act, viewLifecycleOwner)
        val permissionListener = object : PermissionDelegate.Listener {
            override fun onGranted() = Timber.d("Location permission granted")
            override fun onDenied() {
                Timber.d("Location permission denied")
                val deniedAndNoRationaleNeededAfterRequest =
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                if (deniedAndNoRationaleNeededBeforeRequest && deniedAndNoRationaleNeededAfterRequest) {
                    sendUserToAppSettings()
                }
            }

            override fun onError(error: Throwable?) = Timber.e(error)
        }
        locationContract = MeeraLocationDelegate(act, permissionDelegate, permissionListener)
    }

    private fun setupUi() {
        val binding = binding ?: return
        binding.layoutMapLayersMain.ukmltMapLayersEvents.onSettingsClicked = {
            binding.layoutMapLayersMain.root.gone()
            binding.layoutMapLayersEvents.root.visible()
        }
        binding.layoutMapLayersMain.ukmltMapLayersEvents.onDisabledClicked = {
            showTooltip(
                view = binding.layoutMapLayersMain.ukmltMapLayersEvents,
                messageStringResId = R.string.map_layers_tooltip_map_events_feature_disabled,
                pointerLocation = TooltipPointerLocation.LEFT,
            )
        }
        binding.layoutMapLayersMain.ivMapLayersClose.setThrottledClickListener {
            dismiss()
        }
        binding.layoutMapLayersMain.ukscMapLayersUserVisibility.onCheckedIndexChangeListener = { index ->
            viewModel.setUserVisibilityOnMap(index)
        }
        binding.layoutMapLayersMain.ukmltMapLayersPeople.onCheckedChange = { checked, changedByUser ->
            if (changedByUser) viewModel.setShowPeopleEnabled(checked)
        }
        binding.layoutMapLayersMain.ukmltMapLayersEvents.onCheckedChange = { checked, changedByUser ->
            if (changedByUser) viewModel.setShowEventsEnabled(checked)
        }
        binding.layoutMapLayersMain.ukmltMapLayersFriends.onCheckedChange = { checked, changedByUser ->
            if (changedByUser) viewModel.setShowFriendsEnabled(checked)
        }
        binding.layoutMapLayersMain.ukivMapLayersLocationDisabled.actionClickListener = {
            when {
                !locationContract.isPermissionGranted() -> {
                    deniedAndNoRationaleNeededBeforeRequest =
                        !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    locationContract.requestLocationPermissions()
                }

                !locationContract.isLocationEnabled() -> {
                    locationContract.requestEnableLocation()
                }
                else -> {
                    updateLocationAvailability()
                }
            }
        }
        binding.layoutMapLayersEvents. ivEventFiltersBack.setThrottledClickListener {
            binding.layoutMapLayersMain.root.visible()
            binding.layoutMapLayersEvents.root.gone()
        }
        binding.layoutMapLayersEvents.efwEventFilterType.filterChangeListener = { eventFilter ->
            viewModel.setEventFilterType(eventFilter)
        }
        binding.layoutMapLayersEvents.efwEventFilterDate.filterChangeListener = { eventFilter ->
            viewModel.setEventFilterDate(eventFilter)
        }
        binding.layoutMapLayersEvents.tvMapFiltersReset.setThrottledClickListener {
            viewModel.resetEventSettingsToDefaults()
        }
        viewModel.liveUiModel.observe(viewLifecycleOwner, ::handleUiModel)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiEffectsFlow.collect(::handleUiEffect)
        }
    }

    private fun handleUiModel(uiModel: MapLayersUiModel) {
        val binding = binding ?: return
        binding.layoutMapLayersMain.ukscMapLayersUserVisibility.isInvisible = uiModel.showEnableLocationStub
        binding.layoutMapLayersMain.ukscMapLayersUserVisibility
            .setCheckedSegmentByIndex(uiModel.selectedUserVisibilityOnMapTypeIndex)
        binding.layoutMapLayersMain.ukivMapLayersLocationDisabled.isVisible = uiModel.showEnableLocationStub
        binding.layoutMapLayersMain.ukuiMapLayersAvatar.setConfig(uiModel.userpicConfig)
        binding.layoutMapLayersMain.ukmltMapLayersPeople.checked = uiModel.showPeople
        binding.layoutMapLayersMain.ukmltMapLayersEvents.checked = uiModel.showEvents
        binding.layoutMapLayersMain.ukmltMapLayersEvents
            .setNonDefaultSettingsIndicatorVisibility(uiModel.showNonDefaultEventSettings)
        binding.layoutMapLayersMain.ukmltMapLayersEvents.setDisabled(uiModel.isEventsEnabled.not())
        binding.layoutMapLayersMain.ukmltMapLayersFriends.checked = uiModel.showFriends
        binding.layoutMapLayersEvents.efwEventFilterType.setUiModel(uiModel.eventFilterType)
        binding.layoutMapLayersEvents.efwEventFilterDate.setUiModel(uiModel.eventFilterDate)
        binding.layoutMapLayersEvents.tvMapFiltersReset.isEnabled = uiModel.showNonDefaultEventSettings
    }

    private fun handleUiEffect(uiEffect: MapLayersUiEffect) {
        when (uiEffect) {
            is MapLayersUiEffect.ShowLayersTooltip -> handleTooltip(uiEffect.tooltip)
        }
    }

    private fun handleTooltip(tooltip: MapLayersTooltip) {
        val binding = binding ?: return
        when (tooltip) {
            MapLayersTooltip.PEOPLE_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.ukmltMapLayersPeople,
                messageStringResId = R.string.map_layers_tooltip_people_disabled,
                pointerLocation = TooltipPointerLocation.CENTER
            )
            MapLayersTooltip.EVENTS_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.ukmltMapLayersEvents,
                messageStringResId = R.string.map_layers_tooltip_events_disabled,
                pointerLocation = TooltipPointerLocation.LEFT,
            )
            MapLayersTooltip.FRIENDS_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.ukmltMapLayersFriends,
                messageStringResId = R.string.map_layers_tooltip_friends_disabled,
                pointerLocation = TooltipPointerLocation.RIGHT
            )
            MapLayersTooltip.PEOPLE_EVENTS_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.vMapLayersTooltipAnchor,
                messageStringResId = R.string.map_layers_tooltip_people_events_disabled,
                pointerLocation = TooltipPointerLocation.CENTER
            )
            MapLayersTooltip.PEOPLE_FRIENDS_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.vMapLayersTooltipAnchor,
                messageStringResId = R.string.map_layers_tooltip_people_friends_disabled,
                pointerLocation = TooltipPointerLocation.CENTER
            )
            MapLayersTooltip.EVENTS_FRIENDS_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.vMapLayersTooltipAnchor,
                messageStringResId = R.string.map_layers_tooltip_events_friends_disabled,
                pointerLocation = TooltipPointerLocation.CENTER
            )
            MapLayersTooltip.ALL_DISABLED -> showTooltip(
                view = binding.layoutMapLayersMain.vMapLayersTooltipAnchor,
                messageStringResId = R.string.map_layers_tooltip_all_disabled,
                pointerLocation = TooltipPointerLocation.CENTER
            )
            MapLayersTooltip.ONBOARDING -> showTooltip(
                view = binding.layoutMapLayersMain.vMapLayersTooltipAnchor,
                messageStringResId = R.string.map_layers_tooltip_onboarding,
                pointerLocation = TooltipPointerLocation.CENTER
            )
        }
    }

    private fun showTooltip(
        view: View,
        @StringRes messageStringResId: Int,
        pointerLocation: TooltipPointerLocation
    ) {
        val binding = tooltip?.contentView
            ?.let(TooltipLayersBinding::bind)
            ?: return
        binding.tvTooltipLayersMessage.setText(messageStringResId)
        binding.root.gravity = when (pointerLocation) {
            TooltipPointerLocation.LEFT -> Gravity.START
            TooltipPointerLocation.CENTER -> Gravity.CENTER_HORIZONTAL
            TooltipPointerLocation.RIGHT -> Gravity.END
        }
        val xOffset = when (pointerLocation) {
            TooltipPointerLocation.LEFT -> getOffset(binding.root)
            TooltipPointerLocation.CENTER -> 0
            TooltipPointerLocation.RIGHT -> - getOffset(binding.root)
        }
        tooltipDismissJob?.cancel()
        tooltip?.showAboveViewAtCenter(
            fragment = this,
            view = view,
            xOffset = xOffset
        )
        tooltipDismissJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(TOOLTIP_DISMISS_DELAY_MS)
            tooltip?.dismiss()
        }
    }

    private fun getOffset(tooltipView: View): Int {
        tooltipView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val pointerView = tooltipView.findViewById<View>(R.id.v_tooltip_layers_pointer)
        return tooltipView.measuredWidth / 2 - pointerView.measuredWidth / 2 - TOOLTIP_POINTER_OFFSET_X_DP.dp
    }

    private fun updateLocationAvailability() {
        viewModel.setLocationAvailable(locationContract.isLocationEnabled() && locationContract.isPermissionGranted())
    }

    private fun saveWindowAnimations(savedInstanceState: Bundle?) {
        windowAnimations = savedInstanceState
            ?.getInt(KEY_WINDOW_ANIMATIONS, NO_WINDOW_ANIMATIONS)
            ?: dialog?.window?.attributes?.windowAnimations ?: NO_WINDOW_ANIMATIONS
    }

    private fun restoreWindowAnimations() {
        restoreWindowAnimationsJob = doDelayed(100) {
            dialog?.window?.setWindowAnimations(windowAnimations)
        }
    }

    private fun cancelWindowAnimations() {
        restoreWindowAnimationsJob?.cancel()
        dialog?.window?.setWindowAnimations(NO_WINDOW_ANIMATIONS)
    }

    private enum class TooltipPointerLocation {
        LEFT, CENTER, RIGHT
    }

    companion object {
        private const val TOOLTIP_DISMISS_DELAY_MS = 4000L
        private const val TOOLTIP_POINTER_OFFSET_X_DP = 14

        private const val KEY_WINDOW_ANIMATIONS = "KEY_WINDOW_ANIMATIONS"
        private const val NO_WINDOW_ANIMATIONS = -1
    }
}

package com.numplates.nomera3.presentation.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.debouncedAction
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isNotFalse
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.needToUpdateStr
import com.meera.core.extensions.register
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.toJson
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.FragmentMapBinding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsCreateTapWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.events.EventConstants
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.model.toUIMapStyleEntity
import com.numplates.nomera3.modules.maps.ui.GeoAccessDelegate
import com.numplates.nomera3.modules.maps.ui.MapDialogFragment
import com.numplates.nomera3.modules.maps.ui.MapUiActionHandler
import com.numplates.nomera3.modules.maps.ui.equalWithTolerance
import com.numplates.nomera3.modules.maps.ui.events.CreateEventStubDialogFragment
import com.numplates.nomera3.modules.maps.ui.events.EventsAboutPopupDialog
import com.numplates.nomera3.modules.maps.ui.events.EventsStubDialogFragment
import com.numplates.nomera3.modules.maps.ui.events.TimePickerPopupDialog
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListsUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.AddEventButtonState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationUiMode
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurator
import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventsInfoUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventNavigation
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventParticipantsList
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetViewController
import com.numplates.nomera3.modules.maps.ui.friends.FriendStubDialogFragment
import com.numplates.nomera3.modules.maps.ui.friends.FriendStubLowVersionDialogFragment
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiEffect
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiModel
import com.numplates.nomera3.modules.maps.ui.geo_popup.GeoPopupDialog
import com.numplates.nomera3.modules.maps.ui.geo_popup.MeeraGeoPopupDialog
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.GeoPopupOrigin
import com.numplates.nomera3.modules.maps.ui.layers.EnableEventsLayerDialogFragment
import com.numplates.nomera3.modules.maps.ui.layers.MapLayersDialogFragment
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MainMapOpenPayload
import com.numplates.nomera3.modules.maps.ui.model.MapCameraState
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.model.MapObjectsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.maps.ui.model.MapUiState
import com.numplates.nomera3.modules.maps.ui.model.MapUiValuesUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.pin.MapObjectsDelegate
import com.numplates.nomera3.modules.maps.ui.pin.MapObjectsDelegate.Companion.MAP_CLUSTERS_ZINDEX
import com.numplates.nomera3.modules.maps.ui.pin.MapObjectsDelegate.Companion.MAP_EVENTS_ZINDEX
import com.numplates.nomera3.modules.maps.ui.pin.MapObjectsDelegate.Companion.MAP_FRIENDS_ZINDEX
import com.numplates.nomera3.modules.maps.ui.pin.MapObjectsDelegate.Companion.MAP_USERS_ZINDEX
import com.numplates.nomera3.modules.maps.ui.pin.UserPinView
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.UserSnippetBottomSheetWidget
import com.numplates.nomera3.modules.maps.ui.snippet.UserSnippetViewModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetHost
import com.numplates.nomera3.modules.maps.ui.view.MapUiController
import com.numplates.nomera3.modules.maps.ui.widget.model.AllowedPointInfoWidgetVisibility
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapTargetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapUiFactor
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoWidgetAllowedVisibilityChange
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchEvent
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchUiState
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.upload.util.getUploadBundle
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.userprofile.ui.ProfileUiUtils
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.MapViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class MapFragment : BaseFragmentNew<FragmentMapBinding>(),
    OnMapReadyCallback,
    INetworkValues,
    IArgContainer,
    IOnBackPressed,
    IAuthStateObserver,
    BasePermission by BasePermissionDelegate(),
    UserSnippetBottomSheetWidget.Listener,
    EventConfigurator,
    MapSnippetHost,
    MapUiController,
    MapUiActionHandler,
    ScreenshotTakenListener {

    private var currentState: EventConfigurationState? = null
    override var isMapOpenInTab: Boolean = false

    private val mapViewModel: MapViewModel by viewModels { App.component.getViewModelFactory() }

    /** TODO https://nomera.atlassian.net/browse/BR-18804
     * Убрать дополнительную ViewModel, перенести логику во ViewModel MapFragment
     */
    private val userSnippetViewModel: UserSnippetViewModel by viewModels { App.component.getViewModelFactory() }

    private var needToShowNearestFriend = false

    private var mapObjectsDelegate: MapObjectsDelegate? = null

    private var map: GoogleMap? = null
    private var mapViewBundle: Bundle? = null
    private var myMarker: Marker? = null
    private var rxPermissions: RxPermissions? = null
    private val disposables = CompositeDisposable()

    private var isCameraMovingToCurrentLocation = false
    private var isCameraMovingToEventConfigurationMyLocation = false
    private var isCurrentLocationActive = false

    private var requestLocationPermission: Disposable? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMapBinding
        get() = FragmentMapBinding::inflate

    private var uiState: MapUiState? = null

    private var userSnippet: UserSnippetBottomSheetWidget? = null
    private var mapControls = emptyList<View>()

    private var locationServicesBroadcastReceiver: BroadcastReceiver? = null
    private var geoPopupDialog: GeoPopupDialog? = null
    private var meeraGeoPopupDialog: MeeraGeoPopupDialog? = null
    private var needToShowGeoPopupDialogWhenCallIsFinished = false
    private var currentDialog: Dialog? = null
    private var isEventCreationFinished = false

    private var geoAccessDelegate: GeoAccessDelegate? = null
    private var eventSnippetViewController: EventSnippetViewController? = null

    private var lastThrottledActionTime = 0L

    private var cameraMoveReason: Int? = null

    private var mapTargetOverride: MapTargetUiModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
    }

    override fun onScreenshotTaken() {
        binding?.elwMapEventsList?.onScreenshotTaken()
        childFragmentManager.fragments
            .filterIsInstance<ScreenshotTakenListener>()
            .firstOrNull { it is Fragment && isFragmentVisibleForUser(it) }
            ?.onScreenshotTaken()
    }

    private fun isFragmentVisibleForUser(fragment: Fragment): Boolean {
        val fragmentView = fragment.view
        val location = IntArray(2)
        fragmentView?.getLocationInWindow(location)
        if (location[0] == 0) return true
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return
        mapControls = listOf(
            binding.vgMapLayers,
            binding.ukbMapEventsList,
            binding.ukbMapFriendsList,
            binding.ivCurrentLocation,
            binding.vgMapAddEvent
        )
        binding.mvMap.onCreate(mapViewBundle)
        binding.mvMap.getMapAsync(this)
        initStateObservers()
        view.post {
            mapViewModel.handleUiAction(MapUiAction.MapViewCreated(arguments))
        }
    }

    override fun onStart() {
        super.onStart()
        binding?.mvMap?.onStart()
        binding?.nbBar?.selectMap(true)
    }

    override fun onStartFragment() {
        if (isAdded.not()) return
        super.onStartFragment()
        binding?.usbsWidget?.onStartFragment()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted.not() }
            .forEach { it.onStartFragment() }
    }

    override fun onResume() {
        super.onResume()
        binding?.mvMap?.onResume()
        if (geoAccessDelegate?.isGeoAccessProvided().isTrue()) {
            mapViewModel.resetGeoPopupShownCount()
            mapViewModel.handleUiAction(MapUiAction.CreateMyMarkerRequested())
        } else {
            myMarker?.remove()
            myMarker = null
        }
        mapViewModel.handleUiAction(MapUiAction.OnResumeCalled(isMapOpenInTab))
    }

    override fun onPause() {
        super.onPause()
        binding?.mvMap?.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding?.mvMap?.onStop()
        binding?.nbBar?.selectMap(false)
        disposables.dispose()
    }

    override fun onStopFragment() {
        if (isAdded.not()) return
        super.onStopFragment()
        binding?.usbsWidget?.onStopFragment()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted }
            .forEach { it.onStopFragment() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        binding?.mvMap?.onSaveInstanceState(mapViewBundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.mvMap?.onDestroy()
        binding?.mvMap?.removeAllViews()
        locationServicesBroadcastReceiver?.let {
            context?.unregisterReceiver(it)
        }
        map = null
        mapObjectsDelegate = null
        userSnippet?.removeListener(this)
        userSnippet = null
    }

    override fun onDestroy() {
        rxPermissions = null
        map?.clear()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.mvMap?.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (map != null) return
        map = googleMap
        mapViewModel.handleUiAction(MapUiAction.GoogleMapInitialized)
    }

    override fun onBackPressed(): Boolean {
        return if (getMapMode() is MapMode.EventEditing) {
            if (isEventCreationFinished) {
                false
            } else {
                handleEventConfigurationBackPress()
            }
        } else if (getMapMode() is MapMode.UserView || getMapMode() is MapMode.EventView) {
            act.returnToTargetFragment(act.getFragmentsCount() - 2, true)
            true
        } else {
            userSnippet?.onBackPressed().isTrue()
                || eventSnippetViewController?.onBackPressed().isTrue()
                || handleEventConfigurationBackPress()
                || binding?.elwMapEventsList?.onBackPressed().isTrue()
                || binding?.elwMapFriendsList?.onBackPressed().isTrue()
        }
    }

    override fun initAuthObserver(): AuthStatusObserver {
        return object : AuthStatusObserver(act, this) {
            override fun onAuthState() {
                setupUserUi(false)
                mapObjectsDelegate?.removeOwnMarkerFromList()
            }

            override fun onNotAuthState() {
                setupUserUi(true)
            }
        }
    }

    override fun onHideHints() {
        super.onHideHints()
        act.hideHints()
    }

    override fun updateScreenOnTapNavBar() {
        super.updateScreenOnTapNavBar()
        updateMyMarker()
    }

    override fun onEditEvent() {
        (childFragmentManager.findFragmentById(R.id.fl_map_container) as? AddMultipleMediaPostFragment)?.let { postFragment ->
            mapViewModel.eventsOnMap.savedUploadPostBundle = postFragment.createUploadPostBundle()
        }
        mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.OPEN)
        removeCurrentChildFragment()
    }

    override fun onEventPostPublished() {
        if (getMapMode() is MapMode.EventEditing) {
            isEventCreationFinished = true
            act.onBackPressed()
        } else {
            mapViewModel.eventsOnMap.onEventPublished()
            removeCurrentChildFragment()
        }
    }

    override fun getTargetSnippetZoom(): Float = map?.let { map ->
        val currentZoom = map.cameraPosition.zoom
        when {
            currentZoom > MAP_USER_CARD_ZOOM_MAX -> MAP_USER_CARD_ZOOM_MAX
            currentZoom < MAP_USER_CARD_ZOOM_MIN -> MAP_USER_CARD_ZOOM_MIN
            else -> currentZoom
        }
    } ?: MAP_USER_CARD_ZOOM_MAX

    override fun showMapControls() {
        binding?.mpiwMapWidget?.isVisible = true
        if (!isMapOpenInTab && getMapMode() is MapMode.Main) return
        updateMapControls()
        val mapControlsEnterAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.map_controls_show)
        mapControlsEnterAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                updateMapControls()
                mapControls.forEach {
                    it.visible()
                }
            }

            override fun onAnimationEnd(animation: Animation) = Unit
            override fun onAnimationRepeat(animation: Animation) = Unit
        })
        mapControls.forEach {
            it.clearAnimation()
            it.startAnimation(mapControlsEnterAnimation)
        }
    }

    override fun hideMapControls(showUserLocationView: Boolean, hideToolbar: Boolean) {
        updateMapControls()
        binding?.mpiwMapWidget?.isVisible = false
        mapControls.forEach {
            it.clearAnimation()
            it.invisible()
        }
    }

    override fun updateEventsData() {
        if (
            mapObjectsDelegate?.objectsDisabled.isNotFalse()
            || mapViewModel.isEventsOnMapEnabled.not()
            || getMapMode() !is MapMode.Main
        ) {
            return
        }
        val map = this.map ?: return
        if (mapObjectsDelegate?.isEventsVisible().isNotTrue()) {
            mapObjectsDelegate?.clearEventMarkers()
            return
        }
        mapViewModel.eventsOnMap.getEvents(map.projection.visibleRegion.latLngBounds)
    }

    override fun getMapMode(): MapMode? = uiState?.mapMode

    override fun focusMapItem(mapItem: FocusedMapItem?) {
        mapObjectsDelegate?.focusMapItem(mapItem)
    }

    override fun updateEventMapItem(eventObject: EventObjectUiModel) {
        mapObjectsDelegate?.updateEventObject(eventObject)
        when (getMapMode()) {
            is MapMode.Aux -> mapViewModel.handleUiAction(MapUiAction.AuxMapEventUpdated(eventObject))
            MapMode.Main -> mapViewModel.handleUiAction(MapUiAction.MainMapEventUpdated(eventObject))
            null -> Unit
        }
    }

    /**
     * Hack to move camera with predefined Y offset
     * Also try-catch due to [BR-17233](https://nomera.atlassian.net/browse/BR-17233)
     */
    override fun updateCameraLocation(
        location: LatLng,
        zoom: Float?,
        yOffset: Int,
        animate: Boolean,
        cancelCallback: (() -> Unit)?,
        callback: (() -> Unit)?
    ) {
        try {

            val map = map ?: return
            val currentZoom = map.cameraPosition.zoom
            val targetZoom = zoom ?: currentZoom
            val isTargetZoomDifferent = targetZoom != currentZoom
            val needToPrecalculateOffset = yOffset != 0
            val needToMoveCameraForPrecalculation = isTargetZoomDifferent && needToPrecalculateOffset
            if (needToMoveCameraForPrecalculation) {
                map.moveCamera(CameraUpdateFactory.zoomTo(targetZoom))
            }
            val targetLocation = if (needToPrecalculateOffset) {
                val point = map.projection.toScreenLocation(location)
                point.set(point.x, point.y + yOffset)
                try {
                    map.projection.fromScreenLocation(point)
                } catch (t: Throwable) {
                    Timber.e(t)
                    return
                }
            } else {
                location
            }
            if (needToMoveCameraForPrecalculation) {
                map.moveCamera(CameraUpdateFactory.zoomTo(currentZoom))
            }
            val cameraUpdate = if (isTargetZoomDifferent) {
                CameraUpdateFactory.newLatLngZoom(targetLocation, targetZoom)
            } else {
                CameraUpdateFactory.newLatLng(targetLocation)
            }
            if (animate) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(CAMERA_ANIMATION_DELAY_MS)
                    map.animateCamera(
                        cameraUpdate,
                        CAMERA_ANIMATION_DURATION_MS,
                        object : GoogleMap.CancelableCallback {
                            override fun onFinish() {
                                if (yOffset != 0) {
                                    mapTargetOverride = MapTargetUiModel(latLng = location, zoom = targetZoom)
                                }
                                callback?.invoke()
                            }

                            override fun onCancel() {
                                cancelCallback?.invoke()
                            }
                        })
                }
            } else {
                map.moveCamera(cameraUpdate)
                callback?.invoke()
                if (yOffset != 0) {
                    mapTargetOverride = MapTargetUiModel(latLng = location, zoom = targetZoom)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getMapUiValues(): MapUiValuesUiModel = uiState?.mapUiValues ?: MapUiValuesUiModel()

    override fun onUserSelected(userSnippetModel: UserSnippetModel) {
        val user = MapUserUiModel(
            id = userSnippetModel.uid,
            name = userSnippetModel.name,
            uniqueName = userSnippetModel.uniqueName,
            accountType = userSnippetModel.accountType,
            avatar = userSnippetModel.avatar,
            hatLink = userSnippetModel.hatLink,
            gender = userSnippetModel.gender,
            accountColor = userSnippetModel.accountColor,
            latLng = LatLng(
                userSnippetModel.coordinates.lat,
                userSnippetModel.coordinates.lon,
            ),
            isFriend = userSnippetModel.friendStatus == FRIEND_STATUS_CONFIRMED,
            blacklistedMe = userSnippetModel.blacklistedMe,
            blacklistedByMe = userSnippetModel.blacklistedByMe,
            hasMoments = userSnippetModel.moments?.hasMoments.isTrue(),
            hasNewMoments = userSnippetModel.moments?.hasNewMoments.isTrue(),
            moments = userSnippetModel.moments
        )
        focusMapItem(FocusedMapItem.User(user.id))

        updateCameraLocation(
            location = user.latLng,
            yOffset = getMapUiValues().userSnippetYOffset
        )
        mapObjectsDelegate?.addUserMapObject(user)
    }

    override fun onUserSnippetSlide(offset: Float) = Unit

    override fun onUserSnippetStateChanged(state: SnippetState) {
        val mapMode = getMapMode()
        if (mapMode is MapMode.UserView) {
            when (state) {
                SnippetState.Preview -> {
                    focusMapItem(FocusedMapItem.User(mapMode.user.id))
                }

                SnippetState.Closed -> {
                    focusMapItem(null)
                }

                else -> Unit
            }
        } else {
            val mainPostRoadsParent = (parentFragment as? MainPostRoadsFragment) ?: return
            if (mainPostRoadsParent.getMode() != MainRoadMode.MAP) return
            mapViewModel.setUserSnippetState(state)
            when (state) {
                SnippetState.Preview -> {
//                    hideMapControls()
                }

                SnippetState.Closed -> {
                    if (binding?.elwMapFriendsList?.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                        focusMapItem(null)
                        showMapControls()
                    } else {
                        mapViewModel.handleUiAction(MapFriendsListUiAction.UpdateSelectedUser)
                        return
                    }
                }

                else -> Unit
            }
        }
        val stableState = state as? SnippetState.StableSnippetState ?: return
        val visibility = when (stableState) {
            SnippetState.Closed -> AllowedPointInfoWidgetVisibility.EXTENDED
            SnippetState.Expanded -> AllowedPointInfoWidgetVisibility.NONE
            SnippetState.Preview, SnippetState.HalfCollapsedPreview -> AllowedPointInfoWidgetVisibility.COLLAPSED
        }
        val change = PointInfoWidgetAllowedVisibilityChange(
            factor = MapUiFactor.USER_SNIPPET,
            allowedPointInfoWidgetVisibility = visibility
        )
        val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
        mapViewModel.handleUiAction(mapUiAction)
    }

    override fun getEventSnippetViewController(): EventSnippetViewController? = eventSnippetViewController

    override fun handleOuterMapUiAction(uiAction: MapUiAction) = mapViewModel.handleUiAction(uiAction)

    fun setOpenInTab(isOpen: Boolean) {
        setOpenInTab(isOpen = isOpen, mapOpenPayload = null)
    }

    fun setOpenInTab(isOpen: Boolean, mapOpenPayload: MainMapOpenPayload?) {
        if (isOpen != isMapOpenInTab) {
            isMapOpenInTab = isOpen
            mapViewModel.eventsOnMap.setMapOpenInTab(isOpen)
            if (isOpen) {
                onOpenInTab(mapOpenPayload)
            } else {
                onCloseInTab()
            }
            val visibility = if (isOpen) {
                AllowedPointInfoWidgetVisibility.EXTENDED
            } else {
                AllowedPointInfoWidgetVisibility.NONE
            }
            val change = PointInfoWidgetAllowedVisibilityChange(
                factor = MapUiFactor.MAP_TAB,
                allowedPointInfoWidgetVisibility = visibility
            )
            val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
            mapViewModel.handleUiAction(mapUiAction)
        }
    }

    fun applyFilters() {
        if (mapViewModel.mapSettings.showFriendsOnly) {
            needToShowNearestFriend = true
        }
        resetGlobalMap()
        showMapControls()
        mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(false))
    }

    fun resetGlobalMap() {
        mapObjectsDelegate?.clearMarkers()
        updateMapData()
    }

    private fun onMapUiReady(uiState: MapUiState) {
        context ?: return
        val googleMap = map ?: return
        initMapObjectsDelegate(googleMap)
        initDelegates(uiState.mapMode)
        initClickListeners()
        initUiObservers()
        initMap(googleMap = googleMap, uiState = uiState)
        initAuthObserver()
        setMapMode(uiState)
    }

    private fun initMapObjectsDelegate(googleMap: GoogleMap) {
        mapObjectsDelegate = MapObjectsDelegate(
            fragment = this,
            map = googleMap,
            mapViewModel = mapViewModel
        )
    }

    private fun initDelegates(mapMode: MapMode) {
        rxPermissions = RxPermissions(act)
        userSnippet = binding?.usbsWidget
            ?.apply {
                userSnippetViewModel.initialize(isAuxSnippet = mapMode is MapMode.UserView)
                binding?.usbsWidget?.initialize(
                    hostFragment = this@MapFragment,
                    viewModel = userSnippetViewModel
                )
                addListener(this@MapFragment)
            }
        locationServicesBroadcastReceiver = object : BroadcastReceiver() {
            private val resetAction = viewLifecycleOwner.lifecycleScope
                .debouncedAction(LOCATION_SERVICES_CHANGED_DEBOUNCE_DELAY) {
                    if (geoAccessDelegate?.isGeoAccessProvided().isTrue()) {
                        mapViewModel.resetGeoPopupShownCount()
                    }
                }

            override fun onReceive(context: Context, intent: Intent) = resetAction.invoke()
        }.also {
            it.register(
                context = requireContext(),
                filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )
        }
        binding?.ecwMapEventsConfiguration?.setEventListener(this::handleEventConfigurationEvent)
        binding?.psvMapSearchPlaces?.setEventListener(this::handlePlacesSearchEvent)
        geoAccessDelegate = GeoAccessDelegate(
            act = act,
            permissionDelegate = PermissionDelegate(
                activity = activity,
                viewLifecycleOwner = viewLifecycleOwner
            ),
            lifecycle = viewLifecycleOwner.lifecycle
        )
        binding?.msvpEvents?.let { viewPager ->
            eventSnippetViewController = EventSnippetViewController(
                mapParametersCache = mapViewModel.getMapParametersCache(),
                fragment = this,
                viewPager = viewPager,
                eventsOnMap = mapViewModel.eventsOnMap,
                mapUiController = this,
                mapMode = mapMode
            )
        }
        binding?.elwMapEventsList?.uiActionListener = mapViewModel::handleUiAction
        binding?.elwMapFriendsList?.uiActionListener = mapViewModel::handleUiAction
    }

    private fun initMap(googleMap: GoogleMap, uiState: MapUiState) {
        googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            isTrafficEnabled = false
            isIndoorEnabled = true
            isBuildingsEnabled = true
            uiSettings.isRotateGesturesEnabled = false
            uiSettings.isMapToolbarEnabled = false

        }
        googleMap.setPadding(0, 0, 0, uiState.mapUiValues.mapBottomPadding)
        setMapStyle(googleMap)
        googleMap.setOnMarkerClickListener { marker ->
            throttleMapAction { onMarkerClick(marker) }
            true
        }
        googleMap.setOnCameraMoveStartedListener { reason ->
            cameraMoveReason = reason
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                mapViewModel.setMapSnippetCloseMethod(MapSnippetCloseMethod.TAP)
                hideUserSnippet()
                eventSnippetViewController?.closeSnippet()
                binding?.elwMapEventsList?.close()
                binding?.elwMapFriendsList?.close()
                mapTargetOverride = null
            }
            mapViewModel.eventsOnMap.setMapCameraState(
                MapCameraState.Moving(
                    reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
                )
            )
            if (!isCameraMovingToCurrentLocation) setCurrentLocationActive(false)
            if (!isCameraMovingToEventConfigurationMyLocation) mapViewModel.eventsOnMap.setMyLocationActive(false)
        }
        googleMap.setOnMapClickListener {
            mapViewModel.setMapSnippetCloseMethod(MapSnippetCloseMethod.TAP)
            hideUserSnippet()
            eventSnippetViewController?.closeSnippet()
            binding?.elwMapEventsList?.close()
            binding?.elwMapFriendsList?.close()
        }
    }

    private fun setMapMode(uiState: MapUiState) {
        val mapMode = uiState.mapMode
        when (mapMode) {
            is MapMode.Main -> setModeMain()
            is MapMode.EventView -> setModeEventView(mapMode.eventObject)
            is MapMode.UserView -> setModeUserView(user = mapMode.user, isMe = mapMode.isMe)
            is MapMode.EventEditing -> setModeEditing(
                eventEditingSetupUiModel = mapMode.eventEditingSetupUiModel,
                mapUiValues = uiState.mapUiValues
            )
        }
        setupMapControls(mapMode)
    }

    private fun setModeMain() {
        val map = map ?: return
        map.setOnMapLongClickListener(::onMapLongClick)
        val debouncedUpdate = lifecycleScope.debouncedAction(MAP_UPDATE_DEBOUNCE_DELAY) {
            updateMapData()
        }
        map.setOnCameraIdleListener {
            debouncedUpdate.invoke()

            mapViewModel.eventsOnMap.setMapCameraState(MapCameraState.Idle)
            val eventConfigurationOpen =
                binding?.ecwMapEventsConfiguration?.getState() !is EventConfigurationState.Closed
            val cameraMovedByUser = cameraMoveReason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
            if (eventConfigurationOpen && cameraMovedByUser) {
                getAddressFromEventMarkerLocation()
            }
            val target = mapTargetOverride ?: MapTargetUiModel(
                latLng = map.cameraPosition.target,
                zoom = map.cameraPosition.zoom
            )
            mapTargetOverride = null
            mapViewModel.handleUiAction(MapUiAction.MapWidgetPointInfoUiAction.MapTargetChanged(target))
        }
        mapViewModel.handleUiAction(MapUiAction.MainModeInitialized)
    }

    private fun setModeEventView(eventObject: EventObjectUiModel?) {
        map?.setOnCameraIdleListener {
            handleAuxMapEvent()
        }
        eventObject?.let { showEventOnMap(eventObject) }
    }

    private fun setModeUserView(user: MapUserUiModel, isMe: Boolean) {
        mapObjectsDelegate?.addUserMapObject(user)
        if (isMe) {
            updateCameraLocation(
                location = user.latLng,
                zoom = MAP_ZOOM_USER_ON_MAP,
            )
        } else {
            focusMapItem(FocusedMapItem.User(user.id))
            updateCameraLocation(
                location = user.latLng,
                zoom = MAP_ZOOM_SNIPPET_MAP,
                yOffset = getMapUiValues().userSnippetYOffset
            ) {
                showUserSnippet(user)
            }
        }
    }

    private fun setModeEditing(eventEditingSetupUiModel: EventEditingSetupUiModel, mapUiValues: MapUiValuesUiModel) {
        arguments?.getUploadBundle()?.let(this::openPostEditing)
        map?.setOnCameraIdleListener {
            mapViewModel.eventsOnMap.setMapCameraState(MapCameraState.Idle)
            val cameraMovedByUser = cameraMoveReason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
            if (cameraMovedByUser) {
                getAddressFromEventMarkerLocation()
            }
        }
        val location = eventEditingSetupUiModel.place.location.let { LatLng(it.lat, it.lon) }
        updateCameraLocation(
            location = location,
            yOffset = mapUiValues.eventMarkerYOffset,
            zoom = MAP_ZOOM_EVENT_LOCATION,
            animate = false
        ) {
            mapViewModel.setupEventEditing()
        }
    }

    private fun setupMapControls(mapMode: MapMode?) {
        if (mapMode is MapMode.UserView || mapMode is MapMode.EventView) {
            binding?.ukbMapEventsList?.gone()
            binding?.ukbMapFriendsList?.gone()
            binding?.vgMapLayers?.gone()
            binding?.mapBackButton?.visible()
            setAddEventButtonVisibility(false)

            val params = binding?.mapBackButton?.layoutParams as ConstraintLayout.LayoutParams
            val statusBarHeight = context.getStatusBarHeight()
            params.setMargins(dpToPx(16), statusBarHeight + dpToPx(16), 0, 0)
            binding?.mapBackButton?.layoutParams = params
            binding?.mapBackButton?.setOnClickListener { act.onBackPressed() }
        } else {
            setAddEventButtonVisibility(true)
            hideMapControls()
        }
        (binding?.ivCurrentLocation?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            goneBottomMargin = if (mapMode is MapMode.Main) {
                MY_LOCATION_BOTTOM_MARGIN_MAIN_DP.dp
            } else {
                MY_LOCATION_BOTTOM_MARGIN_AUX_DP.dp
            }
        }
        Timber.e("MapMode.Main ${getMapMode()}")
        binding?.nbBar?.visibility = if (getMapMode() is MapMode.Main) {
            View.INVISIBLE
        } else {
            View.GONE
        }
        updateMapControls()
    }

    private fun initStateObservers() {
        mapViewModel.uiStateFlow
            .onEach(::handleUiState)
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mapViewModel.uiEffectsFlow.collect(::handleUiEffect)
        }
    }

    private fun initUiObservers() {
        mapViewModel.observeAvatarChanges()
        mapViewModel.liveMapObjects.observe(viewLifecycleOwner, this::handleMapObjects)
        mapViewModel.liveErrorEvent.observe(viewLifecycleOwner) { resp ->
            NToast.with(act).text(resp.message).show()
        }
        mapViewModel.liveNavBarOpen.observe(viewLifecycleOwner, this::setNavbarVisible)

        mapViewModel.eventsOnMap.liveEventConfigurationState.observe(
            viewLifecycleOwner,
            this::handleEventConfigurationState
        )
        mapViewModel.eventsOnMap.liveMapEvents.observe(viewLifecycleOwner, this::handleEventObjects)
        mapViewModel.eventsOnMap.livePlacesSearchState.observe(viewLifecycleOwner, this::handlePlacesState)
        mapViewModel.eventsOnMap.liveAddEventButtonState.observe(viewLifecycleOwner, this::setAddEventButtonEnabled)

        mapViewModel.eventsListUiModel.observe(viewLifecycleOwner, this::handleEventsListsUiModel)
        mapViewModel.friendListUiEffect.observe(viewLifecycleOwner, this::handleFriendsEffect)
        mapViewModel.mapFriendsListUiModel.observe(viewLifecycleOwner, this::handleMapFriendsListsUiModel)
        mapViewModel.pointInfoWidgetUiModel.observe(viewLifecycleOwner, this::handlePointInfoWidgetUiModel)

    }

    private fun handleFriendsEffect(uiEffect: MapFriendsListUiEffect?) {
        when (uiEffect) {
            is MapFriendsListUiEffect.OpenUserProfile -> {
                hideKeyboard()
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.USER_SNIPPET,
                    allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.NONE
                )
                val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                mapViewModel.handleUiAction(mapUiAction)

                userSnippet?.setUser(
                    selectedUser = uiEffect.userId,
                    isAuxSnippet = false,
                    fullSnippet = true,
                    snippet = uiEffect.mapUserSnippetModel
                )
            }

            is MapFriendsListUiEffect.SendMessage -> {
                binding?.elwMapFriendsList?.saveScrollPosition()
                binding?.mpiwMapWidget?.isVisible = false
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.USER_SNIPPET,
                    allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.NONE
                )
                val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                mapViewModel.handleUiAction(mapUiAction)
                binding?.elwMapFriendsList?.sendMessage(uiEffect.userId.id)
            }

            is MapFriendsListUiEffect.MapFriendListItemSelected -> {
                updateCameraLocation(
                    location = uiEffect.item.location,
                    yOffset = 180,
                    zoom = getFriendsZoom()
                )
            }

            is MapFriendsListUiEffect.OpenMoments -> {
                hideKeyboard()
                act.openUserMoments(
                    userId = uiEffect.userId.uid,
                    fromView = binding?.elwMapFriendsList,
                    viewedEarly = uiEffect.userId.moments?.hasNewMoments?.not()
                )
            }

            is MapFriendsListUiEffect.OpenFriends -> {
                add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
            }

            else -> {}
        }
    }

    private fun getFriendsZoom(): Float {
        var zoom = MAP_USER_CARD_ZOOM_MIN
        val currentZoom = map?.cameraPosition?.zoom ?: MAP_USER_CARD_ZOOM_MIN
        when {
            (currentZoom > 15) -> {
                zoom = 17f
            }

            currentZoom in 13f..16f -> {
                zoom = currentZoom
            }

            currentZoom < 12f -> {
                zoom = 17f
            }
        }
        return zoom
    }

    private fun handlePointInfoWidgetUiModel(uiModel: MapPointInfoWidgetUiModel) {
        binding?.mpiwMapWidget?.setUiModel(uiModel)
    }

    private fun handleEventsListsUiModel(uiModel: EventsListsUiModel) {
        binding?.elwMapEventsList?.setUiModel(uiModel)
    }

    private fun handleMapFriendsListsUiModel(uiModel: MapFriendsListUiModel) {
        binding?.elwMapFriendsList?.setUiModel(uiModel, mapViewModel::handleUiAction)
    }

    private fun handleUiState(uiState: MapUiState) {
        this.uiState = uiState
        binding?.vMapNonDefaultLayersSettingsIndicator?.isVisible = uiState.nonDefaultLayersSettings
    }

    private fun handleUiEffect(uiEffect: MapUiEffect) {
        when (uiEffect) {
            is MapUiEffect.CallUiStateChanged -> {
                if (uiEffect.isVisible) {
                    if (geoPopupDialog?.isShowing == true) {
                        needToShowGeoPopupDialogWhenCallIsFinished = true
                        geoPopupDialog?.dismiss()
                    }
                    currentDialog?.dismiss()
                } else {
                    if (needToShowGeoPopupDialogWhenCallIsFinished) {
                        needToShowGeoPopupDialogWhenCallIsFinished = false
                        geoPopupDialog?.show()
                    }
                }
            }

            is MapUiEffect.ShowEventTimePicker -> showEventTimePickerDialog(uiEffect.uiModel)
            is MapUiEffect.ShowAddressSearch -> openPlacesSearch(uiEffect.searchText)
            MapUiEffect.ShowEventConfigurationUi -> {
                (act.mainFragment as? MainFragment)?.requestAppInfo()
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.OPEN)
                getAddressFromEventMarkerLocation()
            }

            MapUiEffect.ShowEventLimitReached -> showEventLimitReachedDialog()
            MapUiEffect.UpdateEventsOnMap -> updateEventsData()
            MapUiEffect.ShowWidget -> {
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.USER_SNIPPET,
                    allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.EXTENDED
                )
                val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                mapViewModel.handleUiAction(mapUiAction)
            }

            MapUiEffect.HideWidget -> {
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.USER_SNIPPET,
                    allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.NONE
                )
                val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                mapViewModel.handleUiAction(mapUiAction)
//                binding?.mpiwMapWidget?.isVisible = false
            }

            is MapUiEffect.ShowEventsAbout -> showEventsAboutDialog(uiEffect.eventsInfo)
            is MapUiEffect.CreateMyMarker -> createMyMarker(uiEffect)
            is MapUiEffect.ShowFriendAndUserCityBounds -> showFriendAndUserCityBounds(uiEffect)
            is MapUiEffect.UpdateCameraLocation -> updateCameraLocation(
                location = uiEffect.location,
                zoom = uiEffect.zoom,
                yOffset = uiEffect.yOffset,
                animate = uiEffect.animate
            ) {
                setCurrentLocationActive(uiEffect.isMyLocationActive)
            }

            is MapUiEffect.SetMyLocation -> setMyLocation(uiEffect)
            is MapUiEffect.CalculateMapUiValues -> calculateMapUiValues(uiEffect.mapMode)
            is MapUiEffect.InitializeUi -> onMapUiReady(uiEffect.mapUiState)
            MapUiEffect.UpdateMyMarker -> updateMyMarker()
            is MapUiEffect.FocusMapItem -> focusMapItem(uiEffect.focusedMapItem)
            is MapUiEffect.ShowEnableEventsLayerDialog -> showEnableEventsLayerDialog(uiEffect.confirmAction)
            is MapUiEffect.ShowEnableFriendsLayerDialog -> showFriendsLayerDialog()
            MapUiEffect.ShowMapControls -> showMapControls()
            MapUiEffect.HideMapControls -> hideMapControls()
            MapUiEffect.ResetGlobalMap -> resetGlobalMap()
            MapUiEffect.OpenEventsList -> {
                openEventsListWidget()
            }

            MapUiEffect.CloseEventsList -> closeEventsListWidget()
            MapUiEffect.ShowFriendsListStub -> {
                showFriendsListStubDialog()
            }

            is MapUiEffect.OpenEventNavigation -> openEventNavigation(uiEffect.eventPost)
            is MapUiEffect.OpenEventParticipantsList -> openEventParticipantsList(uiEffect.eventPost)
            is MapUiEffect.OpenUserProfile -> openUserInfoFragment(uiEffect.userId)
            is MapUiEffect.OpenEventsListItemDetails -> openEventsListItemDetails(uiEffect.eventPost)
            MapUiEffect.CloseEventsListItemDetails -> closeEventsListItemDetails()
            MapUiEffect.ShowCreateEventStubDialog -> showCreateEventStubDialog()
            MapUiEffect.ShowEventsStubDialog -> showEventsStubDialog()
            is MapUiEffect.SelectEventsListItem -> selectEventsListItem(
                eventsListType = uiEffect.eventsListType,
                item = uiEffect.item
            )

            is MapUiEffect.UpdateUserMarkerMoments -> updateUserMarkerMoments(uiEffect.updateModel)
            is MapUiEffect.OpenUserProfileInSnippet -> {
            }

            is MapUiEffect.SendMessage -> {
                binding?.elwMapFriendsList?.sendMessage(uiEffect.userId)
            }

            MapUiEffect.OpenFriends -> {
                add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
            }
            else -> Unit
        }
    }

    private fun updateUserMarkerMoments(updateModel: UserMomentsStateUpdateModel) =
        mapObjectsDelegate?.updateUserMarkerMoments(updateModel)

    private fun selectEventsListItem(eventsListType: EventsListType, item: EventsListItem) =
        binding?.elwMapEventsList?.selectItem(eventsListType = eventsListType, item = item)

    fun openUserInfoFragment(userId: Long) {
        add(
            UserInfoFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(
                IArgContainer.ARG_USER_ID,
                userId
            ),
            Arg(
                IArgContainer.ARG_TRANSIT_FROM,
                AmplitudePropertyWhere.MAP_EVENTS_LIST_CREATOR
            )
        )
    }

    private fun openEventsListWidget() = binding?.elwMapEventsList?.open()
    private fun openFriendsListWidget() {
        binding?.elwMapFriendsList?.open()
    }

    private fun closeEventsListWidget() = binding?.elwMapEventsList?.close()

    private fun openEventsListItemDetails(eventPost: PostUIEntity) = binding?.elwMapEventsList?.openEventPost(eventPost)

    private fun closeEventsListItemDetails() = binding?.elwMapEventsList?.closeEventPost()

    private fun showEnableEventsLayerDialog(confirmAction: EnableEventsDialogConfirmAction) =
        showMapDialog(EnableEventsLayerDialogFragment.newInstance(confirmAction), TAG_DIALOG_ENABLE_EVENTS_LAYER)

    private fun showFriendsListStubDialog() {
        needAuth {
            val lowVersion =
                BuildConfig.VERSION_NAME.needToUpdateStr(act.serverAppVersionName)
            when {
                mapViewModel.isFriendsOnMapEnabled.not() -> {
                    openFriendsListWidget()
                }

                lowVersion -> {
                    showMapDialog(FriendStubLowVersionDialogFragment(), TAG_DIALOG_FRIENDS_STUB)
                }
            }
        }
    }

    private fun showFriendsLayerDialog() {
        showMapDialog(FriendStubDialogFragment(), TAG_DIALOG_FRIENDS_STUB)
    }

//    private fun showFriendsListStubDialog() = binding?.mapFriendsListWidget?.open()

    fun enableFriendLayer() {
        mapViewModel.enableFriendLayer()
    }

    private fun showEventsStubDialog() = showMapDialog(EventsStubDialogFragment(), TAG_DIALOG_EVENTS_STUB)

    private fun showCreateEventStubDialog() =
        showMapDialog(CreateEventStubDialogFragment(), TAG_DIALOG_CREATE_EVENT_STUB)

    private fun showMapDialog(dialogFragment: MapDialogFragment, tag: String) {
        hideMapControls()
        mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(true))
        dialogFragment.show(childFragmentManager, tag)
    }

    private fun showMapDialog(dialogFragment: MapLayersDialogFragment, tag: String) {
        hideMapControls()
        mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(true))
        dialogFragment.show(childFragmentManager, tag)
    }

//    private fun showMapDialog(dialogFragment: FriendsStubDialogFragment, tag: String) {
//        hideMapControls()
//        mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(true))
//        dialogFragment.show(childFragmentManager, tag)
//    }

    private fun setMyLocation(uiEffect: MapUiEffect.SetMyLocation) {
        isCameraMovingToCurrentLocation = true
        setCurrentLocationActive(true)
        mapViewModel.handleUiAction(MapUiAction.CreateMyMarkerRequested(location = uiEffect.location))
        updateCameraLocation(
            location = uiEffect.location,
            zoom = MAP_ZOOM_DEFAULT,
            cancelCallback = {
                isCameraMovingToCurrentLocation = false
                setCurrentLocationActive(false)
            },
            callback = { isCameraMovingToCurrentLocation = false }
        )
    }

    private fun updateMyMarker() {
        requestLocationPermission?.dispose()
        requestLocationPermission = rxPermissions
            ?.request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )?.subscribe({ granted ->
                val isGranted = granted ?: false
                if (isGranted) {
                    mapViewModel.handleUiAction(MapUiAction.FindMyLocationRequested)
                } else {
                    NToast.with(view)
                        .text(getString(R.string.permission_must_be_granted))
                        .typeAlert()
                        .button(getString(R.string.allow)) {
                            this.updateMyMarker()
                        }
                        .show()
                }
            }, { error ->
                NToast.with(view)
                    .text(getString(R.string.access_is_denied))
                    .typeAlert()
                    .show()
            })
        requestLocationPermission?.let { disposables.add(it) }
        if (geoAccessDelegate?.isGeoAccessProvided().isFalse()) {
            mapViewModel.handleUiAction(MapUiAction.SetCameraToUserCityLocationRequested(false))
        }
    }

    private fun setupUserUi(isAnon: Boolean) {
        mapViewModel.handleUiAction(MapUiAction.CreateMyMarkerRequested(fallbackToDefaultLocation = true))
        binding?.nbBar?.ivProfileBtn?.setImageResource(
            if (isAnon) {
                R.drawable.selector_button_profile_anon
            } else {
                R.drawable.selector_button_profile
            }
        )
    }

    private fun setCurrentLocationActive(active: Boolean) {
        isCurrentLocationActive = active
        if (binding?.ecwMapEventsConfiguration?.getState() == EventConfigurationState.Closed) {
            binding?.ivCurrentLocation?.isInvisible = active
        }
    }

    private fun handlePlacesState(state: PlacesSearchUiState) {
        binding?.psvMapSearchPlaces?.setState(state)
    }

    private fun handlePlacesSearchEvent(event: PlacesSearchEvent) {
        when (event) {
            is PlacesSearchEvent.PlaceSearched -> mapViewModel.eventsOnMap.onSearchPlaces(event.searchText)
            PlacesSearchEvent.SearchCleared -> mapViewModel.eventsOnMap.onPlacesSearchCleared()
            is PlacesSearchEvent.PlaceSelected -> {
                updateCameraLocation(
                    location = event.place.location.let { LatLng(it.lat, it.lon) },
                    yOffset = getMapUiValues().eventMarkerYOffset,
                    zoom = MAP_ZOOM_EVENT_LOCATION
                ) {
                    mapViewModel.eventsOnMap.onEventPlaceSelected(event.place)
                }
                closePlacesSearch()
            }

            PlacesSearchEvent.Canceled -> closePlacesSearch()
        }
    }

    private fun handleEventConfigurationEvent(event: EventConfigurationEvent) {
        when (event) {
            EventConfigurationEvent.MyLocationClicked -> onEventConfigurationMyLocationClicked()
            EventConfigurationEvent.UiCloseInitiated -> {
                if (binding?.ecwMapEventsConfiguration?.getState() is EventConfigurationState.Onboarding) {
                    mapViewModel.handleUiAction(
                        MapUiAction.AnalyticsUiAction.MapEventOnboardingAction(
                            onboardingType = AmplitudePropertyMapEventsOnboardingType.FIRST,
                            actionType = AmplitudePropertyMapEventsOnboardingActionType.CLOSE
                        )
                    )
                }
                if (mapViewModel.eventsOnMap.savedUploadPostBundle == null) {
                    binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
                    mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.CLOSED)
                } else {
                    showEventCreationCancelDialog {
                        if (getMapMode() is MapMode.EventEditing) {
                            act.onBackPressed()
                        } else {
                            binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
                            mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.CLOSED)
                        }
                    }
                }
            }

            EventConfigurationEvent.CreateEventClicked -> {
                mapViewModel.handleUiAction(
                    MapUiAction.AnalyticsUiAction.MapEventCreateTap(
                        AmplitudePropertyMapEventsCreateTapWhere.ONBOARDING
                    )
                )
                mapViewModel.handleUiAction(
                    MapUiAction.AnalyticsUiAction.MapEventOnboardingAction(
                        onboardingType = AmplitudePropertyMapEventsOnboardingType.FIRST,
                        actionType = AmplitudePropertyMapEventsOnboardingActionType.CREATE_EVENT
                    )
                )
                needAuth {
                    mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.OPEN)
                    getAddressFromEventMarkerLocation()
                }
            }

            is EventConfigurationEvent.EventDateItemSelected -> mapViewModel.eventsOnMap.setSelectedEventDate(
                event.eventEventDateItemUiModel
            )

            is EventConfigurationEvent.EventTypeItemSelected -> mapViewModel.eventsOnMap.setSelectedEventType(
                event.eventTypeItemUiModel
            )

            EventConfigurationEvent.ConfigurationFinished -> {
                createEventPost()
            }

            EventConfigurationEvent.RetryClicked -> getAddressFromEventMarkerLocation()
            EventConfigurationEvent.SelectTimeClicked -> mapViewModel.eventsOnMap.onSelectTime()
            EventConfigurationEvent.SearchPlaceClicked -> mapViewModel.eventsOnMap.onSelectAddress()
            EventConfigurationEvent.EventsAboutClicked -> mapViewModel.eventsOnMap.onShowEventsAbout()
            EventConfigurationEvent.RulesOpen -> mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.RulesOpen)
            EventConfigurationEvent.ConfigurationStep1Finished -> Unit
            EventConfigurationEvent.ConfigurationStep2Finished -> Unit
            is EventConfigurationEvent.MeeraConfigurationFinished -> Unit
        }
    }

    private fun showEventsAboutDialog(eventsInfo: EventsInfoUiModel) {
        currentDialog?.dismiss()
        currentDialog = EventsAboutPopupDialog(
            activity = requireActivity(),
            eventsInfo = eventsInfo,
            onAboutClosed = { isConfirmed ->
                val actionType = if (isConfirmed) {
                    AmplitudePropertyMapEventsOnboardingActionType.CONFIRM
                } else {
                    AmplitudePropertyMapEventsOnboardingActionType.CLOSE
                }
                mapViewModel.handleUiAction(
                    MapUiAction.AnalyticsUiAction.MapEventOnboardingAction(
                        onboardingType = AmplitudePropertyMapEventsOnboardingType.SECOND,
                        actionType = actionType
                    )
                )
            },
            onRulesOpen = {
                mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.RulesOpen)
            }
        ).apply {
            show()
        }
    }

    private fun openPostEditing(uploadPostBundle: String) {
        val fragment = AddMultipleMediaPostFragment.getInstance(
            uploadPostBundle = uploadPostBundle,
            showMediaGallery = false,
            openFrom = AddMultipleMediaPostFragment.OpenFrom.Map
        )
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_map_container, fragment)
            .commit()
    }

    private fun createEventPost() {
        mapViewModel.eventsOnMap.getEventParameters()?.let { eventParametersUiModel ->
            val fragment = AddMultipleMediaPostFragment.getInstance(
                event = eventParametersUiModel,
                uploadPostBundle = mapViewModel.eventsOnMap.savedUploadPostBundle?.toJson(),
                showMediaGallery = false,
                openFrom = AddMultipleMediaPostFragment.OpenFrom.Map
            )
            childFragmentManager.beginTransaction()
                .replace(R.id.fl_map_container, fragment)
                .runOnCommit { mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.HIDDEN) }
                .commit()
        }
    }

    private fun handleEventConfigurationState(state: EventConfigurationState) {
        if (currentState == state
            && currentState == EventConfigurationState.Closed
        ) return
        when (state) {
            EventConfigurationState.Closed -> {
                mapObjectsDelegate?.objectsDisabled = false
                updateMapData()
                showMapControls()
            }

            is EventConfigurationState.Configuration -> {
                mapObjectsDelegate?.objectsDisabled = true
                mapObjectsDelegate?.clearMarkers()
                hideMapControls()
            }

            is EventConfigurationState.Onboarding -> hideMapControls()
            else -> Unit
        }
        binding?.ecwMapEventsConfiguration?.setState(state)
        currentState = state
    }

    private fun showEventLimitReachedDialog() {
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(context)
            .setTitle(R.string.map_events_limit_reached_title)
            .setMessage(getString(R.string.map_events_limit_reached_message, EventConstants.MAX_USER_EVENT_COUNT))
            .setPositiveButton(R.string.map_events_limit_reached_close) { _, _ -> }
            .show()
        mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.MapEventLimitAlert)
    }

    private fun openPlacesSearch(searchText: String) {
        binding?.psvMapSearchPlaces?.apply {
            setState(PlacesSearchUiState.Default)
            setSearchText(searchText)
            visible()
            setKeyboardVisible(true)
        }
    }

    private fun closePlacesSearch() {
        binding?.psvMapSearchPlaces?.apply {
            gone()
            setKeyboardVisible(false)
        }
    }

    private fun initClickListeners() {
        binding?.ivMapLayers?.setOnClickListener { btn ->
            throttleMapAction {
                needAuth {
                    mapViewModel.eventsOnMap.cancelAddEvent()
                    hideMapControls()
                    mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(true))
                    MapLayersDialogFragment().show(childFragmentManager, MapLayersDialogFragment.simpleName)
                }
            }
        }
        binding?.ivCurrentLocation?.setOnClickListener { btn ->
            throttleMapAction {
                mapViewModel.logBackToMyPinClicked()
                if (geoAccessDelegate?.isGeoAccessProvided().isTrue()) {
                    updateMyMarker()
                } else {
                    mapViewModel.eventsOnMap.cancelAddEvent()
                    showGeoPopupDialog(GeoPopupOrigin.MY_LOCATION)
                }
            }
        }
        binding?.vgMapAddEvent?.setOnClickListener { btn ->
            throttleMapAction {
                needAuth {
                    mapViewModel.eventsOnMap.addEvent()
                    mapViewModel.handleUiAction(
                        MapUiAction.AnalyticsUiAction.MapEventCreateTap(
                            AmplitudePropertyMapEventsCreateTapWhere.BUTTON
                        )
                    )
                }
            }
        }
        binding?.ukbMapEventsList?.setOnClickListener {
            throttleMapAction {
                needAuth {
                    mapViewModel.handleUiAction(MapUiAction.EventsListUiAction.EventsListPressed)
                }
            }
        }
        binding?.ukbMapFriendsList?.setOnClickListener {
            throttleMapAction {
                needAuth {
                    mapViewModel.handleUiAction(MapUiAction.FriendsListPressed)
                }
            }
        }
    }

    private fun onMapLongClick(location: LatLng) {
        if (mapViewModel.isEventsOnMapEnabled.not() || binding?.ecwMapEventsConfiguration?.getState() !is EventConfigurationState.Closed
            || binding?.elwMapEventsList?.getState() == ViewPagerBottomSheetBehavior.STATE_EXPANDED
        ) return
        needAuth {
            updateCameraLocation(
                location = location,
                yOffset = getMapUiValues().eventMarkerYOffset
            ) {
                mapViewModel.eventsOnMap.addEvent()
                mapViewModel.handleUiAction(
                    MapUiAction.AnalyticsUiAction.MapEventCreateTap(
                        AmplitudePropertyMapEventsCreateTapWhere.LONGTAP
                    )
                )
            }
        }
    }

    private fun showEventOnMap(eventObject: EventObjectUiModel) {
        mapObjectsDelegate?.focusMapItem(FocusedMapItem.Event(eventObject))
        mapViewModel.eventsOnMap.updateAuxMapEvent(eventObject)
        mapViewModel.eventsOnMap.setAuxMapEventSelected(eventObject)
    }

    private fun onEventConfigurationMyLocationClicked() {
        if (geoAccessDelegate?.isGeoAccessProvided().isTrue()) {
            moveToMyLocationWithEventMarkerOffset()
        } else {
            showGeoPopupDialog(GeoPopupOrigin.MY_LOCATION)
        }
    }

    private fun onOpenInTab(mapOpenPayload: MainMapOpenPayload?) {
        showMapControls()
        when {
            mapOpenPayload != null -> handleMapOpenPayload(mapOpenPayload)
            mapViewModel.eventsOnMap.needToShowEventsOnboarding() -> {
                mapViewModel.eventsOnMap.setEventsOnboardingShown()
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.ONBOARDING)
            }

            geoAccessDelegate?.isGeoAccessProvided().isFalse() && mapViewModel.needToShowGeoPopup() -> {
                showGeoPopupDialog(GeoPopupOrigin.MAP)
                mapViewModel.setGeoPopupShown()
            }
        }
    }

    private fun handleMapOpenPayload(mapOpenPayload: MainMapOpenPayload) {
        when (mapOpenPayload) {
            is MainMapOpenPayload.EventPayload -> {
                mapViewModel.createEventObjectFromEventPost(mapOpenPayload.eventPost)?.let { eventObject ->
                    mapObjectsDelegate?.focusMapItem(FocusedMapItem.Event(eventObject))
                    mapViewModel.eventsOnMap.setSelectedEvent(eventObject)
                }
            }
        }
    }

    private fun onCloseInTab() {
        throttleMapAction()
        hideMapControls()
        currentDialog?.dismiss()
        geoPopupDialog?.dismiss()
        mapViewModel.eventsOnMap.cancelAddEvent()
        userSnippet?.setState(SnippetState.Closed)
        eventSnippetViewController?.closeSnippet()
        mapObjectsDelegate?.focusMapItem(null)
    }

    private fun setNavbarVisible(visible: Boolean) {
        if (isMapOpenInTab) {
            (parentFragment as? MainPostRoadsFragment)?.setNavbarVisible(visible)
        }
    }

    private fun onMarkerClick(marker: Marker) {
        Timber.i("MARKER_CLICK: ${marker.id} ${marker.isVisible}")
        val map = map ?: return
        binding?.elwMapEventsList?.close()
        binding?.elwMapFriendsList?.close()
        when (marker.zIndex) {
            MAP_USERS_ZINDEX, MAP_FRIENDS_ZINDEX -> {
                if (isUserMarkerClickable(getMapMode()).not()) return
                eventSnippetViewController?.closeSnippet()
                mapViewModel.eventsOnMap.cancelAddEvent()
                mapObjectsDelegate?.findUserMarkerEntry(marker.id)?.let { (user, marker) ->
                    focusMapItem(FocusedMapItem.User(user.id))
                    val targetZoom = getTargetSnippetZoom()
                    updateCameraLocation(
                        location = marker.position,
//                        zoom = targetZoom,
                        zoom = MAP_ZOOM_SNIPPET_MAP,
                        yOffset = getMapUiValues().userSnippetYOffset
                    ) {
                        showUserSnippet(user)
                    }
                }
            }

            MAP_CLUSTERS_ZINDEX -> {
                if (getMapMode() !is MapMode.Main) return
                val clusterLocation = LatLng(marker.position.latitude, marker.position.longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        clusterLocation,
                        map.cameraPosition.zoom + 2
                    ),
                    CAMERA_ANIMATION_DURATION_MS,
                    null
                )
            }

            MAP_EVENTS_ZINDEX -> {
                when (getMapMode()) {
                    is MapMode.Main -> {
                        hideUserSnippet()
                        mapViewModel.eventsOnMap.cancelAddEvent()
                        mapObjectsDelegate?.findEventMarkerEntry(marker.id)?.let { eventMarkerEntry ->
                            focusMapItem(FocusedMapItem.Event(eventMarkerEntry.event))
                            mapViewModel.eventsOnMap.setSelectedEvent(eventMarkerEntry.event)
                            mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.EventSnippetOpenTap)
                        }
                    }

                    is MapMode.EventView -> {
                        mapObjectsDelegate?.findEventMarkerEntry(marker.id)?.let { eventMarkerEntry ->
                            focusMapItem(FocusedMapItem.Event(eventMarkerEntry.event))
                            mapViewModel.eventsOnMap.setAuxMapEventSelected(eventMarkerEntry.event)
                            mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.EventSnippetOpenTap)
                        }
                    }

                    else -> Unit
                }

            }
        }
    }

    private fun updateMapData() {
        when (getMapMode()) {
            is MapMode.Main -> {
                mapViewModel.handleUiAction(MapUiAction.CreateMyMarkerRequested())
                getMapObjects()
                updateEventsData()
            }

            is MapMode.EventView -> {
                handleAuxMapEvent()
            }

            else -> Unit
        }
    }

    private fun getMapObjects() {
        if (mapObjectsDelegate?.objectsDisabled.isNotFalse()) return
        map?.let { map ->
            val latLngBounds = map.projection.visibleRegion.latLngBounds
            mapViewModel.getMapObjects(
                gpsXMin = latLngBounds.southwest.latitude,
                gpsXMax = latLngBounds.northeast.latitude,
                gpsYMin = latLngBounds.southwest.longitude,
                gpsYMax = latLngBounds.northeast.longitude,
                zoom = map.cameraPosition.zoom.toDouble()
            )
        }
    }

    private fun createMyMarker(uiEffect: MapUiEffect.CreateMyMarker) {
        val myMarker = this.myMarker
        if (myMarker != null && myMarker.isVisible) {
            if (uiEffect.markerIsObsolete) {
                createMyMarkerOnMap(
                    showAnon = !act.getAuthenticationNavigator().isAuthorized(),
                    moments = uiEffect.moments
                )
            } else {
                myMarker.position = uiEffect.latLng
            }
        } else {
            createMyMarkerOnMap(
                showAnon = !act.getAuthenticationNavigator().isAuthorized(),
                moments = uiEffect.moments
            )
        }
        if (isCameraMovingToCurrentLocation.not()
            && uiEffect.checkCurrentLocationActive
            && map?.cameraPosition?.target?.equalWithTolerance(uiEffect.latLng).isNotTrue()
        ) {
            setCurrentLocationActive(false)
        }
    }

    private fun showFriendAndUserCityBounds(uiEffect: MapUiEffect.ShowFriendAndUserCityBounds) {
        val map = map ?: return
        if (map.cameraPosition == uiEffect.cameraPosition) {
            showFriendBounds(
                map = map,
                friendLocation = uiEffect.friendLocation,
                userLocation = uiEffect.userCityLocation
            )
        }
    }

    private fun createMyMarkerOnMap(
        showAnon: Boolean = true,
        moments: PinMomentsUiModel
    ) {
        if (geoAccessDelegate?.isGeoAccessProvided().isFalse()) return

        Glide.with(this@MapFragment)
            .asBitmap()
            .apply {
                if (showAnon) {
                    this.load(R.drawable.ic_anon_map)
                } else {
                    this.load(mapViewModel.getAvatarMarker())
                }
            }
            .apply(
                RequestOptions()
                    .circleCrop()
            )
            .into(object : SimpleTarget<Bitmap>() {

                override fun onResourceReady(
                    @NonNull resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val namedAvatarView = UserPinView(act, null)
                    namedAvatarView.setNameVisible(false)
                    namedAvatarView.showMoments(moments)
                    val avatarView = namedAvatarView.avatarView
                    avatarView.show(
                        mapViewModel.readAccountType(),
                        mapViewModel.readAccountColor(),
                        BitmapDrawable(resources, resource),
                        moments
                    )
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)

                    val placeholderDrawable = ResourcesCompat.getDrawable(
                        resources,
                        if (mapViewModel.getGender() == Gender.MALE) {
                            R.drawable.ic_user_placeholder_man
                        } else {
                            R.drawable.ic_user_placeholder_woman
                        },
                        null
                    )

                    if (act != null) {
                        val namedAvatarView = UserPinView(act, null)
                        namedAvatarView.setNameVisible(false)
                        namedAvatarView.showMoments(moments)
                        val avatarView = namedAvatarView.avatarView
                        avatarView.show(
                            mapViewModel.readAccountType(),
                            mapViewModel.readAccountColor(),
                            placeholderDrawable,
                            moments
                        )
                    }

                }
            })
    }

    private fun handleFriends(mapObjects: MapObjectsUiModel) {
        if (needToShowNearestFriend.not()) return
        needToShowNearestFriend = false
        val map = map ?: return
        val visibleBounds = map.projection.visibleRegion.latLngBounds
        val friendsVisibleOnMap = mapObjects.users.any { it.isFriend && visibleBounds.contains(it.latLng) }
        if (friendsVisibleOnMap) return
        val friendLocation = mapObjects.nearestFriend?.location ?: return
        val userLocation = mapViewModel.readLastLocation()
        if (userLocation == null || geoAccessDelegate?.isGeoAccessProvided().isNotTrue()) {
            val uiAction = MapUiAction.ShowFriendAndUserCityBoundsRequested(
                friendLocation = friendLocation,
                cameraPosition = map.cameraPosition
            )
            mapViewModel.handleUiAction(uiAction)
        } else {
            showFriendBounds(
                map = map,
                friendLocation = friendLocation,
                userLocation = userLocation
            )
        }
    }

    private fun showFriendBounds(map: GoogleMap, friendLocation: LatLng, userLocation: LatLng) {
        val targetBounds = LatLngBounds.builder()
            .include(friendLocation)
            .include(userLocation)
            .build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(targetBounds, FRIEND_VIEW_BOUNDS_PADDING_DP.dp)
        val currentPosition = map.cameraPosition
        map.moveCamera(cameraUpdate)
        val paddingWithTolerancePx = (FRIEND_VIEW_BOUNDS_PADDING_DP - FRIEND_VIEW_BOUNDS_PADDING_TOLERANCE_DP).dp
        val pointNearLeft = map.projection.toScreenLocation(map.projection.visibleRegion.nearLeft)
        pointNearLeft.x = pointNearLeft.x + paddingWithTolerancePx
        pointNearLeft.y = pointNearLeft.y - paddingWithTolerancePx
        val paddedNearLeft = map.projection.fromScreenLocation(pointNearLeft)
        val pointFarRight = map.projection.toScreenLocation(map.projection.visibleRegion.farRight)
        pointFarRight.x = pointFarRight.x - paddingWithTolerancePx
        pointFarRight.y = pointFarRight.y + paddingWithTolerancePx
        val paddedFarRight = map.projection.fromScreenLocation(pointFarRight)
        val actualBounds = LatLngBounds.builder()
            .include(paddedNearLeft)
            .include(paddedFarRight)
            .build()
        val targetUpdate = when {
            actualBounds.contains(userLocation).not() || actualBounds.contains(friendLocation).not() -> null
            map.cameraPosition.zoom > FRIEND_VIEW_MAX_ZOOM -> CameraUpdateFactory.newLatLngZoom(
                targetBounds.center,
                FRIEND_VIEW_MAX_ZOOM
            )

            else -> cameraUpdate
        }
        map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPosition))
        targetUpdate?.let {
            map.animateCamera(targetUpdate, FRIEND_VIEW_ANIMATION_DURATION_MS, null)
        }
    }

    private fun showUserSnippet(selectedUserModel: MapUserUiModel) {
        when {
            getMapMode() is MapMode.UserView ->
                userSnippet?.setUser(selectedUser = selectedUserModel, isAuxSnippet = true)

            isMapOpenInTab ->
                userSnippet?.setUser(selectedUser = selectedUserModel, isAuxSnippet = false)
        }
    }

    private fun hideUserSnippet() = userSnippet?.setState(SnippetState.Closed)

    private fun handleMapObjects(mapObjects: MapObjectsUiModel) {
        if (mapObjectsDelegate?.objectsDisabled.isNotFalse()) return
        mapObjectsDelegate?.handleUsers(mapObjects.users)
        mapObjectsDelegate?.handleClusters(mapObjects.clusters)
        handleFriends(mapObjects)
    }

    private fun handleEventObjects(events: List<EventObjectUiModel>) {
        if (mapObjectsDelegate?.objectsDisabled.isNotFalse()) return
        mapObjectsDelegate?.handleEvents(events)
    }

    private fun getAddressFromEventMarkerLocation() {
        val localBinding = binding ?: return
        val point = localBinding.ecwMapEventsConfiguration.getEventMarkerPositionRelative(localBinding.flMapContainer)
        map?.projection?.fromScreenLocation(point)?.let { latLng ->
            mapViewModel.eventsOnMap.getAddress(latLng)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val mapStyleResId = mapViewModel.mapSettings.mapMode.toUIMapStyleEntity().mapStyleResId
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(act, mapStyleResId))
        } catch (e: Resources.NotFoundException) {
            Timber.e(e)
        }
    }

    private fun calculateMapUiValues(mapMode: MapMode) {
        val mapBottomPadding = calculateMapBottomPadding(mapMode)
        val mapUiValues = MapUiValuesUiModel(
            mapBottomPadding = mapBottomPadding,
            userSnippetYOffset = calculateUserSnippetYOffset(mapBottomPadding),
            eventMarkerYOffset = calculateEventMarkerYOffset(mapBottomPadding),
            eventsListsYOffset = calculateEventsListsYOffset(mapBottomPadding),
            mapHeight = binding?.mvMap?.height ?: 0
        )
        mapViewModel.handleUiAction(MapUiAction.MapUiValuesCalculated(mapUiValues))
    }

    private fun calculateMapBottomPadding(mapMode: MapMode): Int {
        return when (mapMode) {
            is MapMode.Main -> binding?.nbBar?.rootView
                ?.findViewById<View>(R.id.vButtonBarBackground)
                ?.height
                ?: 0

            else -> 0
        }
    }

    private fun calculateUserSnippetYOffset(mapBottomPadding: Int): Int {
        val userSnippetHeight = ProfileUiUtils.getSnippetHeight(context)
        val mapHeight = binding?.mvMap?.height ?: 0
        return userSnippetHeight - (((mapHeight - mapBottomPadding) / 2) + mapBottomPadding) + PIN_Y_OFFSET_FROM_SNIPPET_DP.dp
    }

    private fun calculateEventMarkerYOffset(mapBottomPadding: Int): Int {
        val localBinding = binding ?: return 0
        val mapHeight = localBinding.mvMap.height
        return ((mapHeight - mapBottomPadding) / 2) -
            localBinding.ecwMapEventsConfiguration.getEventMarkerPositionRelative(localBinding.flMapContainer).y
    }

    private fun calculateEventsListsYOffset(mapBottomPadding: Int): Int {
        val localBinding = binding ?: return 0
        val mapHeight = localBinding.mvMap.height
        val eventsListsHeight = resources.getDimensionPixelSize(R.dimen.map_events_lists_height)
        return eventsListsHeight - (((mapHeight - mapBottomPadding) / 2) + mapBottomPadding) +
            EVENT_MARKER_Y_OFFSET_FROM_EVENTS_LISTS_DP.dp
    }

    @SuppressLint("MissingPermission")
    private fun moveToMyLocationWithEventMarkerOffset() {
        viewLifecycleOwner.lifecycleScope.launch {
            val location = mapViewModel.readLastLocation() ?: return@launch
            isCameraMovingToEventConfigurationMyLocation = true
            mapViewModel.eventsOnMap.setMyLocationActive(true)
            mapViewModel.handleUiAction(MapUiAction.CreateMyMarkerRequested())
            updateCameraLocation(
                location = location,
                yOffset = getMapUiValues().eventMarkerYOffset,
                zoom = MAP_ZOOM_CLOSE,
                cancelCallback = {
                    isCameraMovingToEventConfigurationMyLocation = false
                    mapViewModel.eventsOnMap.setMyLocationActive(false)
                }
            ) {
                isCameraMovingToEventConfigurationMyLocation = false
                getAddressFromEventMarkerLocation()
            }
        }
    }

    private fun setAddEventButtonEnabled(state: AddEventButtonState) {
        binding?.apply {
            vgMapAddEvent.isEnabled = state == AddEventButtonState.ENABLED
            val alpha = if (state == AddEventButtonState.DISABLED) 0.5f else 1f
            vgMapAddEvent.alpha = alpha
            ivMapAddEvent.isVisible = state != AddEventButtonState.PROGRESS
            pbMapAddEvent.isVisible = state == AddEventButtonState.PROGRESS
        }
    }

    private fun showGeoPopupDialog(geoPopupOrigin: GeoPopupOrigin) {
        if (IS_APP_REDESIGNED) {
            showMeeraLocationEnableDialog(geoPopupOrigin)
        } else {
            showOldLocationEnableDialog(geoPopupOrigin)
        }
    }

    private fun showMeeraLocationEnableDialog(geoPopupOrigin: GeoPopupOrigin) {
        meeraGeoPopupDialog?.dismiss()
        meeraGeoPopupDialog = MeeraGeoPopupDialog(
            origin = geoPopupOrigin,
            activity = requireActivity(),
            onEnableGeoClicked = {
                geoAccessDelegate?.provideGeoAccess()
            },
            onGeoPopupAction = { action, origin ->
                mapViewModel.logGeoPopupAction(
                    action = action,
                    origin = origin
                )
            }
        ).apply {
            show()
        }
    }

    private fun showOldLocationEnableDialog(geoPopupOrigin: GeoPopupOrigin) {
        geoPopupDialog?.dismiss()
        geoPopupDialog = GeoPopupDialog(
            origin = geoPopupOrigin,
            activity = requireActivity(),
            onEnableGeoClicked = {
                geoAccessDelegate?.provideGeoAccess()
            },
            onGeoPopupAction = { action, origin ->
                mapViewModel.logGeoPopupAction(
                    action = action,
                    origin = origin
                )
            }
        ).apply {
            show()
        }
    }

    private fun showEventTimePickerDialog(uiModel: TimePickerUiModel) {
        currentDialog?.dismiss()
        currentDialog = TimePickerPopupDialog(
            activity = requireActivity(),
            uiModel = uiModel
        ) { time ->
            mapViewModel.eventsOnMap.setSelectedTime(time)
        }.apply {
            show()
        }
    }

    private fun removeCurrentChildFragment() {
        childFragmentManager.findFragmentById(R.id.fl_map_container)?.let { fragment ->
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    private fun handleEventConfigurationBackPress(): Boolean {
        return handlePostEditBackPress() || handlePlacesSearchBackPress() || handleEventConfigurationViewBackPress()
    }

    private fun handlePostEditBackPress(): Boolean {
        val postEditFragment =
            childFragmentManager.findFragmentById(R.id.fl_map_container) as? AddMultipleMediaPostFragment
        return if (postEditFragment != null) {
            if (!postEditFragment.onBackPressed()) {
                mapViewModel.eventsOnMap.savedUploadPostBundle = postEditFragment.createUploadPostBundle()
                childFragmentManager.beginTransaction()
                    .remove(postEditFragment)
                    .commit()
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.OPEN)
            }
            true
        } else {
            false
        }
    }

    private fun handlePlacesSearchBackPress(): Boolean {
        return if (binding?.psvMapSearchPlaces?.isVisible.isTrue()) {
            closePlacesSearch()
            true
        } else {
            false
        }
    }

    private fun handleEventConfigurationViewBackPress(): Boolean = binding?.ecwMapEventsConfiguration
        ?.onBackPressed()
        ?: false

    private fun handleAuxMapEvent() {
        mapViewModel.eventsOnMap.liveMapEvents.value?.let(::handleEventObjects)
    }

    private fun showEventCreationCancelDialog(onConfirmed: () -> Unit) {
        isEventCreationFinished = false
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(context)
            .setTitle(R.string.map_events_creation_cancel_title)
            .setMessage(R.string.map_events_creation_cancel_message)
            .setPositiveButton(R.string.map_events_creation_cancel_positive) { _, _ ->
                isEventCreationFinished = true
                onConfirmed()
            }
            .setNegativeButton(R.string.map_events_creation_cancel_negative) { _, _ -> }
            .show()
    }

    private fun setAddEventButtonVisibility(isVisible: Boolean) {
        binding?.vgMapAddEvent?.isVisible = isVisible && mapViewModel.isEventsOnMapEnabled
    }

    private fun updateMapControls() {
        val binding = binding ?: return
        val updatedMapControls = mutableListOf<View>()
        if (getMapMode() == MapMode.Main) {
            updatedMapControls.add(binding.vgMapLayers)
            updatedMapControls.add(binding.ukbMapFriendsList)
        }
        if (isCurrentLocationActive.not()) {
            updatedMapControls.add(binding.ivCurrentLocation)
        }
        if (getMapMode() == MapMode.Main) {
            updatedMapControls.add(binding.vgMapAddEvent)
            updatedMapControls.add(binding.ukbMapEventsList)
        }
        mapControls = updatedMapControls
    }

    private fun throttleMapAction(action: () -> Unit = {}) {
        val time = SystemClock.elapsedRealtime()
        if (time - lastThrottledActionTime > THROTTLE_DURATION_MS) {
            action.invoke()
            lastThrottledActionTime = time
        }
    }

    private fun isUserMarkerClickable(mapMode: MapMode?): Boolean =
        mapMode is MapMode.Main || (mapMode as? MapMode.UserView)?.isMe.isFalse()

    companion object {

        private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

        const val ARG_EVENT_POST = "ARG_EVENT_POST"
        const val ARG_EVENT_POST_ID = "ARG_EVENT_POST_ID"
        const val ARG_LOG_MAP_OPEN_WHERE = "ARG_LOG_MAP_OPEN_WHERE"

        const val MAP_ZOOM_CLOSE = 15f
        const val MAP_USER_CARD_ZOOM_MAX = 15f
        const val MAP_USER_CARD_ZOOM_MIN = 12f
        const val MAP_ZOOM_EVENT_LOCATION = 17f
        const val MAP_ZOOM_DEFAULT = 17f
        const val MAP_ZOOM_USER_ON_MAP = 12f
        const val MAP_ZOOM_SNIPPET_MAP = 17f

        private const val CAMERA_ANIMATION_DURATION_MS = 450
        private const val CAMERA_ANIMATION_DELAY_MS = 100L

        private const val PIN_Y_OFFSET_FROM_SNIPPET_DP = 48

        private const val EVENT_MARKER_Y_OFFSET_FROM_EVENTS_LISTS_DP = 16

        private const val MY_LOCATION_BOTTOM_MARGIN_MAIN_DP = -4
        private const val MY_LOCATION_BOTTOM_MARGIN_AUX_DP = 48

        private const val LOCATION_SERVICES_CHANGED_DEBOUNCE_DELAY = 100L
        private const val MAP_UPDATE_DEBOUNCE_DELAY = 300L

        private const val FRIEND_VIEW_MAX_ZOOM = 18f
        private const val FRIEND_VIEW_BOUNDS_PADDING_DP = 80
        private const val FRIEND_VIEW_BOUNDS_PADDING_TOLERANCE_DP = 2
        private const val FRIEND_VIEW_ANIMATION_DURATION_MS = 300

        private const val THROTTLE_DURATION_MS = 1000

        private const val TAG_DIALOG_EVENTS_STUB = "TAG_DIALOG_EVENTS_STUB"
        private const val TAG_DIALOG_CREATE_EVENT_STUB = "TAG_DIALOG_CREATE_EVENT_STUB"
        private const val TAG_DIALOG_FRIENDS_STUB = "TAG_DIALOG_FRIENDS_STUB"
        private const val TAG_DIALOG_ENABLE_EVENTS_LAYER = "TAG_DIALOG_ENABLE_EVENTS_LAYER"
    }
}

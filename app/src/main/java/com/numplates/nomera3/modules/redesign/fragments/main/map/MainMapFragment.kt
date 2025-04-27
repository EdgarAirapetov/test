package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.Manifest
import android.annotation.SuppressLint
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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.debouncedAction
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isNotFalse
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.needToUpdateStr
import com.meera.core.extensions.register
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.toJson
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.LocationUtility
import com.meera.core.utils.graphics.NGraphics
import com.meera.core.utils.showCommonError
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.MeeraFragmentMapBinding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsCreateTapWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.events.EventConstants
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.model.toUIMapStyleEntity
import com.numplates.nomera3.modules.maps.ui.MapUiActionHandler
import com.numplates.nomera3.modules.maps.ui.equalWithTolerance
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListsUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.AddEventButtonState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurator
import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventsInfoUiModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.EventParticipantsListFragment
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.EventRoadPrivacyDialogFragment
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiEffect
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiModel
import com.numplates.nomera3.modules.maps.ui.geo_popup.GeoPopupDialog
import com.numplates.nomera3.modules.maps.ui.geo_popup.MeeraGeoPopupDialog
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.GeoPopupOrigin
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
import com.numplates.nomera3.modules.maps.ui.pin.UserPinView
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapUiController
import com.numplates.nomera3.modules.maps.ui.widget.model.AllowedPointInfoWidgetVisibility
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetState
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapTargetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapUiFactor
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoWidgetAllowedVisibilityChange
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchEvent
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchUiState
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.main.MakePublicationMenuBottomSheet
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MakePostDialogState
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraMapObjectsDelegate
import com.numplates.nomera3.modules.redesign.fragments.main.map.configuration.MeeraMapViewModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.MeeraCreateEventStubDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.MeeraEnableEventsLayerDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.MeeraEventsAboutPopupDialog
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.MeeraEventsStubDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.friends.MeeraFriendStubDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.friends.MeeraFriendStubLowVersionDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.layers.MeeraMapLayersDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraEventPostPageFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraUserSnippetBottomSheetWidget
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraUserSnippetFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraUserSnippetViewModel
import com.numplates.nomera3.modules.redesign.fragments.secondary.MeeraCreatePostFragment
import com.numplates.nomera3.modules.redesign.fragments.secondary.MeeraCreatePostFragment.Companion.KEY_MAP_EVENT
import com.numplates.nomera3.modules.redesign.fragments.secondary.MeeraCreatePostFragment.Companion.KEY_OPEN_MAP_EVENT
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigateWithResult
import com.numplates.nomera3.modules.redesign.util.setHiddenState
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.upload.data.post.UploadMediaModel
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.upload.util.getUploadBundle
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.uploadpost.ui.viewmodel.MeeraCreatePostViewModel
import com.numplates.nomera3.modules.userprofile.ui.ProfileUiUtils
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment.Companion.USERINFO_HIDDEN_HEIGHT
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment.Companion.USERINFO_HIDE_SHOW_ANIM_DURATION_MS
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.AddPostViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.properties.Delegates


private const val CLOSING_SNIPPET_DELAY = 600L

private const val OPENNING_EVENT_LIST_DELAY = 400L

private const val BACK_STACK_DELAY = 200L
private const val MAKE_PUBLICATION_MENU_SHOW_TAG = "MakePublicationMenuBottomSheet"
private const val MAKE_PUBLICATION_MENU_RESHOW_TAG = "MakePublicationMenuBottomSheetReshow"


// Будет реализован иным способом
// Добавлен как заглушка
class MainMapFragment : MeeraBaseFragment(R.layout.meera_fragment_map),
    OnMapReadyCallback,
    INetworkValues,
    IArgContainer,
    IOnBackPressed,
    IAuthStateObserver,
    BasePermission by BasePermissionDelegate(),
    MeeraUserSnippetBottomSheetWidget.Listener,
    MeeraUserSnippetFragment.Listener,
    EventConfigurator,
    MeeraMapSnippetHost,
    MapUiController,
    MapUiActionHandler,
    ScreenshotTakenListener {

    private var shouldShowEventOnMap: Boolean = true
    private var currentMapZoom: Float? = null
    private var currentLocation: LatLng? = null
    private var currentAttachments: List<UploadMediaModel> = emptyList()
    override var isMapOpenInTab: Boolean = true
    var isQuasiMap: Boolean = false
    private val mapViewModel: MeeraMapViewModel by viewModels { App.component.getViewModelFactory() }

    private val binding by viewBinding(MeeraFragmentMapBinding::bind)

    /** TODO https://nomera.atlassian.net/browse/BR-18804
     * Убрать дополнительную ViewModel, перенести логику во ViewModel MapFragment
     */
    private val userSnippetViewModel: MeeraUserSnippetViewModel by viewModels { App.component.getViewModelFactory() }

    // TODO: move MeeraUserSnippetViewModel to map vm
    private val addPostViewModel: MeeraCreatePostViewModel by viewModels { App.component.getViewModelFactory() }

    private var needToShowNearestFriend = false

    private var mapObjectsDelegate: MeeraMapObjectsDelegate? = null

    private var map: GoogleMap? = null
    private var mapViewBundle: Bundle? = null
    private var myMarker: Marker? = null
    private var rxPermissions: RxPermissions? = null
    private val disposables = CompositeDisposable()
    private var menu: MakePublicationMenuBottomSheet? = null
    var navigatingFromServices: Boolean = false
    var openingEventCreate: Boolean = false

    private var isCameraMovingToCurrentLocation = false
    private var isCameraMovingToEventConfigurationMyLocation = false
    private var isCurrentLocationActive = false

    private var requestLocationPermission: Disposable? = null

    private var uiState: MapUiState? = null

    private var isEventOpenFromService = false

    private var userSnippet: MeeraUserSnippetBottomSheetWidget? = null
    private var mapControls = emptyList<View>()

    private var locationServicesBroadcastReceiver: BroadcastReceiver? = null
    private var geoPopupDialog: GeoPopupDialog? = null
    private var meeraGeoPopupDialog: MeeraGeoPopupDialog? = null
    private var needToShowGeoPopupDialogWhenCallIsFinished = false
    private var currentDialog: Dialog? = null
    private var isEventCreationFinished = false

    private var geoAccessDelegate: MeeraGeoAccessDelegate? = null
    private var eventSnippetViewController: MeeraEventSnippetViewController? = null
    private var openEventSnippetOnMapMode: Boolean = false

    private var lastThrottledActionTime = 0L

    private var cameraMoveReason: Int? = null

    private var mapTargetOverride: MapTargetUiModel? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = saveToExternalPermissions().all { key -> permissions[key] == true }
            if (isGranted) {
                mapViewModel.handleUiAction(MapUiAction.FindMyLocationRequested)
            }
        }

    //TODO: refactor to coroutine after redesign
    private val disposable = CompositeDisposable()
    private var act by Delegates.notNull<MeeraAct>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = activity as MeeraAct
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
    }

    override fun onScreenshotTaken() {
        val currentPage = binding.msvpEvents.getCurrentPage() as? MeeraEventPostPageFragment?
        currentPage?.onScreenshotTaken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded.not()) return
        binding.usbsWidget.onStartFragment()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted.not() }
            .forEach { it.onStartFragment() }

        mapControls = listOf(
            binding.vgMapLayers,
            binding.ukbMapEventsList,
            binding.ukbMapFriendsList,
            binding.ivCurrentLocation,
        )

        binding.mvMap.onCreate(mapViewBundle)
        binding.mvMap.getMapAsync(this)
        initStateObservers()
        view.post {
            mapViewModel.handleUiAction(MapUiAction.MapViewCreated(arguments))
        }
        subscribeViewEvent()
        initAttachmentObserver()
        subscribePostViewEvent()
        checkLocationPermission()
        NavigationManager.getManager().mapModeLiveData.observe(viewLifecycleOwner) { isMap ->
            if (!isQuasiMap) {
                currentLocation?.let { updateCameraLocation(it, currentMapZoom) }
            }
            binding.tbGradient.isVisible = isMap
            if (isMap) {
                mapViewModel.handleUiAction(MapUiAction.MapWidgetPointInfoUiAction.RefreshMapPoint)
                // TODO: fix for applyConfigToToolbar in MeeraBaseDialogFragment for ScreenBehaviourState.MapTransparent toolbat state

                if (isFromProfileOpen()) {
                    NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                        UiKitToolbarViewState.COLLAPSED
                }

                if (isMapWidgetsOpen()) {
                    NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                        UiKitNavigationBarViewVisibilityState.GONE
                    return@observe
                }

                onOpenInTab(null)

                Handler(Looper.getMainLooper()).postDelayed({
                    if (mapViewModel.eventsOnMap.needToShowEventsOnboarding() || isMapWidgetsOpen()) return@postDelayed
                    NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                        UiKitToolbarViewState.EXPANDED
                    NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().hasSecondButton = true
                }, 300)
                initNavigationButtonsListeners()
                if (openEventSnippetOnMapMode) {
                    eventSnippetViewController?.openSnippet()
                    openEventSnippetOnMapMode = false
                }
            } else {
                clearNavigationFields()
            }
        }
    }

    private fun clearNavigationFields() {
        navigatingFromServices = false
        openingEventCreate = false
    }

    private fun checkLocationPermission() {
        if (!LocationUtility.checkPermissionLocation(act)) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun saveToExternalPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            emptyList()
        }
    }

    private fun addEventMenuClick(eventListener: (() -> Unit)? = null) {
        openingEventCreate = true
        NavigationManager.getManager().getForceUpdatedTopBehavior()?.setHiddenState()
        eventListener?.invoke()
        mapViewModel.eventsOnMap.addEvent()
        mapViewModel.handleUiAction(
            MapUiAction.AnalyticsUiAction.MapEventCreateTap(
                AmplitudePropertyMapEventsCreateTapWhere.BUTTON
            )
        )
    }

    fun initNavigationButtonsListeners(
        fromMap: Boolean = true
    ) {
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().setButtonsListeners(
            addButtonListener = {
                throttleMapAction {
                    needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
                        showMakePublicationDialog(fromMap = fromMap)
                    }
                }
            },
            secondButtonListener = {
                NavigationManager.getManager().isMapMode = false
                needAuthToNavigate {
                    NavigationManager.getManager().topNavController.safeNavigate(
                        resId = R.id.searchNavGraph, bundle = bundleOf(
                            IArgContainer.ARG_SEARCH_FROM_MAP to fromMap
                        )
                    )
                }
            },
            notificationsButtonListener = {
                needAuthToNavigate {
                    NavigationManager.getManager().initGraph(R.navigation.bottom_notifications_graph)
                }
            }
        )
    }

    private fun showMakePublicationDialog(
        fromMap: Boolean,
        postNavigationId: Int = R.id.meeraCreatePostFragment,
        eventListener: (() -> Unit)? = null
    ) {
        menu = MakePublicationMenuBottomSheet()
        menu?.setHideDismissListener {
            menu = null
        }
        menu?.setActionListener { publicationType ->
            menu = null
            when (publicationType) {
                MakePublicationMenuBottomSheet.Companion.PublicationType.POST
                    -> NavigationManager.getManager().topNavController
                    .safeNavigate(postNavigationId, bundleOf(KEY_OPEN_MAP_EVENT to fromMap))

                MakePublicationMenuBottomSheet.Companion.PublicationType.MOMENT
                    -> act.getMomentsViewController().open()

                MakePublicationMenuBottomSheet.Companion.PublicationType.EVENT -> {
                    addEventMenuClick(eventListener)
                }
            }
        }
        menu?.show(childFragmentManager, MAKE_PUBLICATION_MENU_SHOW_TAG)
    }

    private fun hideMakePublicationDialogAfterCall() {
        menu?.dismiss()
    }

    private fun showMakePublicationDialogAfterCall() {
        menu?.show(childFragmentManager, MAKE_PUBLICATION_MENU_RESHOW_TAG)
    }

    private fun initAttachmentObserver() {
        mapViewModel.attachmentsMedia.observe(viewLifecycleOwner) { attachments ->
            binding.ecwMapEventsConfiguration?.submitAttachment(attachments)
        }
        addPostViewModel.attachmentsMedia.observe(viewLifecycleOwner) { attachments ->

            currentAttachments = if (mapViewModel.currentImageChoosen.value.isNullOrEmpty()) {
                emptyList()
            } else {
                addPostViewModel.parseAttachments(attachments)
            }

            mapViewModel.currentBundle.value = mapViewModel.currentBundle.value?.copy(
                mediaList = currentAttachments
            )
            if (binding.ecwMapEventsConfiguration.getBehaviorState() == STATE_HIDDEN) return@observe

            mapViewModel.currentBundle.value?.let {
                addPostViewModel.addPostV2(it)
                mapViewModel.onImageChosen("")
            }
        }
        mapViewModel.showRemoveEventMediaDialog.observe(viewLifecycleOwner) { model ->
            MeeraConfirmDialogBuilder()
                .setHeader(getString(R.string.post_reset_media_dialog_title))
                .setDescription(getString(R.string.post_reset_media_dialog_description))
                .setTopBtnText(getString(R.string.delete))
                .setTopBtnType(ButtonType.FILLED)
                .setTopClickListener {
                    mapViewModel.handleUiAction(MapUiAction.RemoveMediaEvent(model))
                }
                .setBottomBtnText(getString(R.string.cancel))
                .setCancelable(false)
                .show(childFragmentManager)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapViewModel.makePostDialogState.collectLatest { state ->
                    when (state) {
                        is MakePostDialogState.Show -> {
                            Timber.d("MAP SC showMakePublicationDialog collect $menu")
                            showMakePublicationDialogAfterCall()
                        }

                        is MakePostDialogState.Hide -> {
                            Timber.d("MAP SC hideMakePublicationDialog collect $menu")
                            hideMakePublicationDialogAfterCall()
                        }
                    }
                }
            }
        }

        mapViewModel.livePostViewEvents.observe(viewLifecycleOwner) { postViewEvent ->
            when (postViewEvent) {
                is PostViewEvent.OnEditImageByClick -> {
                    binding?.ecwMapEventsConfiguration?.openEditor(uri = Uri.parse(postViewEvent.path))
                }

                is PostViewEvent.OnOpenImage -> {
                    binding?.ecwMapEventsConfiguration?.openPhoto(postViewEvent.path)
                }

                is PostViewEvent.MediaAttachmentSelected -> {
                    binding?.ecwMapEventsConfiguration?.mediaAttachmentSelected(postViewEvent.uri)
                }

                is PostViewEvent.PermissionsReady -> Unit

                else -> {
                    throw RuntimeException("Unprocessed event - $postViewEvent")
                }
            }
        }
    }

    private fun subscribeViewEvent() {
        disposable.add(
            mapViewModel.streamEvent.observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    onViewEvent(event)
                })
    }

    private fun onViewEvent(event: AddPostViewEvent) {
        when (event) {
            is AddPostViewEvent.NeedToShowModerationDialog -> {

                MeeraConfirmDialogBuilder()
                    .setHeader(getString(R.string.map_events_moderation_dialog_title))
                    .setDescription(getString(R.string.map_events_moderation_dialog_message))
                    .setBottomBtnText(getString(R.string.map_events_moderation_dialog_edit).uppercase())
                    .setTopBtnText(getString(R.string.map_events_moderation_dialog_confirm).uppercase())
                    .setCancelable(false)
                    .setTopClickListener {
                        addPostViewModel.publishCurrentUploadBundle()
                        mapViewModel.onImageChosen("")
                    }
                    .setBottomClickListener {
                        binding.ecwMapEventsConfiguration.resetStep2()
                    }
                    .setColorBg(R.color.uiKitColorAccentPrimary)
                    .setDialogCancelledListener {

                    }
                    .show(childFragmentManager)
            }

            is AddPostViewEvent.Empty -> {
                return
            }

            is AddPostViewEvent.UploadStarting -> {
                binding.ecwMapEventsConfiguration.setState(EventConfigurationState.Closed)
            }

            is AddPostViewEvent.ShowAvailabilityError -> {
                showNotAvailableError(event.reason)
            }

            is AddPostViewEvent.Success -> {
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
            }

            is AddPostViewEvent.NeedToShowRoadPrivacyDialog -> EventRoadPrivacyDialogFragment
                .getInstance(event.roadPrivacySetting)
                .show(childFragmentManager, EventRoadPrivacyDialogFragment::class.java.name)
//            is AddPostViewEvent.NeedToShowResetEditedMediaDialog -> binding?.ecwMapEventsConfiguration?.showPostMediaResetDialog(
//                event.uri,
//                event.isAdding,
//                event.openCamera
//            )
//            is AddPostViewEvent.SetAttachment -> binding?.ecwMapEventsConfiguration?.getMediaByPicker(event.uri, event.afterEdit)
            else -> {}
        }
        mapViewModel.clearLastEvent()
    }

    override fun onStart() {
        super.onStart()
        binding?.mvMap?.onStart()
        binding?.nbBar?.selectMap(true)
    }

    // TODO: Fix moved to onstart
//    override fun onStartFragment() {
//        if (isAdded.not()) return
//        super.onStartFragment()
//        binding?.usbsWidget?.onStartFragment()
//        childFragmentManager.fragments
//            .filterIsInstance<BaseFragmentNew<*>>()
//            .filter { it.isFragmentStarted.not() }
//            .forEach { it.onStartFragment() }
//    }

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

        if (mapObjectsDelegate?.objectsDisabled == true) {
            NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                UiKitNavigationBarViewVisibilityState.GONE
        }

        mapViewModel.handleUiAction(MapUiAction.OnResumeCalled(isMapOpenInTab))

        if (eventSnippetViewController?.getCurrentState() != SnippetState.Closed && eventSnippetViewController?.getCurrentState() != null) {
            NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                UiKitToolbarViewState.COLLAPSED
            NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                UiKitNavigationBarViewVisibilityState.GONE
        }
    }

    private fun isFromProfileOpen(): Boolean {
        return NavigationManager.getManager().topNavController.currentDestination?.id == R.id.userInfoFragment
    }

    private fun isMapWidgetsOpen(): Boolean {
        return (eventSnippetViewController?.getCurrentState() != SnippetState.Closed &&
            eventSnippetViewController?.getCurrentState() != null)
            || binding.ecwMapEventsConfiguration.getBehaviorState() != STATE_HIDDEN
            || binding.msvpEvents.isVisible
            || binding?.elwMapFriendsList?.getState() != STATE_HIDDEN
            || isFromProfileOpen()
    }

    override fun onPause() {
        super.onPause()
        binding?.mvMap?.onPause()
        if (isAdded.not()) return
        binding?.usbsWidget?.onStopFragment()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted }
            .forEach { it.onStopFragment() }
    }

    override fun onStop() {
        super.onStop()
        binding?.mvMap?.onStop()
        binding?.nbBar?.selectMap(false)
        disposables.dispose()
    }


    //TODO: Fix, moved to onpause
//    override fun onStopFragment() {
//        if (isAdded.not()) return
//        super.onStopFragment()
//        binding?.usbsWidget?.onStopFragment()
//        childFragmentManager.fragments
//            .filterIsInstance<BaseFragmentNew<*>>()
//            .filter { it.isFragmentStarted }
//            .forEach { it.onStopFragment() }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        if (view != null) {
            binding?.mvMap?.onSaveInstanceState(mapViewBundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        disposable.clear()
    }

    override fun onDestroy() {

        if (view != null) {
            binding.mvMap.onDestroy()
            binding.mvMap.removeAllViews()
        }

        locationServicesBroadcastReceiver?.let {
            context?.unregisterReceiver(it)
        }
        map = null
        mapObjectsDelegate = null
        userSnippet?.removeListener(this)
        userSnippet = null

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
        } else {
            userSnippet?.onBackPressed().isTrue()
                || eventSnippetViewController?.onBackPressed().isTrue()
                || handleEventConfigurationBackPress()
                // TODO: FIX Event list redesign

//                || binding?.elwMapEventsList?.onBackPressed().isTrue()
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

    //TODO: fix
//    override fun updateScreenOnTapNavBar() {
//        super.updateScreenOnTapNavBar()
//        updateMyMarker()
//    }

    override fun onEditEvent() {
        (childFragmentManager.findFragmentById(R.id.fl_map_container) as? MeeraAddPostFragmentNew)?.let { postFragment ->
            mapViewModel.eventsOnMap.savedUploadPostBundle = postFragment.createUploadPostBundle()
        }
        mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.OPEN)
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

    // TODO: FIXED AFTER REDESIGN MAP FRAGMENT
    fun getStateOfFriendListView(): Int? =
        binding.elwMapFriendsList.getState()

    fun getStateEventView(): EventConfigurationState? =
        binding?.ecwMapEventsConfiguration?.getState()

    fun showFriendsList(){
        binding.elwMapFriendsList.setState(SnippetState.Preview)
        binding.elwMapFriendsList.turnOffProfile()
    }

    fun getShouldShowBottomNavBarInEmptyFragment() = shouldShowEventOnMap

    override fun showMapControls() {
        binding?.mpiwMapWidget?.visible()
        binding.mpiwMapWidget.setSingleLineWeather(true)
        showMapGradientIfMapMode()

        // TODO fix after friends widget moving to fragment
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state = UiKitToolbarViewState.EXPANDED

        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
            UiKitNavigationBarViewVisibilityState.VISIBLE

        if (!isMapOpenInTab && getMapMode() is MapMode.Main) return

        updateMapControls()
        val mapControlsEnterAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.map_controls_show)
        mapControlsEnterAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                updateMapControls()
                mapControls.forEach {
                    it.visible()
                    if (it is UiKitButton) it.post { it.requestLayout() }
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
        if (NavigationManager.getManager().isMapMode || hideToolbar) {
            NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                UiKitToolbarViewState.COLLAPSED
        }
        binding.mpiwMapWidget.setSingleLineWeather(false)
        hideMapGradientIfNotMapMode()

        updateMapControls()
        mapControls.forEach {
            it.clearAnimation()
            it.invisible()
        }
        if (showUserLocationView) {
            binding.ivCurrentLocation.visible()
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
            else -> Unit
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
            if (!isQuasiMap) {
                currentLocation = location
                currentMapZoom = targetZoom
            }
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

        if (state is SnippetState.Closed) {
            mapObjectsDelegate?.focusMapItem(null)
        }

        if (mapMode is MapMode.UserView) {
            when (state) {
                SnippetState.Preview -> {
                    focusMapItem(FocusedMapItem.User(mapMode.user.id))
                }

                else -> Unit
            }
        } else {
            val mainPostRoadsParent = (parentFragment as? MainPostRoadsFragment) ?: return
            if (mainPostRoadsParent.getMode() != MainRoadMode.MAP) return
            mapViewModel.setUserSnippetState(state)
            when (state) {
                SnippetState.Preview -> {
                    hideMapControls()
                }

                SnippetState.Closed -> {
                    if (binding?.elwMapFriendsList?.getState() == STATE_HIDDEN) {
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

    override fun getEventSnippetViewController(): MeeraEventSnippetViewController? = eventSnippetViewController

    override fun setMapMode(mapMode: MapMode) {
        mapViewModel.setMapMode(mapMode)
    }

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
        mapObjectsDelegate = MeeraMapObjectsDelegate(
            fragment = this,
            map = googleMap,
            mapViewModel = mapViewModel
        )
    }

    private fun initDelegates(mapMode: MapMode) {
        rxPermissions = RxPermissions(act)

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
        binding?.ecwMapEventsConfiguration?.setEventListener(this::handleEventConfigurationEvent, mapViewModel)
        binding?.ecwMapEventsConfiguration?.setFragmentManager()
        binding?.psvMapSearchPlaces?.setEventListener(this::handlePlacesSearchEvent)
        geoAccessDelegate = MeeraGeoAccessDelegate(
            act = act,
            permissionDelegate = PermissionDelegate(
                activity = activity,
                viewLifecycleOwner = viewLifecycleOwner
            ),
            lifecycle = viewLifecycleOwner.lifecycle
        )
        binding?.msvpEvents?.let { viewPager ->
            eventSnippetViewController = MeeraEventSnippetViewController(
                mapParametersCache = mapViewModel.getMapParametersCache(),
                fragment = this,
                viewPager = viewPager,
                eventsOnMap = mapViewModel.eventsOnMap,
                mapUiController = this,
                mapMode = mapMode
            )
        }
        // TODO: FIX Event list redesign
//        binding?.elwMapEventsList?.uiActionListener = mapViewModel::handleUiAction
        binding?.elwMapFriendsList?.uiActionListener = mapViewModel::handleUiAction
    }

    fun handleUiActionFromViewModel(): (MapUiAction.EventsListUiAction) -> Unit = mapViewModel::handleUiAction

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
                hideUserProfile()
                eventSnippetViewController?.closeSnippet()
                binding?.elwMapFriendsList?.close()
                mapTargetOverride = null
                doDelayed(CLOSING_SNIPPET_DELAY) {
                    if (isEventOpenFromService) {
                        isEventOpenFromService = false
                        closeEventsListWidget()
                    }
                    NavigationManager.getManager().getTopBehaviour(isOnlyMapMode = true)?.state =
                        STATE_HIDDEN
                }
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
            binding?.elwMapFriendsList?.close()
            doDelayed(CLOSING_SNIPPET_DELAY) {
                NavigationManager.getManager().getTopBehaviour(isOnlyMapMode = true)?.state =
                    STATE_HIDDEN
            }
        }
    }

    private fun hideUserProfile() {
        if(isFromProfileOpen().not()) return
        NavigationManager.getManager().getTopContainer()?.animate()?.translationY(USERINFO_HIDDEN_HEIGHT.dp)
            ?.setDuration(USERINFO_HIDE_SHOW_ANIM_DURATION_MS)?.start()
    }

    fun closeSnippet(openOnResume: Boolean = false) {
        eventSnippetViewController?.closeSnippet()
        this.openEventSnippetOnMapMode = openOnResume
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
        if (user.isShowOnMap.not()) return
        resetGlobalMap()
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
        if (mapMode is MapMode.UserView || mapMode is MapMode.EventView || mapMode is MapMode.Main) {
            binding?.ukbMapEventsList?.gone()
            binding?.ukbMapFriendsList?.gone()
            binding?.vgMapLayers?.gone()
            setAddEventButtonVisibility(false)
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
        binding?.nbBar?.visibility = if (getMapMode() is MapMode.Main) {
            View.INVISIBLE
        } else {
            View.GONE
        }
        updateMapControls()
    }

    private fun initStateObservers() {
        mapViewModel.isMapFriendsListClosed.observe(viewLifecycleOwner) {
            shouldShowEventOnMap = it
        }
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
            NToast.with(requireView()).text(resp.message).show()
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
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.USER_SNIPPET,
                    allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.NONE
                )
                binding.mpiwMapWidget.gone()
                val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                mapViewModel.handleUiAction(mapUiAction)
                openUserInfoFragment(uiEffect.userId.id, isFull = true)
                hideKeyboard()
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
                NavigationManager.getManager().topNavController.navigate(
                    R.id.action_global_meeraViewMomentFragment,
                    bundleOf(KEY_USER_ID to uiEffect.userId.uid)
                )
            }

            is MapFriendsListUiEffect.OpenFriends -> {
                //TODO: check
                NavigationManager.getManager().topNavController.safeNavigate(R.id.action_mainRoadFragment_to_searchNavGraph)
//                add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
            }

            else -> {}
        }
    }

    private fun getFriendsZoom(): Float {
        var zoom = MAP_USER_CARD_ZOOM_MIN

        val currentZoom = map?.cameraPosition?.zoom ?: MAP_USER_CARD_ZOOM_MIN
        when {
            (currentZoom > MAP_ZOOM_DEFAULT) -> {
                zoom = MAP_ZOOM_SNIPPET_MAP
            }

            currentZoom in 13f..16f -> {
                zoom = currentZoom
            }

            currentZoom < MAP_ZOOM_USER_ON_MAP -> {

                zoom = MAP_ZOOM_USER_ON_MAP
            }
        }
        return zoom
    }

    private fun handlePointInfoWidgetUiModel(uiModel: MapPointInfoWidgetUiModel) {
        binding?.mpiwMapWidget?.setUiModel(uiModel)
        if (uiModel.state is MapPointInfoWidgetState.Shown.ExtendedDetailed) {
            showMapGradientIfMapMode()
        }
    }

    @Suppress("detekt:UnusedPrivateMember")
    private fun handleEventsListsUiModel(uiModel: EventsListsUiModel) {
        // TODO: FIX Event list redesign
//        binding?.elwMapEventsList?.setUiModel(uiModel)
    }

    private fun handleMapFriendsListsUiModel(uiModel: MapFriendsListUiModel) {
        binding?.elwMapFriendsList?.setUiModel(uiModel, mapViewModel::handleUiAction)
    }

    private fun handleUiState(uiState: MapUiState) {
        this.uiState = uiState
        binding?.vMapNonDefaultLayersSettingsIndicator?.isVisible = uiState.nonDefaultLayersSettings
        setMapMode(uiState)
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

            is MapUiEffect.ShowEventTimePicker -> {
//                showEventTimePickerDialog(uiEffect.uiModel)
            }

            is MapUiEffect.ShowAddressSearch -> openPlacesSearch(uiEffect.searchText)
            MapUiEffect.ShowEventConfigurationUi -> {
                // TODO: fix
//                (act.mainFragment as? MainFragment)?.requestAppInfo()
//                mapViewModel.eventsOnMap.setEventConfigurationUiMode(EventConfigurationUiMode.FIRST_STEP)

                mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.OPEN)
                getAddressFromEventMarkerLocation()
            }

            MapUiEffect.ShowEventLimitReached -> showEventLimitReachedDialog()
            MapUiEffect.UpdateEventsOnMap -> updateEventsData()
            MapUiEffect.ShowWidget -> {
                binding?.mpiwMapWidget?.visible()
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
                binding?.mpiwMapWidget?.isVisible = false
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
            MapUiEffect.ShowMapControls -> {
                showMapControls()
            }

            MapUiEffect.HideMapControls -> hideMapControls()
            MapUiEffect.ResetGlobalMap -> resetGlobalMap()
            MapUiEffect.OpenEventsList -> {
                openEventsListWidget()
            }

            MapUiEffect.CloseEventsList -> {
                NavigationManager.getManager().getTopBehaviour(isOnlyMapMode = true)?.state =
                    STATE_HIDDEN
                closeEventsListWidget()
            }

            MapUiEffect.ShowFriendsListStub -> {
                showFriendsListStubDialog()
            }

            is MapUiEffect.OpenEventNavigation -> {
                val initUiModel = EventNavigationInitUiModel(
                    event = uiEffect.eventPost.event ?: return,
                    authorId = uiEffect.eventPost.user?.userId ?: return
                )
                MeeraEventNavigationBottomsheetDialogFragment.getInstance(initUiModel)
                    .show(childFragmentManager, MeeraEventNavigationBottomsheetDialogFragment::class.java.name)
//                openEventNavigation(uiEffect.eventPost)
            }

            is MapUiEffect.OpenEventParticipantsList -> {
                //TODO: check
                uiEffect.eventPost.event ?: return
                NavigationManager.getManager().topNavController.safeNavigate(
                    resId = R.id.action_emptyMapFragment_to_eventParticipantsListFragment,
                    bundle = Bundle().apply {
                        putLong(
                            EventParticipantsListFragment.ARG_EVENT_ID, uiEffect.eventPost.event.id
                        )
                        putLong(
                            EventParticipantsListFragment.ARG_POST_ID, uiEffect.eventPost.postId
                        )
                        putInt(
                            EventParticipantsListFragment.ARG_PARTICIPANTS_COUNT,
                            uiEffect.eventPost.event.participation.participantsCount
                        )
//                putParcelable(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.MAP_EVENTS_LIST_CREATOR)
                    }
                )
//                openEventParticipantsList(uiEffect.eventPost)
            }

            is MapUiEffect.OpenUserProfile -> openUserInfoFragment(uiEffect.userId)
            is MapUiEffect.OpenEventCreatorAvatarProfile -> openAvatarCreateFragment(uiEffect.userId)
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

            is MapUiEffect.ShowErrorMessage -> {
                showCommonError(getString(uiEffect.message), requireView())
            }

            MapUiEffect.OpenFriends -> {
                // TODO: check
                NavigationManager.getManager().topNavController.safeNavigate(R.id.action_mainRoadFragment_to_searchNavGraph)
//                add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
            }
        }
    }

    private fun updateUserMarkerMoments(updateModel: UserMomentsStateUpdateModel) =
        mapObjectsDelegate?.updateUserMarkerMoments(updateModel)

    @Suppress("detekt:UnusedPrivateMember")
    private fun selectEventsListItem(eventsListType: EventsListType, item: EventsListItem) {
        // TODO: FIX Event list redesign
//        binding?.elwMapEventsList?.selectItem(eventsListType = eventsListType, item = item)
    }

    private fun openUserInfoFragment(userId: Long, isFull: Boolean = false) {
        // TODO: check
        binding.elwMapFriendsList.setState(SnippetState.Closed)

        NavigationManager.getManager().topNavController.safeNavigate(
            resId = R.id.userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                MeeraUserInfoFragment.ARG_USER_SNIPPET_DATA_FULL to isFull
            )
        )

        doDelayed(CLOSING_SNIPPET_DELAY) {
            NavigationManager.getManager().isMapMode = true
            hideMapControls()
            binding.tbGradient.gone()
        }
    }

    private fun openAvatarCreateFragment(userId: Long) {
        NavigationManager.getManager().topNavController.safeNavigate(
            resId = R.id.userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                MeeraUserInfoFragment.ARG_USER_SNIPPET_DATA_FULL to true,
                MeeraUserInfoFragment.ARG_USER_SNIPPET_EVENT to true
            )
        )
    }

    private fun openEventsListWidget() =
        {
            // TODO: FIX Event list redesign
//            binding?.elwMapEventsList?.open()
        }

    private fun openFriendsListWidget() {
        binding?.elwMapFriendsList?.open()
    }

    @Suppress("detekt:UnusedPrivateMember")
    private fun closeEventsListWidget() {
        NavigationManager.getManager().topNavController.popBackStack()
        showMapControls()
        // TODO: FIX Event list redesign
        //  binding?.elwMapEventsList?.close()
    }

    @Suppress("detekt:UnusedPrivateMember")
    private fun openEventsListItemDetails(eventPost: PostUIEntity) = Unit

    private fun closeEventsListItemDetails() {
        // TODO: FIX Event list redesign
        //  binding?.elwMapEventsList?.closeEventPost()
    }

    private fun showEnableEventsLayerDialog(confirmAction: EnableEventsDialogConfirmAction) =
        showMapDialog(MeeraEnableEventsLayerDialogFragment.newInstance(confirmAction), TAG_DIALOG_ENABLE_EVENTS_LAYER)

    private fun showFriendsListStubDialog() {
//        needAuth {
        val lowVersion =
            BuildConfig.VERSION_NAME.needToUpdateStr(act.serverAppVersionName)
        when {
            mapViewModel.isFriendsOnMapEnabled.not() -> {
                NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                    UiKitNavigationBarViewVisibilityState.GONE
                openFriendsListWidget()
            }

            lowVersion -> {
                showMapDialog(MeeraFriendStubLowVersionDialogFragment(), TAG_DIALOG_FRIENDS_STUB)
            }
//            }
        }
    }

    private fun showFriendsLayerDialog() {
        showMapDialog(MeeraFriendStubDialogFragment(), TAG_DIALOG_FRIENDS_STUB)
    }

//    private fun showFriendsListStubDialog() = binding?.mapFriendsListWidget?.open()

    fun enableFriendLayer() {
        mapViewModel.enableFriendLayer()
    }

    private fun showEventsStubDialog() = showMapDialog(MeeraEventsStubDialogFragment(), TAG_DIALOG_EVENTS_STUB)

    private fun showCreateEventStubDialog() =
        showMapDialog(MeeraCreateEventStubDialogFragment(), TAG_DIALOG_CREATE_EVENT_STUB)

    private fun showMapDialog(dialogFragment: MeeraMapDialogFragment, tag: String) {
        hideMapControls()
        mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(true))
        dialogFragment.show(childFragmentManager, tag)
    }

    private fun showMapDialog(dialogFragment: MeeraMapLayersDialogFragment, tag: String) {
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
        binding.ivCurrentLocation.isInvisible = active
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

    fun handleEventConfigurationEvent(event: EventConfigurationEvent) {
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
                    binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
                    mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
//                    showMapControls()
                } else if (binding.ecwMapEventsConfiguration.getState() is EventConfigurationState.StepFirstConfiguration) {
                    if (getMapMode() is MapMode.EventEditing) {
                        act.onBackPressed()
                    } else {
                        binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
                        mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
                    }
                    closePlacesSearch()
                } else {
                    showEventCreationCancelDialog {
                        if (getMapMode() is MapMode.EventEditing) {
                            act.onBackPressed()
                        } else {
                            binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
                            mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
                        }
                        closePlacesSearch()
                    }
                }
//                if (mapViewModel.eventsOnMap.savedUploadPostBundle == null) {
//                    binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
//                    mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
//                } else {
//                    showEventCreationCancelDialog {
//                        if (getMapMode() is MapMode.EventEditing) {
//                            act.onBackPressed()
//                        } else {
//                            binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
//                            mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
//                        }
//                    }
//                }
                mapViewModel.clearSelectedEditedMediaUri()
            }

            EventConfigurationEvent.CreateEventClicked -> {

                needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {

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
                    mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.OPEN)
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
                mapViewModel.eventsOnMap.getEventParameters()?.let { eventParametersUiModel ->
                    NavigationManager.getManager().topNavController.safeNavigate(
                        resId = R.id.meeraCreatePostFragment,
                        bundle = bundleOf(
                            KEY_MAP_EVENT to eventParametersUiModel,
                            IArgContainer.ARG_SHOW_MEDIA_GALLERY to false,
                            UPLOAD_BUNDLE_KEY to mapViewModel.eventsOnMap.savedUploadPostBundle?.toJson(),
                            MeeraCreatePostFragment.OpenFrom.EXTRA_KEY to MeeraAddPostFragmentNew.OpenFrom.Map
                        )
                    )
                }
            }

            EventConfigurationEvent.ConfigurationStep1Finished -> {
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.STEP1_FINISHED)
            }

            EventConfigurationEvent.ConfigurationStep2Finished -> {
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.STEP2_FINISHED())
            }

            EventConfigurationEvent.RetryClicked -> getAddressFromEventMarkerLocation()
            EventConfigurationEvent.SelectTimeClicked -> mapViewModel.eventsOnMap.onSelectTime()
            EventConfigurationEvent.SearchPlaceClicked -> mapViewModel.eventsOnMap.onSelectAddress()
            EventConfigurationEvent.EventsAboutClicked -> mapViewModel.eventsOnMap.onShowEventsAbout()
            EventConfigurationEvent.RulesOpen -> mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.RulesOpen)
            is EventConfigurationEvent.MeeraConfigurationFinished -> {
                showEventModerationDialog(event)
            }
        }
    }

    private fun showEventsAboutDialog(eventsInfo: EventsInfoUiModel) {
        currentDialog?.dismiss()
        currentDialog = MeeraEventsAboutPopupDialog(
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
        val fragment = MeeraAddPostFragmentNew.getInstance(
            uploadPostBundle = uploadPostBundle,
            showMediaGallery = false,
            openFrom = MeeraAddPostFragmentNew.OpenFrom.Map
        )
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_map_container, fragment)
            .commit()
    }

    private fun showEventModerationDialog(event: EventConfigurationEvent.MeeraConfigurationFinished) {
        createEventPost(event)
    }

    private fun subscribePostViewEvent() {
        disposable.add(
            addPostViewModel.streamEvent.observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    onViewEvent(event)
                })
    }

    private fun showNotAvailableError(reason: NotAvailableReasonUiEntity) {
        when (reason) {
            NotAvailableReasonUiEntity.POST_NOT_FOUND -> showError(R.string.post_edit_error_not_found_message)
            NotAvailableReasonUiEntity.USER_NOT_CREATOR -> showError(R.string.post_edit_error_not_creator_message)
            NotAvailableReasonUiEntity.POST_DELETED -> showError(R.string.post_edit_error_deleted_message)
            NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE,
            NotAvailableReasonUiEntity.UPDATE_TIME_IS_OVER -> {
                showAlert(
                    title = getString(R.string.post_edit_error_expired_title),
                    message = getString(R.string.post_edit_error_expired_description),
                    onOkClick = {
                        context?.hideKeyboard(requireView())
//                        mediaPicker?.dismissAllowingStateLoss()
                    }
                )
            }
        }
    }

    private fun showError(messageRes: Int) {
        UiKitSnackBar.makeError(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(messageRes)
                )
            )
        ).show()
    }

    private fun showAlert(
        title: String,
        message: String,
        onOkClick: () -> Unit
    ) {
        MeeraConfirmDialogBuilder()
            .setHeader(title)
            .setDescription(message)
            .setTopBtnText(getString(R.string.i_have_read))
            .setTopBtnType(ButtonType.FILLED)
            .setTopClickListener {
                onOkClick.invoke()
            }
            .show(childFragmentManager)
    }

    private fun createEventPost(event: EventConfigurationEvent.MeeraConfigurationFinished) {
        currentAttachments = emptyList()
        mapViewModel.eventsOnMap.getEventParameters()?.let { eventParametersUiModel ->
            eventParametersUiModel.let(addPostViewModel::setEventParameters)
            createUploadPostBundle(eventParametersUiModel, event)
        }
    }

    fun createUploadPostBundle(
        eventParametersUiModel: EventParametersUiModel,
        eventResult: EventConfigurationEvent.MeeraConfigurationFinished
    ) {

        val event = addPostViewModel.getEventEntity()?.copy(title = eventResult.title)

        mapViewModel.currentBundle.value = UploadPostBundle(
            text = eventResult.subtitle,
            postId = 0,
            groupId = 0,
            imagePath = null,
            videoPath = null,
            mediaAttachmentUriString = null,
            roadType = eventResult.roadType.state,
            whoCanComment = eventResult.whoCanComment,
            media = null,
            backgroundId = null,
            backgroundUrl = null,
            fontSize = null,
            event = event,
            mediaPositioning = null,
            mediaList = currentAttachments
        )

        val mediaList = if (eventParametersUiModel.model?.attachmentResource?.isEmpty() == false) {
            listOf(
                MediaUriModel(
                    initialUri = Uri.parse(eventParametersUiModel.model.attachmentResource)
                )
            )
        } else {
            listOf()
        }
        addPostViewModel.setMediaList(mediaList)
    }

    private fun handleEventConfigurationState(state: EventConfigurationState) {
        when (state) {
            EventConfigurationState.Closed -> {
                binding?.mpiwMapWidget?.visible()
                mapObjectsDelegate?.objectsDisabled = false
                updateMapData()
                showMapControls()
            }

            is EventConfigurationState.Configuration -> {
                mapObjectsDelegate?.objectsDisabled = true
                mapObjectsDelegate?.clearMarkers()
                hideMapControls()
                NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                    UiKitNavigationBarViewVisibilityState.GONE
            }

            is EventConfigurationState.StepFirstConfiguration -> {
                binding?.mpiwMapWidget?.gone()
                mapObjectsDelegate?.objectsDisabled = true
                mapObjectsDelegate?.clearMarkers()
                hideMapControls()
                NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                    UiKitNavigationBarViewVisibilityState.GONE
                NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                    UiKitToolbarViewState.COLLAPSED
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.EVENT_CONFIGURATION,
                    allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.NONE
                )
                val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                mapViewModel.handleUiAction(mapUiAction)
            }

            is EventConfigurationState.Step2 -> {
                mapObjectsDelegate?.objectsDisabled = true
                mapObjectsDelegate?.clearMarkers()
                hideMapControls()
                NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                    UiKitNavigationBarViewVisibilityState.GONE
            }

            else -> Unit
        }
        binding?.ecwMapEventsConfiguration?.setState(state)
    }

    private fun showEventLimitReachedDialog() {
        currentDialog?.dismiss()

        MeeraConfirmDialogBuilder()
            .setHeader(R.string.meera_map_events_limit_reached_title)
            .setDescription(getString(R.string.map_events_limit_reached_message, EventConstants.MAX_USER_EVENT_COUNT))
            .setTopBtnText(R.string.map_events_info_okay)
            .setTopBtnType(ButtonType.FILLED)
            .hideBottomBtn()
            .setCancelable(true)
            .show(childFragmentManager)

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
                needAuthToNavigate {
                    mapViewModel.eventsOnMap.cancelAddEvent()
                    hideMapControls()
                    mapViewModel.handleUiAction(MapUiAction.MapBottomSheetDialogStateChanged(true))
                    MeeraMapLayersDialogFragment().show(childFragmentManager, MeeraMapLayersDialogFragment.simpleName)
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
                needAuthToNavigate {
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
            openEventList()
        }
        binding?.ukbMapFriendsList?.setOnClickListener {
            throttleMapAction {
                needAuthToNavigate {
                    mapViewModel.handleUiAction(MapUiAction.FriendsListPressed)
                }
            }
        }
    }

    private fun onMapLongClick(location: LatLng) {
        if (mapViewModel.isEventsOnMapEnabled.not() || binding?.ecwMapEventsConfiguration?.getState() !is EventConfigurationState.Closed
        // TODO: FIX Event list redesign
//            || binding?.elwMapEventsList?.getState() == ViewPagerBottomSheetBehavior.STATE_EXPANDED
        ) return
//        needAuth {
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
//            }
        }
    }

    fun openEventFromAnotherScreen(post: PostUIEntity, shouldShowEventMap: Boolean) {
        shouldShowEventOnMap = false
        val roadPost = post.copy(openedFromRoad = shouldShowEventMap)
        mapViewModel.createEventObjectFromEventPost(roadPost)?.let { eventObject ->
            isCameraMovingToEventConfigurationMyLocation = true
            if (!shouldShowEventMap) {
                binding.mpiwMapWidget.gone()
            }
            mapObjectsDelegate?.focusMapItem(FocusedMapItem.Event(eventObject))
            mapViewModel.eventsOnMap.setSelectedEvent(roadPost)
            if (shouldShowEventMap) {
                mapViewModel.setMapMode(MapMode.EventView(eventObject))
            }
        }
    }

    fun setMapModeToDefault() {
        mapViewModel.setMapMode(MapMode.Main)
    }

    fun setMapModeForUserProfile(user: MapUserUiModel, isMe: Boolean) {
        mapViewModel.setMapMode(MapMode.UserView(user, isMe))
    }

    fun handleIsShowNoMapPlaceholder(
        isShowNoMapPlaceholder: Boolean,
        placeholderType: NoShowOnMapPlaceholderType = NoShowOnMapPlaceholderType.OTHER_PROFILE
    ) {
        if (isShowNoMapPlaceholder) {
            binding.noShowOnMapPlaceholder.setPlaceholderType(placeholderType)
            binding.noShowOnMapPlaceholder.visible()
            binding.noShowOnMapPlaceholder.initSettingsButton()
            binding.noShowOnMapPlaceholder.clickActionButton { openAppSettingsForResult() }
        } else {
            binding.noShowOnMapPlaceholder.gone()
        }
    }

    private fun openAppSettingsForResult() = try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        TedOnActivityResult.with(requireContext())
            .setIntent(intent)
            .setListener { _, _ ->
                val isLocationPermissionGranted = LocationUtility.checkPermissionLocation(requireContext())
                if (isLocationPermissionGranted) binding.noShowOnMapPlaceholder.gone()
            }
            .startActivityForResult()
    } catch (e: Exception) {
        Timber.e(e)
    }

    fun openEventList(openFromService: Boolean = false) {
        isEventOpenFromService = openFromService
        throttleMapAction {
            doDelayed(OPENNING_EVENT_LIST_DELAY) {
                needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
                    NavigationManager.getManager().topNavController
                        .safeNavigate(
                            resId = R.id.event_nav,
                            navBuilder = { builder ->
                                builder.setLaunchSingleTop(true)
                            }
                        )
                    mapViewModel.handleUiAction(MapUiAction.EventsListUiAction.EventsListPressed)
                }
            }
        }
    }

    private fun showEventOnMap(eventObject: EventObjectUiModel) {
        mapObjectsDelegate?.focusMapItem(FocusedMapItem.Event(eventObject))
        mapViewModel.eventsOnMap.setSelectedEvent(eventObject)
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
            mapViewModel.eventsOnMap.needToShowEventsOnboarding() && !navigatingFromServices && !openingEventCreate -> {
                mapViewModel.eventsOnMap.setEventsOnboardingShown()
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.ONBOARDING)
                Handler(Looper.getMainLooper()).postDelayed({
                    NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                        UiKitNavigationBarViewVisibilityState.GONE
                    NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                        UiKitToolbarViewState.EXPANDED
                }, 300)
            }

            geoAccessDelegate?.isGeoAccessProvided().isFalse() && mapViewModel.needToShowGeoPopup() -> {
                showGeoPopupDialog(GeoPopupOrigin.MAP)
                mapViewModel.setGeoPopupShown()
            }
        }
    }

    @Suppress("detekt:UnusedPrivateMember")
    private fun checkPopUpShown() {
        if (geoAccessDelegate?.isGeoAccessProvided().isFalse() && mapViewModel.needToShowGeoPopup()) {
            showGeoPopupDialog(GeoPopupOrigin.MAP)
            mapViewModel.setGeoPopupShown()
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
//        userSnippet?.setState(SnippetState.Closed)
        eventSnippetViewController?.closeSnippet()
        mapObjectsDelegate?.focusMapItem(null)
    }

    private fun setNavbarVisible(visible: Boolean) {
        if (isMapOpenInTab) {
            (parentFragment as? MainPostRoadsFragment)?.setNavbarVisible(visible)
        }
    }

    fun closeFriendList() {
        binding?.elwMapFriendsList?.close()
    }

    fun closeConficurationCreate() {
        binding?.ecwMapEventsConfiguration?.setState(EventConfigurationState.Closed)
        mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
    }

    fun showWeatherWidger() {
        binding?.mpiwMapWidget?.visible()
    }

    fun hideWeatherWidget() {
        binding?.mpiwMapWidget?.gone()
    }

    private fun onMarkerClick(marker: Marker) {
        Timber.i("MARKER_CLICK: ${marker.id} ${marker.isVisible}")
        val map = map ?: return
        NavigationManager.getManager().getTopBehaviour(isOnlyMapMode = true)?.state = STATE_HIDDEN
        // TODO: FIX Event list redesign
        binding?.elwMapFriendsList?.close()

        when (marker.zIndex) {

            MeeraMapObjectsDelegate.MAP_USERS_ZINDEX, MeeraMapObjectsDelegate.MAP_FRIENDS_ZINDEX -> {
                if (isUserMarkerClickable(getMapMode()).not()) return
                eventSnippetViewController?.closeSnippet()
                mapViewModel.eventsOnMap.cancelAddEvent()
                mapObjectsDelegate?.findUserMarkerEntry(marker.id)?.let { (user, marker) ->
                    focusMapItem(FocusedMapItem.User(user.id))
                    val targetZoom = getTargetSnippetZoom()
                    updateCameraLocation(
                        location = marker.position,
                        zoom = targetZoom,
                        yOffset = getMapUiValues().userSnippetYOffset
                    ) {
                        showUserSnippet(user)
                    }
                }
            }

            MeeraMapObjectsDelegate.MAP_CLUSTERS_ZINDEX -> {
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

            MeeraMapObjectsDelegate.MAP_EVENTS_ZINDEX, MeeraMapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX -> {
                when (getMapMode()) {
                    is MapMode.Main -> {
                        hideUserSnippet()
                        isCameraMovingToEventConfigurationMyLocation = true
                        binding?.mpiwMapWidget?.gone()
//                        mapViewModel.eventsOnMap.cancelAddEvent()
//                        val change = PointInfoWidgetAllowedVisibilityChange(
//                            factor = MapUiFactor.EVENT_SNIPPET,
//                            allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.COLLAPSED
//                        )
//                        val mapUiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
//                        mapViewModel.handleUiAction(mapUiAction)
                        hideMapControls()
                        mapObjectsDelegate?.findEventMarkerEntry(marker.id)?.let { eventMarkerEntry ->
                            focusMapItem(FocusedMapItem.Event(eventMarkerEntry.event))
                            mapViewModel.eventsOnMap.setSelectedEvent(eventMarkerEntry.event)
                            mapViewModel.handleUiAction(MapUiAction.AnalyticsUiAction.EventSnippetOpenTap)
                        }
                    }

                    is MapMode.EventView -> {
                        hideMapControls()
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
                    showMeOnMap = uiEffect.isShowMeOnMapEnabled,
                    latLng = uiEffect.latLng,
                    showAnon = !mapViewModel.isAuthorized(),
                    moments = uiEffect.moments
                )
            } else {
                myMarker.position = uiEffect.latLng
            }
        } else {
            createMyMarkerOnMap(
                showMeOnMap = uiEffect.isShowMeOnMapEnabled,
                latLng = uiEffect.latLng,
                showAnon = !mapViewModel.isAuthorized(),
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
        showMeOnMap: Boolean,
        latLng: LatLng,
        showAnon: Boolean = true,
        moments: PinMomentsUiModel
    ) {
        if (geoAccessDelegate?.isGeoAccessProvided().isFalse()) return

        Glide.with(this@MainMapFragment)
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

                    if (!showMeOnMap) {
                        avatarView.setPrivateAvatar()
                    }

                    val markerOptions = MarkerOptions()
                    markerOptions.anchor(0.5f, 0.5f)
                    markerOptions.position(latLng)
                    try {
                        markerOptions.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                NGraphics.getBitmapView(
                                    namedAvatarView
                                )
                            )
                        ) // crashed NullPointer
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                    if (myMarker != null)
                        myMarker!!.remove()

                    myMarker = map?.addMarker(markerOptions)
                    myMarker?.zIndex = MeeraMapObjectsDelegate.MAP_MY_ZINDEX
                    avatarView.clear()

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


                        if (!showMeOnMap) {
                            avatarView.setPrivateAvatar()
                        }

                        val markerOptions = MarkerOptions()
                        markerOptions.anchor(0.5f, 0.5f)
                        markerOptions.position(latLng)
                        markerOptions.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                NGraphics.getBitmapView(
                                    namedAvatarView
                                )
                            )
                        )

                        if (myMarker != null)
                            myMarker!!.remove()
                        myMarker = map!!.addMarker(markerOptions)
                        myMarker!!.zIndex = MeeraMapObjectsDelegate.MAP_MY_ZINDEX

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
        NavigationManager.getManager().topNavController.safeNavigate(R.id.mapUserSnippetSheetFragment)
        binding.mpiwMapWidget.setSingleLineWeather(false)
        when {
            getMapMode() is MapMode.UserView -> {
                (fragmentsOfNavHost().firstOrNull() as? MeeraUserSnippetFragment)?.apply {
                    initialize(viewModel = userSnippetViewModel)
                    addListener(this@MainMapFragment)
                }

                (fragmentsOfNavHost().firstOrNull() as? MeeraUserSnippetFragment)?.setUser(
                    selectedUser = selectedUserModel,
                    isAuxSnippet = true
                )
            }

            isMapOpenInTab -> {
                // Задержка из-за того что стек не успевает поменяться до кастинга первого фрагмента к MeeraUserSnippetFragment
                view?.postDelayed({
                    val snippetFragment = fragmentsOfNavHost().firstOrNull() as? MeeraUserSnippetFragment

                    snippetFragment?.apply {
                        addListener(this@MainMapFragment)
                        initialize(
                            viewModel = userSnippetViewModel
                        )
                    }

                    snippetFragment?.setUser(
                        selectedUser = selectedUserModel,
                        isAuxSnippet = false
                    )
                }, BACK_STACK_DELAY)
            }
        }
    }

    private fun fragmentsOfNavHost(): List<Fragment> =
        NavigationManager.getManager().topNavHost.childFragmentManager.fragments

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
        showMeeraLocationEnableDialog(geoPopupOrigin)
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

//    private fun showEventTimePickerDialog(uiModel: TimePickerUiModel) {
//        binding?.ecwMapEventsConfiguration?.setTime(uiModel) { time ->
//            Timber.e("showEventTimePickerDialog called")
//            mapViewModel.eventsOnMap.setSelectedTime(time)
//        }
//
//        currentDialog?.dismiss()
//        currentDialog = TimePickerPopupDialog(
//            activity = requireActivity(),
//            uiModel = uiModel
//        ) { time ->
//            mapViewModel.eventsOnMap.setSelectedTime(time)
//        }.apply {
//            show()
//        }
//    }

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
        val postEditFragment = childFragmentManager.findFragmentById(R.id.fl_map_container) as? MeeraAddPostFragmentNew
        return if (postEditFragment != null) {
            if (!postEditFragment.onBackPressed()) {
                mapViewModel.eventsOnMap.savedUploadPostBundle = postEditFragment.createUploadPostBundle()
                childFragmentManager.beginTransaction()
                    .remove(postEditFragment)
                    .commit()
                mapViewModel.eventsOnMap.setEventConfigurationUiMode(MeeraEventConfigurationUiMode.OPEN)
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
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.map_events_creation_cancel_title)
            .setDescription(R.string.meera_map_events_creation_cancel_message)
            .setTopBtnText(R.string.map_events_creation_cancel_negative)
            .setTopBtnType(ButtonType.FILLED)
            .setCancelable(false)
            .setBottomClickListener {
                isEventCreationFinished = true
                onConfirmed()
            }
            .setBottomBtnText(R.string.map_events_creation_cancel_positive)
            .show(childFragmentManager)
        currentDialog?.dismiss()
//        currentDialog = AlertDialog.Builder(context)
//            .setTitle(R.string.map_events_creation_cancel_title)
//            .setMessage(R.string.map_events_creation_cancel_message)
//            .setPositiveButton(R.string.map_events_creation_cancel_positive) { _, _ ->
//                isEventCreationFinished = true
//                onConfirmed()
//            }
//            .setNegativeButton(R.string.map_events_creation_cancel_negative) { _, _ ->
//
//            }
//            .show()
    }

    private fun setAddEventButtonVisibility(isVisible: Boolean) {
        Timber.e("setAddEventButtonVisibility $isVisible")
    }

    private fun updateMapControls() {
        val binding = binding
        val updatedMapControls = mutableListOf<View>()
        if (getMapMode() == MapMode.Main) {
            updatedMapControls.add(binding.vgMapLayers)
            updatedMapControls.add(binding.ukbMapFriendsList)
        }
        if (isCurrentLocationActive.not()) {
            updatedMapControls.add(binding.ivCurrentLocation)
        }
        if (getMapMode() == MapMode.Main) {
            updatedMapControls.add(binding.ukbMapEventsList)
        }
        mapControls = updatedMapControls
    }

    fun throttleMapAction(action: () -> Unit = {}) {
        val time = SystemClock.elapsedRealtime()
        if (time - lastThrottledActionTime > THROTTLE_DURATION_MS) {
            action.invoke()
            lastThrottledActionTime = time
        }
    }

    private fun isUserMarkerClickable(mapMode: MapMode?): Boolean =
        mapMode is MapMode.Main || (mapMode as? MapMode.UserView)?.isMe.isFalse()

    private fun showMapGradientIfMapMode() {
        if (isMapMode()) binding.tbGradient.visible()
    }

    private fun hideMapGradientIfNotMapMode() {
        if (isMapMode().not()) binding.tbGradient.gone()
    }

    private fun isMapMode() = NavigationManager.getManager().isMapMode

    companion object {

        private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

        const val ARG_EVENT_POST = "ARG_EVENT_POST"
        const val ARG_EVENT_POST_ID = "ARG_EVENT_POST_ID"
        const val ARG_LOG_MAP_OPEN_WHERE = "ARG_LOG_MAP_OPEN_WHERE"

        const val MAP_ZOOM_CLOSE = 15f
        const val MAP_USER_CARD_ZOOM_MAX = 15f
        const val MAP_USER_CARD_ZOOM_MIN = 12f
        const val MAP_ZOOM_EVENT_LOCATION = 17f
        const val MAP_ZOOM_DEFAULT = 15f
        const val MAP_ZOOM_USER_ON_MAP = 12f
        const val MAP_ZOOM_SNIPPET_MAP = 17f

        private const val CAMERA_ANIMATION_DURATION_MS = 450
        private const val CAMERA_ANIMATION_DELAY_MS = 100L

        private const val PIN_Y_OFFSET_FROM_SNIPPET_DP = 48

        private const val EVENT_MARKER_Y_OFFSET_FROM_EVENTS_LISTS_DP = 16

        private const val MY_LOCATION_BOTTOM_MARGIN_MAIN_DP = -4
        private const val MY_LOCATION_BOTTOM_MARGIN_AUX_DP = 195

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


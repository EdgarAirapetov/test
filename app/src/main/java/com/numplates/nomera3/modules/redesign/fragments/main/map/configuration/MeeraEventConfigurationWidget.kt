package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.applyTopRoundedCorners
import com.meera.core.extensions.clearText
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.debouncedAction1
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.newHeight
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewEventConfigurationBinding
import com.numplates.nomera3.modules.feed.ui.getScreenWidth
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.adapter.EventDateAdapter
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventLabelUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationMarkerState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.redesign.fragments.main.MeeraMainContainerFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.events.adapter.MeeraEventTypeAdapter
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraSurveyBottomMenu
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraSurveyBottomMenuMode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

private const val EVENT_TYPE_TAB_INDEX = 0
private const val EVENT_DATE_TAB_INDEX = 1
private const val EVENT_TIME_TAB_INDEX = 2
private const val OPEN_KEYBOARD_DELAY = 400L
private const val APAI_ATTACHMENT_PADDINGS = 32
private const val TOP_CORNER_RADIUS_FOR_IMAGE_VIEW = 0
private const val SCROLL_HORIZONTAL_MARGIN = 16
private const val SCROLL_BOTTOM_MARGIN = 20
private const val SCROLL_OUTLINE_CORNER = 16f
private const val HEIGHT_TOOLBAR = 64
private const val SCROLL_PADDING = 50

class MeeraEventConfigurationWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), IOnBackPressed,
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate() {

    private var currentKeyobardHeight: Int = 0
    private lateinit var tabMediator: TabLayoutMediator
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_event_configuration, this, false)
        .apply(::addView)
        .let(MeeraViewEventConfigurationBinding::bind)
    private var behavior: BottomSheetBehavior<*>? = null
    private var eventTypeAdapter: MeeraEventTypeAdapter? = null
    private var eventDateAdapter: EventDateAdapter? = null

    private var pagerListener: ViewPager2.OnPageChangeCallback? = null

    private val eventLabelUiMapper = EventLabelUiMapper(EventsCommonUiMapper(context))

    private var onEvent: ((EventConfigurationEvent) -> Unit)? = null

    private var state: EventConfigurationState = EventConfigurationState.Closed

    private var currentFragment: MeeraConfigurationStepThirdFragment? = null

    private var adapter: MeeraEventsConfigurationPagerAdapter? = null

    private var debouncedAnimatedPeekHeightUpdate: ((Int) -> Unit)? = null

    private var mapViewModel: MeeraMapViewModel? = null

    private val mainContainerFragment: MeeraMainContainerFragment?
        get() {
            val mapFragment = NavigationManager.getManager().mainMapFragment
            return mapFragment.parentFragment as? MeeraMainContainerFragment?
        }

    private var selectedDate: LocalDate? = null
    private var selectedType: EventType? = null
    private var selectedTime: LocalTime? = null

    private var whoCanComment = WhoCanCommentPostEnum.EVERYONE

    private val disposables = CompositeDisposable()

    init {
        invisible()
        binding.ivMapEventsMyLocation.setThrottledClickListener {
            binding.ivMapEventsMyLocation.clickAnimate()
            onEvent?.invoke(EventConfigurationEvent.MyLocationClicked)
        }
        binding.vMapEventsOverlayTop.setThrottledClickListener {
            close()
        }
        binding.vMapEventsOverlayBottom.setThrottledClickListener {
            close()
        }
        binding.layoutMapEventsOnboarding.ibMapEventsOnboardingClose.setThrottledClickListener {
            close()
        }

        binding.layoutMapEventsOnboarding.tvMapEventsOnboardingCreate.setThrottledClickListener {
            mainContainerFragment?.setToolbarOverlayVisibility(isVisible = false)
            binding.vMapEventsOverlayBottom.gone()
            binding.vMapEventsOverlayTop.gone()
            onEvent?.invoke(EventConfigurationEvent.CreateEventClicked)
        }
        binding.layoutMapEventsConfigurationFirstStep.tvMapEventsConfigurationAbout.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.EventsAboutClicked)
        }
        binding.layoutMapEventsConfiguration.tvMapEventsConfigurationCategory.setThrottledClickListener {

        }

        binding.layoutMapEventsConfiguration.nsvRoot.setHeaderView(R.id.ukrtl_events_lists_main_tabs)

        eventTypeAdapter = MeeraEventTypeAdapter(isOnboarding = true) { item ->
            onEvent?.invoke(EventConfigurationEvent.EventTypeItemSelected(item))
        }
        binding.layoutMapEventsOnboarding.rvMapEventsOnboardingTypes.apply {
            adapter = eventTypeAdapter
            itemAnimator = null
        }

        binding.layoutMapEventsConfiguration.tvMapEventsConfigurationContinue.setThrottledClickListener(500) {
            if (context.getString(R.string.map_layers_events_header_date)
                == binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(
                    EVENT_DATE_TAB_INDEX
                )?.customView?.findViewById<TextView>(
                    com.meera.uikit.R.id.tv_tab_row_title
                )?.text
            ) {
                binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.setCurrentItem(EVENT_DATE_TAB_INDEX, true)
            } else if (context.getString(R.string.map_layers_events_header_time)
                == binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(
                    EVENT_TIME_TAB_INDEX
                )?.customView?.findViewById<TextView>(
                    com.meera.uikit.R.id.tv_tab_row_title
                )?.text
            ) {
                binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.setCurrentItem(EVENT_TIME_TAB_INDEX, true)
            } else {
                initKeyboardBehavior()
                binding.layoutMapEventsConfiguration.nsvRoot
                    .setMargins(SCROLL_HORIZONTAL_MARGIN.dp,0,SCROLL_HORIZONTAL_MARGIN.dp,SCROLL_BOTTOM_MARGIN.dp)
                binding.layoutMapEventsConfiguration.nsvRoot.background = ContextCompat.getDrawable(context, R.drawable.bg_event_config)

                binding.layoutMapEventsConfiguration.nsvRoot.applyRoundedOutline(SCROLL_OUTLINE_CORNER.dp)

                binding.layoutMapEventsConfiguration?.llStepThird?.visible()
                binding.tvSend.gone()
                checkUpload()
                binding.layoutMapEventsConfiguration.rlEventMedia.visible()
                binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.gone()
                binding.layoutMapEventsConfiguration?.ukrtlEventsListsMainTabs
                    ?.setSmallBadgeVisible(
                        binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.selectedTabPosition,
                        false
                    )
                binding.layoutMapEventsConfiguration?.ukrtlEventsListsMainTabs?.showTabIndicator(false)
                binding.layoutMapEventsConfiguration?.ukrtlEventsListsMainTabs?.setupFilledTabsColor()
                binding.layoutMapEventsConfiguration?.frameLayout2?.gone()
                openKeyboardOnEventTitleInput()
//                onEvent?.invoke(EventConfigurationEvent.ConfigurationFinished)
            }
        }
        binding.layoutMapEventsError.tvMapErrorStubAction.setThrottledClickListener {
            onEvent?.invoke(EventConfigurationEvent.RetryClicked)
        }
        binding.layoutConfigStep2Address.root.setThrottledClickListener {
            hideKeyboard()
            onEvent?.invoke(EventConfigurationEvent.CreateEventClicked)
        }

        binding.ecmvMapEventsMarker.setOnAddressClickListener {
            onEvent?.invoke(EventConfigurationEvent.SearchPlaceClicked)
        }
        binding.layoutMapEventsConfigurationFirstStep.tvMapEventsConfigurationContinue.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                onEvent?.invoke(EventConfigurationEvent.ConfigurationStep2Finished)
            }, 100)
        }
        binding.layoutMapEventsConfigurationFirstStep.tvConfigLocation.setOnClickListener {
            onEvent?.invoke(EventConfigurationEvent.SearchPlaceClicked)
        }
        binding.layoutMapEventsConfigurationSecondStep.tvMapEventsConfigurationStep2.setOnClickListener {
            onEvent?.invoke(EventConfigurationEvent.ConfigurationStep2Finished)
        }
        binding?.toolbarContentContainer?.setBackIcon(R.drawable.ic_outlined_close_m)
        binding?.toolbarContentContainer?.backButtonClickListener = {
            close()
        }

        binding.layoutMapEventsConfiguration.ivCommentsSetting.setThrottledClickListener {
//            val where = when {
//                roadType == RoadSelectionEnum.MY -> AmplitudePropertyWhere.SELF_FEED
//                else -> AmplitudePropertyWhere.OTHER
//            }
//            amplitudeHelper.logPostShareSettingsTap(where)
            openCommentsMenu()
        }
        currentFragment = MeeraConfigurationStepThirdFragment()
        binding.layoutMapEventsConfiguration.viewPhotoBg.setOnClickListener {
            showMediaFragment()
        }
        binding.layoutMapEventsConfiguration.ivAttach.setOnClickListener {
            showMediaFragment()
        }
        binding.layoutMapEventsOnboarding.eivEventsOnboardingAboutInfo.rulesOpenListener = {
            onEvent?.invoke(EventConfigurationEvent.RulesOpen)
        }
        onMeasured {
            behavior = createBottomSheetBehavior()
        }

        binding?.tvSend?.setThrottledClickListener {
            handleSendPost()
        }
        setupTextChangedObservable()

        binding.layoutMapEventsConfiguration.apaiAttachment.applyTopRoundedCorners(SCROLL_OUTLINE_CORNER.dp)
    }

    fun resetStep2(currentItem: Int = 0) {
        binding?.root?.hideKeyboard()
        binding.layoutMapEventsConfiguration.nsvRoot.background = null
        binding.layoutMapEventsConfiguration.nsvRoot.setMargins(SCROLL_HORIZONTAL_MARGIN.dp,0,SCROLL_HORIZONTAL_MARGIN.dp,0)
        binding.layoutMapEventsConfiguration?.llStepThird?.gone()
        binding.tvSend.gone()
        binding.layoutMapEventsConfiguration.rlEventMedia.gone()
        binding.tvSend.gone()
        binding.layoutMapEventsConfiguration?.ukrtlEventsListsMainTabs?.showTabIndicator(true)
        binding.layoutMapEventsConfiguration?.configGroup?.visible()
        binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.visible()
        binding.layoutMapEventsConfiguration?.frameLayout2?.visible()
        binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.currentItem = currentItem
    }

    private fun TabLayout.setupFilledTabsColor() {
        for (i in 0 until tabCount) {
            getTabAt(i)?.customView?.findViewById<TextView>(R.id.tv_tab_row_title)?.setTextColor(
                ContextCompat.getColor(context, R.color.uiKitColorForegroundPrimary)
            )
        }
    }

    private fun showMediaFragment() {
        if (currentFragment != null) {
            val fragmentManager = findFragment<Fragment>().childFragmentManager
            fragmentManager.beginTransaction()
                .replace(
                    binding.layoutMapEventsConfigurationSecondStep.step2Container.id,
                    currentFragment!!
                )
                .commit()
            Handler(Looper.getMainLooper()).postDelayed({
                currentFragment?.showMediaPicker()
            }, 200)
        } else {
            currentFragment?.showMediaPicker()
        }
    }

    private fun handleSendPost() {
//        if (addPostViewModel.isAlreadyUploading()) {
//            showPostCantBePublishedYetDialog()
//        } else {
        if (canPublish()) {
            context?.hideKeyboard(rootView)
            binding.layoutMapEventsConfiguration.etWrite.suggestionMenu?.dismiss()
            binding.layoutMapEventsConfiguration.etAddPostTitle.suggestionMenu?.dismiss()
            onEvent?.invoke(
                EventConfigurationEvent.MeeraConfigurationFinished(
                    title = binding.layoutMapEventsConfiguration.etAddPostTitle.text.toString(),
                    subtitle = binding.layoutMapEventsConfiguration.etWrite.text.toString(),
                    whoCanComment = whoCanComment,
                    roadType = RoadSelectionEnum.MAIN
                )
            )
        } else {
//            resetSendButton()
        }
//        }
    }

    private fun openCommentsMenu() {
        val menu = MeeraSurveyBottomMenu()
        menu.mode = MeeraSurveyBottomMenuMode.EVENT
        menu.commentsState = whoCanComment
        menu.allClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.EVERYONE
            checkCommentSettingsIndicator()
        }

        menu.noOneClickedListener = {
            whoCanComment = WhoCanCommentPostEnum.NOBODY
            checkCommentSettingsIndicator()
        }

        menu.friendsClickedListener = {
            whoCanComment =
                if (menu.isCommunityCommentingOptionMode) WhoCanCommentPostEnum.COMMUNITY_MEMBERS else WhoCanCommentPostEnum.FRIENDS
            checkCommentSettingsIndicator()
        }

        menu.show(findFragment<Fragment>().childFragmentManager)
    }

    private fun checkCommentSettingsIndicator() {
        val visibleCommentIndicator = whoCanComment.state != WhoCanCommentPostEnum.EVERYONE.state
        binding.layoutMapEventsConfiguration.vCommentSettingIndicator.setVisible(visibleCommentIndicator)
    }

    private fun openKeyboardOnEventTitleInput() {
        Timber.d("openKeyboard")
        doDelayed(OPEN_KEYBOARD_DELAY) {
            binding.layoutMapEventsConfiguration.etAddPostTitle.let(::doOpenKeyboard)
        }
    }

    private fun doOpenKeyboard(inputView: EditText) {
        inputView.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        debouncedAnimatedPeekHeightUpdate = binding.root.findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?.debouncedAction1(PEEK_HEIGHT_DEBOUNCE_DURATION_MS) { peekHeight ->
                behavior?.setPeekHeight(peekHeight, true)
            }
    }

    private fun initKeyboardBehavior() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            binding.layoutMapEventsConfiguration.rlEventMedia.isVisible = imeVisible
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            currentKeyobardHeight = imeHeight
            binding.layoutMapEventsConfiguration.rlEventMedia.setMargins(bottom = if (imeVisible) imeHeight else 0)
            insets
        }
    }

    private fun setupTextChangedObservable() {
        binding.layoutMapEventsConfiguration.etAddPostTitle?.let { headerInputText ->
            disposables.add(
                RxTextView.textChanges(headerInputText)
                    .debounce(150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            scrollConfigText(binding.layoutMapEventsConfiguration.nsvRoot, binding.layoutMapEventsConfiguration.etAddPostTitle)
                            checkUpload()
                        },
                        { Timber.e(it) }
                    )
            )
        }
        binding.layoutMapEventsConfiguration.etWrite?.let { inputText ->
            disposables.add(
                RxTextView.textChanges(inputText)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->
                        scrollConfigText(binding.layoutMapEventsConfiguration.nsvRoot, binding.layoutMapEventsConfiguration.etWrite)
//                        newPostFormatter?.onPostChanged()
                    }, { Timber.e(it) })
            )

            disposables.add(
                RxTextView.textChanges(inputText)
                    .debounce(150, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ text ->
                        checkUpload()
                    }, { Timber.e(it) })
            )
        }
    }

    private fun scrollConfigText(sv: NestedScrollView, et: EditText) {
        getHeightToCursorWithScroll(et, sv)
    }

    private fun getHeightToCursorWithScroll(editText: EditText, nestedScrollView: NestedScrollView) {
        nestedScrollView.post {
            val scrollTo = editText.bottom - nestedScrollView.height + SCROLL_PADDING + binding.layoutMapEventsConfiguration.stubView.height
            nestedScrollView.smoothScrollTo(0, scrollTo)
        }
    }

    private fun checkUpload() {
        if (canPublish().not()) {
            binding?.tvSend?.isEnabled = false
            binding.tvSend.gone()
        } else {
            binding.tvSend.isEnabled = true
            binding.tvSend.visible()
        }
    }

    private fun canPublish(): Boolean {
        val hasContent = binding.layoutMapEventsConfiguration.etWrite.text?.trim()?.isNotEmpty() == true
        val hasTitle = binding.layoutMapEventsConfiguration.etAddPostTitle.text?.trim()?.isNotEmpty() == true
        return hasContent
            && hasTitle
    }

    override fun onBackPressed(): Boolean {
        return if (state != EventConfigurationState.Closed) {
            close()
            true
        } else {
            false
        }
    }

    fun setEventListener(onEvent: (EventConfigurationEvent) -> Unit, meeraMapViewModel: MeeraMapViewModel) {
        this.onEvent = onEvent
        mapViewModel = meeraMapViewModel
        findViewTreeLifecycleOwner()?.let {
            (mapViewModel?.eventsOnMap as MeeraEventsOnMapImpl).selectedEventTypeFlow.asLiveData()
                ?.observe(it) { type ->
                    selectedType = type
                    setTabTitle()
                }
        }
        findViewTreeLifecycleOwner()?.let {
            (mapViewModel?.eventsOnMap as MeeraEventsOnMapImpl).selectedDateFlow.asLiveData().observe(it) { date ->
                selectedDate = date
                setTabTitle()
            }
        }
        findViewTreeLifecycleOwner()?.let {
            (mapViewModel?.eventsOnMap as MeeraEventsOnMapImpl).selectedTimeFlow.asLiveData().observe(it) { time ->
                selectedTime = time
//                time?.let { time -> adapter?.setTimeConfiguretion(time, null) }
                setTabTitle()
            }
        }
    }

    private fun setTabTitle() {
        when (binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.selectedTabPosition) {
            EVENT_TYPE_TAB_INDEX -> {
                if (binding.layoutMapEventsConfiguration.llStepThird.isVisible) {
                    resetStep2(EVENT_TYPE_TAB_INDEX)
                }
                selectedType?.let { type ->
                    binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_TYPE_TAB_INDEX)?.customView?.findViewById<TextView>(
                        com.meera.uikit.R.id.tv_tab_row_title
                    )?.apply {
                        text = context.getString(eventLabelUiMapper.mapEventToText(type))
                    }
                }
            }

            EVENT_DATE_TAB_INDEX -> {
                if (binding.layoutMapEventsConfiguration.llStepThird.isVisible) {
                    resetStep2(EVENT_DATE_TAB_INDEX)
                }
                selectedDate?.let {
                    binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_DATE_TAB_INDEX)?.customView?.findViewById<TextView>(
                        com.meera.uikit.R.id.tv_tab_row_title
                    )?.apply {
                        text = eventLabelUiMapper.mapDateToShortPattern(it)
                    }
                }
                binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_DATE_TAB_INDEX)?.view?.isEnabled =
                    true
            }

            EVENT_TIME_TAB_INDEX -> {
                if (binding.layoutMapEventsConfiguration.llStepThird.isVisible) {
                    resetStep2(EVENT_TIME_TAB_INDEX)
                }
                selectedTime?.let {
                    binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_TIME_TAB_INDEX)?.customView?.findViewById<TextView>(
                        com.meera.uikit.R.id.tv_tab_row_title
                    )?.apply {
                        text = it.toString()
                    }
                    adapter?.setTimeConfiguretion(it, null)
                    binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_TIME_TAB_INDEX)?.view?.isEnabled =
                        true
                }
            }
        }
    }

    fun getState(): EventConfigurationState = state
    fun getBehaviorState() = behavior?.state

    fun setState(newState: EventConfigurationState) {
        if (state == newState) return
        when {
            state is EventConfigurationState.StepFirstConfiguration &&
                newState is EventConfigurationState.StepFirstConfiguration
                && (state as EventConfigurationState.StepFirstConfiguration).markerState == newState.markerState ->
                    return
        }

        when (newState) {
            is EventConfigurationState.Onboarding -> {
                visible()
                eventTypeAdapter?.submitList(newState.eventTypeItems)
                adapter?.submitTypeItems(newState.eventTypeItems)
                showOnboarding()
            }

            EventConfigurationState.Closed -> {
                clearPreviousEventData()
                behavior?.state = BottomSheetBehavior.STATE_HIDDEN
                if (state !is EventConfigurationState.Onboarding) {
                    animateTopBar(false)
                    animateControlsUi(false)
                }
            }

            is EventConfigurationState.StepFirstConfiguration -> {
                isInvisible = newState.isHidden
                eventTypeAdapter?.submitList(newState.eventTypeItems)
                adapter?.submitTypeItems(newState.eventTypeItems)
                adapter?.submitDateItems(newState.eventDateItems)

                eventDateAdapter?.submitList(newState.eventDateItems)
                binding.ecmvMapEventsMarker.setState(newState.markerState)
                binding.layoutMapEventsConfigurationFirstStep.tvConfigLocation.text =
                    (newState.markerState as? EventConfigurationMarkerState.Address)?.markerAddress
                if (state !is EventConfigurationState.StepFirstConfiguration) {
                    animateConfigurationUiEnter(state is EventConfigurationState.Onboarding, ConfigState.STEP1)
                }
                if (newState.markerState == EventConfigurationMarkerState.Error) {
                    setViewState(ConfigState.ERROR)
                } else {
                    setViewState(ConfigState.STEP1)
                }
                (state as? EventConfigurationState.StepFirstConfiguration)?.let { oldState ->
                    if (newState.markerState.isLevitating != oldState.markerState.isLevitating) {
                        animateTopBar(!newState.markerState.isLevitating)
                        val height = if (newState.markerState.isLevitating) {
                            0
                        } else {
                            binding.layoutMapEventsConfigurationFirstStep.root.height
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

            is EventConfigurationState.Step2 -> {
                binding.layoutConfigStep2Address.tvStep2Location.text =
                    (newState.markerState as? EventConfigurationMarkerState.Address)?.markerAddress
                setViewState(ConfigState.STEP2)
            }

            is EventConfigurationState.Configuration -> {
                isInvisible = newState.isHidden
                eventTypeAdapter?.submitList(newState.eventTypeItems)
                adapter?.submitTypeItems(newState.eventTypeItems)
                adapter?.submitDateItems(newState.eventDateItems)
                binding.layoutConfigStep2Address.tvStep2Location.text =
                    (newState.markerState as? EventConfigurationMarkerState.Address)?.markerAddress
                eventDateAdapter?.submitList(newState.eventDateItems)
                binding.layoutMapEventsConfiguration?.clConfigPhotoPlaceholder?.isVisible =
                    !binding.layoutMapEventsConfiguration.apaiAttachment.isVisible

                binding.ecmvMapEventsMarker.setState(newState.markerState)
                binding.layoutMapEventsConfiguration.tvMapEventsConfigurationContinue.isEnabled =
                    newState.isContinueEnabled

//                if (state !is EventConfigurationState.Configuration) {
//                    animateConfigurationUiEnter(state is EventConfigurationState.Onboarding, ConfigState.CONFIGURATION)
//                }
//                if (newState.markerState == EventConfigurationMarkerState.Error) {
//                    setViewState(ConfigState.ERROR)
//                } else {
//                    setViewState(ConfigState.CONFIGURATION)
//                }
                setViewState(ConfigState.CONFIGURATION)

            }

            EventConfigurationState.UploadingStarted -> {
                binding.tvSend.gone()
                binding.layoutMapEventsConfiguration.rlEventMedia.gone()
                binding.pbSendPost.visible()
            }

            EventConfigurationState.Empty -> Unit
        }
        state = newState
    }


    fun getEventMarkerPositionRelative(parentView: View): Point =
        binding.ecmvMapEventsMarker.getTipPositionRelative(parentView)

    private fun createBottomSheetBehavior(): BottomSheetBehavior<*> {
        var draggedByUser = false
        return BottomSheetBehavior.from(findViewById(R.id.vg_map_events_configuration_bottomsheet)).apply {
            isHideable = true
            isFitToContents = true
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            binding.layoutMapEventsConfiguration.nsvRoot.background = null
                            binding.layoutMapEventsConfiguration.nsvRoot
                                .setMargins(SCROLL_HORIZONTAL_MARGIN.dp,0, SCROLL_HORIZONTAL_MARGIN.dp,0)
                            binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.setCurrentItem(0, false)
                            binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_DATE_TAB_INDEX)?.view?.isEnabled =
                                false
                            binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_TIME_TAB_INDEX)?.view?.isEnabled =
                                false
                            binding.layoutMapEventsConfiguration.rlEventMedia.gone()
                            mainContainerFragment?.setToolbarOverlayVisibility(isVisible = false)
                            binding.tvSend.gone()
                            binding.pbSendPost.gone()
                            binding.tvSend.isEnabled = false
                            binding.layoutMapEventsConfiguration?.frameLayout2?.visible()

                            binding.layoutMapEventsConfiguration?.llStepThird?.gone()
                            binding.tvSend.isEnabled = false

                            binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.visible()
                            binding.layoutMapEventsConfiguration?.ukrtlEventsListsMainTabs?.showTabIndicator(true)
                            binding.layoutMapEventsConfiguration?.configGroup?.visible()

                            resetTabNames()
                            invisible()
                            if (draggedByUser) {
                                onEvent?.invoke(EventConfigurationEvent.UiCloseInitiated)
                            }
                        }

                        BottomSheetBehavior.STATE_EXPANDED -> {
                            if (this@MeeraEventConfigurationWidget.state is EventConfigurationState.Step2) {
                                binding.layoutMapEventsConfigurationSecondStep.tvMapEventsConfigurationStep2.gone()
                            }
                        }

                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            if (this@MeeraEventConfigurationWidget.state is EventConfigurationState.Step2) {
                                binding.layoutMapEventsConfigurationSecondStep.tvMapEventsConfigurationStep2.visible()
                                binding.layoutMapEventsConfigurationSecondStep.step2Container.layoutParams.height =
                                    144.dp
                            }
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

    @Suppress("DEPRECATION", "UNUSED_VARIABLE")
    private fun getContainerHeight(): Int = binding.clCoordinatorRoot.height

    private fun resetTabNames() {
        binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_TIME_TAB_INDEX)?.customView?.findViewById<TextView>(
            com.meera.uikit.R.id.tv_tab_row_title
        )?.apply {
            text = context.getString(R.string.map_layers_events_header_time)
        }
        binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs.getTabAt(EVENT_DATE_TAB_INDEX)?.customView?.findViewById<TextView>(
            com.meera.uikit.R.id.tv_tab_row_title
        )?.apply {
            text = context.getString(R.string.map_layers_events_header_date)
        }
    }

    private fun close() {
        onEvent?.invoke(EventConfigurationEvent.UiCloseInitiated)
        binding.layoutMapEventsConfiguration?.apaiAttachment?.resetView()
        binding.layoutMapEventsConfiguration?.apaiAttachment?.gone()
    }

    private fun showOnboarding() {
        val viewHeight = findViewById<View>(R.id.layout_map_events_onboarding).height
        behavior?.maxHeight = viewHeight
        behavior?.peekHeight = viewHeight
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        behavior?.skipCollapsed = true
        behavior?.isDraggable = true
        binding.ivMapEventsMyLocation.invisible()
        binding.vgMapEventsTopBar.invisible()
        binding.layoutMapEventsOnboarding.root.visible()
        binding.layoutMapEventsConfiguration.configEventClRoot.invisible()
        binding.layoutMapEventsError.root.invisible()
        binding.layoutMapEventsConfigurationFirstStep.root.invisible()
        binding.vgMapEventsControlsContainer.invisible()
        setupMapEventsOverlay()
    }

    private fun setupMapEventsOverlay() {
        binding.vMapEventsOverlayTop.newHeight(context.getStatusBarHeight())
        binding.vMapEventsOverlayBottom.setMargins(top = context.getStatusBarHeight() + HEIGHT_TOOLBAR.dp)
        binding.vMapEventsOverlayTop.visible()
        binding.vMapEventsOverlayBottom.visible()
        mainContainerFragment?.setToolbarOverlayVisibility(isVisible = true)
    }

    private fun animateConfigurationUiEnter(fromOnboarding: Boolean, finalState: ConfigState) {
        binding.vMapEventsOverlayBottom.gone()
        binding.vMapEventsOverlayTop.gone()
        mainContainerFragment?.setToolbarOverlayVisibility(isVisible = false)
        animateTopBar(true)
        animateControlsUi(true)
        if (fromOnboarding) {
            animateOnboardingUiExit()
            animateConfigurationUiEnter()
            binding.vgMapEventsConfigurationBottomsheet.post {
                behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        } else {
            setViewState(finalState)
            behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private var lastConfigState: ConfigState? = null

    private fun setViewState(state: ConfigState) {
        if (lastConfigState == state) return
        binding.layoutMapEventsOnboarding.root.invisible()
        binding.layoutMapEventsConfiguration.root.isVisible = state == ConfigState.CONFIGURATION
        binding.layoutMapEventsError.root.isVisible = state == ConfigState.ERROR
        binding.layoutMapEventsConfigurationFirstStep.root.isVisible = state == ConfigState.STEP1
        binding.layoutConfigStep2Stub.root.isVisible = state == ConfigState.STEP2
        binding.layoutConfigStep2Address.root.isVisible = state != ConfigState.STEP1
        binding.ivMapEventsMyLocation.isVisible = state == ConfigState.STEP1
        behavior?.isDraggable = state == ConfigState.STEP2
        binding.vgMapEventsControlsContainer.isVisible = state == ConfigState.STEP1

        if (state == ConfigState.CONFIGURATION) {
            behavior?.peekHeight = getContainerHeight()
        }

        if (state == ConfigState.STEP1) {
            behavior?.peekHeight = binding.layoutMapEventsConfigurationFirstStep.root.height
        }
        lastConfigState = state
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

    fun setFragmentManager() {
        pagerListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageSelected(position: Int) {
                setTabTitle()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
        }
        adapter = MeeraEventsConfigurationPagerAdapter(findFragment(), onEvent)
        val roadFragments =
            mutableListOf(
                MeeraEventConfigurationTypeFragment(),
                MeeraConfigurationDateFragment(),
                MeeraConfigurationTimeFragment(),
            )
        adapter?.addPeopleOnboardingFragments(
            roadFragments, listOf(
                context.getString(R.string.map_events_type_education),
                context.getString(R.string.map_layers_events_header_date),
                context.getString(R.string.map_layers_events_header_time),
            )
        )

        binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.offscreenPageLimit = 3
        binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.adapter = adapter
        binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.isUserInputEnabled = false
        createTabs()

        pagerListener?.let {
            binding.layoutMapEventsConfiguration?.vpEventsListsMainPages?.registerOnPageChangeCallback(
                it
            )
        }
    }

    private fun createTabs() {
        tabMediator = TabLayoutMediator(
            binding.layoutMapEventsConfiguration.ukrtlEventsListsMainTabs,
            binding.layoutMapEventsConfiguration.vpEventsListsMainPages
        ) { tab, position ->
            tab.text = adapter?.getName(position)
            setTabTitle()
        }
        binding?.layoutMapEventsConfiguration?.ukrtlEventsListsMainTabs?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) = Unit

            override fun onTabUnselected(p0: TabLayout.Tab?) = Unit

            override fun onTabReselected(p0: TabLayout.Tab?) {
                if (binding.layoutMapEventsConfiguration.llStepThird.isVisible) {
                    resetStep2(p0?.position ?: 0)
                }
            }
        })
        tabMediator.attach()
    }

    fun submitAttachment(attachment: List<UIAttachmentPostModel>) {
        if (attachment.isEmpty()) {
            binding.layoutMapEventsConfiguration?.clConfigPhotoPlaceholder?.visible()
            binding.layoutMapEventsConfiguration?.apaiAttachment?.resetView()
            binding.layoutMapEventsConfiguration?.apaiAttachment?.gone()
            binding.layoutMapEventsConfiguration.vAttachmentBg.gone()
            return
        } else {
            binding.layoutMapEventsConfiguration?.apaiAttachment?.resetView()
            binding.layoutMapEventsConfiguration?.apaiAttachment?.visible()
            binding.layoutMapEventsConfiguration.vAttachmentBg.visible()
            binding.layoutMapEventsConfiguration?.clConfigPhotoPlaceholder?.gone()
        }
        mapViewModel?.let {
            binding.layoutMapEventsConfiguration?.apaiAttachment?.bind(
                actions = it,
                attachment = attachment.first(),
                mediaPreviewMaxWidth = getScreenWidth() - APAI_ATTACHMENT_PADDINGS.dp,
                mediaPreviewMaxHeight = 0,
                isNeedMediaPositioning = false,
                cornerRadius = TOP_CORNER_RADIUS_FOR_IMAGE_VIEW
            )
        }
        currentFragment?.hidePicker()
    }

    fun openEditor(uri: Uri?) {
        uri?.let { currentFragment?.openEditor(it) }
    }

    fun openPhoto(photo: String) {
        currentFragment?.openPhoto(photo)
    }

    fun mediaAttachmentSelected(uri: Uri) {
        currentFragment?.mediaAttachmentSelected(uri)
    }

    private fun clearPreviousEventData(){
        binding.layoutMapEventsConfiguration.clConfigPhotoPlaceholder.visible()
        binding.layoutMapEventsConfiguration.etWrite.clearText()
        binding.layoutMapEventsConfiguration.etAddPostTitle.clearText()
        binding.layoutMapEventsConfiguration.apaiAttachment.resetView()
        binding.layoutMapEventsConfiguration.apaiAttachment.gone()
        binding.layoutMapEventsConfiguration.vAttachmentBg.gone()
    }

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
        private const val PEEK_HEIGHT_DEBOUNCE_DURATION_MS = 100L
    }
}

private enum class ConfigState {
    STEP1,
    STEP2,
    ONBOARDING,
    ERROR,
    CONFIGURATION
}

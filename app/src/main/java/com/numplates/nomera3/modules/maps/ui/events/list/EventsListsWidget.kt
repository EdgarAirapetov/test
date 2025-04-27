package com.numplates.nomera3.modules.maps.ui.events.list

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.meera.core.extensions.gone
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventsListsBinding
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.adapter.EventsListsPagerAdapter
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventFiltersUpdateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPageContent
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha

class EventsListsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), IOnBackPressed {

    var uiActionListener: ((MapUiAction.EventsListUiAction) -> Unit)? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_events_lists, this, false)
        .apply(::addView)
        .let(ViewEventsListsBinding::bind)

    private var eventPostBehavior: BottomSheetBehavior<View>? = null
    private var eventsListsBehavior: ViewPagerBottomSheetBehavior<View>? = null

    private val adapter = EventsListsPagerAdapter { uiAction ->
        uiActionListener?.invoke(uiAction)
    }

    private var checkedIndex = DEFAULT_PARTICIPATION_INDEX
    private var uiModel: EventsListsUiModel? = null
    private var interceptTouchEvents = false
    private var contentFragment: PostFragmentV2? = null

    init {
        binding.layoutEventsListsMain.vpEventsListsMainPages.adapter = adapter
        binding.layoutEventsListsMain.vpEventsListsMainPages.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT

        binding.layoutEventsListsMain.ivEventsListsMainFilters.setThrottledClickListener {
            binding.layoutEventsListsMain.root.gone()
            binding.layoutEventsListsFilters.root.visible()
            binding.llUkscEventsListsFilterParticipation.gone()
        }

        binding.layoutEventsListsFilters.ivEventsListsFiltersBack.setThrottledClickListener {
            binding.layoutEventsListsMain.root.visible()
            binding.layoutEventsListsFilters.root.gone()
            handleParticipationSwitchVisibility()
            handleEventFiltersClosed()
        }

        binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.filterChangeListener = {
            handleEventFiltersUpdate()
        }

        binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.filterChangeListener = {
            handleEventFiltersUpdate()
        }

        binding.layoutEventsListsMain.ukrtlEventsListsMainTabs
            .setupWithViewPager(binding.layoutEventsListsMain.vpEventsListsMainPages)

        binding.layoutEventsListsMain.vpEventsListsMainPages.addOnPageChangeListener(
            onPageSelected = { index ->
                uiActionListener?.invoke(MapUiAction.EventsListUiAction.SelectedPageChanged(index))
            }
        )

        binding.ukscEventsListsFilterParticipation.onCheckedIndexChangeListener = { index ->
            uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventParticipationCategoryChanged(index))
            binding.ukscEventsListsFilterParticipation.isGroupEnabled(false)
        }

        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.setThrottledClickListener {
            binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.clear()
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.clear()
        }

        setupEventPostBottomSheetUi()
        onMeasured {
            createEventsListsBottomSheetBehaviour()
            createEventPostBottomSheetBehaviour()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return interceptTouchEvents || super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return interceptTouchEvents || super.onTouchEvent(event)
    }

    override fun onBackPressed(): Boolean {
        return when {
            eventPostBehavior?.state == BottomSheetBehavior.STATE_EXPANDED -> {
                eventPostBehavior?.isDraggable = false
                eventPostBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                true
            }

            eventsListsBehavior?.state == ViewPagerBottomSheetBehavior.STATE_EXPANDED -> {
                eventsListsBehavior?.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
                true
            }

            else -> false
        }
    }

    fun onScreenshotTaken() {
        contentFragment?.onScreenshotTaken()
    }

    fun selectItem(eventsListType: EventsListType, item: EventsListItem) {
        adapter.selectItem(eventsListType = eventsListType, item = item)
    }

    fun getState() = eventsListsBehavior?.state

    fun setUiModel(uiModel: EventsListsUiModel) {
        if (uiModel.eventsListsPages.isEmpty()) return
        this.uiModel = uiModel
        val selectedPage = uiModel.eventsListsPages[uiModel.selectedPageIndex]
        handleEventFilterType(selectedPage.filters.eventFilterType)
        handleEventFilterDate(selectedPage.filters.eventFilterDate)
        handleEventParticipationCategory(selectedPage.filters.participationCategoryIndex)
        handleFiltersNonDefault(selectedPage.filters.nonDefaultFilters)
        adapter.setEventListPages(uiModel.eventsListsPages)
        binding.layoutEventsListsMain.vpEventsListsMainPages.currentItem = uiModel.selectedPageIndex
        val isStub = selectedPage.eventsListItems.items.any { it is EventsListItem.StubItemUiModel }
        binding.ukscEventsListsFilterParticipation.isGroupEnabled(!isStub)
    }

    fun open() {
        eventsListsBehavior?.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED
    }

    fun close() {
        eventPostBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        eventsListsBehavior?.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
    }

    fun openEventPost(eventPost: PostUIEntity) {
        contentFragment = PostFragmentV2().apply {
            arguments = Bundle().apply {
                putLong(IArgContainer.ARG_FEED_POST_ID, eventPost.postId)
                putBoolean(IArgContainer.ARG_FEED_POST_NEED_TO_UPDATE, true)
                putParcelable(IArgContainer.ARG_FEED_POST, eventPost)
            }
        }
        val contentFragment = contentFragment ?: return
        contentFragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                if (contentFragment.isFragmentStarted.not()) {
                    contentFragment.onStartFragment()
                    binding.llUkscEventsListsFilterParticipation.gone()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (contentFragment.isFragmentStarted) {
                    contentFragment.onStopFragment()
                    if (uiModel?.selectedPageIndex != 0) {
                        binding.llUkscEventsListsFilterParticipation.visible()
                    }
                }
                super.onStop(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                this@EventsListsWidget.contentFragment = null
                super.onDestroy(owner)
            }
        })
        findFragment<Fragment>().childFragmentManager.beginTransaction()
            .replace(R.id.vg_event_post, contentFragment)
            .runOnCommit {
                binding.vgEventPost.alpha = ALPHA_INVISIBLE
                binding.vgEventPost.visible()
                binding.vgEventPost.animateAlpha(
                    from = ALPHA_INVISIBLE,
                    to = ALPHA_VISIBLE,
                    duration = EVENT_POST_ENTER_ANIM_DURATION_MS
                ) {
                    eventPostBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
            .commit()
    }

    fun closeEventPost() {
        eventPostBehavior?.isDraggable = false
        eventPostBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun handleParticipationSwitchVisibility() {
        val uiModel = this.uiModel ?: return
        val selectedPage = uiModel.eventsListsPages[uiModel.selectedPageIndex]
        binding.llUkscEventsListsFilterParticipation.isVisible = selectedPage.filters.participationCategoryIndex != null
    }

    private fun handleEventFiltersClosed() {
        val eventFilterUpdate = EventFiltersUpdateUiModel(
            eventFilterType = binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.getUiModel(),
            eventFilterDate = binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.getUiModel()
        )
        uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventFiltersChanged(eventFilterUpdate))
    }

    private fun handleEventFiltersUpdate() {
        val filtersInDefault = binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.isDefault()
            && binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.isDefault()
        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.isEnabled = filtersInDefault.not()
    }

    private fun handleEventFilterType(eventFilterType: EventFilterTypeUiModel) =
        binding.layoutEventsListsFilters.efwEventsListsFilterTypeWidget.setUiModel(eventFilterType)

    private fun handleEventFilterDate(eventFilterDate: EventFilterDateUiModel?) {
        if (eventFilterDate != null) {
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.setUiModel(eventFilterDate)
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.visible()
        } else {
            binding.layoutEventsListsFilters.efwEventsListsFilterDateWidget.gone()
        }
    }

    private fun handleEventParticipationCategory(participationCategoryIndex: Int?) {
        if (participationCategoryIndex != null) {
            binding.llUkscEventsListsFilterParticipation.visible()
            if (checkedIndex != participationCategoryIndex) {
                checkedIndex = participationCategoryIndex
                binding.ukscEventsListsFilterParticipation.setCheckedSegmentByIndex(participationCategoryIndex)
            }
        } else {
            binding.llUkscEventsListsFilterParticipation.gone()
        }
    }

    private fun handleFiltersNonDefault(nonDefaultFilters: Boolean) {
        binding.layoutEventsListsMain.vEventsListsMainFiltersNonDefault.isVisible = nonDefaultFilters
        binding.layoutEventsListsFilters.tvEventsListsFiltersReset.isEnabled = nonDefaultFilters
    }

    private fun createEventsListsBottomSheetBehaviour() {
        eventsListsBehavior = ViewPagerBottomSheetBehavior.from<View>(binding.vgEventsListsBottomsheet).apply {
            isHideable = true
            skipCollapsed = true
            state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        ViewPagerBottomSheetBehavior.STATE_DRAGGING -> {
                            binding.llUkscEventsListsFilterParticipation.gone()
                        }

                        ViewPagerBottomSheetBehavior.STATE_EXPANDED -> {
                            if (uiModel?.selectedPageIndex != 0 && uiModel?.selectedPageIndex != null) {
                                binding.llUkscEventsListsFilterParticipation.visible()
                            }
                        }

                        ViewPagerBottomSheetBehavior.STATE_HIDDEN -> {
                            binding.layoutEventsListsMain.root.visible()
                            binding.layoutEventsListsFilters.root.gone()
                            uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventsListsClosed)
                        }
                    }
                }

                override fun onSlide(view: View, offset: Float) = Unit
            })
        }
    }

    private fun setupEventPostBottomSheetUi() {
        binding.vgEventPost.gone()
        binding.vgEventPost.apply {
            val cornerSizePx = resources.getDimension(R.dimen.corner_radius_large)
            shapeAppearanceModel = shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerSizePx)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerSizePx)
                .setBottomLeftCornerSize(0f)
                .setBottomRightCornerSize(0f)
                .build()
            interceptTouchEvents = false
        }
    }

    private fun createEventPostBottomSheetBehaviour() {
        eventPostBehavior = BottomSheetBehavior.from<View>(binding.vgEventPost).apply {
            skipCollapsed = false
            peekHeight = resources.getDimensionPixelSize(R.dimen.map_events_lists_height)
            state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    val fragmentManager = findFragment<Fragment>().childFragmentManager
                    val snippetState = SnippetState.fromBehaviorValue(newState)
                    (fragmentManager.findFragmentById(R.id.vg_event_post) as? MapSnippetPageContent)
                        ?.onSnippetStateChanged(snippetState)
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> eventPostBehavior?.isDraggable = true
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            interceptTouchEvents = true
                            eventPostBehavior?.isDraggable = false
                            binding.vgEventPost.animateAlpha(
                                from = ALPHA_VISIBLE,
                                to = ALPHA_INVISIBLE,
                                duration = EVENT_POST_EXIT_ANIM_DURATION_MS
                            ) {
                                binding.vgEventPost.gone()
                                interceptTouchEvents = false
                                fragmentManager.findFragmentById(R.id.vg_event_post)
                                    ?.let { contentFragment ->
                                        fragmentManager.beginTransaction()
                                            .remove(contentFragment)
                                            .commit()
                                    }
                            }
                            uiActionListener?.invoke(MapUiAction.EventsListUiAction.EventPostClosed)
                        }

                        else -> Unit
                    }
                }

                override fun onSlide(view: View, offset: Float) = Unit
            })
        }
    }

    companion object {
        private const val OFFSCREEN_PAGE_LIMIT = 2
        private const val DEFAULT_PARTICIPATION_INDEX = 0
        private const val ALPHA_VISIBLE = 1f
        private const val ALPHA_INVISIBLE = 0f
        private const val EVENT_POST_ENTER_ANIM_DURATION_MS = 500L
        private const val EVENT_POST_EXIT_ANIM_DURATION_MS = 300L
    }
}

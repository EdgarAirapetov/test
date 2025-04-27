package com.numplates.nomera3.modules.maps.ui.events.snippet

import androidx.fragment.app.Fragment
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.MapParametersCache
import com.numplates.nomera3.modules.maps.ui.events.EventsOnMap
import com.numplates.nomera3.modules.maps.ui.events.adapter.EventPostsPagerAdapter
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetDataUiState
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetItem
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetViewPager
import com.numplates.nomera3.modules.maps.ui.view.MapUiController
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener

class EventSnippetViewController(
    mapParametersCache: MapParametersCache,
    private val fragment: Fragment,
    private val viewPager: MapSnippetViewPager,
    private val eventsOnMap: EventsOnMap,
    private val mapUiController: MapUiController,
    mapMode: MapMode
): IOnBackPressed {

    private var needToAnimatePageEnter = true
    private var dataUiState: EventSnippetDataUiState = EventSnippetDataUiState.Empty
    private val eventPostMeasuringUtil = EventPostMeasuringUtil(
        fragment = fragment,
        settings = eventsOnMap.getSettings()
    )
    private var auxEventPostDeleted = false

    init {
        viewPager.gone()
        viewPager.pageMargin = PAGE_MARGIN_DP.dp
        viewPager.adapter = EventPostsPagerAdapter(
            fragment.childFragmentManager,
            mapParametersCache
        )
        viewPager.endOverscrollCallback = {
            eventsOnMap.getNextEventSnippetPage()
        }
        viewPager.addOnPageChangeListener(
            onPageSelected = { pageIndex ->
                eventsOnMap.logMapEventSnippetOpen(AmplitudePropertyMapSnippetOpenType.SWIPE)
                viewPager.post { handlePageChange(pageIndex) }
            }
        )
        when (mapMode) {
            is MapMode.Main -> eventsOnMap.liveEventSnippetDataUiState
                .observe(fragment.viewLifecycleOwner, ::handleEventSnippetUiModel)
            is MapMode.EventView -> eventsOnMap.liveAuxEventSnippetDataUiState
                .observe(fragment.viewLifecycleOwner, ::handleEventSnippetUiModel)
            else -> Unit
        }
    }

    override fun onBackPressed(): Boolean {
        if (auxEventPostDeleted) return false
        val snippetState = viewPager.getCurrentPage()?.getSnippetState()
        return when {
            snippetState == null -> false
            snippetState == SnippetState.Preview
                || snippetState == SnippetState.Expanded && viewPager.getCurrentPage() is EventSnippetErrorFragment -> {
                eventsOnMap.setMapEventSnippetCloseMethod(MapSnippetCloseMethod.BACK_BUTTON)
                viewPager.getCurrentPage()?.setSnippetState(SnippetState.Closed)
                true
            }
            else -> {
                viewPager.getCurrentPage()?.setSnippetState(SnippetState.Preview)
                true
            }
        }
    }

    fun consumeNeedToAnimatePageEnter(): Boolean {
        val value = needToAnimatePageEnter
        needToAnimatePageEnter = false
        return value
    }

    fun closeSnippet() {
        viewPager.getCurrentPage()?.setSnippetState(SnippetState.Closed)
    }

    fun onSnippetState(pageIndex: Int, snippetState: SnippetState) {
        if (pageIndex != viewPager.currentItem) return
        viewPager.isPagingEnabled = snippetState == SnippetState.Preview
        handleMapUi(snippetState)
        handleSnippetStateAnalytics(snippetState)
    }

    fun onErrorSnippetState(snippetState: SnippetState) {
        viewPager.isPagingEnabled = false
        handleMapUi(snippetState)
        handleSnippetStateAnalytics(snippetState)
    }

    fun onPageHeightChanged(pageIndex: Int, height: Int) {
        if (viewPager.currentItem == pageIndex) {
            val event = dataUiState.getSnippetListItem(pageIndex)
                ?.eventObject?.eventPost?.event ?: return
            mapUiController.updateCameraLocation(
                location = event.address.location,
                yOffset = calculateEventSnippetYOffset(height)
            )
        }
    }

    fun updateEventPost(pageIndex: Int, post: PostUIEntity) {
        when (val localDataUiState = dataUiState) {
            is EventSnippetDataUiState.PreloadedSnippet ->
                handlePreloadedSnippetUpdate(dataUiState = localDataUiState, post = post)
            is EventSnippetDataUiState.SnippetList ->
                handleSnippetListItemUpdate(dataUiState = localDataUiState, pageIndex = pageIndex, post = post)
            else -> Unit
        }
    }

    fun retryDataLoading() {
        eventsOnMap.getNextEventSnippetPage()
    }

    fun onUserRemovedOwnPost() {
        if (mapUiController.getMapMode() is MapMode.Main) {
            closeSnippet()
        } else {
            auxEventPostDeleted = true
            (fragment.activity as? Act)?.navigateBack()
        }
    }

    fun setMapEventSnippetCloseMethod(closeMethod: MapSnippetCloseMethod) {
        eventsOnMap.setMapEventSnippetCloseMethod(closeMethod)
    }

    private fun handleSnippetListItemUpdate(
        dataUiState: EventSnippetDataUiState.SnippetList,
        pageIndex: Int,
        post: PostUIEntity
    ) {
        val currentEventPostItem = (dataUiState.items.getOrNull(pageIndex) as? EventSnippetItem.EventPostItem) ?: return
        if (currentEventPostItem.eventObject.eventPost == post) return
        val snippetHeight = eventPostMeasuringUtil.calculateSnippetHeight(post)
        val updatedEventObject = currentEventPostItem.eventObject.copy(eventPost = post)
        mapUiController.updateEventMapItem(updatedEventObject)
        val updatedItems = dataUiState.items.mapIndexed { index, eventSnippetItem ->
            if (index == pageIndex) {
                currentEventPostItem.copy(eventObject = updatedEventObject, snippetHeight = snippetHeight)
            } else {
                eventSnippetItem
            }
        }
        handleDataUiStateUpdate(dataUiState.copy(items = updatedItems))
        if (snippetHeight != currentEventPostItem.snippetHeight) {
            updateItemCameraLocation(
                pageIndex = pageIndex,
                snippetHeight = snippetHeight,
                eventObject = updatedEventObject
            )
        }
    }

    private fun handlePreloadedSnippetUpdate(
        dataUiState: EventSnippetDataUiState.PreloadedSnippet,
        post: PostUIEntity
    ) {
        val currentEventPostItem = dataUiState.item
        if (currentEventPostItem.eventObject.eventPost == post) return
        val snippetHeight = eventPostMeasuringUtil.calculateSnippetHeight(post)
        val updatedEventObject = currentEventPostItem.eventObject.copy(eventPost = post)
        mapUiController.updateEventMapItem(updatedEventObject)
        val updatedItem = currentEventPostItem.copy(eventObject = updatedEventObject, snippetHeight = snippetHeight)
        handleDataUiStateUpdate(dataUiState.copy(item = updatedItem))
        if (snippetHeight != currentEventPostItem.snippetHeight) {
            updateItemCameraLocation(
                pageIndex = 0,
                snippetHeight = snippetHeight,
                eventObject = updatedEventObject
            )
        }
    }

    private fun updateItemCameraLocation(pageIndex: Int, snippetHeight: Int, eventObject: EventObjectUiModel) {
        viewPager.getPage(pageIndex)?.setSnippetHeight(snippetHeight)
        if (viewPager.currentItem == pageIndex) {
            val location = eventObject.eventPost.event?.address?.location ?: return
            mapUiController.updateCameraLocation(
                location = location,
                yOffset = calculateEventSnippetYOffset(snippetHeight),
                zoom = mapUiController.getTargetSnippetZoom()
            )
        }
    }

    private fun handlePageChange(pageIndex: Int) {
        val eventObject = dataUiState.getSnippetListItem(pageIndex)
            ?.eventObject
            ?: return
        val event = eventObject.eventPost.event ?: return
        mapUiController.focusMapItem(FocusedMapItem.Event(eventObject))
        mapUiController.updateCameraLocation(
            location = event.address.location,
            yOffset = calculateEventSnippetYOffset(viewPager.getCurrentPage()?.getSnippetHeight() ?: 0)
        )
        viewPager.getPage(pageIndex)?.onSelectPage()
    }

    private fun calculateEventSnippetYOffset(snippetHeight: Int): Int {
        val mapBottomPadding = mapUiController.getMapUiValues().mapBottomPadding
        val mapHeight = mapUiController.getMapUiValues().mapHeight
        return snippetHeight - (((mapHeight - mapBottomPadding) / 2) + mapBottomPadding) +
            EVENT_PIN_Y_OFFSET_FROM_SNIPPET_DP.dp
    }

    private fun handleEventSnippetUiModel(dataUiState: EventSnippetDataUiState) {
        val updatedDataUiState = measurePostItems(dataUiState)
        when {
            this.dataUiState is EventSnippetDataUiState.Empty
                && (updatedDataUiState is EventSnippetDataUiState.PreloadedSnippet || updatedDataUiState is EventSnippetDataUiState.SnippetList) -> {
                handleInitialSnippetData(updatedDataUiState)
            }
            this.dataUiState is EventSnippetDataUiState.PreloadedSnippet && updatedDataUiState is EventSnippetDataUiState.SnippetList -> {
                updateSelectedItemFromSnippetList(updatedDataUiState)
            }
            updatedDataUiState is EventSnippetDataUiState.Error -> handleErrorState(updatedDataUiState)
        }
        handleDataUiStateUpdate(updatedDataUiState)
    }

    private fun handleInitialSnippetData(updatedDataUiState: EventSnippetDataUiState) {
        val item = (updatedDataUiState.getItems().firstOrNull() as? EventSnippetItem.EventPostItem) ?: return
        val eventObject = item.eventObject
        eventObject.eventPost.event?.address?.location?.let { location ->
            mapUiController.updateCameraLocation(
                location = location,
                yOffset = calculateEventSnippetYOffset(item.snippetHeight),
                zoom = mapUiController.getTargetSnippetZoom()
            )
        }
        viewPager.getCurrentPage()?.setSnippetHeight(item.snippetHeight)
    }

    private fun updateSelectedItemFromSnippetList(updatedDataUiState: EventSnippetDataUiState.SnippetList) {
        val firstItem = (updatedDataUiState.items.firstOrNull() as? EventSnippetItem.EventPostItem) ?: return
        val eventObject = firstItem.eventObject
        eventObject.eventPost.event?.address?.location?.let { location ->
            mapUiController.updateCameraLocation(
                location = location,
                yOffset = calculateEventSnippetYOffset(firstItem.snippetHeight),
                zoom = mapUiController.getTargetSnippetZoom()
            )
        }
        (viewPager.getPage(0) as? EventSnippetPage)?.apply {
            setSnippetHeight(firstItem.snippetHeight)
            val eventPost = (updatedDataUiState.items[0] as EventSnippetItem.EventPostItem).eventObject.eventPost
            updateEventSnippetPageContent(eventPost)
        }
    }

    private fun handleErrorState(updatedDataUiState: EventSnippetDataUiState.Error) {
        val snippetHeight = fragment.resources.getDimensionPixelSize(R.dimen.map_events_error_snippet_height)
        mapUiController.updateCameraLocation(
            location = updatedDataUiState.item.pinLocation,
            yOffset = calculateEventSnippetYOffset(snippetHeight),
            zoom = mapUiController.getTargetSnippetZoom()
        )
    }

    private fun measurePostItems(uiModel: EventSnippetDataUiState): EventSnippetDataUiState {
        return when (uiModel) {
            is EventSnippetDataUiState.Empty, is EventSnippetDataUiState.Error -> uiModel
            is EventSnippetDataUiState.PreloadedSnippet -> {
                val snippetHeight = eventPostMeasuringUtil.calculateSnippetHeight(
                    uiModel.item.eventObject.eventPost
                )
                uiModel.copy(item = uiModel.item.copy(snippetHeight = snippetHeight))
            }
            is EventSnippetDataUiState.SnippetList -> {
                val updatedItems = uiModel.items.map { item ->
                    val eventPostItem = (item as? EventSnippetItem.EventPostItem) ?: return@map item
                    val snippetHeight = eventPostMeasuringUtil.calculateSnippetHeight(
                        eventPostItem.eventObject.eventPost
                    )
                    eventPostItem.copy(snippetHeight = snippetHeight)
                }
                uiModel.copy(items = updatedItems)
            }
        }
    }

    private fun handleDataUiStateUpdate(uiModel: EventSnippetDataUiState) {
        this.dataUiState = uiModel
        when {
            uiModel is EventSnippetDataUiState.Empty -> {
                viewPager.currentItem = 0
                viewPager.gone()
                (viewPager.adapter as? EventPostsPagerAdapter)?.setItemModels(emptyList())
            }
            mapUiController.isMapOpenInTab.not() && mapUiController.getMapMode() is MapMode.Main -> eventsOnMap.setSelectedEvent(null)
            else -> {
                viewPager.visible()
                (viewPager.adapter as? EventPostsPagerAdapter)?.setItemModels(uiModel.getItems())
            }
        }
    }

    private fun handleMapUi(snippetState: SnippetState) {
        when (snippetState) {
            SnippetState.Closed -> {
                eventsOnMap.setSelectedEvent(null)
                eventsOnMap.setAuxMapEventSelected(null)
                needToAnimatePageEnter = true
                mapUiController.showMapControls()
                mapUiController.focusMapItem(null)
                mapUiController.updateEventsData()
            }
            SnippetState.Preview, SnippetState.Expanded -> {
                mapUiController.hideMapControls()
            }
            else -> Unit
        }
        eventsOnMap.setEventSnippetState(snippetState)
    }

    private fun handleSnippetStateAnalytics(snippetState: SnippetState) {
        when (snippetState) {
            SnippetState.Closed -> eventsOnMap.logMapEventSnippetClosed()
            SnippetState.DraggedByUser -> eventsOnMap.setMapEventSnippetCloseMethod(MapSnippetCloseMethod.SWIPE)
            else -> Unit
        }
    }

    private fun EventSnippetDataUiState?.getSnippetListItem(pageIndex: Int): EventSnippetItem.EventPostItem? =
        (this as? EventSnippetDataUiState.SnippetList)?.items?.getOrNull(pageIndex) as? EventSnippetItem.EventPostItem

    private fun EventSnippetDataUiState?.getItems(): List<EventSnippetItem> =
        (this as? EventSnippetDataUiState.PreloadedSnippet)?.item?.let(::listOf)
        ?: (this as? EventSnippetDataUiState.SnippetList)?.items
        ?: (this as? EventSnippetDataUiState.Error)?.item?.let(::listOf)
        ?: emptyList()

    companion object {
        private const val EVENT_PIN_Y_OFFSET_FROM_SNIPPET_DP = 23
        private const val PAGE_MARGIN_DP = 24
    }
}

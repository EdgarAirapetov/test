package com.numplates.nomera3.modules.maps.ui.pin

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.loadBlocking
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.modules.maps.ui.animate
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.eventPostId
import com.numplates.nomera3.modules.maps.ui.pin.model.EventLargePinUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.EventMarkerEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.EventMarkerJobEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.EventPinImage
import com.numplates.nomera3.modules.maps.ui.pin.model.EventSmallPinUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.MarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext


class MapEventObjectsDelegate(
    private val fragment: Fragment,
    private val map: GoogleMap,
    private val coordinator: MapObjectsDelegateCoordinator
) {
    private val eventMarkers: MutableMap<Long, EventMarkerEntry> = mutableMapOf()
    private val eventMarkerJobs: MutableMap<Long, EventMarkerJobEntry> = mutableMapOf()

    fun setEventFocus(eventObject: EventObjectUiModel, focused: Boolean) {
        val zIndex = if (focused) MapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX else MapObjectsDelegate.MAP_EVENTS_ZINDEX
        val postId = eventObject.eventPost.postId
        val isLarge = focused || map.cameraPosition.zoom >= LARGE_EVENT_PIN_ZOOM
        handleEventObject(eventObject = eventObject, isLarge = isLarge)
        eventMarkers[postId]?.marker?.zIndex = zIndex
    }

    fun updateEventObject(eventObject: EventObjectUiModel) {
        eventMarkers[eventObject.eventPost.postId]?.let { entry ->
            eventMarkers[eventObject.eventPost.postId] = entry.copy(event = eventObject)
        }
    }

    fun findEventMarkerEntry(markerId: String): EventMarkerEntry? = eventMarkers.values
        .firstOrNull { it.marker.id == markerId }

    fun handleEvents(events: List<EventObjectUiModel>) {
        val focusItemHandler = coordinator.getFocusedItemHandler()
        val isLargeByCurrentZoom = map.cameraPosition.zoom >= LARGE_EVENT_PIN_ZOOM
        val focusedEventId = focusItemHandler.getFocusedItem()?.eventPostId()
        for (event in events) {
            val postId = event.eventPost.postId
            val isLarge = focusedEventId == postId || isLargeByCurrentZoom
            handleEventObject(eventObject = event, isLarge = isLarge)
            if (postId == focusedEventId) {
                eventMarkers[postId]?.marker?.zIndex = MapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX
            }
        }
        val eventMarkersIterator = eventMarkers.iterator()
        while (eventMarkersIterator.hasNext()) {
            val (postId, eventMarkerEntry) = eventMarkersIterator.next()
            val notInList = events.none { it.eventPost.postId == postId }
            val isFocused = postId == focusedEventId
            val isLarge = isFocused || isLargeByCurrentZoom
            val sizeChanged = eventMarkerEntry.isLarge != isLarge
            if ((notInList && isFocused.not()) || sizeChanged) {
                eventMarkerEntry.marker.remove()
                eventMarkersIterator.remove()
            }
        }
        val markerJobsIterator = eventMarkerJobs.iterator()
        while (markerJobsIterator.hasNext()) {
            val (postId, eventMarkerJobEntry) = markerJobsIterator.next()
            val markerExists = eventMarkers.containsKey(postId)
            val notInList = events.none { it.eventPost.postId == postId }
            val isFocused = postId == focusedEventId
            val isLarge = isFocused || isLargeByCurrentZoom
            val sizeChanged = eventMarkerJobEntry.isLarge != isLarge
            if (((markerExists || notInList) && isFocused.not()) || sizeChanged) {
                eventMarkerJobEntry.job.cancel()
                markerJobsIterator.remove()
            }
        }
    }

    fun clearEventMarkers() {
        val focusedEventPostId = coordinator.getFocusedItemHandler().getFocusedItem().eventPostId()
        val eventMarkersIterator = eventMarkers.iterator()
        while (eventMarkersIterator.hasNext()) {
            val (postId, eventMarkerEntry) = eventMarkersIterator.next()
            if (postId != focusedEventPostId) {
                eventMarkerEntry.marker.remove()
                eventMarkersIterator.remove()
            }
        }
    }

    private fun createEventMarkerJob(eventObject: EventObjectUiModel, isLarge: Boolean): Job {
        val config = coordinator.getConfig()
        val focusedItemHandler = coordinator.getFocusedItemHandler()
        return coordinator.getMarkerJobHandler().launch {
            if (eventObject.eventPost.event == null) return@launch
            val markerOptions = MarkerOptions()
            withContext(Dispatchers.IO) {
                val eventView = if (isLarge) {
                    val imageUrl = eventObject.eventPost.postSmallUrl
                    val image = if (imageUrl != null && eventObject.eventPost.deleted.toBoolean().not()) {
                        val imageBitmap = Glide.with(fragment).loadBlocking(imageUrl)
                        if (imageBitmap != null) {
                            EventPinImage.ImageLoaded(imageBitmap)
                        } else {
                            EventPinImage.ImageError
                        }
                    } else {
                        EventPinImage.NoImage
                    }
                    val title = eventObject.eventPost.event.tagSpan?.text.orEmpty()
                    val eventPinUiModel = EventLargePinUiModel(
                        id = eventObject.eventPost.postId,
                        title = title,
                        image = image,
                        eventIconResId = eventObject.eventIconResId,
                        eventColorResId = eventObject.eventColorResId
                    )
                    EventLargePinView(fragment.requireContext()).apply {
                        show(eventPinUiModel)
                    }
                } else {
                    val eventPinUiModel = EventSmallPinUiModel(
                        id = eventObject.eventPost.postId,
                        eventIconResId = eventObject.eventPinIconResId
                    )
                    EventSmallPinView(fragment.requireContext()).apply {
                        show(eventPinUiModel)
                    }
                }
                val pinBitmap = NGraphics.getBitmapView(eventView)
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(pinBitmap))
                markerOptions.anchor(eventView.getAnchorX(), eventView.getAnchorY())
            }
            if (config.objectsDisabled) return@launch
            markerOptions.position(eventObject.eventPost.event.address.location)
            map.addMarker(markerOptions)?.apply {
                zIndex = when {
                    focusedItemHandler.getFocusedItem().eventPostId() == eventObject.eventPost.postId ->
                        MapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX
                    else -> MapObjectsDelegate.MAP_EVENTS_ZINDEX
                }
                alpha = 0f
                eventMarkers[eventObject.eventPost.postId] = EventMarkerEntry(
                    event = eventObject,
                    isLarge = isLarge,
                    marker = this
                )
                animate()
            }
        }
    }

    private fun getMarkerState(eventObject: EventObjectUiModel, isLarge: Boolean): MarkerState {
        val postId = eventObject.eventPost.postId
        val markerCreated = eventMarkers.containsKey(postId)
        val markerSizeIsObsolete = eventMarkers[postId]?.isLarge != isLarge
            || (markerCreated.not() && eventMarkerJobs[postId]?.isLarge != isLarge)
        val markerDataIsObsolete = eventMarkers[postId]?.event != eventObject
            || (markerCreated.not() && eventMarkerJobs[postId]?.event != eventObject)
        val noActiveMarkerJob = eventMarkerJobs[postId]?.job?.isActive.isNotTrue()
        return when {
            markerCreated && markerDataIsObsolete && markerSizeIsObsolete.not() -> MarkerState.NeedToUpdate
            markerSizeIsObsolete || markerDataIsObsolete || (markerCreated.not() && noActiveMarkerJob) -> MarkerState.NeedToCreate
            else -> MarkerState.UpToDate
        }
    }

    private fun handleEventObject(eventObject: EventObjectUiModel, isLarge: Boolean) {
        val postId = eventObject.eventPost.postId
        when (getMarkerState(eventObject = eventObject, isLarge = isLarge)) {
            MarkerState.NeedToCreate -> {
                eventMarkers[postId]?.marker?.remove()
                eventMarkers.remove(postId)
                eventMarkerJobs[postId]?.job?.cancel()
                val job = createEventMarkerJob(eventObject = eventObject, isLarge = isLarge)
                eventMarkerJobs[postId] = EventMarkerJobEntry(
                    event = eventObject,
                    isLarge = isLarge,
                    job = job
                )
            }
            MarkerState.NeedToUpdate -> {
                eventMarkers[postId]?.let {
                    eventMarkers[postId] = it.copy(event = eventObject)
                }
            }
            MarkerState.UpToDate -> Unit
        }
    }

    companion object {
        private const val LARGE_EVENT_PIN_ZOOM = 15
    }
}

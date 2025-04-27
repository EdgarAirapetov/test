package com.numplates.nomera3.modules.maps.ui.pin

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.meera.core.extensions.loadBlocking
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.modules.maps.ui.animate
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.model.userId
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.UserMarkerEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.UserMarkerJobEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.UserPinUiModel
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class MapUserObjectsDelegate(
    private val fragment: Fragment,
    private val map: GoogleMap,
    private val coordinator: MapObjectsDelegateCoordinator,
) {
    private val userMarkers: MutableMap<Long, UserMarkerEntry> = mutableMapOf()
    private val userMarkerJobs: MutableMap<Long, UserMarkerJobEntry> = mutableMapOf()

    fun setUserFocus(userId: Long, focused: Boolean) {
        val zIndex = if (focused) MapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX else MapObjectsDelegate.MAP_USERS_ZINDEX
        userMarkers[userId]?.marker?.zIndex = zIndex
    }

    fun handleUsers(users: List<MapUserUiModel>) {
        val visibleBounds = map.projection.visibleRegion.latLngBounds
        val myUid = coordinator.getConfig().userUid
        for (user in users) {
            if (visibleBounds.contains(user.latLng)) {
                val userIsNotMe = user.id != myUid
                val markerNotCreated = userMarkers.containsKey(user.id).not()
                val currentUser = userMarkers[user.id]?.user
                val markerIsObsolete =
                    currentUser != null && momentValuesChanged(oldUserUiModel = currentUser, newUserUiModel = user)
                val noActiveMarkerJob = userMarkerJobs[user.id]?.user != user
                    || userMarkerJobs[user.id]?.job?.isActive != true
                if (userIsNotMe && noActiveMarkerJob && (markerNotCreated || markerIsObsolete)) {
                    userMarkers[user.id]?.marker?.remove()
                    userMarkers.remove(user.id)
                    userMarkerJobs[user.id]?.job?.cancel()
                    val job = createUserMarkerJob(user)
                    userMarkerJobs[user.id] = UserMarkerJobEntry(
                        user = user,
                        job = job
                    )
                }
                if (user.id == coordinator.getFocusedItemHandler().getFocusedItem().userId()) {
                    userMarkers[user.id]?.marker?.zIndex = MapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX
                }
            } else {
                userMarkerJobs[user.id]?.job?.cancel()
                userMarkerJobs.remove(user.id)
                if (userMarkers.containsKey(user.id)) {
                    userMarkers[user.id]?.marker?.remove()
                    userMarkers.remove(user.id)
                }
            }
        }

        val userMarkersIterator = userMarkers.iterator()
        while (userMarkersIterator.hasNext()) {
            val (uid, userMarkerEntry) = userMarkersIterator.next()
            if (!visibleBounds.contains(userMarkerEntry.marker.position) || users.none { it.id == uid }) {
                userMarkerEntry.marker.remove()
                userMarkersIterator.remove()
            }
        }
        val markerJobsIterator = userMarkerJobs.iterator()
        while (markerJobsIterator.hasNext()) {
            val (uid, userMarkerJobEntry) = markerJobsIterator.next()
            if (userMarkers.containsKey(uid) || users.none { it.id == uid }) {
                userMarkerJobEntry.job.cancel()
                markerJobsIterator.remove()
            }
        }
    }

    fun addUserMapObject(user: MapUserUiModel) {
        if (!userMarkers.containsKey(user.id)) {
            createUserMarkerJob(user)
        }
    }

    fun findUserMarkerEntry(markerId: String): UserMarkerEntry? = userMarkers.entries
        .firstOrNull { it.value.marker.id == markerId }
        ?.value

    fun removeOwnMarkerFromList() = userMarkers[coordinator.getConfig().userUid]?.marker?.remove()

    fun clearUserMarkers() {
        userMarkerJobs.values.forEach { it.job.cancel() }
        userMarkers.forEach { it.value.marker.remove() }
        userMarkers.clear()
    }

    fun updateUserMarkerMoments(updateModel: UserMomentsStateUpdateModel) {
        userMarkers[updateModel.userId]?.let { userMarkerEntry ->
            userMarkerEntry.marker.remove()
            userMarkers.remove(updateModel.userId)
            val updatedMapUser = userMarkerEntry.user.copy(
                hasNewMoments = updateModel.hasNewMoments,
                hasMoments = updateModel.hasMoments
            )
            val job = createUserMarkerJob(updatedMapUser)
            userMarkerJobs[updateModel.userId] = UserMarkerJobEntry(
                user = updatedMapUser,
                job = job
            )
        }
    }

    private fun createUserMarkerJob(mapUserUiModel: MapUserUiModel): Job {
        val config = coordinator.getConfig()
        val focusedItemHandler = coordinator.getFocusedItemHandler()
        return coordinator.getMarkerJobHandler().launch {
            val pinBitmap = withContext(Dispatchers.IO) {
                val avatar = mapUserUiModel.avatar?.let { avatarUrl ->
                    val options = RequestOptions()
                        .override(config.userMarkerSize)
                        .circleCrop()
                    Glide.with(fragment).loadBlocking(avatarUrl, options)
                }
                yield()
                val pinMoments = PinMomentsUiModel(
                    hasMoments = mapUserUiModel.hasMoments,
                    hasNewMoments = mapUserUiModel.hasNewMoments
                )
                val userPinUiModel = UserPinUiModel(
                    id = mapUserUiModel.id,
                    name = mapUserUiModel.name,
                    accountType = mapUserUiModel.accountType,
                    accountColor = mapUserUiModel.accountColor,
                    isFriend = mapUserUiModel.isFriend,
                    avatarBitmap = avatar,
                    moments = pinMoments
                )
                val avatarView = UserPinView(fragment.requireContext())
                avatarView.show(userPinUiModel)
                NGraphics.getBitmapView(avatarView)
            }

            if (userMarkers.containsKey(mapUserUiModel.id) || config.objectsDisabled) return@launch

            val markerOptions = MarkerOptions()
            markerOptions.anchor(0.5f, 0.5f)
            markerOptions.position(mapUserUiModel.latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(pinBitmap))
            map.addMarker(markerOptions)?.apply {
                zIndex = when {
                    focusedItemHandler.getFocusedItem().userId() == mapUserUiModel.id ->
                        MapObjectsDelegate.MAP_FOCUSED_PIN_ZINDEX
                    mapUserUiModel.isFriend -> MapObjectsDelegate.MAP_FRIENDS_ZINDEX
                    else -> MapObjectsDelegate.MAP_USERS_ZINDEX
                }
                alpha = 0f
                userMarkers[mapUserUiModel.id] = UserMarkerEntry(
                    user = mapUserUiModel,
                    marker = this
                )
                animate()
            }
        }
    }

    private fun momentValuesChanged(oldUserUiModel: MapUserUiModel, newUserUiModel: MapUserUiModel): Boolean =
        oldUserUiModel.hasMoments != newUserUiModel.hasMoments
            || oldUserUiModel.hasNewMoments != newUserUiModel.hasNewMoments
}

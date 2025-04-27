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
import com.numplates.nomera3.modules.maps.ui.model.MapClusterUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.ClusterMarkerEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.ClusterMarkerJobEntry
import com.numplates.nomera3.modules.maps.ui.pin.model.ClusterPinUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class MapClusterObjectsDelegate(
    private val fragment: Fragment,
    private val map: GoogleMap,
    private val coordinator: MapObjectsDelegateCoordinator
) {
    private val clusterMarkers: MutableMap<Long, ClusterMarkerEntry> = mutableMapOf()
    private val clusterMarkerJobs: MutableMap<Long, ClusterMarkerJobEntry> = mutableMapOf()

    fun handleClusters(clusters: List<MapClusterUiModel>) {
        for (cluster in clusters) {
            val markerNotCreated = !clusterMarkers.containsKey(cluster.id)
            val noActiveMarkerJob = clusterMarkers[cluster.id]?.cluster != cluster
                || clusterMarkerJobs[cluster.id]?.job?.isActive != true
            if (markerNotCreated && noActiveMarkerJob) {
                clusterMarkerJobs[cluster.id]?.job?.cancel()
                val job = createClusterMarkerJob(cluster)
                clusterMarkerJobs[cluster.id] = ClusterMarkerJobEntry(
                    cluster = cluster,
                    job = job
                )
            }
        }
        val clusterMarkersIterator = clusterMarkers.iterator()
        while (clusterMarkersIterator.hasNext()) {
            val (clusterId, clusterMarkerEntry) = clusterMarkersIterator.next()
            if (clusters.none { it.id == clusterId }) {
                clusterMarkerEntry.marker.remove()
                clusterMarkersIterator.remove()
            }
        }
        val markerJobsIterator = clusterMarkerJobs.iterator()
        while (markerJobsIterator.hasNext()) {
            val (clusterId, clusterMarkerJobEntry) = markerJobsIterator.next()
            if (clusterMarkers.containsKey(clusterId) || clusters.none { it.id == clusterId }) {
                clusterMarkerJobEntry.job.cancel()
                markerJobsIterator.remove()
            }
        }
    }

    private fun createClusterMarkerJob(cluster: MapClusterUiModel): Job {
        return coordinator.getMarkerJobHandler().launch {
            val config = coordinator.getConfig()
            val pinBitmap = withContext(Dispatchers.IO) {
                val options = RequestOptions()
                    .override(config.userMarkerSize)
                    .circleCrop()
                val avatars = cluster.userAvatars
                    .take(VISIBLE_CLUSTER_USER_COUNT)
                    .map { avatarUrl ->
                        val avatarBitmap = Glide.with(fragment).loadBlocking(avatarUrl, options)
                        yield()
                        avatarBitmap
                    }
                val clusterPinUiModel = ClusterPinUiModel(
                    userAvatars = avatars,
                    capacity = cluster.capacity
                )
                val clusterView = ClusterPinView(fragment.requireContext())
                clusterView.show(clusterPinUiModel)
                NGraphics.getBitmapView(clusterView)
            }
            if (config.objectsDisabled) return@launch
            val markerOptions = MarkerOptions()
            markerOptions.anchor(0.5f, 0.5f)
            markerOptions.position(cluster.latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(pinBitmap))
            map.addMarker(markerOptions)?.apply {
                zIndex = MapObjectsDelegate.MAP_CLUSTERS_ZINDEX
                alpha = 0f
                clusterMarkers[cluster.id] = ClusterMarkerEntry(
                    cluster = cluster,
                    marker = this
                )
                animate()
            }
        }
    }

    fun clearClusterMarkers() {
        clusterMarkerJobs.values.forEach { it.job.cancel() }
        clusterMarkers.forEach { it.value.marker.remove() }
        clusterMarkers.clear()
    }

    companion object {
        private const val VISIBLE_CLUSTER_USER_COUNT = 3
    }
}

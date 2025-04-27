package com.numplates.nomera3.modules.moments.show.presentation.controller

import com.numplates.nomera3.modules.moments.show.domain.PreloadGroupMomentsUseCase
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.util.MomentPreloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

private const val PRELOAD_MOMENTS_DEPTH_INSIDE_GROUP = 1

/**
 * Preloads nearby moments inside a single group.
 *
 * Depth of the preload = [PRELOAD_MOMENTS_DEPTH_INSIDE_GROUP]
 */
class MomentsPreloadController @Inject constructor(
    private val preloadMomentsUseCase: PreloadGroupMomentsUseCase
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val requestedPreload = CopyOnWriteArraySet<Long>()

    fun momentGroupUpdated(currentMoment: MomentItemUiModel?, currentGroup: MomentGroupUiModel?) {
        if (currentMoment == null || currentGroup == null) return
        val indexOfCurrentMoment = currentGroup.moments.indexOf(currentMoment)
        if (indexOfCurrentMoment < 0 || indexOfCurrentMoment >= currentGroup.moments.size) return

        if (requestedPreload.isEmpty()) requestedPreload.add(currentMoment.id)

        val preloadItems = getSurroundingMoments(
            depth = PRELOAD_MOMENTS_DEPTH_INSIDE_GROUP,
            currentIndex = indexOfCurrentMoment,
            group = currentGroup
        ).filter {
            !requestedPreload.contains(it.id)
        }.map {
            MomentPreloadItem(
                momentId = it.id,
                contentUrl = it.contentUrl,
                contentType = it.contentType
            )
        }

        if (preloadItems.isEmpty()) return

        coroutineScope.launch {
            val idsToPreload = preloadItems.map { it.momentId }.toSet()
            runCatching {
                requestedPreload.addAll(idsToPreload)
                preloadMomentsUseCase.invoke(preloadItems)
            }.onFailure {
                requestedPreload.removeAll(idsToPreload)
            }
        }
    }

    fun onViewModelCleared() {
        coroutineScope.cancel()
    }

    private fun getSurroundingMoments(
        depth: Int,
        currentIndex: Int,
        group: MomentGroupUiModel
    ): List<MomentItemUiModel> {
        return when {
            group.moments.size == 1 || depth <= 0 -> return emptyList()
            currentIndex == 0 -> {
                val toIndex = getToIndex(depth = depth, currentIndex = currentIndex, listSize = group.moments.size)
                group.moments.subList(fromIndex = 1, toIndex = toIndex)
            }
            currentIndex == group.moments.lastIndex -> {
                val fromIndex = getFromIndex(depth = depth, currentIndex = currentIndex)
                group.moments.subList(fromIndex = fromIndex, toIndex = currentIndex)
            }
            else -> {
                val leftFromIndex = getFromIndex(depth = depth, currentIndex = currentIndex)
                val leftSubList = group.moments.subList(leftFromIndex, currentIndex)
                val rightToIndex = getToIndex(depth = depth, currentIndex = currentIndex, listSize = group.moments.size)
                val rightSubList = group.moments.subList(currentIndex + 1, rightToIndex)
                leftSubList + rightSubList
            }
        }
    }

    private fun getFromIndex(depth: Int, currentIndex: Int): Int {
        return max(0, currentIndex - depth)
    }

    private fun getToIndex(depth: Int, currentIndex: Int, listSize: Int): Int {
        return min(listSize, 1 + currentIndex + depth)
    }

}

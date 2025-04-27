package com.numplates.nomera3.modules.moments.show.domain

import android.content.Context
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.util.MomentPreloadItem
import com.numplates.nomera3.modules.moments.util.MomentsPreloadUtil
import javax.inject.Inject
import kotlin.math.max

private const val MAX_GROUP_TO_PRELOAD = 10

/**
 * Preloads 1 moment from last n-amount of groups, where n is [MAX_GROUP_TO_PRELOAD] or [MomentInfoModel.lastPageSize]
 */
class PreloadPaginatedMomentsUseCase @Inject constructor(applicationContext: Context) {
    private val momentsPreloadUtil = MomentsPreloadUtil(applicationContext)

    fun invoke(momentInfoModel: MomentInfoModel) {
        val momentGroups = momentInfoModel.momentGroups

        val groupsSize = momentGroups.size
        val fromStartIndexLastPage = if (momentInfoModel.lastPageSize == 0) {
            0
        } else {
            max(groupsSize - momentInfoModel.lastPageSize, 0)
        }
        val fromStartIndexMaxPageSize = max(groupsSize - MAX_GROUP_TO_PRELOAD, 0)
        val fromStartIndex = max(fromStartIndexLastPage, fromStartIndexMaxPageSize)

        runCatching {
            val momentItemsToPreload = momentGroups.subList(fromStartIndex, groupsSize)
                .mapNotNull { group ->
                    group.moments.sortedBy { it.createdAt }.find { !it.isViewed } ?: group.moments.firstOrNull()
                }.map { item ->
                    MomentPreloadItem(momentId = item.id, contentUrl = item.contentUrl, contentType = item.contentType)
                }

            momentsPreloadUtil.preload(momentItemsToPreload)
        }
    }
}

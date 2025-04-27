package com.numplates.nomera3.modules.post_view_statistic.data.mapper

import com.meera.db.models.PostViewLocalData
import com.numplates.nomera3.modules.post_view_statistic.data.net.FeatureViewRestRequest
import com.numplates.nomera3.modules.post_view_statistic.data.net.PostViewRestData
import com.numplates.nomera3.modules.post_view_statistic.data.net.PostViewRestRequest

class PostViewLocalDataMapper {
    fun mapToPostRestDataRequest(postViewLocalData: List<PostViewLocalData>): PostViewRestRequest {
        return PostViewRestRequest(
            posts = postViewLocalData.map { mapToRestDataList(it) }
        )
    }

    fun mapToFeaturePostRestDataRequest(featurePostViewLocalData: List<PostViewLocalData>): FeatureViewRestRequest {
        return FeatureViewRestRequest(
            features = featurePostViewLocalData.map { mapToRestDataList(it) }
        )
    }

    fun mapToRestDataList(postViewLocalData: PostViewLocalData): PostViewRestData {
        val viewId = when (postViewLocalData.isFeaturePost) {
            true -> postViewLocalData.featureId
            else -> postViewLocalData.postId
        }

        return PostViewRestData(
            id = viewId,
            groupId = postViewLocalData.groupId,
            roadType = postViewLocalData.roadSource,
            duration = postViewLocalData.viewDuration,
            viewAt = getViewAtValue(postViewLocalData)
        )
    }

    private fun getViewAtValue(postViewLocalData: PostViewLocalData): Long {
        return postViewLocalData.stopViewTime - postViewLocalData.viewDuration
    }
}

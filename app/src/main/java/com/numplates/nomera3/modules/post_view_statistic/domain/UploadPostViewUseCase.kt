package com.numplates.nomera3.modules.post_view_statistic.domain

import com.numplates.nomera3.data.network.ApiMain
import com.meera.db.DataStore
import com.numplates.nomera3.modules.post_view_statistic.data.mapper.PostViewLocalDataMapper
import javax.inject.Inject

class UploadPostViewUseCase @Inject constructor(
    private val dataStore: DataStore,
    private val api: ApiMain
) {
    private val postViewLocalDataMapper = PostViewLocalDataMapper()

    suspend fun execute() {
        val postViewItems = dataStore.postViewStatisticDao().getAllViewedPosts()
        val postViewFilteredItems = postViewItems.filter { it.isValidPost() }
        val featureViewFilteredItems = postViewItems.filter { it.isValidFeature() }

        if (postViewFilteredItems.isNotEmpty()) {
            val postViewRequest = postViewLocalDataMapper.mapToPostRestDataRequest(postViewFilteredItems)
            api.uploadPostViews(postViewRequest)
        }

        if (featureViewFilteredItems.isNotEmpty()) {
            val featureViewRequest = postViewLocalDataMapper.mapToFeaturePostRestDataRequest(featureViewFilteredItems)
            api.uploadFeatureViews(featureViewRequest)
        }

        if (postViewItems.isNotEmpty()) {
            dataStore.postViewStatisticDao().removeAllPostsViews()
        }
    }
}

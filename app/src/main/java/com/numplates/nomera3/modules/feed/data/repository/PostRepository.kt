package com.numplates.nomera3.modules.feed.data.repository

import com.numplates.nomera3.modules.feed.data.entity.CheckPostUpdateAvailabilityResponse
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.domain.model.PostModelEntity
import com.numplates.nomera3.modules.feed.domain.model.PostsModelEntity
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    suspend fun getPost(
        id: Long,
        success: (PostModelEntity) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getPosts(
        startId: Long,
        quantity: Int,
        roadType: Int,
        cityId: String,
        userId: Long,
        groupId: Int,
        countryIds: String,
        hashtag: String,
        includeGroups: Boolean?,
        recommended: Boolean?,
        success: (PostsModelEntity) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun subscribePost(id: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    suspend fun unsubscribePost(id: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    suspend fun hidePost(id: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    suspend fun deletePost(id: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    suspend fun hideUserPosts(userId: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    suspend fun showUserPosts(userId: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    suspend fun postComplain(id: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    fun refreshPostById(postId: Long, success: (Boolean) -> Unit, fail: (Exception) -> Unit)

    fun refreshPost(event: FeedUpdateEvent.FeedUpdatePayload)

    fun refreshMoments(event: FeedUpdateEvent.FeedUpdateMoments)

    fun refreshPostComments(event: FeedUpdateEvent.FeedUpdatePostComments)

    fun getFeedStateObserver() : Observable<FeedUpdateEvent>

    suspend fun actionOnFeature(
        featureId: Long,
        dismiss: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun updateReactiveUserSubscription(
        postId: Long?,
        userId: Long,
        isSubscribed: Boolean,
        needToHideFollowButton: Boolean,
        isBlocked: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun updateReactivePostSubscription(
        postId: Long,
        isSubscribed: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    fun downloadPostVideoToGallery(postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType)

    fun stopDownloadingPostVideoToGallery(id: Long)

    fun getDownloadHelperEvent(): Flow<DownloadMediaEvent>

    fun readOnboarding() : Boolean

    suspend fun checkPostUpdateAvailability(
        postId: Long,
        success: (CheckPostUpdateAvailabilityResponse) -> Unit,
        fail: (Exception) -> Unit
    )
}

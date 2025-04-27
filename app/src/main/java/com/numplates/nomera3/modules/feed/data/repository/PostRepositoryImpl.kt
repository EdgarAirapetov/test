package com.numplates.nomera3.modules.feed.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.feed.data.FeedException
import com.numplates.nomera3.modules.feed.data.api.RoadApi
import com.numplates.nomera3.modules.feed.data.entity.CheckPostUpdateAvailabilityResponse
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.domain.mapper.toDataPostModel
import com.numplates.nomera3.modules.feed.domain.mapper.toDataPostsModel
import com.numplates.nomera3.modules.feed.domain.model.PostModelEntity
import com.numplates.nomera3.modules.feed.domain.model.PostsModelEntity
import com.numplates.nomera3.modules.feed.domain.usecase.GetEditInProcessPostIdUseCase
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

private const val FIELD_HIDE = "hide"
private const val HIDE_VALUE = 1
private const val SHOW_VALUE = 0

private const val FILED_POST_ID = "post_id"

const val ACTION_SHOW = "show"
const val ACTION_HIDE = "hide"
const val PARAM_FEATURE_ID = "feature_id"
const val PARAM_ACTION = "action"

private const val EMPTY_ERROR_MESSAGE = "Empty response"

@AppScope
class PostRepositoryImpl @Inject constructor(
    private val api: RoadApi,
    private val downloadMediaHelper: DownloadMediaHelper,
    private val appSettings: AppSettings,
    private val getEditInProcessPostIdUseCase: GetEditInProcessPostIdUseCase
) : PostRepository {

    private val feedStateSubject = PublishSubject.create<FeedUpdateEvent>()

    override fun getFeedStateObserver() = feedStateSubject

    override fun refreshPostById(
        postId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        feedStateSubject.onNext(FeedUpdateEvent.FeedUpdateAll(postId))
        success(true)
    }

    override fun refreshPost(event: FeedUpdateEvent.FeedUpdatePayload) =
        feedStateSubject.onNext(event)

    override fun refreshMoments(event: FeedUpdateEvent.FeedUpdateMoments) =
        feedStateSubject.onNext(event)

    override fun refreshPostComments(event: FeedUpdateEvent.FeedUpdatePostComments) =
        feedStateSubject.onNext(event)

    override suspend fun getPosts(
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
    ) {
        execute({ postEntityResponse ->
            val currentPostInEditStateId = getEditInProcessPostIdUseCase.invoke()
            success(postEntityResponse.toDataPostsModel(currentPostInEditStateId))
        }, fail) {
            api.getPosts(
                startId,
                quantity,
                roadType,
                cityId,
                userId,
                groupId,
                countryIds,
                hashtag,
                includeGroups,
                recommended
            )
        }
    }

    override suspend fun getPost(
        id: Long,
        success: (PostModelEntity) -> Unit,
        fail: (Exception) -> Unit
    ) {
        execute({
            feedStateSubject.onNext(
                FeedUpdateEvent.FeedUpdatePayload(
                    postId = it.id,
                    repostCount = it.repostsCount,
                    commentCount = it.commentsCount,
                    reactions = it.reactions
                )
            )
            success(
                it.toDataPostModel(getEditInProcessPostIdUseCase.invoke())
            )
        }, fail) { api.getPost(id) }
    }

    override suspend fun subscribePost(
        id: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        execute({
            success(it)
            feedStateSubject.onNext(FeedUpdateEvent.FeedPostSubscriptionChanged(id, true))
        }, fail) {
            api.subscribePost(id)
        }
    }

    override suspend fun unsubscribePost(
        id: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        execute({
            success(it)
            feedStateSubject.onNext(FeedUpdateEvent.FeedPostSubscriptionChanged(id, false))
        }, fail) {
            api.unsubscribePost(id)
        }
    }

    override suspend fun updateReactiveUserSubscription(
        postId: Long?,
        userId: Long,
        isSubscribed: Boolean,
        needToHideFollowButton: Boolean,
        isBlocked: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        feedStateSubject.onNext(
            FeedUpdateEvent.FeedUserSubscriptionChanged(
                postId = postId,
                userId = userId,
                isSubscribed = isSubscribed,
                needToHideFollowButton = needToHideFollowButton,
                isBlocked = isBlocked
            )
        )
        success(true)
    }

    override suspend fun updateReactivePostSubscription(
        postId: Long,
        isSubscribed: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        feedStateSubject.onNext(FeedUpdateEvent.FeedPostSubscriptionChanged(postId, isSubscribed))
        success(true)
    }

    override suspend fun hidePost(
        id: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        execute({
            feedStateSubject.onNext(FeedUpdateEvent.FeedPostRemoved(id))
            success(true)
        }, fail) { api.hidePost(id) }

    }

    override suspend fun deletePost(
        id: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        execute({
            feedStateSubject.onNext(FeedUpdateEvent.FeedPostRemoved(id))
            success(true)
        }, fail) { api.deletePost(id) }

    }

    override suspend fun hideUserPosts(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val result = api.hidePosts(
                userId,
                hashMapOf(FIELD_HIDE to HIDE_VALUE)
            )
            if (result.data == null) {
                fail(IllegalArgumentException(EMPTY_ERROR_MESSAGE))
            } else {
                feedStateSubject.onNext(FeedUpdateEvent.FeedHideUserRoad(userId))
                success(true)
            }
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }

    }

    override suspend fun showUserPosts(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        executeCompletable(success, fail) {
            api.hidePosts(
                userId,
                hashMapOf(FIELD_HIDE to SHOW_VALUE)
            )
        }
    }

    override suspend fun postComplain(
        id: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        executeCompletable(success, fail) { api.addComplain(hashMapOf(FILED_POST_ID to id)) }
    }

    override suspend fun actionOnFeature(
        featureId: Long,
        dismiss: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        executeCompletable(success, fail) {
            api.actionOnFeature(
                hashMapOf(
                    PARAM_FEATURE_ID to featureId,
                    PARAM_ACTION to if (dismiss) ACTION_HIDE else ACTION_SHOW
                )
            )
        }
    }

    private suspend fun <T> execute(
        success: (T) -> Unit,
        fail: (Exception) -> Unit,
        action: suspend () -> ResponseWrapper<T>?
    ) {
        try {
            val r = action()
            if (r?.data == null) fail(FeedException(EMPTY_ERROR_MESSAGE, r?.err))
            else success(r.data)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    private suspend fun executeCompletable(
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit,
        action: suspend () -> ResponseWrapper<Any>?
    ) {
        try {
            val r = action()
            if (r?.data == null) fail(IllegalArgumentException(EMPTY_ERROR_MESSAGE))
            else success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    override fun downloadPostVideoToGallery(postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType) {
        downloadMediaHelper.downloadVideoToGallery(postMediaDownloadType)
    }

    override fun stopDownloadingPostVideoToGallery(id: Long) = downloadMediaHelper.stopDownloadingVideo(id)

    override fun getDownloadHelperEvent(): Flow<DownloadMediaEvent> {
        return downloadMediaHelper.downloadEvent
    }

    override fun readOnboarding(): Boolean {
        return appSettings.readNeedOnBoarding()
    }

    override suspend fun checkPostUpdateAvailability(
        postId: Long,
        success: (CheckPostUpdateAvailabilityResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        execute(success, fail) {
            api.checkPostUpdateAvailability(postId)
        }
    }
}

package com.numplates.nomera3.modules.newroads.data

import androidx.work.Data
import androidx.work.ListenableWorker
import com.meera.core.extensions.toJson
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.AddPostComplaintUseCase
import com.numplates.nomera3.domain.interactornew.AddPostUseCase
import com.numplates.nomera3.domain.interactornew.DeletePostUseCase
import com.numplates.nomera3.domain.interactornew.EditPostUseCase
import com.numplates.nomera3.domain.interactornew.FeatureActionUseCase
import com.numplates.nomera3.domain.interactornew.GetPostUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.HideUserPostsUseCase
import com.numplates.nomera3.domain.interactornew.IMAGE_TYPE
import com.numplates.nomera3.domain.interactornew.SubscribePostUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.domain.interactornew.UnsubscribePostUseCase
import com.numplates.nomera3.domain.interactornew.VIDEO_MP4_TYPE
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.domain.repository.EMPTY_VIDEO_AMPLITUDE_VALUE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentsSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialFileUploadUseCase
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.newroads.data.entities.GetSubscriptionMomentListenerUseCase
import com.numplates.nomera3.modules.newroads.data.entities.GetSubscriptionPostListenerUseCase
import com.numplates.nomera3.modules.newroads.data.entities.MarkSubscriptionPostReadUseCase
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.newroads.data.entities.SubscriptionNewPostEntity
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.modules.upload.data.post.EditedAssetModel
import com.numplates.nomera3.modules.upload.data.post.UploadMediaModel
import com.numplates.nomera3.modules.upload.domain.usecase.post.GetVideoLengthUseCase
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class PostsRepository : ISensitiveContentManager {

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var addPostUseCase: AddPostUseCase

    @Inject
    lateinit var editPostUseCase: EditPostUseCase

    @Inject
    lateinit var partialFileUploadUseCase: PartialFileUploadUseCase

    @Inject
    lateinit var subscribePost: SubscribePostUseCase

    @Inject
    lateinit var unsubscribePost: UnsubscribePostUseCase

    @Inject
    lateinit var addPostComplaint: AddPostComplaintUseCase

    @Inject
    lateinit var hidePosts: HideUserPostsUseCase

    @Inject
    lateinit var deletePost: DeletePostUseCase

    @Inject
    lateinit var getPost: GetPostUseCase

    @Inject
    lateinit var subscriptionsUseCase: SubscriptionUseCase

    @Inject
    lateinit var myTracker: ITrackerActions

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var textUtilProcessor: TextProcessorUtil

    @Inject
    lateinit var featureActionUseCase: FeatureActionUseCase

    @Inject
    lateinit var getSubscriptionsNewPostListenerUseCase: GetSubscriptionPostListenerUseCase

    @Inject
    lateinit var getSubscriptionsNewMomentListenerUseCase: GetSubscriptionMomentListenerUseCase

    @Inject
    lateinit var markSubscriptionPostReadUseCase: MarkSubscriptionPostReadUseCase

    @Inject
    lateinit var getVideoLengthUseCase: GetVideoLengthUseCase

    val newPostObservable: PublishSubject<PostActionModel> = PublishSubject.create()

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    private var lastPostMediaViewInfo: PostMediaViewInfo? = null

    init {
        App.component.inject(this)
    }

    private fun newPostCreated(postCreationSuccess: PostActionModel) = newPostObservable.onNext(postCreationSuccess)

    fun setLastPostMediaViewInfo(lastPostMediaViewInfo: PostMediaViewInfo?) {
        this.lastPostMediaViewInfo = lastPostMediaViewInfo
    }

    fun getLastPostMediaViewInfo() = lastPostMediaViewInfo

    suspend fun markAllSubscriptionPostViewed(): Boolean {
        return markSubscriptionPostReadUseCase.execute()
    }

    fun setSubscriptionPostsWereRequestedWithinSession(value: Boolean) {
        subscriptionPostsWereRequestedWithinSession = value
    }

    fun getSubscriptionPostsWereRequestedWithinSession(): Boolean {
        return subscriptionPostsWereRequestedWithinSession
    }

    fun getNewSubscriptionPostsObservable(): Observable<SubscriptionNewPostEntity> {
        return getSubscriptionsNewPostListenerUseCase.execute()
    }

    private fun mapRoadTypeToFeedName(roadType: Int, groupId: Int): AmplitudePropertyWhere {
        return if (groupId == 0) {
            if (roadType == 0) {
                AmplitudePropertyWhere.MAIN_FEED
            } else {
                AmplitudePropertyWhere.SELF_FEED
            }
        } else {
            AmplitudePropertyWhere.COMMUNITY_FEED
        }
    }

    suspend fun addNewPostV2(
        groupId: Int,
        text: String,
        imagePath: String?,
        videoPath: String?,
        roadType: Int,
        whoCanComment: Int,
        media: MediaEntity? = null,
        mediaList: List<UploadMediaModel>? = null,
        event: EventEntity? = null,
        backgroundId: Int,
        fontSize: Int,
        mediaPositioning: String?
    ): ListenableWorker.Result = withContext(Dispatchers.IO) {
        try {
            var uploadId: String? = doPartialUploadIfNeeded(imagePath, videoPath, PartialUploadSourceType.POST)
            var uploadIds: List<String>? = null
            val positioningList = hashMapOf<String, MediaPositioningDto>()
            var singlePositioning : String? = mediaPositioning

            if (event == null) {
                mediaList?.let { list ->
                    uploadIds = uploadMediaParts(list, positioningList)
                }
            } else {
                uploadId = doPartialUploadIfNeeded(
                    videoPath = null,
                    imagePath = mediaList?.firstOrNull()?.mediaUriPath,
                    sourceType = PartialUploadSourceType.POST
                )
                singlePositioning = mediaList?.firstOrNull()?.dtoPositioning?.toJson()
            }

            val response = addPostUseCase.addPostV2(
                groupId = groupId,
                text = text,
                imagePath = imagePath,
                videoPath = videoPath,
                roadType = roadType,
                whoCanComment = whoCanComment,
                media = media,
                event = event,
                partialUploadId = uploadId,
                partialUploadIds = uploadIds,
                backgroundId = backgroundId,
                fontSize = fontSize,
                mediaPositioning = singlePositioning,
                mediaPositioningList = positioningList
            )

            if (response?.data != null) {
                trackNewPost(text, imagePath, videoPath, roadType)

                response.data?.let {
                    val videoLengthSec = videoPath?.let { videoPath ->
                        getVideoLengthUseCase.execute(videoPath).toInt()
                    } ?: EMPTY_VIDEO_AMPLITUDE_VALUE

                    // Amplitude post created log
                    val where = mapRoadTypeToFeedName(roadType, groupId)
                    val postType = AmplitudePropertyPostType.POST
                    val haveMusic = media != null

                    var i = 0
                    if (text.isNotBlank()) i += 1
                    if (imagePath != null) i += 1
                    if (videoPath != null) i += 1
                    if (haveMusic) i += 1

                    val contentType = when {
                        i == 1 -> AmplitudePropertyContentType.SINGLE
                        i > 1 -> AmplitudePropertyContentType.MULTIPLE
                        else -> AmplitudePropertyContentType.NONE
                    }

                    val commentSettings = when (whoCanComment) {
                        0 -> AmplitudePropertyCommentsSettings.NOBODY
                        1 -> AmplitudePropertyCommentsSettings.FOR_ALL
                        else -> AmplitudePropertyCommentsSettings.FRIENDS
                    }

                    amplitudeHelper.logPostCreated(
                        postId = it.postId,
                        authorId = getUserUidUseCase.invoke(),
                        where = where,
                        postType = postType,
                        postContentType = contentType,
                        haveText = text.isNotBlank(),
                        havePic = imagePath != null,
                        haveVideo = videoPath != null,
                        haveGif = imagePath?.contains("gif") ?: false,
                        commentsSettings = commentSettings,
                        haveMusic = haveMusic,
                        videoDurationSec = videoLengthSec,

                        haveBackground = backgroundId != 0,
                        backgroundId = backgroundId
                    )

                    val postCreationSuccess = PostActionModel.PostCreationSuccessModel(
                        postId = it.postId,
                        eventId = it.eventId
                    )
                    newPostCreated(postCreationSuccess)
                }

                Timber.e("ADD new POST RESP:${response.data}")

                return@withContext ListenableWorker.Result.success()
            } else {
                return@withContext ListenableWorker.Result.failure()
            }
        } catch (e: HttpException) {
            Timber.e("Error send post to server $e")
            return@withContext if (e.code() == 400) {
                ListenableWorker.Result.failure(
                    Data.Builder()
                        .putString(IArgContainer.ARG_ERROR_DETAILS, e.response()?.errorBody()?.string())
                        .build()
                )
            } else ListenableWorker.Result.failure()
        } catch (e: Exception) {
            Timber.e("Error send post to server $e")
            return@withContext ListenableWorker.Result.failure()
        }
    }

    suspend fun editPost(
        postId: Long,
        text: String,
        imagePath: String?,
        videoPath: String?,
        media: MediaEntity? = null,
        mediaList: List<UploadMediaModel>? = null,
        backgroundId: Int,
        fontSize: Int,
        mediaChanged: Boolean
    ): ListenableWorker.Result = withContext(Dispatchers.IO) {
        try {

            var uploadEditedIds: ArrayList<EditedAssetModel>? = null

            mediaList?.let { media ->
                uploadEditedIds = uploadEditedMediaParts(media)
            }

            val uploadId: String? = if (mediaChanged) {
                doPartialUploadIfNeeded(
                    imagePath = imagePath,
                    videoPath = videoPath,
                    sourceType = PartialUploadSourceType.POST
                )
            } else {
                null
            }

            val response = editPostUseCase.editPost(
                postId = postId,
                text = text,
                media = media,
                uploadId = uploadId,
                backgroundId = backgroundId,
                fontSize = fontSize,
                mediaChanged = mediaChanged,
                uploadEditedIds = uploadEditedIds
            )

            if (response?.data != null) {
                val data = requireNotNull(response.data)
                val postEditingComplete = PostActionModel.PostEditingCompleteModel(post = data)
                newPostCreated(postEditingComplete)

                ListenableWorker.Result.success()
            } else {
                ListenableWorker.Result.failure()
            }
        } catch (exception: HttpException) {
            if (exception.code() == 400) {
                ListenableWorker.Result.failure(
                    Data.Builder()
                        .putString(IArgContainer.ARG_ERROR_DETAILS, exception.response()?.errorBody()?.string())
                        .build()
                )
            } else ListenableWorker.Result.failure()
        } catch (exception: Exception) {
            Timber.e("Error send post to server $exception")
            return@withContext ListenableWorker.Result.failure()
        }
    }

    private suspend fun uploadMediaParts(
        list: List<UploadMediaModel>,
        positioningList: HashMap<String, MediaPositioningDto>
    ): List<String> {
        val uploadIds = arrayListOf<String>()
        list.forEach { media ->
            val uploadMultiId = makePartialMediaUpload(media)
            uploadMultiId?.let {
                positioningList[uploadMultiId] = media.dtoPositioning
                uploadIds.add(uploadMultiId)
            }
        }
        return uploadIds.toList()
    }

    private suspend fun uploadEditedMediaParts(mediaList: List<UploadMediaModel>): ArrayList<EditedAssetModel> {
        val uploadEditedIds = arrayListOf<EditedAssetModel>()
        mediaList.forEach { media ->
            media.uploadMediaId?.let { uploadId ->
                uploadEditedIds.add(EditedAssetModel(assetId = uploadId, mediaPositioning = media.dtoPositioning))
            } ?: kotlin.run {
                val uploadMediaId = makePartialMediaUpload(media)
                uploadMediaId?.let {
                    uploadEditedIds.add(
                        EditedAssetModel(uploadId = uploadMediaId, mediaPositioning = media.dtoPositioning)
                    )
                }
            }
        }
        return uploadEditedIds
    }

    private suspend fun makePartialMediaUpload(media: UploadMediaModel): String? {
        return if (media.mediaType == AttachmentPostType.ATTACHMENT_VIDEO)
            doPartialUploadIfNeeded(
                videoPath = media.mediaUriPath,
                imagePath = null,
                sourceType = PartialUploadSourceType.POST
            )
        else doPartialUploadIfNeeded(
            videoPath = null,
            imagePath = media.mediaUriPath,
            sourceType = PartialUploadSourceType.POST
        )
    }

    private suspend fun doPartialUploadIfNeeded(
        imagePath: String?,
        videoPath: String?,
        sourceType: PartialUploadSourceType
    ): String? {
        return imagePath?.let {
            partialFileUploadUseCase.invoke(File(it), IMAGE_TYPE.toMediaTypeOrNull(), sourceType)
        } ?: videoPath?.let {
            partialFileUploadUseCase.invoke(File(it), VIDEO_MP4_TYPE.toMediaTypeOrNull(), sourceType)
        }
    }

    private fun trackNewPost(text: String, imagePath: String?, videoPath: String?, roadType: Int) {
        if (text.isNotEmpty() && imagePath.isNullOrEmpty() && videoPath.isNullOrEmpty()) {
            myTracker.trackNewTextPost()
        }

        if (!imagePath.isNullOrEmpty()) {
            myTracker.trackNewImagePost()
        }

        if (!videoPath.isNullOrEmpty()) {
            myTracker.trackNewVideoPost()
        }

        if (roadType == 1) {
            myTracker.trackOwnRoadPost()
        }

        myTracker.trackNewPost()

    }

    suspend fun addPostComplaint(
        postId: Long,
        success: () -> Unit,
        fail: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val response = addPostComplaint.addPostComplaintV2(postId)
            if (response?.data != null) {
                success.invoke()
            } else {
                success.invoke()
            }
        } catch (e: Exception) {
            fail.invoke()
            e.printStackTrace()
        }
    }

    suspend fun addPostCommentComplaint(
        commentId: Long,
        success: () -> Unit,
        fail: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val response = addPostComplaint.addPostCommentComplaintV2(commentId)
            if (response?.data != null) {
                success.invoke()
            } else {
                success.invoke()
            }
        } catch (e: Exception) {
            fail.invoke()
            e.printStackTrace()
        }
    }

    suspend fun subscribeToUser(
        userId: Long,
        success: () -> Unit,
        fail: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val response = subscriptionsUseCase.addSubscription(mutableListOf(userId))
            if (response.data != null) {
                success.invoke()
            } else {
                fail.invoke()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail.invoke()
        }
    }

    fun updatePostReactions() = Unit

    suspend fun unsubscribeFromUser(
        userId: Long,
        success: () -> Unit,
        fail: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val response = subscriptionsUseCase.deleteFromSubscriptions(mutableListOf(userId))
            if (response.data != null) {
                success.invoke()
            } else {
                fail.invoke()
            }
        } catch (e: Exception) {
            Timber.e("Delete subscription failed ${e.message}")
            fail.invoke()
        }
    }

    suspend fun getPostFromNetwork(postId: Long): ResponseWrapper<Post?>? =
        withContext(Dispatchers.IO) {
            return@withContext getPost.getPostV2(postId)
        }

    suspend fun updatePostById() = withContext(Dispatchers.IO) {}

    //обработка неприемлемого контента
    private val notSensitiveContent = hashSetOf<Long>()

    override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
        postId?.let {
            notSensitiveContent.add(it)
        }
    }

    override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean {
        postId?.let {
            return notSensitiveContent.contains(it)
        }

        return false
    }

    override fun getPosts(): HashSet<Long> = notSensitiveContent

    override fun clear() {
        notSensitiveContent.clear()
    }

    companion object {
        private var subscriptionPostsWereRequestedWithinSession = false
    }
}

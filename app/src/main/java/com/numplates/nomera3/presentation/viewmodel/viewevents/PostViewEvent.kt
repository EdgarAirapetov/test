package com.numplates.nomera3.presentation.viewmodel.viewevents

import android.net.Uri
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData

sealed class PostViewEvent {

    object AddPostFailure : PostViewEvent()

    object AddPostComment : PostViewEvent()

    object ErrorPostComment : PostViewEvent()

    class ShowTextError(
        val message: String
    ) : PostViewEvent()

    class DeletePost : PostViewEvent() {
        var postId: Long? = null
    }

    class PostEditedEvent(val post: PostUIEntity) : PostViewEvent()

    class HideUserRoad(val userId: Long?) : PostViewEvent()

    class HideUserPost(val postId: Long?) : PostViewEvent()

    object SubscribePost : PostViewEvent()

    object UnsubscribePost : PostViewEvent()

    object AddComplaint : PostViewEvent()

    object AddPostCommentComplaint : PostViewEvent()

    class MarkCommentForDeletion(
        val commentID: Long,
        val whoDeleteComment: WhoDeleteComment
    ) : PostViewEvent()

    class CancelDeleteComment(
        val originalComment: CommentUIType
    ) : PostViewEvent()

    class DeleteComment(
        val commentID: Long,
        val whoDeleteComment: WhoDeleteComment
    ) : PostViewEvent()

    object GetPostsLoading : PostViewEvent()
    object GetPostsError : PostViewEvent()

    data class GetPostsEmpty(val isFirstPagePosts: Boolean) : PostViewEvent()

    class OnZeroItemLoaded(val countPosts: Int? = 0) : PostViewEvent()

    object StopOnRefresh : PostViewEvent()

    object OnScrollToTop : PostViewEvent()

    object OnRefresh : PostViewEvent()
    object EnableComments : PostViewEvent()

    object OnRoadChanged : PostViewEvent()

    class UserBlocked(var userId: Long) : PostViewEvent()

    object ComplainSuccess : PostViewEvent()
    object NoInternet : PostViewEvent()
    object NoInternetAction : PostViewEvent()
    object TryLater : PostViewEvent()
    object OnScrollToBottom : PostViewEvent()

    data class NewCommentSuccess(
        var myCommentId: Long,
        var beforeMyComment: List<CommentUIType>,
        var afterMyComment: List<CommentUIType>,
        var hasIntersection: Boolean,
        var needSmoothScroll: Boolean,
        var needToShowLastFullComment: Boolean
    ) : PostViewEvent()

    data class NewInnerCommentSuccess(
        val parentId: Long,
        val chunk: CommentChunk
    ) : PostViewEvent()

    data class ErrorInnerPagination(
        val data: CommentSeparatorEntity
    ) : PostViewEvent()

    class ErrorDeleteComment(
        val comment: CommentUIType
    ) : PostViewEvent()

    class OpenFeatureDeepLink(val deepLink: String?) : PostViewEvent()

    data class UpdatePostReaction(
        val reactionUpdate: ReactionUpdate,
        val post: PostUIEntity
    ) : PostViewEvent()

    data class MeeraUpdatePostReaction(
        val reactionUpdate: MeeraReactionUpdate,
        val post: PostUIEntity
    ) : PostViewEvent()

    data class UpdateLoadingState(
        val loadingInfo: LoadingPostVideoInfoUIModel,
        val post: PostUIEntity
    ) : PostViewEvent()

    data class UpdateUserState(
        val post: PostUIEntity
    ) : PostViewEvent()

    data class UpdateCommentReaction(
        val position: Int,
        val reactionUpdate: ReactionUpdate
    ) : PostViewEvent()

    data class MeeraUpdateCommentReaction(
        val position: Int,
        val reactionUpdate: MeeraReactionUpdate
    ) : PostViewEvent()

    data class UpdateCommentsReplyAvailability(
        val needToShowReplyBtn: Boolean
    ) : PostViewEvent()

    data class PermissionsReady(
        val permissions: UserPermissions
    ) : PostViewEvent()

    object CloseScreen : PostViewEvent()

    class OpenSupportAdminChat(val adminId: Long) : PostViewEvent()

    class OnAddStickerClick(val path: String) : PostViewEvent()

    @Deprecated("BR-29237 Не будет использоваться в новом экране, удалить, когда полностью уберём старый")
    class OnEditImageByClick(val path: String) : PostViewEvent()

    class OnEditMediaImageByClick(val media: UIAttachmentMediaModel) : PostViewEvent()

    @Deprecated("BR-29237 Не будет использоваться в новом экране, удалить, когда полностью уберём старый")
    class OnOpenImage(val path: String) : PostViewEvent()

    @Deprecated("BR-29237 Не будет использоваться в новом экране, удалить, когда полностью уберём старый")
    class OnVideoPlay(val path: String) : PostViewEvent()

    class OnOpenMedia(val position: Int, val attachmentsList: MutableList<ImageViewerData>): PostViewEvent()

    @Deprecated("BR-29237 Не будет использоваться в новом экране, удалить, когда полностью уберём старый")
    class OnEditVideoByClick(val path: String) : PostViewEvent()

    class OnEditMediaVideoByClick(val media: UIAttachmentMediaModel) : PostViewEvent()

    class OpenRepostMenu(val post: PostUIEntity) : PostViewEvent()

    class MediaAttachmentSelected(val uri: Uri): PostViewEvent()

    class CopyLinkEvent(val link: String) : PostViewEvent()

    object FinishSnippetSetupEvent : PostViewEvent()

    data class UpdateEventParticipationEvent(val post: PostUIEntity) : PostViewEvent()

    data class ShowEventSharingSuggestion(val post: PostUIEntity) : PostViewEvent()

    data class UpdateUserMomentsState(val post: PostUIEntity) : PostViewEvent()

    class ShowScreenshotPopup(
        val link: String,
        val eventIconRes: Int?,
        val eventDateAndTime: String?
    ) : PostViewEvent()

    class PostEditAvailableEvent(
        val post: PostUIEntity,
        val isEditAvailable: Boolean,
        val currentMedia: MediaAssetEntity?): PostViewEvent()

    class OpenEditPostEvent(val post: PostUIEntity) : PostViewEvent()

    class ShowAvailabilityError(val reason: NotAvailableReasonUiEntity) : PostViewEvent()

    class UpdateVolumeState(
        val volumeState: VolumeState
    ): PostViewEvent()

    class UpdateTagSpan(
        val postUpdate: UIPostUpdate.UpdateTagSpan
    ): PostViewEvent()

    class UpdatePostValues(
        val postUpdate: UIPostUpdate
    ): PostViewEvent()

    data class ShowReactionStatisticsEvent(
        val post: PostUIEntity,
        val entityType: ReactionsEntityType
    ) : PostViewEvent()

    data object PostWasDeleted : PostViewEvent()

    data object PostWasHidden : PostViewEvent()

    data object HideTopRefresh : PostViewEvent()
    data object ShowMediaPicker : PostViewEvent()
    data object HideMediaPicker : PostViewEvent()

    class UpdatePostEvent(
        val post: UIPostUpdate,
        val adapterPosition: Int = 0
    ): PostViewEvent()
}

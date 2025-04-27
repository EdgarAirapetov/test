package com.numplates.nomera3.modules.comments.ui.viewmodel

import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin

sealed class PostDetailsActions {

    class RepostClick(val post: PostUIEntity) : PostDetailsActions()

    class OnPostClick(val post: PostUIEntity) : PostDetailsActions()

    object Refresh : PostDetailsActions()

    class OnBadWordClicked(
        val post: PostUIEntity,
        val tagOrigin: TagOrigin,
        val click: SpanDataClickType.ClickBadWord
    ) : PostDetailsActions()

    object AddEmptyCommentsPlaceHolder : PostDetailsActions()

    object RemoveEmptyCommentsPlaceHolder : PostDetailsActions()

    class RefreshPost(val postId: Long?) : PostDetailsActions()

    class DeletePost(val postId: Long?) : PostDetailsActions()

    class SubscribePost(val postId: Long?, val notifyUser: Boolean = true) : PostDetailsActions()

    class UnsubscribePost(val postId: Long?) : PostDetailsActions()

    class AddComplaintPost(val postId: Long?) : PostDetailsActions()

    class AddComplaintPostComment(val commentId: Long?) : PostDetailsActions()

    class HideUserRoad(val userId: Long?) : PostDetailsActions()

    class UnsubscribeUser(
        val postId: Long?,
        val userId: Long?,
        val postOrigin: DestinationOriginEnum?,
        val fromFollowButton: Boolean,
        val approved: Boolean,
        val topContentMaker: Boolean
    ) : PostDetailsActions()

    class SubscribeUser(
        val postId: Long?,
        val userId: Long?,
        val postOrigin: DestinationOriginEnum?,
        val needToHideFollowButton: Boolean,
        val fromFollowButton: Boolean,
        val approved: Boolean,
        val topContentMaker: Boolean
    ) : PostDetailsActions()

    class BlockUser(val userId: Long?, val remoteUserId: Long?) : PostDetailsActions()

    class CopyPostLink(val postId: Long?) : PostDetailsActions()

    class GetPostDataForScreenshotPopup(val postId: Long?, val event: EventUiModel?) : PostDetailsActions()

    class CheckUpdateAvailability(val post: PostUIEntity, val currentMedia: MediaAssetEntity?) : PostDetailsActions()

    class EditPost(val post: PostUIEntity) : PostDetailsActions()

    class SaveLastPostMediaViewInfo(val lastPostMediaViewInfo: PostMediaViewInfo?) : PostDetailsActions()
}

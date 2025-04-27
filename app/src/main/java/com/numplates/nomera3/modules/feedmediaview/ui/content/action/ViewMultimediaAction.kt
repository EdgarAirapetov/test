package com.numplates.nomera3.modules.feedmediaview.ui.content.action

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

sealed class ViewMultimediaAction {
    data class SetPostData(
        val postId: Long,
        val post: PostUIEntity?,
        val postOrigin: DestinationOriginEnum?,
        val isVolumeEnabled: Boolean
    ) : ViewMultimediaAction()

    data class OnSubscribeToPost(val postId: Long) : ViewMultimediaAction()

    data class OnUnsubscribeFromPost(val postId: Long) : ViewMultimediaAction()

    object OnRepostClick : ViewMultimediaAction()
    data class OnOpenMenuClicked(val mediaAsset: MediaAssetEntity) : ViewMultimediaAction()

    data class SendAnalytic(
        val post: PostUIEntity?,
        val where: AmplitudePropertyWhere,
        val actionType: AmplitudePropertyMenuAction? = null
    ) : ViewMultimediaAction()

    data class OnRepostSuccess(val post: PostUIEntity, val repostTargetCount: Int = 1) : ViewMultimediaAction()

    data class OnShareOutsideOpened(val isOpened: Boolean) : ViewMultimediaAction()

    data class OnDownloadVideoClicked(val postId: Long, val assetId: String?) : ViewMultimediaAction()

    object OnCancelDownloadClicked : ViewMultimediaAction()

    data class OnSubscribeToUserClicked(val userId: Long, val fromMenu: Boolean = false) : ViewMultimediaAction()

    data class OnUnsubscribeFromUserClicked(val userId: Long) : ViewMultimediaAction()

    data class OnHideUserRoad(val userId: Long) : ViewMultimediaAction()

    data class AddComplaintPost(val postId: Long) : ViewMultimediaAction()

    data class OnDeletePost(val postId: Long) : ViewMultimediaAction()

    data class OnCopyPostLink(val postId: Long) : ViewMultimediaAction()

    data class SaveLastMediaViewInfo(val mediaViewInfo: PostMediaViewInfo) : ViewMultimediaAction()
}

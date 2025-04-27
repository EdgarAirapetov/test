package com.numplates.nomera3.modules.feedmediaview.ui

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

sealed class ViewMultimediaMenuItems(
    val titleResId: Int,
    val iconResId: Int,
    val iconAndTitleColor: Int = R.color.uiKitColorForegroundPrimary
) {
    data class DownloadImage(val postId: Long, val mediaAssetId: String?) :
        ViewMultimediaMenuItems(R.string.meera_save_media, R.drawable.ic_outlined_download_m)

    data class DownloadVideo(val postId: Long, val mediaAssetId: String?) :
        ViewMultimediaMenuItems(R.string.meera_save_media, R.drawable.ic_outlined_download_m)

    data class SubscribeToPost(val postId: Long) :
        ViewMultimediaMenuItems(R.string.subscribe_post_txt, R.drawable.ic_outlined_post_m)

    data class UnsubscribeFromPost(val postId: Long) :
        ViewMultimediaMenuItems(R.string.unsubscribe_post_txt, R.drawable.ic_outlined_post_delete_m)

    data class SubscribeToUser(val userId: Long) :
        ViewMultimediaMenuItems(R.string.subscribe_user_txt, R.drawable.ic_outlined_user_add_m)

    data class SharePost(val post: PostUIEntity) :
        ViewMultimediaMenuItems(R.string.general_share, R.drawable.ic_outlined_repost_m)

    data class CopyPostLink(val postId: Long) :
        ViewMultimediaMenuItems(R.string.copy_link, R.drawable.ic_outlined_copy_m)

    data class HideUserRoad(val userId: Long) :
        ViewMultimediaMenuItems(
            R.string.profile_complain_hide_all_posts, R.drawable.ic_outlined_eye_off_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        )

    data class AddComplaintPost(val postId: Long) :
        ViewMultimediaMenuItems(
            R.string.complain_about_post,
            R.drawable.ic_outlined_attention_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        )

    data class DeletePost(val postId: Long) :
        ViewMultimediaMenuItems(
            R.string.road_delete, R.drawable.ic_outlined_archive_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong
        )
}

package com.numplates.nomera3.modules.viewvideo.presentation

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

sealed class ViewVideoMenuItems(
    val titleResId: Int,
    val iconResId: Int,
    val iconAndTitleColor: Int = R.color.uiKitColorForegroundPrimary
) {
    data class DownloadVideo(val postId: Long)
        : ViewVideoMenuItems(R.string.save_to_device, R.drawable.ic_outlined_download_m)
    data class SubscribeToPost(val postId: Long) :
        ViewVideoMenuItems(R.string.subscribe_post_txt, R.drawable.ic_outlined_post_m)

    data class UnsubscribeFromPost(val postId: Long) :
        ViewVideoMenuItems(R.string.unsubscribe_post_txt, R.drawable.ic_outlined_post_delete_m)

    data class SubscribeToUser(val userId: Long) :
        ViewVideoMenuItems(R.string.subscribe_user_txt, R.drawable.ic_outlined_user_add_m)

    data class SharePost(val post: PostUIEntity) :
        ViewVideoMenuItems(R.string.general_share, R.drawable.ic_outlined_repost_m)

    data class CopyPostLink(val postId: Long) : ViewVideoMenuItems(R.string.copy_link, R.drawable.ic_outlined_copy_m)
    data class HideUserRoad(val userId: Long) :
        ViewVideoMenuItems(R.string.profile_complain_hide_all_posts, R.drawable.ic_outlined_eye_off_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong)

    data class AddComplaintPost(val postId: Long) :
        ViewVideoMenuItems(R.string.complain_about_post,
            R.drawable.ic_outlined_attention_m,
            iconAndTitleColor = R.color.uiKitColorAccentWrong)

    data class DeletePost(val postId: Long) : ViewVideoMenuItems(R.string.road_delete, R.drawable.ic_outlined_archive_m,
        iconAndTitleColor = R.color.uiKitColorAccentWrong)
}

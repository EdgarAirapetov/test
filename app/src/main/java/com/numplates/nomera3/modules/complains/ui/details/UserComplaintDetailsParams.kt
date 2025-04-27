package com.numplates.nomera3.modules.complains.ui.details

/**
 * Helper class which simplify work with complaints details params
 *
 * @param comment user's text from memo field
 * @param videoPath link to the local video file
 * @param imagePath link to the local image file
 */
class UserComplaintDetailsParams(
    val comment: String?,
    val videoPath: String?,
    val imagePath: String?,
) {

    fun hasText(): Boolean {
        return comment.isNullOrBlank().not()
    }

    fun charCount(): Int {
        return comment?.length ?: 0
    }

    fun hasMedia(): Boolean {
        return videoPath != null || imagePath != null
    }

    fun videoCount(): Int {
        return if (videoPath != null) 1 else 0
    }

    fun imageCount(): Int {
        return if (imagePath != null) 1 else 0
    }

    override fun toString(): String {
        return "UserComplaintDetailsParams(comment=$comment, videoPath=$videoPath, imagePath=$imagePath)"
    }


}

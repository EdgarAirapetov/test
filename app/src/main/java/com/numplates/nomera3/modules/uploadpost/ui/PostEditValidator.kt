package com.numplates.nomera3.modules.uploadpost.ui

import com.numplates.nomera3.modules.feed.domain.mapper.toMediaEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.presentation.utils.getTrueTextWithProfanity

object PostEditValidator {

    /**
     * Проверка на изменение поста
     */
    fun isEdited(post: PostUIEntity, uploadPost: UploadPostBundle) : Boolean {
        return isTextEdited(post, uploadPost)
            || isImageEdited(post, uploadPost)
            || isVideoEdited(post, uploadPost)
            || isMediaEdited(post, uploadPost)
            || isBackgroundEdited(post, uploadPost)
    }

    fun isMultiplePostEdited(
        post: PostUIEntity,
        uploadPost: UploadPostBundle,
        mediaInitializingInProcess: Boolean
    ): Boolean {
        return isTextEdited(post, uploadPost)
            || isMultipleMediaEdited(
                post,
                uploadPost,
                mediaInitializingInProcess
            )
            || isMediaEdited(post, uploadPost)
            || isBackgroundEdited(post, uploadPost)
    }

    fun isTextEdited(post: PostUIEntity, uploadPost: UploadPostBundle) : Boolean =
        (post.tagSpan?.getTrueTextWithProfanity() ?: post.postText) != uploadPost.text

    fun isImageEdited(post: PostUIEntity, uploadPost: UploadPostBundle) : Boolean =
        post.getImageUrl()?.fileName() != uploadPost.imagePath?.fileName()

    fun isVideoEdited(post: PostUIEntity, uploadPost: UploadPostBundle) : Boolean =
        post.getVideoUrl()?.fileName() != uploadPost.videoPath?.fileName()

    fun isMediaEdited(post: PostUIEntity, uploadPost: UploadPostBundle) : Boolean =
        post.media?.toMediaEntity()?.track != uploadPost.media?.track

    fun isMultipleMediaEdited(
        post: PostUIEntity,
        uploadPost: UploadPostBundle,
        mediaInitializingInProcess: Boolean
    ): Boolean {
        if (mediaInitializingInProcess) return false
        val sizeIsDifferent = post.assets?.size != uploadPost.mediaList?.size
        var mediaAddedOrEdited = false
        uploadPost.mediaList?.forEach { media ->
            if (media.uploadMediaId == null) {
                mediaAddedOrEdited = true
            }
        }

        return sizeIsDifferent || mediaAddedOrEdited
    }

    fun isBackgroundEdited(post: PostUIEntity, uploadPost: UploadPostBundle) : Boolean =
        post.backgroundId != uploadPost.backgroundId

    private fun String.fileName(): String = substringAfterLast('.')
}

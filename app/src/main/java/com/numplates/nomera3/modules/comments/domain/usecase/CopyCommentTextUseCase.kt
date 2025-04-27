package com.numplates.nomera3.modules.comments.domain.usecase

import android.content.ClipData
import android.content.ClipboardManager
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import javax.inject.Inject

private const val COMMENT_CLIP_LABEL = "Comment"

class CopyCommentTextUseCase @Inject constructor(
    private val clipManager: ClipboardManager
) {

    /**
     * Copy text of the comment
     *
     * @return true if text was properly copied
     */
    fun invoke(comment: CommentEntityResponse): Boolean {
        return runCatching {
            val commentText = comment.text
            if (commentText.isNullOrBlank()) return false
            val clipData = ClipData.newPlainText(COMMENT_CLIP_LABEL, commentText)
            clipManager.setPrimaryClip(clipData)
        }.isSuccess
    }
}

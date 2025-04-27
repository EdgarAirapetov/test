package com.numplates.nomera3.modules.share.domain.usecase

import android.content.ClipData
import android.content.ClipboardManager
import javax.inject.Inject

private const val POST_LINK_LABEL = "post_link"

class CopyPostLinkUseCase @Inject constructor(
    private val getPostLinkUseCase: GetPostLinkUseCase,
    private val clipManager: ClipboardManager
) {

    suspend fun invoke(postId: Long): Boolean {
        val postLink = getPostLinkUseCase.invoke(GetPostLinkParams(postId))
        if (postLink.isEmpty()) return false
        val clipData = ClipData.newPlainText(POST_LINK_LABEL, postLink)
        clipManager.setPrimaryClip(clipData)
        return true
    }

}

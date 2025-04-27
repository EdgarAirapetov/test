package com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions

import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource

fun PostUIEntity.getContentAmplitudeReactionsPropertyType(
    postType: AmplitudePropertyReactionsPostType
): AmplitudePropertyReactionsContentType {
    return if (postType == AmplitudePropertyReactionsPostType.REPOST) {
        getContentTypeFromText(postText)
    } else {
        val contentTypesCount = listOf(
            postText.isNotBlank(),
            !getImageUrl().isNullOrBlank(),
            hasPostVideo(),
            media != null
        ).count { it }
        getContentTypeFromCounter(contentTypesCount)
    }
}

fun PostUIEntity.createAmplitudeReactionsParams(originEnum: DestinationOriginEnum?): AmplitudeReactionsParams {
    val whence = originEnum.toAmplitudePropertyWhence()
    val postType = if (parentPost != null) AmplitudePropertyReactionsPostType.REPOST else AmplitudePropertyReactionsPostType.POST
    val postContentType = getContentAmplitudeReactionsPropertyType(postType)
    val where  = when {
        isEvent() -> AmplitudePropertyReactionWhere.MAP_EVENT
        hasPostVideo() -> AmplitudePropertyReactionWhere.VIDEO_POST
        else -> AmplitudePropertyReactionWhere.POST
    }
    return AmplitudeReactionsParams(
        whence = whence,
        postId = postId,
        authorId = user?.userId ?: -1L,
        postType = postType,
        postContentType = postContentType,
        haveText = postText.isNotEmpty(),
        havePic = !getImageUrl().isNullOrBlank(),
        haveVideo = hasPostVideo(),
        haveMusic = media != null,
        isEvent = isEvent(),
        where = where
    )
}

fun createAmplitudeProfileReactionsParams(
    authorId: Long?,
    originEnum: DestinationOriginEnum?,
    where: AmplitudePropertyReactionWhere
): AmplitudeReactionsParams {
    val whence = originEnum.toAmplitudePropertyWhence()
    val postType = AmplitudePropertyReactionsPostType.NONE
    val postContentType = AmplitudePropertyReactionsContentType.NONE_ZERO

    return AmplitudeReactionsParams(
        whence = whence,
        postId = 0,
        authorId = authorId ?: -1L,
        postType = postType,
        postContentType = postContentType,
        haveText = false,
        havePic = true,
        haveVideo = false,
        haveMusic = false,
        isEvent = false,
        where = where
    )
}

fun getAmplitudePostReactionName(reactionType: ReactionType): AmplitudePropertyReactionsType {
    return when (reactionType) {
        ReactionType.Crying -> AmplitudePropertyReactionsType.SAD
        ReactionType.Facepalm -> AmplitudePropertyReactionsType.OOPS
        ReactionType.Fire -> AmplitudePropertyReactionsType.FIRE
        ReactionType.GreenLight -> AmplitudePropertyReactionsType.LIKE
        ReactionType.InLove -> AmplitudePropertyReactionsType.WOW
        ReactionType.LaughTears -> AmplitudePropertyReactionsType.HA
        ReactionType.RedLight -> AmplitudePropertyReactionsType.SHIT
        ReactionType.Amazing -> AmplitudePropertyReactionsType.OHO
        ReactionType.Morning -> AmplitudePropertyReactionsType.MORNING
        ReactionType.Evening -> AmplitudePropertyReactionsType.NIGHT
    }
}

fun PostUIEntity.createReactionSourcePost(reactionHolderViewId: ContentActionBar.ReactionHolderViewId): ReactionSource.Post {
    return ReactionSource.Post(
        postId = postId,
        reactionHolderViewId = reactionHolderViewId,
        originEnum = null
    )
}

fun PostUIEntity.createMeeraReactionSourcePost(reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) =
    MeeraReactionSource.Post(
        postId = postId,
        reactionHolderViewId = reactionHolderViewId,
        originEnum = null
    )




private fun getContentTypeFromCounter(contentTypesCount: Int): AmplitudePropertyReactionsContentType {
    return when {
        contentTypesCount == 1 -> AmplitudePropertyReactionsContentType.SINGLE
        contentTypesCount > 1 -> AmplitudePropertyReactionsContentType.MULTIPLE
        else -> AmplitudePropertyReactionsContentType.NONE
    }
}

private fun getContentTypeFromText(text: String?): AmplitudePropertyReactionsContentType {
    return when {
        !text.isNullOrBlank() -> AmplitudePropertyReactionsContentType.SINGLE
        else -> AmplitudePropertyReactionsContentType.NONE
    }
}

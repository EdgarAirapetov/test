package com.numplates.nomera3.modules.baseCore.helper.amplitude

import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.network.TYPE_REPOST
import com.numplates.nomera3.data.network.getPostListAdapterLegacyType

class AnalyticsPost(
    val postId: Long,
    val postType: AmplitudePropertyPostType,
    val contentProperty: AmplitudePropertyContentType,
    val haveText: Boolean,
    val havePic: Boolean,
    val haveVideo: Boolean,
    val haveGif: Boolean,
    val commentsSettings: AmplitudePropertyCommentsSettings,
    val haveMusic: Boolean,
    val videoDuration: Int
)


@Suppress("UselessCallOnNotNull")
fun Post.toAnalyticPost(): AnalyticsPost {

    val postType = when (getPostListAdapterLegacyType(this)) {
        TYPE_REPOST -> AmplitudePropertyPostType.REPOST
        else -> AmplitudePropertyPostType.POST
    }

    val commentsSettingAmplitudeProperty = when (commentAvailability.orEmpty()) {
        AmplitudePropertyCommentsSettings.NOBODY._value -> AmplitudePropertyCommentsSettings.NOBODY
        AmplitudePropertyCommentsSettings.FRIENDS._value -> AmplitudePropertyCommentsSettings.FRIENDS
        else -> AmplitudePropertyCommentsSettings.FOR_ALL
    }

    val counter = listOf(
            !text.isNullOrBlank(),
            !image.isNullOrBlank(),
            hasPostVideo(),
            hasPostGif(),
            mediaEntity != null,
    ).count { it }

    val contentAmplitudePropertyType = when (counter) {
        1 -> AmplitudePropertyContentType.SINGLE
        0 -> AmplitudePropertyContentType.NONE
        else -> AmplitudePropertyContentType.MULTIPLE
    }

    return AnalyticsPost(
            postId = id,
            postType = postType,
            contentProperty = contentAmplitudePropertyType,
            haveText = !text.isNullOrBlank(),
            havePic = !image.isNullOrBlank(),
            haveVideo = hasPostVideo(),
            haveGif = hasPostGif(),
            commentsSettings = commentsSettingAmplitudeProperty,
            haveMusic = mediaEntity != null,
            videoDuration = videoDurationInSeconds ?: 0
    )
}
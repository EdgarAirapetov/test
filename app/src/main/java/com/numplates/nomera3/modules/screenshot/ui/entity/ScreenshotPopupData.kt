package com.numplates.nomera3.modules.screenshot.ui.entity

import android.os.Parcelable
import com.meera.db.models.message.ParsedUniquename
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenshotPopupData(
    val title: String,
    val description: String,
    val buttonTextStringRes: Int,
    var link: String? = null,
    val additionalInfo: String? = null,
    val imageLink: String? = null,
    val eventIconRes: Int? = null,
    val isDeleted: Boolean? = null,
    val isVipUser: Boolean? = null,
    val isApprovedUser: Boolean? = null,
    val isInterestingAuthor: Boolean? = null,
    val profileId: Long = 0,
    val eventId: Long = 0,
    val momentId: Long = 0,
    val postId: Long = 0,
    val communityId: Long = 0,
    val tagSpan: ParsedUniquename? = null,
    val screenshotPlace: ScreenshotPlace
) : Parcelable

@Parcelize
enum class ScreenshotPlace : Parcelable {
    OWN_PROFILE,
    USER_PROFILE,
    MAP_EVENT,
    POST_EVENT,
    MOMENT,
    FEED_POST,
    COMMUNITY,
    COMMUNITY_POST,
    OTHER;
}

package com.numplates.nomera3.modules.baseCore.helper.amplitude.comments

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NONE_VALUE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyPostWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyPostWhere
import javax.inject.Inject

interface AmplitudeCommentsAnalytics {

    /**
     * Пользователь совершил действие в меню комментария
     */
    fun logCommentMenuAction(
        where: AmplitudePropertyCommentWhere,
        action: AmplitudePropertyCommentMenuAction,
        whence: AmplitudePropertyWhence
    )

    /**
     * Пользователь открыл экран детального просмотра поста
     */
    fun logOpenPost(
        postId: Long,
        authorId: Long,
        momentId: Long?,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        where: AmplitudePropertyPostWhere,
        commentCount: Int,
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        haveMusic: Boolean,
        recFeed: Boolean,
        whence: AmplitudePropertyPostWhence
    )

    /**
     * Пользователь открыл меню при лонг тапе на коментарий
     */
    fun logOpenCommentOptionsMenu()

    /**
     * Пользователь отправил комментарий к посту или моменту
     */
    fun logSentComment(
        postId: Long,
        authorId: Long,
        commentorId: Long,
        momentId: Long?,
        commentType: AmplitudePropertyCommentType,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        where: AmplitudePropertyWhere,
        whence: AmplitudePropertyWhence,
        recFeed: Boolean
    )

}

class AmplitudeCommentsAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeCommentsAnalytics {
    override fun logCommentMenuAction(
        where: AmplitudePropertyCommentWhere,
        action: AmplitudePropertyCommentMenuAction,
        whence: AmplitudePropertyWhence
    ) {
        delegate.logEvent(
            eventName = AmplitudeCommentEventName.COMMENT_MENU_ACTION,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(action)
                    addProperty(whence)
                }
            }
        )
    }

    override fun logOpenPost(
        postId: Long,
        authorId: Long,
        momentId: Long?,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        where: AmplitudePropertyPostWhere,
        commentCount: Int,
        haveText: Boolean,
        havePic: Boolean,
        haveVideo: Boolean,
        haveGif: Boolean,
        haveMusic: Boolean,
        recFeed: Boolean,
        whence: AmplitudePropertyPostWhence
    ) {
        delegate.logEvent(
            eventName = AmplitudeCommentEventName.POST_OPEN,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentId ?: NONE_VALUE)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(postType)
                    addProperty(where)
                    addProperty(postContentType)
                    addProperty(AmplitudePropertyNameConst.HAVE_TEXT, haveText)
                    addProperty(AmplitudePropertyNameConst.HAVE_PIC, havePic)
                    addProperty(AmplitudePropertyNameConst.HAVE_VIDEO, haveVideo)
                    addProperty(AmplitudePropertyNameConst.HAVE_GIF, haveGif)
                    addProperty(AmplitudePropertyNameConst.HAVE_MUSIC, haveMusic)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(AmplitudePropertyNameConst.COMMENT_COUNT, commentCount)
                    addProperty(whence)
                }
            }
        )
    }

    override fun logOpenCommentOptionsMenu() {
        delegate.logEvent(eventName = AmplitudeCommentEventName.COMMENT_MENU_OPEN)
    }

    override fun logSentComment(
        postId: Long,
        authorId: Long,
        commentorId: Long,
        momentId: Long?,
        commentType: AmplitudePropertyCommentType,
        postType: AmplitudePropertyPostType,
        postContentType: AmplitudePropertyContentType,
        where: AmplitudePropertyWhere,
        whence: AmplitudePropertyWhence,
        recFeed: Boolean
    ) {
        delegate.logEvent(
            eventName = AmplitudeCommentEventName.COMMENT_SENT,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.POST_ID, postId)
                    addProperty(commentType)
                    addProperty(postType)
                    addProperty(postContentType)
                    addProperty(where)
                    addProperty(whence)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentId ?: NONE_VALUE)
                    addProperty(AmplitudePropertyNameConst.AUTHOR_ID, authorId)
                    addProperty(AmplitudePropertyNameConst.COMMENTOR_ID, commentorId)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                }
            }
        )
    }

}

package com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_AUTHOR_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_HAVE_MUSIC
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_HAVE_PIC
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_HAVE_TEXT
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_HAVE_VIDEO
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_POST_ID
import javax.inject.Inject

interface AmplitudeReactions {
    /**
     * Пользователь убрал лайк или реакцию
     */
    fun unlikeAction(
        userId: Long,
        actionType: AmplitudePropertyReactionsType,
        reactionsParams: AmplitudeReactionsParams,
        recFeed: Boolean,
        where: AmplitudePropertyReactionWhere
    )

    /**
     * Пользователь совершил активное действие (лайк/реакция)
     */
    fun likeAction(
        userId: Long,
        actionType: AmplitudePropertyReactionsType,
        reactionsParams: AmplitudeReactionsParams,
        recFeed: Boolean,
        where: AmplitudePropertyReactionWhere
    )

    /**
     * Пользователь тапнул на статистику реакций
     */
    fun statisticReactionsTap(
        where: AmplitudePropertyReactionWhere,
        whence: AmplitudePropertyWhence,
        recFeed: Boolean
    )
}

data class AmplitudeReactionsParams(
    val whence: AmplitudePropertyWhence,
    val postId: Long = 0,
    val momentId: Long = 0,
    val authorId: Long,
    val postType: AmplitudePropertyReactionsPostType,
    val postContentType: AmplitudePropertyReactionsContentType,
    val haveText: Boolean,
    val havePic: Boolean,
    val haveVideo: Boolean,
    val haveMusic: Boolean,
    val isEvent: Boolean,
    val where: AmplitudePropertyReactionWhere
)

class AmplitudeHelperReactionsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeReactions {

    override fun likeAction(
        userId: Long,
        actionType: AmplitudePropertyReactionsType,
        reactionsParams: AmplitudeReactionsParams,
        recFeed: Boolean,
        where: AmplitudePropertyReactionWhere
    ) {
        delegate.logEvent(
            eventName = ReactionsConstants.LIKE_ACTION,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(actionType)
                    addProperty(REACTIONS_POST_ID, reactionsParams.postId)
                    addProperty(REACTIONS_AUTHOR_ID, reactionsParams.authorId)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, reactionsParams.momentId)
                    addProperty(reactionsParams.postType)
                    addProperty(reactionsParams.postContentType)
                    addProperty(reactionsParams.whence)
                    addProperty(REACTIONS_HAVE_TEXT, reactionsParams.haveText)
                    addProperty(REACTIONS_HAVE_PIC, reactionsParams.havePic)
                    addProperty(REACTIONS_HAVE_VIDEO, reactionsParams.haveVideo)
                    addProperty(REACTIONS_HAVE_MUSIC, reactionsParams.haveMusic)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(where)
                }
            }
        )
    }

    override fun unlikeAction(
        userId: Long,
        actionType: AmplitudePropertyReactionsType,
        reactionsParams: AmplitudeReactionsParams,
        recFeed: Boolean,
        where: AmplitudePropertyReactionWhere
    ) {
        delegate.logEvent(
            eventName = ReactionsConstants.UNLIKE_ACTION,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(actionType)
                    addProperty(REACTIONS_POST_ID, reactionsParams.postId)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, reactionsParams.momentId)
                    addProperty(reactionsParams.postType)
                    addProperty(reactionsParams.postContentType)
                    addProperty(reactionsParams.whence)
                    addProperty(REACTIONS_HAVE_TEXT, reactionsParams.haveText)
                    addProperty(REACTIONS_HAVE_PIC, reactionsParams.havePic)
                    addProperty(REACTIONS_HAVE_VIDEO, reactionsParams.haveVideo)
                    addProperty(REACTIONS_HAVE_MUSIC, reactionsParams.haveMusic)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                    addProperty(where)
                }
            }
        )
    }

    override fun statisticReactionsTap(where: AmplitudePropertyReactionWhere, whence: AmplitudePropertyWhence, recFeed: Boolean) {
        delegate.logEvent(
            eventName = ReactionsConstants.STATISTIC_REACTIONS_TAP,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(whence)
                    addProperty(AmplitudePropertyNameConst.REC_FEED, recFeed)
                }
            }
        )
    }
}

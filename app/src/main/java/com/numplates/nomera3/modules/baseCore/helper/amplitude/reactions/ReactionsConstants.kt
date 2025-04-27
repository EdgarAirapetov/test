package com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_ACTION_TYPE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_POST_CONTENT_TYPE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyNameConst.REACTIONS_POST_TYPE

object AmplitudePropertyNameConst {
    const val REACTIONS_ACTION_TYPE = "action type" // Отмечаем какое событие убрал пользователь
    const val REACTIONS_POST_ID = "post id" // Отмечаем id поста
    const val REACTIONS_AUTHOR_ID = "author id" // Отмечаем id автора публикации
    const val REACTIONS_POST_TYPE = "post type" // Отмечаем, это пост, или репост чужого поста
    const val REACTIONS_POST_CONTENT_TYPE = "post content type" // Содержание поста
    const val REACTIONS_HAVE_TEXT = "have text" // Содержит ли пост тип контента – текст
    const val REACTIONS_HAVE_PIC = "have pic" // Содержит ли пост тип контента – изображение
    const val REACTIONS_HAVE_VIDEO = "have video" // Содержит ли пост тип контента – видео
    const val REACTIONS_HAVE_MUSIC = "have music" // Содержит ли пост тип контента - музыка
    const val REACTIONS_REC_FEED = "rec feed" // Содержит ли пост тип контента - музыка
}

enum class ReactionsConstants(
    private val event: String
) : AmplitudeName {

    STATISTIC_REACTIONS_TAP("statistic reactions tap"),
    LIKE_ACTION("like action"),
    UNLIKE_ACTION("unlike action");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyReactionsType(val property: String) : AmplitudeProperty {
    LIKE("like"),
    SHIT("shit"),
    SAD("sad"),
    WOW("wow"),
    HA("ha"),
    FIRE("fire"),
    OOPS("oops"),
    OHO("oho"),
    MORNING("good morning"),
    NIGHT("good night");

    override val _name: String
        get() = REACTIONS_ACTION_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyReactionsContentType(val property: String) : AmplitudeProperty {
    SINGLE("single"),
    MULTIPLE("multiple"),
    NONE_ZERO("0"),
    NONE("none");

    override val _value: String
        get() = property

    override val _name: String
        get() = REACTIONS_POST_CONTENT_TYPE
}

enum class AmplitudePropertyReactionsPostType(val property: String) : AmplitudeProperty {
    POST("post"),
    REPOST("repost"),
    NONE("0");

    override val _value: String
        get() = property

    override val _name: String
        get() = REACTIONS_POST_TYPE
}

enum class AmplitudePropertyReactionWhere(val property: String) : AmplitudeProperty {
    POST("post"),
    COMMENT("comment"),
    MAP_EVENT("map event"),
    VIDEO_POST("video post"),
    AVATAR("avatar"),
    PHOTO_IN_GALLERY("photo in gallery"),
    MOMENT("moment");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

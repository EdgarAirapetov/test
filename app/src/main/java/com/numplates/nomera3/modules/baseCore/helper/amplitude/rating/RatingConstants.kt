package com.numplates.nomera3.modules.baseCore.helper.amplitude.rating

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

const val RATING_NO_TEXT_REVIEW = "none"

object AmplitudePropertyNameConst {
    const val RATING_ACTION_TYPE = "action type" // Отмечаем, какое действие совершил пользователь
    const val RATING_STARS_AMOUNT = "rating" // Отмечаем, сколько звезд поставил пользователь в модуле
    const val RATING_USER_ID = "user id" // Отмечаем id пользователя
    const val RATING_RATING_CHANGE = "rating change" // Изменил ли пользователь оценку после 1 экрана модуля оценки
    const val RATING_REVIEW = "review" // Оставил ли пользователь отзыв
    const val RATING_TEXT_REVIEW = "text review" // Присылать текст, который написал пользователь в отзыве
    const val RATING_WHERE = "where" // Где пользователь совершил действие с модулем оценки приложения
}

enum class RatingConstants(
    private val event: String
) : AmplitudeName {

    MODULE_RATING_FIRST_ACTION("module rating 1 action"),
    MODULE_RATING_SECOND_ACTION("module rating 2 action"),
    MODULE_RATING_THIRD_ACTION("module rating 3 action");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyRatingActionType(val property: String) : AmplitudeProperty {

    CLOSE("close"),
    SEND("send");

    override val _name: String
        get() = AmplitudePropertyNameConst.RATING_ACTION_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRatingStarsAmount(val property: String) : AmplitudeProperty {

    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5");

    override val _name: String
        get() = AmplitudePropertyNameConst.RATING_STARS_AMOUNT

    override val _value: String
        get() = property
}

enum class AmplitudePropertyRatingWhere(val property: String) : AmplitudeProperty {

    SETTINGS("settings"),
    FEED("feed"),
    GIFT_SEND("gift send"),
    GIFT_RECEIVE("gift receive"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyNameConst.RATING_WHERE

    override val _value: String
        get() = property
}

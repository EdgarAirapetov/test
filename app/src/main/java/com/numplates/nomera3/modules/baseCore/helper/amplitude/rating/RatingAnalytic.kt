package com.numplates.nomera3.modules.baseCore.helper.amplitude.rating

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyNameConst.RATING_RATING_CHANGE
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyNameConst.RATING_REVIEW
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyNameConst.RATING_TEXT_REVIEW
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyNameConst.RATING_USER_ID
import javax.inject.Inject

interface AmplitudeRating {
    /**
     * Пользователь совершил действие на первом экране модуля оценки приложения "Нравится приложение"
     */
    fun ratingFirstAction(
        actionType: AmplitudePropertyRatingActionType,
        rating: AmplitudePropertyRatingStarsAmount,
        userId: Long,
        where: AmplitudePropertyRatingWhere
    )

    /**
     * Пользователь совершил действие на втором экране модуля оценки приложения "Мы станем еще лучше"
     */
    fun ratingSecondAction(
        actionType: AmplitudePropertyRatingActionType,
        rating: AmplitudePropertyRatingStarsAmount,
        ratingChange: Boolean,
        review: Boolean,
        textReview: String,
        userId: Long,
        where: AmplitudePropertyRatingWhere
    )

    /**
     * Пользователь совершил действие на третьем экране модуля оценки приложения "Оцени приложение в ..."
     */
    fun ratingThirdAction(
        actionType: AmplitudePropertyRatingActionType,
        where: AmplitudePropertyRatingWhere,
        userId: Long
    )
}

class AmplitudeHelperRatingImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeRating {

    override fun ratingFirstAction(
        actionType: AmplitudePropertyRatingActionType,
        rating: AmplitudePropertyRatingStarsAmount,
        userId: Long,
        where: AmplitudePropertyRatingWhere
    ) {
        delegate.logEvent(
            eventName = RatingConstants.MODULE_RATING_FIRST_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(rating)
                    addProperty(RATING_USER_ID, userId)
                    addProperty(where)
                }
            }
        )
    }

    override fun ratingSecondAction(
        actionType: AmplitudePropertyRatingActionType,
        rating: AmplitudePropertyRatingStarsAmount,
        ratingChange: Boolean,
        review: Boolean,
        textReview: String,
        userId: Long,
        where: AmplitudePropertyRatingWhere
    ) {
        delegate.logEvent(
            eventName = RatingConstants.MODULE_RATING_SECOND_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(rating)
                    addProperty(RATING_RATING_CHANGE, ratingChange)
                    addProperty(RATING_REVIEW, review)
                    addProperty(RATING_TEXT_REVIEW, textReview)
                    addProperty(RATING_USER_ID, userId)
                    addProperty(where)
                }
            }
        )
    }

    override fun ratingThirdAction(
        actionType: AmplitudePropertyRatingActionType,
        where: AmplitudePropertyRatingWhere,
        userId: Long
    ) {
        delegate.logEvent(
            eventName = RatingConstants.MODULE_RATING_THIRD_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(where)
                    addProperty(RATING_USER_ID, userId)
                }
            }
        )
    }
}

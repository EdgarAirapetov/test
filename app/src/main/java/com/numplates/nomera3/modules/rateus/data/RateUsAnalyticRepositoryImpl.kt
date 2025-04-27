package com.numplates.nomera3.modules.rateus.data

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingStarsAmount
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudeRating
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.RATING_NO_TEXT_REVIEW
import javax.inject.Inject

interface RateUsAnalyticRepository {
    suspend fun rateUs(rateUsAnalyticsRating: RateUsAnalyticsRating)
}

@AppScope
class RateUsAnalyticRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings,
    private val amplitudeRating: AmplitudeRating
) : RateUsAnalyticRepository {

    override suspend fun rateUs(rateUsAnalyticsRating: RateUsAnalyticsRating) {
        when (rateUsAnalyticsRating) {
            is RateUsAnalyticsRating.First -> {
                amplitudeRatingFirstAction(
                    rating = rateUsAnalyticsRating.rating,
                    amplitudeWhere = rateUsAnalyticsRating.amplitudeWhere,
                    actionType = rateUsAnalyticsRating.actionType
                )
            }
            is RateUsAnalyticsRating.Second -> {
                amplitudeRatingSecondAction(
                    rating = rateUsAnalyticsRating.rating,
                    amplitudeWhere = rateUsAnalyticsRating.amplitudeWhere,
                    actionType = rateUsAnalyticsRating.actionType,
                    rawReviewText = rateUsAnalyticsRating.rawReviewText,
                    amplitudeRatingChange = rateUsAnalyticsRating.amplitudeRatingChange
                )
            }
            is RateUsAnalyticsRating.Third -> {
                amplitudeRatingThirdAction(
                    amplitudeWhere = rateUsAnalyticsRating.amplitudeWhere,
                    actionType = rateUsAnalyticsRating.actionType
                )
            }
        }
    }

    fun amplitudeRatingFirstAction(
        rating: Int,
        amplitudeWhere: AmplitudePropertyRatingWhere?,
        actionType: AmplitudePropertyRatingActionType
    ) {
        amplitudeWhere ?: return

        amplitudeRating.ratingFirstAction(
            actionType = actionType,
            rating = rating.toAmplitudePropertyRatingStarsAmount(),
            userId = appSettings.readUID(),
            where = amplitudeWhere
        )
    }

    fun amplitudeRatingSecondAction(
        rating: Int,
        rawReviewText: String,
        amplitudeRatingChange: Boolean,
        amplitudeWhere: AmplitudePropertyRatingWhere?,
        actionType: AmplitudePropertyRatingActionType
    ) {
        amplitudeWhere ?: return

        val reviewText = rawReviewText.ifBlank { RATING_NO_TEXT_REVIEW }
        amplitudeRating.ratingSecondAction(
            actionType = actionType,
            rating = rating.toAmplitudePropertyRatingStarsAmount(),
            ratingChange = amplitudeRatingChange,
            review = rawReviewText.isNotBlank(),
            textReview = reviewText,
            userId = appSettings.readUID(),
            where = amplitudeWhere
        )
    }

    fun amplitudeRatingThirdAction(
        amplitudeWhere: AmplitudePropertyRatingWhere?,
        actionType: AmplitudePropertyRatingActionType
    ) {
        amplitudeWhere ?: return

        amplitudeRating.ratingThirdAction(
            actionType = actionType,
            where = amplitudeWhere,
            userId = appSettings.readUID(),
        )
    }

    private fun Int.toAmplitudePropertyRatingStarsAmount(): AmplitudePropertyRatingStarsAmount {
        return when (this) {
            1 -> AmplitudePropertyRatingStarsAmount.ONE
            2 -> AmplitudePropertyRatingStarsAmount.TWO
            3 -> AmplitudePropertyRatingStarsAmount.THREE
            4 -> AmplitudePropertyRatingStarsAmount.FOUR
            5 -> AmplitudePropertyRatingStarsAmount.FIVE
            else -> AmplitudePropertyRatingStarsAmount.ZERO
        }
    }

}

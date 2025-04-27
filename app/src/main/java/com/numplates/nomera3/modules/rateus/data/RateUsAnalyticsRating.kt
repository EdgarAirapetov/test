package com.numplates.nomera3.modules.rateus.data

import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere

sealed class RateUsAnalyticsRating {
    data class First(
        val rating: Int,
        val amplitudeWhere: AmplitudePropertyRatingWhere,
        val actionType: AmplitudePropertyRatingActionType
    ) : RateUsAnalyticsRating()

    data class Second(
        val rating: Int,
        val rawReviewText: String,
        val amplitudeRatingChange: Boolean,
        val amplitudeWhere: AmplitudePropertyRatingWhere,
        val actionType: AmplitudePropertyRatingActionType
    ) : RateUsAnalyticsRating()

    data class Third(
        val amplitudeWhere: AmplitudePropertyRatingWhere,
        val actionType: AmplitudePropertyRatingActionType
    ) : RateUsAnalyticsRating()
}

package com.numplates.nomera3.modules.userprofile.profilestatistics.data.mapper

import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.ButtonContentModel
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.ButtonContentResponse
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.ProfileStatisticsTrend
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideResponse
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlideModel
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListModel
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListResponse
import javax.inject.Inject

class SlidesMapper @Inject constructor() {

    fun mapSlidesListModel(slidesListResponse: SlidesListResponse) = SlidesListModel(
        slidesListResponse.createdAt,
        mapSlidesModel(slidesListResponse.slides)
    )

    private fun mapSlidesModel(slides: List<SlideResponse>) = slides.map { slideResponse ->
        val trend = when {
            slideResponse.growth == null -> ProfileStatisticsTrend.SAME
            slideResponse.growth > 0 -> ProfileStatisticsTrend.POSITIVE
            slideResponse.growth < 0 -> ProfileStatisticsTrend.NEGATIVE
            slideResponse.growth == 0L -> ProfileStatisticsTrend.SAME
            else -> ProfileStatisticsTrend.SAME
        }
        SlideModel(
            type = slideResponse.type,
            count = slideResponse.count,
            growth = slideResponse.growth,
            title = slideResponse.title ?: "",
            text = slideResponse.text ?: "",
            button = mapButtonContentModel(slideResponse.button),
            imageUrl = slideResponse.imageUrl,
            trend = trend
        )
    }

    private fun mapButtonContentModel(content: ButtonContentResponse?) = ButtonContentModel(
        text = content?.text ?: "",
        link = content?.link
    )

}
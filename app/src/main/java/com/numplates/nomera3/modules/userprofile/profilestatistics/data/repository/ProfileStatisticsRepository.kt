package com.numplates.nomera3.modules.userprofile.profilestatistics.data.repository

import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListResponse

interface ProfileStatisticsRepository {

    fun getSlides(): SlidesListResponse?

    fun setSlides(slides: SlidesListResponse?)

}
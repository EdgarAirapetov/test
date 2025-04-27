package com.numplates.nomera3.modules.userprofile.profilestatistics.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListResponse
import javax.inject.Inject

@AppScope
class ProfileStatisticsRepositoryImpl @Inject constructor() : ProfileStatisticsRepository {

    //Слайды хранятся в переменной, поскольку нам не надо хранить их дольше времени работы приложения
    private var slidesListResponse: SlidesListResponse? = null

    override fun getSlides(): SlidesListResponse? {
        return slidesListResponse
    }

    override fun setSlides(slides: SlidesListResponse?) {
        this.slidesListResponse = slides
    }
}

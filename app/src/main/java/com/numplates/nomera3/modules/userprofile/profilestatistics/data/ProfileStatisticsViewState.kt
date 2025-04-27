package com.numplates.nomera3.modules.userprofile.profilestatistics.data

import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListModel

sealed class ProfileStatisticsViewState {
    data class Data(val slidesListModel: SlidesListModel, val currentSlideIndex: Int = 0) :
        ProfileStatisticsViewState()

    object Empty : ProfileStatisticsViewState()
}
package com.numplates.nomera3.modules.moments.show.presentation.data

data class MomentsIndicatorParams(
    val hasMoments: Boolean,
    val hasNewMoments: Boolean,
    val isPremiumAccountType: Boolean = false,
    val accountColor: Int? = null,
    val alwaysShowIndicator: Boolean = false
)

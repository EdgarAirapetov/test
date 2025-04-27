package com.numplates.nomera3.modules.peoples.ui.entity

import androidx.annotation.StringRes

data class PeopleOnboardingUiState(
    @StringRes val buttonTextRes: Int = -1,
    val pageIndicatorVisibility: Boolean = false
)

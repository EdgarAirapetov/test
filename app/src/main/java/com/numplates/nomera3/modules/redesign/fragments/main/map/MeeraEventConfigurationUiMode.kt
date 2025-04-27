package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.net.Uri

sealed interface MeeraEventConfigurationUiMode {
    data object ONBOARDING : MeeraEventConfigurationUiMode
    data object OPEN : MeeraEventConfigurationUiMode
    data object CLOSED : MeeraEventConfigurationUiMode
    data object EMPTY : MeeraEventConfigurationUiMode
    data object HIDDEN : MeeraEventConfigurationUiMode
    data object FIRST_STEP : MeeraEventConfigurationUiMode
    data object STEP1_FINISHED : MeeraEventConfigurationUiMode
    data class STEP2_FINISHED(val imageUri: Uri? = null) : MeeraEventConfigurationUiMode
}

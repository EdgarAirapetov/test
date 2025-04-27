package com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar

import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType

interface PostAvatarAlertListener {
    fun onPublishOptionsSelected(
        imagePath: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int,
        amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType
    )
}


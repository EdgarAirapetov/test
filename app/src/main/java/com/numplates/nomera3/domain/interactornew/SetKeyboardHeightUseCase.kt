package com.numplates.nomera3.domain.interactornew

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

class SetKeyboardHeightUseCase @Inject constructor(
    private val appSettings: AppSettings
) {
    fun invoke(height: Int) {
        appSettings.keyboardHeight = height
    }
}

package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class ClearMediaContentUseCase @Inject constructor(
    private val repository: MediaKeyboardPickerRepository
) {
    fun invoke() = repository.clearMediaContent()
}

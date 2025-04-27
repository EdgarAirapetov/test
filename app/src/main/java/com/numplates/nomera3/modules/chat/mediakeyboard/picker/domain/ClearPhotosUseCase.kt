package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class ClearPhotosUseCase @Inject constructor(
    private val mediaKeyboardPickerRepository: MediaKeyboardPickerRepository
) {

    fun invoke() {
        mediaKeyboardPickerRepository.clear()
    }

}
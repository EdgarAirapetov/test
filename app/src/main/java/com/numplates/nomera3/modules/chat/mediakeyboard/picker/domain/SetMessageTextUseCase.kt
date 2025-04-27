package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class SetMessageTextUseCase @Inject constructor(
    private val mediaKeyboardPickerRepository: MediaKeyboardPickerRepository
) {

    fun invoke(roomId: Long, text: String) {
        mediaKeyboardPickerRepository.setMessageText(roomId, text)
    }

}

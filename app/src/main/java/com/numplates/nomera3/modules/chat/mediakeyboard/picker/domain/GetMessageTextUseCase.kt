package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import androidx.lifecycle.LiveData
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.TemporaryMessageText
import javax.inject.Inject

class GetMessageTextUseCase @Inject constructor(
    private val mediaKeyboardPickerRepository: MediaKeyboardPickerRepository
) {

    fun invoke(): LiveData<TemporaryMessageText> {
        return mediaKeyboardPickerRepository.messageTextLiveData
    }

}

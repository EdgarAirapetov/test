package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import androidx.lifecycle.LiveData
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val mediaKeyboardPickerRepository: MediaKeyboardPickerRepository
) {

    fun invoke(): LiveData<Set<String>> = mediaKeyboardPickerRepository.totalMediaListLive

}

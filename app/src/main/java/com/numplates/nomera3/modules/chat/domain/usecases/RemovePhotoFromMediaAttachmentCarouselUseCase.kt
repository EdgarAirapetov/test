package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class RemovePhotoFromMediaAttachmentCarouselUseCase @Inject constructor(
    private val repository: MediaKeyboardPickerRepository
) {

    fun invoke(uri: String) {
        return repository.removePhotoFromTotalMedia(uri)
    }

}

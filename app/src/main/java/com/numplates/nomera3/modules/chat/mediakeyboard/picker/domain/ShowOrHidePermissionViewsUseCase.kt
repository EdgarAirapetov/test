package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class ShowOrHidePermissionViewsUseCase @Inject constructor(
    private val repository: MediaKeyboardPickerRepository
) {
    fun invoke(isShow: Boolean) = repository.permissionViewsVisibilityChanged(isShow)
}

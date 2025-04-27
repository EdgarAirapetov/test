package com.numplates.nomera3.modules.chat.mediakeyboard.picker.domain

import com.meera.core.base.enums.PermissionState
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.repository.MediaKeyboardPickerRepository
import javax.inject.Inject

class SetPermissionViewStateUseCase @Inject constructor(
    private val repository: MediaKeyboardPickerRepository
) {
    fun invoke(permissionState: PermissionState) = repository.permissionViewStateChanged(permissionState)
}

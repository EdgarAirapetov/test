package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import javax.inject.Inject

class CheckPostNowUploadingUseCase @Inject constructor(
    private val uploadRepository: UploadRepository
) {

    fun invoke(): Boolean {
        return uploadRepository.isNowUploading()
    }
}

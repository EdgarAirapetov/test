package com.numplates.nomera3.domain.interactornew

import com.meera.db.models.UploadBundle
import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import javax.inject.Inject

class UploadPostByTypeUseCase @Inject constructor(
    private val uploadRepository: UploadRepository
) {
    fun invoke(type: UploadType, uploadBundle: UploadBundle) {
        uploadRepository.upload(type, uploadBundle)
    }
}

package com.numplates.nomera3.modules.upload.domain.usecase.post

import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import javax.inject.Inject

class GetUploadStateUseCase @Inject constructor(
    private val repository: UploadRepository
) {
    fun invoke() = repository.getState()
}

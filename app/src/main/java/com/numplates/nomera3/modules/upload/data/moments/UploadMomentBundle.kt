package com.numplates.nomera3.modules.upload.data.moments

import com.meera.db.models.UploadBundle
import com.numplates.nomera3.modules.moments.show.data.MomentToUpload
import com.numplates.nomera3.modules.moments.show.domain.UploadMomentUseCase

data class UploadMomentBundle(
    val momentToUpload: MomentToUpload,
    val amplitudeMomentUploadParams: UploadMomentUseCase.AmplitudeMomentUploadParams
) : UploadBundle()

package com.numplates.nomera3.modules.moments.show.domain

import android.content.Context
import android.os.Environment
import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.moments.show.data.MomentToUpload
import com.numplates.nomera3.modules.upload.data.moments.UploadMomentBundle
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import java.io.File
import javax.inject.Inject

const val COPY_CACHE_FOLDER_NAME = "moments"

class UploadMomentUseCase @Inject constructor(val context: Context, val uploadRepository: UploadRepository) {
    fun invoke(
        momentToUpload: MomentToUpload,
        momentAmplitudeParams: AmplitudeMomentUploadParams
    ) {
        val uploadModel = UploadMomentBundle(
            momentToUpload.copyFileAndReturnNewInstance(context, momentToUpload.isVideo),
            momentAmplitudeParams
        )
        uploadRepository.upload(UploadType.Moment, uploadModel)
    }

    private fun MomentToUpload.copyFileAndReturnNewInstance(context: Context, isVideo: Boolean): MomentToUpload {
        val sourceFile = File(file)
        val copyToFile = File(getAppSpecificAlbumStorageDir(context, isVideo), sourceFile.name)
        val resultFile = sourceFile.copyTo(copyToFile)

        return this.copy(
            file = resultFile.toString()
        )
    }

    private fun getAppSpecificAlbumStorageDir(context: Context, isVideo: Boolean): File {
        val environment = if (isVideo) {
            Environment.DIRECTORY_MOVIES
        } else {
            Environment.DIRECTORY_PICTURES
        }

        val mediaDirectory = context.getExternalFilesDir(
            environment
        )

        return File(mediaDirectory, COPY_CACHE_FOLDER_NAME)
    }

    data class AmplitudeMomentUploadParams(
        val momentsPackSize: Int,
        var loadOrderNumber: Int = 0
    )
}

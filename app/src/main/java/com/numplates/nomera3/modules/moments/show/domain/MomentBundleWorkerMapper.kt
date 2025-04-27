package com.numplates.nomera3.modules.moments.show.domain

import androidx.work.Data
import com.meera.core.extensions.toJson
import com.numplates.nomera3.modules.moments.show.data.UploadMomentWorker
import com.numplates.nomera3.modules.upload.data.moments.UploadMomentBundle
import java.io.Serializable

class MomentBundleWorkerMapper {
    fun map(uploadBundle: UploadMomentBundle): Data.Builder {
        return Data.Builder()
            .apply {
                putString(UploadMomentWorker.FILE_PATH, uploadBundle.momentToUpload.file)
                putBoolean(UploadMomentWorker.IS_VIDEO, uploadBundle.momentToUpload.isVideo)
                putString(UploadMomentWorker.MEDIA, uploadBundle.momentToUpload.media)
                putStringArray(
                    UploadMomentWorker.MEDIA_KEYBOARD,
                    uploadBundle.momentToUpload.mediaKeyboard
                        ?.map(Serializable::toJson)
                        ?.toTypedArray()
                        ?: emptyArray()
                )
                putDouble(UploadMomentWorker.GPS_X, uploadBundle.momentToUpload.position.x)
                putDouble(UploadMomentWorker.GPS_Y, uploadBundle.momentToUpload.position.y)
                putInt(UploadMomentWorker.MOMENTS_PACK_SIZE, uploadBundle.amplitudeMomentUploadParams.momentsPackSize)
                putInt(UploadMomentWorker.MOMENT_UPLOAD_ORDER, uploadBundle.amplitudeMomentUploadParams.loadOrderNumber)
            }
    }
}

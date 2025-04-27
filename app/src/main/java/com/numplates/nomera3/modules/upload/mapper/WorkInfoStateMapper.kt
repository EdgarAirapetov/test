package com.numplates.nomera3.modules.upload.mapper

import androidx.work.WorkInfo
import com.numplates.nomera3.modules.upload.data.UploadResult

object WorkInfoStateMapper {
    fun mapFinishState(workState: WorkInfo.State?): UploadResult {
        return when (workState) {
            WorkInfo.State.SUCCEEDED -> {
                UploadResult.Success
            }

            WorkInfo.State.FAILED, null -> {
                UploadResult.Fail
            }
            else -> {
                UploadResult.Fail
            }
        }
    }
}

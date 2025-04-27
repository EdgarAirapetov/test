package com.numplates.nomera3.presentation.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo

interface IUploadContract {

    fun uploadImageToGallery(params: List<Uri>?): LiveData<WorkInfo>?

    fun stopWorkMessageWithVideoToChat()

    fun checkIsLastWorkCompleted(): Boolean = false
}

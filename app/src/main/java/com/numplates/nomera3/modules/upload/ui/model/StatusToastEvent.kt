package com.numplates.nomera3.modules.upload.ui.model

import androidx.annotation.StringRes
import com.meera.db.models.UploadItem
import com.numplates.nomera3.presentation.download.DownloadMediaHelper

sealed interface StatusToastEvent {
    data class ShowToast(val statusToastUiModel: StatusToastUiModel) : StatusToastEvent
    object HideToast : StatusToastEvent
    data class ShowUploadError(val uploadItem: UploadItem) : StatusToastEvent
    data class ShowBottomToast(@StringRes val message: Int, val isError: Boolean = false) : StatusToastEvent
    data class ShowMediaDownloadSuccessToast(val postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType) : StatusToastEvent
    data class ShowMediaDownloadErrorBottomToast(val postId: Long, val assetId: String?) : StatusToastEvent
}

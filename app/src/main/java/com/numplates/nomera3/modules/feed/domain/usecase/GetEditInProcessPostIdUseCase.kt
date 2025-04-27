package com.numplates.nomera3.modules.feed.domain.usecase

import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import com.numplates.nomera3.modules.upload.mapper.UploadBundleMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetEditInProcessPostIdUseCase @Inject constructor(
    val repository: UploadRepository
) {

    private var editInProcessPostId: Long? = null
    private var coroutineContext = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        repository.getState()
            .onEach { uploadState ->
                editInProcessPostId = if (uploadState.status == UploadStatus.Processing && uploadState.uploadItem.type == UploadType.EditPost) {
                    uploadState.uploadItem.uploadBundleStringify.postIdFromUploadBundleStringify()
                } else {
                    null
                }
            }
            .launchIn(coroutineContext)
    }

    fun invoke(): Long? {
        return editInProcessPostId
    }

    private fun String.postIdFromUploadBundleStringify(): Long? {
        return (UploadBundleMapper.map(UploadType.Post, this) as? UploadPostBundle)?.postId
    }

}

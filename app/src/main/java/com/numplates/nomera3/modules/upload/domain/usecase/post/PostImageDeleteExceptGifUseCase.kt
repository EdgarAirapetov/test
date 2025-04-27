package com.numplates.nomera3.modules.upload.domain.usecase.post

import com.meera.core.utils.files.FileManager
import timber.log.Timber
import javax.inject.Inject

private const val GIF_EXTENSION = ".gif"

class PostImageDeleteExceptGifUseCase @Inject constructor(
    val updateGalleryUseCase: UpdateGalleryUseCase,
    private val fileManager: FileManager
) {
    fun execute(imageUrl: String?) = try {
        val extension = imageUrl?.substring(imageUrl.lastIndexOf("."))

        if (imageUrl == null || extension == GIF_EXTENSION) {
            // do nothing
        } else {
            fileManager.deleteFile(imageUrl)
            updateGalleryUseCase.execute(imageUrl)
        }
    } catch (throwable: Throwable) {
        Timber.e(throwable)
        // do nothing
    }
}

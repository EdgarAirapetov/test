package com.numplates.nomera3.modules.upload.domain.usecase.post

import com.meera.core.utils.files.FileManager
import timber.log.Timber
import javax.inject.Inject

class PostVideoDeleteUseCase @Inject constructor(
    val updateGalleryUseCase: UpdateGalleryUseCase,
    private val fileManager: FileManager
) {
    fun execute(videoPath: String?) = try {
        if (videoPath == null) {
            // do nothing
        } else {
            fileManager.deleteFile(videoPath)
            updateGalleryUseCase.execute(videoPath)
        }
    } catch (throwable: Throwable) {
        Timber.e(throwable)
        // do nothing
    }
}

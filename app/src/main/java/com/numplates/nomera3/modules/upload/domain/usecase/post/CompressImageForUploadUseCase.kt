package com.numplates.nomera3.modules.upload.domain.usecase.post

import android.net.Uri
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.core.utils.files.FileManager
import com.meera.media_controller_common.CropInfo
import com.meera.media_controller_implementation.presentation.DEFAULT_IMAGE_QUALITY
import com.meera.media_controller_implementation.presentation.DEFAULT_MEDIA_WIDTH
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.modules.upload.util.compressImage
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class CompressImageForUploadUseCase @Inject constructor(
    @Named(CACHE_DIR) private val cacheDir: File,
    private val fileManager: FileManager,
    private val fileMetaDataDelegate: MediaFileMetaDataDelegate
) {
    suspend operator fun invoke(
        imagePath: String,
        cropInfo: CropInfo
    ): String = invoke(
        imageUri = Uri.parse(imagePath),
        cropInfo = cropInfo
    )

    suspend operator fun invoke(
        imageUri: Uri,
        cropInfo: CropInfo
    ): String = compressImage(
        cacheDir = cacheDir,
        source = imageUri,
        fileManager = fileManager,
        imageMetadataModel = imageUri.path?.let(fileMetaDataDelegate::getImageMetadata),
        quality = cropInfo.imageQuality ?: DEFAULT_IMAGE_QUALITY,
        maxWidth = cropInfo.mediaWidth ?: DEFAULT_MEDIA_WIDTH
    )
}

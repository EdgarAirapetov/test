package com.numplates.nomera3.modules.upload.domain.usecase.post

import com.meera.core.utils.files.FileManager
import com.meera.core.utils.imagecompressor.Compressor
import com.meera.core.utils.imagecompressor.constraint.Compression
import com.meera.core.utils.imagecompressor.constraint.default
import com.numplates.nomera3.di.CACHE_DIR
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class CompressImageUseCase @Inject constructor(
    @Named(CACHE_DIR) private val cacheDir: File,
    private val fileManager: FileManager
) {
    suspend fun invoke(
        imagePath: String,
        compressionPatch: Compression.() -> Unit = { default() }
    ): File {
        return Compressor.compress(
            cacheDir = cacheDir,
            imagePath = imagePath,
            fileManager = fileManager,
            compressionPatch = compressionPatch
        ) ?: File(imagePath)
    }
}

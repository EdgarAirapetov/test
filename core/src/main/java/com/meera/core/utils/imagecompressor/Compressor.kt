package com.meera.core.utils.imagecompressor

import android.content.Context
import android.net.Uri
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.imagecompressor.constraint.Compression
import com.meera.core.utils.imagecompressor.constraint.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * Created on : January 22, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
object Compressor {
    suspend fun compress(
        context: Context,
        imageFile: File,
        coroutineContext: CoroutineContext = Dispatchers.IO,
        compressionPatch: Compression.() -> Unit = { default() }
    ) = withContext(coroutineContext) {
        val compression = Compression().apply(compressionPatch)
        var result = copyToCache(context, imageFile)
        compression.constraints.forEach { constraint ->
            while (constraint.isSatisfied(result).not()) {
                result = constraint.satisfy(result)
            }
        }
        return@withContext result
    }

    suspend fun compress(
        cacheDir: File,
        imagePath: String,
        fileManager: FileManager,
        coroutineContext: CoroutineContext = Dispatchers.IO,
        compressionPatch: Compression.() -> Unit = { default() }
    ) = withContext(coroutineContext) {
        val compression = Compression().apply(compressionPatch)
        val uri = Uri.parse(imagePath)

        var result = fileManager.duplicateTo(uri, cacheDir) ?: return@withContext null
        compression.constraints.forEach { constraint ->
            while (constraint.isSatisfied(result).not()) {
                result = constraint.satisfy(result)
            }
        }
        return@withContext result
    }

}

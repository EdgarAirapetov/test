package com.numplates.nomera3.domain.interactornew

import android.net.Uri
import com.numplates.nomera3.data.network.ApiFileStorage
import com.meera.core.extensions.empty
import com.meera.core.extensions.randomString
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.getFileExtensionFromUrlPath
import okhttp3.ResponseBody

private const val RANDOM_FILENAME_STRING_LENGTH = 12

class DownloadFileUseCase(
    private val repository: ApiFileStorage?,
    private val fileUtils: FileManager,
    var url: String = String.empty(),
) {

    fun setParams(url: String) {
        this.url = url
    }

    suspend fun downloadFileV2(): ResponseBody? =
        repository?.downloadFileFromUrl(url)

    suspend fun downloadFileAndSaveToCache(downloadUrl: String): Uri? {
        val response = repository?.downloadFileFromUrl(downloadUrl)
        val fileName = "${randomString(RANDOM_FILENAME_STRING_LENGTH)}.${getFileExtensionFromUrlPath(downloadUrl)}"
        return fileUtils.saveToCache(response, fileName)
    }
}

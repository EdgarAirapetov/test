package com.numplates.nomera3.modules.contentsharing.ui.rooms

import android.content.Context
import android.net.Uri
import com.meera.core.utils.files.FileManager
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.MediaType
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.SharingDataCache
import javax.inject.Inject

private const val ALLOWED_DURATION_MS = 300000L

class ContentChecker @Inject constructor(
    private val fileManager: FileManager,
    private val appContext: Context,
) {

    private val sharingDataCache: SharingDataCache = SharingDataCache

    fun hasIncorrectVideo(): Boolean {
        return containsNowAllowedDuration(filterVideoUris(sharingDataCache.getUris()))
    }

    private fun filterVideoUris(uris: List<Uri>): List<Uri> {
        return uris.filter { uri: Uri ->
            appContext.contentResolver.getType(uri)?.startsWith(MediaType.VIDEO.value) == true
        }
    }

    private fun containsNowAllowedDuration(uris: List<Uri>): Boolean {
        return uris.any { uri -> fileManager.getVideoDurationMils(uri) > ALLOWED_DURATION_MS }
    }
}

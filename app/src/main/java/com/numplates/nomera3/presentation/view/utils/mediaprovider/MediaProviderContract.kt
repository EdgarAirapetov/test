package com.numplates.nomera3.presentation.view.utils.mediaprovider

import android.net.Uri

interface MediaProviderContract {

    suspend fun getRecentImages(maxCount: Int, includeGifs: Boolean): List<Uri>

    suspend fun getRecentVideo(maxCount: Int): List<Uri>

    suspend fun getRecentVideosImagesCombined(limit: Int): List<Uri>
}

package com.numplates.nomera3.presentation.view.utils.mediaprovider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaProvider(val context: Context) : MediaProviderContract {

    override suspend fun getRecentImages(maxCount: Int, includeGifs: Boolean): List<Uri> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Uri>()
        var cursor: Cursor? = null
        try {
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            if (includeGifs.not()) {
                selection = MediaStore.Files.FileColumns.MIME_TYPE + "!= ?"
                selectionArgs = arrayOf("image/gif")
            }

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Query all the device storage volumes instead of the primary only
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val columns: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            val orderBy: String = MediaStore.Images.Media.DATE_ADDED + " DESC"

            cursor = context.applicationContext
                .contentResolver
                .query(uri, columns, selection, selectionArgs, orderBy)

            if (cursor != null) {
                var count = 0

                while (cursor.moveToNext() && count < maxCount) {
                    val dataIndex: String = MediaStore.Images.Media.DATA
                    val imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex))
                    val imageFile = File(imageLocation)
                    result.add(Uri.fromFile(imageFile))
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        result
    }

    override suspend fun getRecentVideo(maxCount: Int): List<Uri> {
        return getRecentVideoUri(maxCount)
    }

    private suspend fun getRecentVideoUri(limit: Int = 20): List<Uri> = withContext(Dispatchers.IO) {
        // список последних видео
        val recentVideoList = mutableListOf<Uri>()

        // запрос
        val query = context
            .applicationContext
            .contentResolver
            .query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED),
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC LIMIT $limit"
            )

        // автоматически закрывающийся запрос
        query?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val data = cursor.getString(dataColumn)
                val imageFile = File(data)

                recentVideoList.add(Uri.fromFile(imageFile))
            }
        }

        recentVideoList
    }

    override suspend fun getRecentVideosImagesCombined(limit: Int): List<Uri> = withContext(Dispatchers.IO) {
        val combinedVideoImageUriList = mutableListOf<Uri>()

        //limit does'nt work on 11 android
        val recentlyAddedImagesVideosCursor = context
            .applicationContext
            .contentResolver
            .query(
                MediaStore.Files.getContentUri("external"),
                arrayOf(MediaStore.Files.FileColumns.DATA),
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}",
                null,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )

        recentlyAddedImagesVideosCursor?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val data = cursor.getString(dataColumn)
                val uri: Uri = Uri.fromFile(File(data))

                combinedVideoImageUriList.add(uri)
            }
        }

        if (combinedVideoImageUriList.size > limit) {
            combinedVideoImageUriList.subList(0, limit)
        } else {
            combinedVideoImageUriList
        }
    }
}

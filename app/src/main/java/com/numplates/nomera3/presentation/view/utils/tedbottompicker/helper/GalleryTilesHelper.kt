package com.numplates.nomera3.presentation.view.utils.tedbottompicker.helper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.model.PickerTile
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class GalleryTilesHelper @Inject constructor(
    private val context: Context,
    private val fileManager: FileManager
) {

    fun loadPickerTiles(
        builder: TedBottomSheetDialogFragment.BaseBuilder<*>,
        bucketId: String? = null,
    ): List<PickerTile> {
        val pickerTiles = mutableListOf<PickerTile>()
        if (builder.showCamera) {
            pickerTiles.add(PickerTile(PickerTile.CAMERA))
        }
        if (builder.showGallery) {
            pickerTiles.add(PickerTile(PickerTile.GALLERY))
        }
        var cursor: Cursor? = null
        try {
            val columns: Array<String>
            val orderBy: String
            var selection: String? = null
            val uri: Uri
            when (builder.mediaType) {
                TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE -> {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    columns = arrayOf(
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_ID
                    )
                    orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC"
                }

                TedBottomSheetDialogFragment.BaseBuilder.MediaType.VIDEO -> {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    columns = arrayOf(
                        MediaStore.Video.VideoColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_ID
                    )
                    orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC"
                }

                else -> {
                    uri = MediaStore.Files.getContentUri("external")
                    orderBy = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                    columns = arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_ID
                    )
                    selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                }
            }

            cursor = context.applicationContext
                .contentResolver
                .query(uri, columns, selection, null, orderBy)
            if (cursor != null) {
                var count = 0
                while (cursor.moveToNext() && count < builder.previewMaxCount) {
                    val bucketIdIndex = MediaStore.Images.ImageColumns.BUCKET_ID
                    val dataIndex =
                        if (builder.mediaType == TedBottomSheetDialogFragment.BaseBuilder.MediaType.IMAGE) {
                            MediaStore.Images.Media.DATA
                        } else {
                            MediaStore.Video.VideoColumns.DATA
                        }

                    @Suppress("LocalVariableName")
                    val _bucketId = cursor.getString(cursor.getColumnIndex(bucketIdIndex))
                    val imageLocation = cursor.getString(cursor.getColumnIndex(dataIndex))
                    val tileData = getPickerTile(imageLocation)
                    if (bucketId == null || bucketId == _bucketId) {
                        pickerTiles.add(tileData)
                    }
                    count++
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return pickerTiles
    }

    fun getPickerTile(filePath: String): PickerTile {
        val mediaType = fileManager.getMediaType(Uri.parse(filePath))
        val tileType = when (mediaType) {
            FileUtilsImpl.MEDIA_TYPE_VIDEO -> PickerTile.VIDEO
            else -> PickerTile.IMAGE
        }
        val imageFile = File(filePath)
        val tileData = PickerTile(Uri.fromFile(imageFile), tileType)
        if (tileType == PickerTile.VIDEO) {
            tileData.duration = fileManager.getVideoDurationMils(tileData.imageUri)
        }
        return tileData
    }
}

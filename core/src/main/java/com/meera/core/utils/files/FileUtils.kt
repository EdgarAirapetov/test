package com.meera.core.utils.files

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URLConnection
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.math.min

const val TEXT_PLAIN = "text/plain"
const val IMAGE_GIF = "image/gif"
const val IMAGE_PNG = "image/png"
const val IMAGE_JPEG = "image/jpeg"
const val IMAGE_WEBP = "image/webp"
private const val MIME_TYPE_IMAGE = "image/*"
private const val NOMERA_COPY_FILE = "_nomera_copy"
private const val TEMP_FILE_NAME = "temp_file"
private const val CONTENT_URI_SCHEME = "content"
private const val MAX_BUFFER_SIZE = 1 * 1024 * 1024


interface FileManager {

    fun getFileForContentUri(mimeType: String, media: Uri): File?

    fun getFileForContentUri(uri: Uri, fileExtension: String = ""): File?

    fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri

    fun copy(source: File, dest: File)

    fun getUriForFile(context: Context, file: File): Uri

    fun getRealPathFromUri(uri: Uri): String

    fun isVideoUri(uri: Uri): Boolean

    fun saveMediaToPuplicFolder(uri: Uri, toDir: String): File?

    fun isContentUri(uri: Uri): Boolean

    fun duplicateTo(uri: Uri, toDir: File?, extNumber: Int? = null): File?

    fun saveBitmapInFile(pictureBitmap: Bitmap): String

    fun isGooglePhotosUri(uri: Uri): Boolean

    fun isGooglePhoto(uri: Uri?): Boolean

    fun saveImageFromGoogleDrives(temp: Uri?): String?

    fun saveBitmapInFile(pictureBitmap: Bitmap, filePath: String, isPng: Boolean): String

    fun saveBitmapInFile(pictureBitmap: Bitmap, filePath: String): String

    fun createImageFile(): File

    fun createPngImageFile(context: Context): File

    fun saveToDisk(
        cacheDir: File?,
        body: ResponseBody,
        filename: String?,
        progressListener: FileUtilsImpl.DownloadFileProgressListener
    )

    suspend fun saveToCache(
        body: ResponseBody?,
        fileName: String
    ) : Uri?

    fun getVideoDurationMils(uri: Uri?): Long

    fun getFileSize(fileUri: Uri): Long

    fun getMediaType(uri: Uri?): Int

    fun deleteFile(filePath: String?): Boolean
}

@Suppress("detekt:UnusedPrivateMember")
class FileUtilsImpl @Inject constructor(
    private val context: Context
) : FileManager {

    private val DEBUG = false // Set to true to enable logging

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    override fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    override fun isGooglePhoto(uri: Uri?): Boolean {
        return uri.toString().contains("content://com.google.android.apps.docs.storage/document")
    }

    override fun saveImageFromGoogleDrives(temp: Uri?): String? {
        try {
            val `is` = temp?.let { context.contentResolver.openInputStream(it) }
            if (`is` != null) {
                val pictureBitmap = BitmapFactory.decodeStream(`is`)
                return saveBitmapInFile(pictureBitmap)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return temp.toString()
    }

    override fun saveBitmapInFile(pictureBitmap: Bitmap): String {
        val file = createImageFile()
        try {
            val outputStream = FileOutputStream(file)
            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return file.path.toString()
    }

    /**
     * Create Bitmap from existing file
     * @param pictureBitmap
     * @param filePath
     * @return
     */
    override fun saveBitmapInFile(pictureBitmap: Bitmap, filePath: String): String {
        return saveBitmapInFile(pictureBitmap, filePath, false)
    }

    /**
     * Create Bitmap from existing file
     * @param pictureBitmap
     * @param filePath
     * @param isPng
     * @return
     */
    override fun saveBitmapInFile(pictureBitmap: Bitmap, filePath: String, isPng: Boolean): String {
        val file = File(filePath)
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            pictureBitmap.compress(compressFormat, 100, outputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush()
                    outputStream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file.path
    }

    override fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
    }

    override fun createPngImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_" + "_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName,
                ".png",
                storageDir
            )
    }

    override fun deleteFile(filePath: String?): Boolean {
        if (filePath != null) {
            val file = File(filePath)
            return if (file.exists()) {
                file.delete()
            } else false
        } else return false
    }

    override fun saveToDisk(
        cacheDir: File?,
        body: ResponseBody,
        filename: String?,
        progressListener: DownloadFileProgressListener
    ) {
        try {
            //File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            //File destFile = getCacheDir() File(context.filesDir, filename)
            val destinationFile = File(cacheDir, filename)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(destinationFile)
                val data = ByteArray(4096)
                var count: Int
                var progress = 0
                val fileSize = body.contentLength()
                Log.d(TAG, "File Size=$fileSize")
                var prevProgress = 0
                while ((inputStream.read(data).also { count = it }) != -1) {
                    outputStream.write(data, 0, count)
                    progress += count
                    val progressInt = Math.round((progress.toFloat() / fileSize) * 100)
                    if (progressInt != prevProgress) {
                        progressListener.onProgressUpdate(progressInt)
                        prevProgress = progressInt
                    }
                    Log.d(TAG, "Progress: " + progress + "/" + fileSize + " >>>> " + (progress.toFloat() / fileSize))
                }
                outputStream.flush()
                // Log.d(TAG, destinationFile.getParent());
                //progressListener.onProgressUpdate(100);
                return
            } catch (e: IOException) {
                e.printStackTrace()
                progressListener.onProgressUpdate(-1)
                Log.d(TAG, "Failed to save the file!")
                return
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Failed to save the file!")
            return
        }
    }

    override suspend fun saveToCache(
        body: ResponseBody?,
        fileName: String
    ): Uri? {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val cacheFile = File(context.cacheDir, fileName)
                body?.byteStream().use { input ->
                    cacheFile.outputStream().use { output ->
                        input?.copyTo(output)
                    }
                }
                Uri.fromFile(cacheFile)
            }.onFailure { error ->
                error.printStackTrace()
                Log.d(TAG, "Failed to save the file to cache!")
            }.getOrNull()
        }
    }

    override fun isVideoUri(uri: Uri): Boolean {
        val uriString = uri.toString()
        val indexOfLastDot = uriString.lastIndexOf(PATH_DOT_SEPARATOR)
        var fileExtension: String? = null
        if (indexOfLastDot > 0) {
            fileExtension = uriString.substring(indexOfLastDot + 1)
        }
        return fileExtension != null && fileExtension.contains(VIDEO_MP4_EXTENSTION)
    }

    override fun isContentUri(uri: Uri): Boolean = uri.scheme == CONTENT_URI_SCHEME

    override fun getMediaType(uri: Uri?): Int {
        if (uri == null) return MEDIA_TYPE_UNKNOWN
        try {
            /*
             * https://nomera.atlassian.net/browse/BR-3537 часть видео отображается как фото, в карусели
             * и в пикере на экране создания поста. Файлы не определяется как видео из-за метода
             * URLConnection.guessContentTypeFromName(uri.getPath()) когда имя файла с пробелами либо со
             * спец. символами, например #. Как временное сделал определение видео по расширению файла.
             * В будущем лучше сделать определение типа файла через поиск по начальным байтам файла, см.
             * https://en.wikipedia.org/wiki/List_of_file_signatures
             * Todo добавить класс FileTypeDetector определяющий тип файла по заголовочным байтам
             * */
            var extension = ""
            val uriString = uri.toString()
            if (URLUtil.isHttpUrl(uriString) || URLUtil.isHttpsUrl(uriString)) return MEDIA_TYPE_IMAGE
            val indexOfLastDot = uriString.lastIndexOf('.')
            if (indexOfLastDot > 0) {
                extension = uriString.substring(indexOfLastDot + 1)
            }
            if (extension.contains("mp4")) {
                return MEDIA_TYPE_VIDEO
            }
            val mimeType = URLConnection.guessContentTypeFromName(uri.path) ?: return MEDIA_TYPE_UNKNOWN
            val type = mimeType.split("/").toTypedArray()
            if (type.size == 0) return MEDIA_TYPE_UNKNOWN
            if ((type[0] == "video")) return MEDIA_TYPE_VIDEO else if ((type[0] == "image")) {
                return if ((type.get(1) == "gif")) MEDIA_TYPE_IMAGE_GIF else MEDIA_TYPE_IMAGE
            } else return MEDIA_TYPE_UNKNOWN
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return MEDIA_TYPE_UNKNOWN
        }
    }

    override fun getVideoDurationMils(uri: Uri?): Long {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time!!.toLong()
            retriever.release()
            return timeInMillisec
        } catch (e: java.lang.Exception) {
            Timber.e(e)
            return 0
        }
    }

    override fun getFileSize(fileUri: Uri): Long {
        val returnCursor = requireNotNull(context.contentResolver.query(fileUri, null, null, null, null))
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val size = returnCursor.getLong(sizeIndex)
        returnCursor.close()
        return size
    }

    override fun saveMediaToPuplicFolder(uri: Uri, toDir: String): File? {
        return try {
            val inputFile = File(uri.path ?: return null)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, uri.lastPathSegment)
                put(MediaStore.MediaColumns.RELATIVE_PATH, toDir)
            }
            val uriType = if (isVideoUri(uri)) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            with(context.contentResolver) {
                val outputUri = insert(uriType, values) ?: return null
                openOutputStream(outputUri)?.use { outputStream ->
                    val inputStream = FileInputStream(inputFile)
                    copyFile(inputStream, outputStream)
                } ?: return null
                update(outputUri, values, null, null)
                outputUri.path?.let(::File)
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun duplicateTo(uri: Uri, toDir: File?, extNumber: Int?): File? {
        return try {
            val sample = File(uri.path)
            val pos = sample.name.lastIndexOf('.')
            val name = sample.name.substring(0, pos)
            val ext = sample.name.substring(pos)
            var finalName = name + NOMERA_COPY_FILE + ext
            if (extNumber != null) finalName += extNumber
            val copy = File(toDir, finalName)
            copyFile(FileInputStream(sample), FileOutputStream(copy))
            copy
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    override fun getFileForContentUri(mimeType: String, media: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(media) ?: return null
        val extension = when (mimeType) {
            IMAGE_GIF -> "gif"
            IMAGE_PNG -> "png"
            IMAGE_JPEG -> "jpeg"
            IMAGE_WEBP -> "webp"
            else -> null
        } ?: return null
        val filename = "${UUID.randomUUID()}.${extension}"
        FileOutputStream(File(context.externalCacheDir, filename)).use { fos ->
            copy(inputStream, fos)
            fos.flush()
            fos.close()
        }
        inputStream.close()
        return File(context.externalCacheDir, filename)
    }

    @Throws(FileNotFoundException::class, IOException::class)
    override fun getFileForContentUri(uri: Uri, fileExtension: String): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = TEMP_FILE_NAME + if (fileExtension.isNotEmpty()) ".$fileExtension" else ""
        FileOutputStream(File(context.externalCacheDir, fileName)).use { fos ->
            copy(inputStream, fos)
            fos.flush()
            fos.close()
        }
        inputStream.close()
        return File(context.externalCacheDir, fileName)
    }

    override fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, getAuthority(), file)

    override fun getRealPathFromUri(uri: Uri): String {
        val returnCursor = requireNotNull(context.contentResolver.query(uri, null, null, null, null))
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.filesDir, name)
        try {
            val inputStream = requireNotNull(context.contentResolver.openInputStream(uri))
            val outputStream = FileOutputStream(file)
            var read: Int
            val bytesAvailable = inputStream.available()
            val bufferSize = min(bytesAvailable.toDouble(), MAX_BUFFER_SIZE.toDouble()).toInt()
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
        return file.path
    }

    override fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val file = saveBitmapToInternalStorage(context, bitmap)
        return FileProvider.getUriForFile(context, getAuthority(), file)
    }

    override fun copy(source: File, dest: File) {
        var fi: FileInputStream? = null
        var fo: FileOutputStream? = null
        var ic: FileChannel? = null
        var oc: FileChannel? = null
        try {
            if (!dest.exists()) {
                dest.createNewFile()
            }
            fi = FileInputStream(source)
            fo = FileOutputStream(dest)
            ic = fi.channel
            oc = fo.channel
            ic.transferTo(0, ic.size(), oc)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fi?.close()
            fo?.close()
            ic?.close()
            oc?.close()
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access Framework Documents,
     * as well as the _data field for the MediaStore and other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it represents a local file.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     * @see .isLocal
     * @see .getFile
     */
    private fun getPath(context: Context, uri: Uri): String? {
        if (DEBUG) {
            Log.d(
                "$TAG File -",
                "Authority: " + uri.authority +
                    ", Fragment: " + uri.fragment +
                    ", Port: " + uri.port +
                    ", Query: " + uri.query +
                    ", Scheme: " + uri.scheme +
                    ", Host: " + uri.host +
                    ", Segments: " + uri.pathSegments.toString()
            )
        }
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri)
            } else if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if (("image" == type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if (("video" == type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if (("audio" == type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            val cursor = context.contentResolver
                .query(
                    uri, null, null,
                    null, null
                )
            cursor!!.moveToFirst()
            val imageColum = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val filePath = cursor.getString(imageColum)
            cursor.close()
            return filePath
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @Throws(IOException::class)
    private fun toByteArray(`in`: InputStream): ByteArray? {
        val os = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while ((`in`.read(buffer).also { len = it }) != -1) {
            os.write(buffer, 0, len)
        }
        return os.toByteArray()
    }

    private fun getFolderSizeLabel(file: File): String? {
        val size = getFolderSize(file) / 1024 // Get size and convert bytes into Kb.
        return if (size >= 1024) {
            (size / 1024).toString() + " Mb"
        } else {
            "$size Kb"
        }
    }

    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        if (file.isDirectory) {
            for (child: File in file.listFiles()) {
                size += getFolderSize(child)
            }
        } else {
            size = file.length()
        }
        return size
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri?): String? { //works with content provider
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query((contentUri)!!, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ""
        } finally {
            cursor?.close()
        }
    }

    private fun duplicate(uri: Uri): File? {
        try {
            val sample = File(uri.path)
            val dir = sample.parentFile
            val pos = sample.name.lastIndexOf('.')
            val name = sample.name.substring(0, pos)
            val ext = sample.name.substring(pos)
            val copy = File(dir, name + NOMERA_COPY_FILE + ext)
            copyFile(FileInputStream(sample), FileOutputStream(copy))
            return copy
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     * @author paulburke
     */
    private fun createGetContentIntent(): Intent? {
        // Implicitly allow the user to selectCheckbox a particular kind of data
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // The MIME data type filter
        intent.type = "*/*"
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while ((`in`.read(buffer).also { read = it }) != -1) {
            out.write(buffer, 0, read)
        }
    }

    private fun move(source: File, dest: File) {
        copy(source, dest)
        source.delete()
    }

    private fun File.copyDirectory(dest: File) {
        if (!dest.exists()) {
            dest.mkdirs()
        }
        val files = listFiles()
        files?.forEach {
            if (it.isFile) {
                copy(it, File("${dest.absolutePath}/${it.name}"))
            }
            if (it.isDirectory) {
                val dirSrc = File("$absolutePath/${it.name}")
                val dirDest = File("${dest.absolutePath}/${it.name}")
                dirSrc.copyDirectory(dirDest)
            }
        }
    }

    private fun moveDirectory(source: File, dest: File) {
        source.copyDirectory(dest)
        deleteAll(source)
    }

    private fun deleteAll(file: File) {
        if (file.isFile && file.exists()) {
            file.delete()
            return
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files == null || files.isEmpty()) {
                file.delete()
                return
            }
            files.forEach { deleteAll(it) }
            file.delete()
        }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other
     * file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG) {
                    DatabaseUtils.dumpCursor(cursor)
                }
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the Uri is unsupported or
     * pointed to a remote resource.
     * @author paulburke
     * @see .getPath
     */
    private fun getFile(context: Context, uri: Uri?): File? {
        if (uri != null) {
            val path = getPath(context, uri)
            if (path != null && isLocal(path)) {
                return File(path)
            }
        }
        return null
    }

    private fun md5(file: File): String? {
        if (!file.isFile) {
            return null
        }
        return encryptFile(file, "MD5")
    }

    private fun sha1(file: File): String? {
        if (!file.isFile) {
            return null
        }
        return encryptFile(file, "SHA-1")
    }


    /**
     * Get the file size in a human-readable string.
     *
     * @author paulburke
     */
    private fun getReadableFileSize(size: Int): String? {
        val BYTES_IN_KILOBYTES = 1024
        val dec = DecimalFormat("###.#")
        val KILOBYTES = " KB"
        val MEGABYTES = " MB"
        val GIGABYTES = " GB"
        var fileSize = 0f
        var suffix = KILOBYTES
        if (size > BYTES_IN_KILOBYTES) {
            fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES
                    suffix = GIGABYTES
                } else {
                    suffix = MEGABYTES
                }
            }
        }
        return dec.format(fileSize.toDouble()) + suffix
    }

    /**
     * Attempt to retrieve the thumbnail of given File from the MediaStore. This should not be called
     * on the UI thread.
     *
     * @author paulburke
     */
    private fun getThumbnail(context: Context, file: File): Bitmap? {
        return getThumbnail(context, getUri(file), getMimeType(file))
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This should not be called
     * on the UI thread.
     *
     * @author paulburke
     */
    private fun getThumbnail(context: Context, uri: Uri): Bitmap? {
        return getThumbnail(context, uri, getMimeType(context, uri))
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This should not be called
     * on the UI thread.
     *
     * @author paulburke
     */
    private fun getThumbnail(context: Context, uri: Uri?, mimeType: String?): Bitmap? {
        if (DEBUG) {
            Log.d(TAG, "Attempting to get thumbnail")
        }
        if (!isMediaUri(uri)) {
            Log.e(TAG, "You can only retrieve thumbnails for images and videos.")
            return null
        }
        var bm: Bitmap? = null
        if (uri != null) {
            val resolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = resolver.query(uri, null, null, null, null)
                if (cursor!!.moveToFirst()) {
                    val id = cursor.getInt(0)
                    if (DEBUG) {
                        Log.d(TAG, "Got thumb ID: $id")
                    }
                    if (mimeType!!.contains("video")) {
                        bm = MediaStore.Video.Thumbnails.getThumbnail(
                            resolver,
                            id.toLong(),
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null
                        )
                    } else if (mimeType.contains(MIME_TYPE_IMAGE)) {
                        bm = MediaStore.Images.Thumbnails.getThumbnail(
                            resolver,
                            id.toLong(),
                            MediaStore.Images.Thumbnails.MINI_KIND,
                            null
                        )
                    }
                }
            } catch (e: java.lang.Exception) {
                if (DEBUG) {
                    Log.e(TAG, "getThumbnail", e)
                }
            } finally {
                cursor?.close()
            }
        }
        return bm
    }

    private fun toByteArray(file: File): ByteArray {
        val bos = ByteArrayOutputStream(file.length().toInt())
        val input = FileInputStream(file)
        val size = 1024
        val buffer = ByteArray(size)
        var len = input.read(buffer, 0, size)
        while (len != -1) {
            bos.write(buffer, 0, len)
            len = input.read(buffer, 0, size)
        }
        input.close()
        bos.close()
        return bos.toByteArray()
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @return Extension including the dot("."); "" if there is no extension; null if uri was null.
     */
    private fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }
        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    private fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    private fun isMediaUri(uri: Uri?): Boolean {
        return "media".equals(uri!!.authority, ignoreCase = true)
    }

    /**
     * Convert File into Uri.
     *
     * @return uri
     */
    private fun getUri(file: File?): Uri? {
        return if (file != null) {
            Uri.fromFile(file)
        } else null
    }

    /**
     * Returns the path only (without file name).
     */
    private fun getPathWithoutFilename(file: File?): File? {
        if (file != null) {
            if (file.isDirectory) {
                // no file to be split off. Return everything
                return file
            } else {
                val filename = file.name
                val filepath = file.absolutePath

                // Construct path without file name.
                var pathwithoutname = filepath.substring(
                    0,
                    filepath.length - filename.length
                )
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length - 1)
                }
                return File(pathwithoutname)
            }
        }
        return null
    }

    /**
     * @return The MIME type for the given file.
     */
    private fun getMimeType(file: File): String? {
        val extension = getExtension(file.name)
        return if (extension!!.length > 0) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1))
        } else "application/octet-stream"
    }

    /**
     * @return The MIME type for the give Uri.
     */
    private fun getMimeType(context: Context, uri: Uri): String? {
        val file = File(getPath(context, uri))
        return getMimeType(file)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is
     * @author paulburke
     */
    private fun isLocalStorageDocument(uri: Uri): Boolean {
        return LocalStorageProvider.AUTHORITY == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun bytes2Hex(bts: ByteArray): String {
        var des = ""
        var tmp: String
        for (i in bts.indices) {
            tmp = Integer.toHexString(bts[i].toInt() and 0xFF)
            if (tmp.length == 1) {
                des += "0"
            }
            des += tmp
        }
        return des
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream) {
        val buf = ByteArray(1024)
        var length: Int
        while (input.read(buf).also { length = it } != -1) {
            output.write(buf, 0, length)
        }
    }

    private fun encryptFile(file: File, type: String): String {
        val digest: MessageDigest = MessageDigest.getInstance(type)
        val input = FileInputStream(file)
        val buffer = ByteArray(1024)
        var len = input.read(buffer, 0, 1024)
        while (len != -1) {
            digest.update(buffer, 0, len)
            len = input.read(buffer, 0, 1024)
        }
        input.close()
        return bytes2Hex(digest.digest())
    }

    private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): File {
        val filename = "${UUID.randomUUID()}.jpg"

        FileOutputStream(File(context.externalCacheDir, filename)).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()
        }

        return File(context.externalCacheDir, filename)
    }

    private fun getAuthority() = "${context.packageName}.fileprovider"

    companion object {
        const val MEDIA_TYPE_IMAGE = 0
        const val MEDIA_TYPE_VIDEO = 1
        const val MEDIA_TYPE_UNKNOWN = 3
        const val MEDIA_TYPE_IMAGE_GIF = 4
        const val VIDEO_MP4_EXTENSTION = "mp4"
        const val PATH_DOT_SEPARATOR = '.'
        const val TAG = "FileUtils"
    }

    interface DownloadFileProgressListener {
        fun onProgressUpdate(progress: Int)
    }
}

fun getFileExtensionFromUrlPath(path: String): String = MimeTypeMap.getFileExtensionFromUrl(path)

fun isPathTypeContent(path: String): Boolean = path.contains("content://")

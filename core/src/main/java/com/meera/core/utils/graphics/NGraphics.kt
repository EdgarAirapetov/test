package com.meera.core.utils.graphics

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.R
import com.meera.core.extensions.getAuthority
import com.meera.core.extensions.randomString
import com.meera.core.utils.files.IMAGE_PNG
import com.meera.core.utils.files.getFileExtensionFromUrlPath
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException

private const val ACCOUNT_TYPE_REGULAR = 0
private const val ACCOUNT_TYPE_PREMIUM = 1
private const val ACCOUNT_TYPE_VIP = 2
private const val COLOR_GREEN = 1
private const val COLOR_RED = 0
private const val COLOR_BLUE = 2
private const val COLOR_PURPLE = 4
private const val COLOR_PINK = 3
private const val GIPHY_BRAND_NAME = "giphy"
private const val MEDIA_EXT_GIF = ".gif"
private const val MEDIA_IMAGE = "image"
private const val MEDIA_VIDEO = "video"
private const val UNIX_SECOND_MULTIPLIER = 1000


private const val RANDOM_FILENAME_STRING_LENGTH = 12

object NGraphics {

    fun getScreenWidth(context: Context): Int {
        val columnWidth: Int
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val point = Point()
        try {
            display.getSize(point)
        } catch (ignore: NoSuchMethodError) { // Older device
            point.x = display.width
            point.y = display.height
        }

        columnWidth = point.x
        return columnWidth
    }

    @JvmOverloads
    fun getBitmapFromView(
        view: View,
        config: Bitmap.Config = Bitmap.Config.ARGB_8888
    ): Bitmap {     //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, config)
        val canvas = Canvas(returnedBitmap) //Bind a canvas to it
        val bgDrawable = view.background  //Get the view's background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)        //has background drawable, then draw it on the canvas
        } else {
            canvas.drawColor(Color.WHITE)  //No background drawable - draw white background on the canvas
        }
        view.draw(canvas)
        return returnedBitmap
    }


    fun getBitmapView(view: View): Bitmap {
        try {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val bitmap = Bitmap.createBitmap(
                view.measuredWidth, view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }

    }

    fun addShadowToBitmap(
        bm: Bitmap,
        dstHeight: Int,
        dstWidth: Int,
        color: Int,
        size: Int,
        dx: Int,
        dy: Int
    ): Bitmap {
        val mask = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ALPHA_8)

        val scaleToFit = Matrix()
        val src = RectF(0f, 0f, bm.width.toFloat(), bm.height.toFloat())
        val dst = RectF(0f, 0f, dstWidth - dx.toFloat(), dstHeight - dy.toFloat())
        scaleToFit.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)

        val dropShadow = Matrix(scaleToFit)
        dropShadow.postTranslate(dx.toFloat(), dy.toFloat())

        val maskCanvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskCanvas.drawBitmap(bm, scaleToFit, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        paint.alpha = 60
        maskCanvas.drawBitmap(bm, dropShadow, paint)

        val filter = BlurMaskFilter(size.toFloat(), BlurMaskFilter.Blur.NORMAL)
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
        paint.maskFilter = filter
        paint.isFilterBitmap = true

        val ret = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
        val retCanvas = Canvas(ret)
        retCanvas.drawBitmap(mask, 0f, 0f, paint)
        retCanvas.drawBitmap(bm, scaleToFit, paint)
        mask.recycle()
        return ret
    }


    fun getColorResourceId(
        accountType: Int,
        accountColor: Int
    ): Int {

        when (accountType) {
            ACCOUNT_TYPE_REGULAR -> {
                return R.color.ui_white
            }

            ACCOUNT_TYPE_PREMIUM -> {
                when (accountColor) {
                    COLOR_RED -> {
                        return R.color.colorVipRed
                    }

                    COLOR_GREEN -> {
                        return R.color.colorVipGreen
                    }

                    COLOR_BLUE -> {
                        return R.color.colorVipBlue
                    }

                    COLOR_PINK -> {
                        return R.color.colorVipPink
                    }

                    COLOR_PURPLE -> {
                        return R.color.colorVipPurple
                    }
                }
            }

            ACCOUNT_TYPE_VIP -> {
                return R.color.ui_yellow
            }
        }
        return R.color.ui_white
    }

    fun saveImageToDevice(
        context: Context,
        imageUrl: String,
        onSaved: (Uri) -> Unit = {},
        onLoadFailed: () -> Unit = {},
        saveToCache: Boolean = false
    ) {
        val isFileUrl = URLUtil.isFileUrl(imageUrl)
        if (isFileUrl) {
            saveImageFile(
                context = context,
                sourceImage = File(Uri.parse(imageUrl).path),
                fileName = "${randomString(RANDOM_FILENAME_STRING_LENGTH)}.${getFileExtensionFromUrlPath(imageUrl)}",
                onSuccess = { uri ->
                    Timber.d("COPY_IMAGE_LOG success image URI:$uri")
                    onSaved.invoke(Uri.parse(imageUrl))
                },
                onError = { e ->
                    Timber.e("COPY_IMAGE_LOG error image URI:$e")
                    onLoadFailed.invoke()
                }
            )
            return
        }

        Glide.with(context)
            .asFile()
            .load(imageUrl)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onLoadFailed.invoke()
                    return false
                }

                    override fun onResourceReady(
                        resource: File?,
                        model: Any?,
                        target: Target<File>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let { file ->
                            if (saveToCache) {
                                onSaved.invoke(
                                    cacheImageFile(
                                        context = context,
                                        url = imageUrl,
                                        inputFile = file
                                    )
                                )
                            } else {
                                saveImageFile(context, file, imageUrl, onSaved)
                            }
                        }
                        return false
                    }
                })
                .submit()
        }

    fun shareImageToDevice(
        act: Activity?,
        imageUrl: String,
        onSaved: (Uri) -> Unit = {},
        onLoadFailed: () -> Unit = {}
    ) {
        act?.let {
            Glide.with(act)
                .asFile()
                .load(imageUrl)
                .listener(object : RequestListener<File> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<File>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoadFailed.invoke()
                        return false
                    }

                    override fun onResourceReady(
                        resource: File?,
                        model: Any?,
                        target: Target<File>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        copyImageFile(act, resource, onSaved)
                        return false
                    }
                })
                .submit()
        }
    }

    @Deprecated("БАГ. Не используется, т.к. каждый раз при копировании создаётся новый файл.")
    fun saveImageForCopy(
        act: Activity?,
        imageUrl: String,
        onSaved: (Uri) -> Unit = {},
        onLoadFailed: () -> Unit = {}
    ) {
        act?.let {
            Glide.with(act)
                .asBitmap()
                .load(imageUrl)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoadFailed.invoke()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let { bitmap ->
                            val uri = saveBitmapImage(bitmap, act)
                            if (uri != null) {
                                onSaved.invoke(uri)
                            }
                        }
                        return false
                    }
                })
                .submit()
        }
    }

    private fun saveBitmapImage(bitmap: Bitmap, act: Activity): Uri? {
        var uri: Uri? = null
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, IMAGE_PNG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + act.getString(R.string.app_name))
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            uri = act.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    val outputStream = act.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        } catch (e: Exception) {
                            Timber.e(e.toString())
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    act.contentResolver.update(uri, values, null, null)
                    val clipboardManager = act.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val theClip = ClipData.newUri(act.contentResolver, "Image", uri)
                    clipboardManager.setPrimaryClip(theClip)
                } catch (e: Exception) {
                    Timber.e(e.toString())
                }
            }
        } else {
            val imageFileFolder =
                File(Environment.getExternalStorageDirectory().toString() + '/' + act.getString(R.string.app_name))
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "image.png"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Timber.e(e.toString())
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                uri = act.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                val clipboardManager = act.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val theClip = ClipData.newUri(act.contentResolver, "Image", uri)
                clipboardManager.setPrimaryClip(theClip)
            } catch (e: Exception) {
                Timber.e(e.toString())
            }
        }
        return uri
    }


    fun saveImageToDeviceFromAppDirectory(
        act: Activity?,
        imageUrl: String?,
        onSaved: (Uri) -> Unit = {}
    ) {
        act?.let { activity ->
            imageUrl?.let { image ->
                val file = File(image)
                saveImageFile(activity, file, image, onSaved)
            }
        }
    }

    fun saveImageFile(context: Context, image: File?, url: String, onSaved: (Uri) -> Unit = {}) {
        val imageFileName = imageUrlRecognizer(url)
        val imageFile = getOutputMediaFile(imageFileName) ?: return

        try {
            val outputStream = FileOutputStream(imageFile)
            val inputStream = FileInputStream(image)

            val inputFileChannel = inputStream.channel
            val outputFileChannel = outputStream.channel

            inputFileChannel.transferTo(0, inputFileChannel.size(), outputFileChannel)
            outputFileChannel.close()
            inputFileChannel.close()

            galleryAddPic(context, imageFile.absolutePath, onSaved)
            Timber.d(this::class.java.simpleName, "Image was saved successfully url: $url, image: $imageFile,")
        } catch (e: Exception) {
            Timber.e(this::class.java.simpleName, "ERROR Save image file:$e")
            e.printStackTrace()
        }
    }

    fun saveImageFile(
        context: Context,
        sourceImage: File?,
        fileName: String,
        onSuccess: (Uri) -> Unit = {},
        onError: (Exception) -> Unit
    ) {
        val imageFile = getOutputMediaFile(fileName) ?: return
        try {
            val outputStream = FileOutputStream(imageFile)
            val inputStream = FileInputStream(sourceImage)

            val inputFileChannel = inputStream.channel
            val outputFileChannel = outputStream.channel

            inputFileChannel.transferTo(0, inputFileChannel.size(), outputFileChannel)
            outputFileChannel.close()
            inputFileChannel.close()

            galleryAddPic(context, imageFile.absolutePath, onSuccess)
            Timber.d("Image was saved successfully url: $sourceImage, image: $imageFile,")
        } catch (e: Exception) {
            Timber.e("ERROR Save image file:$e")
            e.printStackTrace()
            onError(e)
        }
    }

    fun copyImageFile(act: Activity, image: File?, onSaved: (Uri) -> Unit = {}) {
        val cachePath = File(act.cacheDir, "images")
        cachePath.mkdirs()
        image?.let {
            onSaved(FileProvider.getUriForFile(act, act.getAuthority(), it))
        }
    }

    @Throws(IOException::class)
    fun cacheFile(context: Context, url: String, inputFile: File): Uri {
        val tempFileName = imageUrlRecognizer(url)
        val outputFile = File(context.cacheDir, tempFileName)
        val outputStream = FileOutputStream(outputFile)
        val inputStream = FileInputStream(inputFile)
        val inputFileChannel = inputStream.channel
        val outputFileChannel = outputStream.channel
        inputFileChannel.transferTo(0, inputFileChannel.size(), outputFileChannel)
        outputFileChannel.close()
        inputFileChannel.close()
        return FileProvider.getUriForFile(context, context.getAuthority(), outputFile)
    }

    @Throws(IOException::class)
    fun cacheImageFile(context: Context, url: String, inputFile: File): Uri {
        val tempFileName = imageUrlRecognizer(url)
        val dirFolder = File(context.cacheDir, url.hashCode().toString())
        if (!dirFolder.exists()) {
            dirFolder.mkdir()
        }
        val outputFile = File(dirFolder, tempFileName)
        val outputStream = FileOutputStream(outputFile)
        val inputStream = FileInputStream(inputFile)
        val inputFileChannel = inputStream.channel
        val outputFileChannel = outputStream.channel
        inputFileChannel.transferTo(0, inputFileChannel.size(), outputFileChannel)
        outputFileChannel.close()
        inputFileChannel.close()
        return Uri.fromFile(outputFile)
    }

    /**
     * Метод распознаёт различные URL, парсит их на наличие
     * имени файл
     * т.к. url могут быть и от Noomeera и сторонние сервисы, например Giphy
     */
    private fun imageUrlRecognizer(url: String): String {
        return if (url.isGiphyUrl()) {
            val random = (0..Int.MAX_VALUE).random()
            "${GIPHY_BRAND_NAME}_$random$MEDIA_EXT_GIF"
        } else {
            url.substring(url.lastIndexOf('/') + 1)
        }
    }

    private fun getOutputMediaFile(imageFileName: String): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString()
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs())
                return null
        }
        return File(mediaStorageDir.path + File.separator + imageFileName)
    }

    private fun galleryAddPic(context: Context, imagePath: String, onSaved: (Uri) -> Unit = {}) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
        onSaved(contentUri)
    }

    fun saveMediaToGallery(context: Context, filePath: String): Uri? {
        val sourceFile = File(filePath)

        if (!sourceFile.exists()) {
            return null
        }

        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(filePath.replace(" ",""))
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension) ?: run {
            return null
        }

        val contentValues = ContentValues().apply {
            val currentTime = System.currentTimeMillis() / UNIX_SECOND_MULTIPLIER
            put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, when {
                mimeType.startsWith(MEDIA_IMAGE) -> Environment.DIRECTORY_PICTURES
                mimeType.startsWith(MEDIA_VIDEO) -> Environment.DIRECTORY_MOVIES
                else -> Environment.DIRECTORY_DOWNLOADS
            })
            put(MediaStore.MediaColumns.DATE_ADDED, currentTime)
            put(MediaStore.MediaColumns.DATE_MODIFIED, currentTime)
        }

        val contentUri: Uri = when {
            mimeType.startsWith(MEDIA_IMAGE) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            mimeType.startsWith(MEDIA_VIDEO) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> throw IllegalArgumentException("Unsupported file type")
        }

        val fileUri: Uri? = context.contentResolver.insert(contentUri, contentValues)

        fileUri?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return uri
        }

        return null
    }



    fun getNavigationBarSize(context: Context): Point? {
        val appUsableSize = getAppUsableScreenSize(context)
        val realScreenSize = getRealScreenSize(context)

        // navigation bar on the side
        if (appUsableSize.x < realScreenSize.x) {
            return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
        }

        // navigation bar at the bottom
        return if (appUsableSize.y < realScreenSize.y) {
            Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
        } else Point()

        // navigation bar is not present
    }

    private fun getAppUsableScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    private fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size)
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Display::class.java.getMethod("getRawWidth").invoke(display) as Int)
                size.y = (Display::class.java.getMethod("getRawHeight").invoke(display) as Int)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        return size
    }

    private fun String.isGiphyUrl() = GIPHY_BRAND_NAME in this
}

fun getScreenWidth(fragment: Fragment): Int {
    val activity = fragment.activity ?: return 0
    return getScreenWidth(activity)
}

fun getScreenWidth(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
        val insets: Insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun isRotatedFromExif(path: String?): Boolean? {
    if (path == null) return null
    val exif = getExif(path)
    return if (exif != null) {
        isRotated(exif.getRotation())
    } else {
        null
    }
}

fun isRotated(rotation: Int): Boolean = rotation == 90 || rotation == 270

fun ExifInterface.getRotation(): Int {
    return when (getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

fun getExif(path: String): ExifInterface? {
    return try {
        ExifInterface(path)
    } catch (ex: Exception) {
        Timber.d(ex)
        null
    }
}


package com.meera.core.utils.imagecapture.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.hardware.camera2.CameraCharacteristics
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.meera.core.extensions.getAuthority
import com.meera.core.utils.camera.CameraLensFacing
import com.meera.core.utils.imagecapture.ui.ImageCaptureUtils.getCameraImageFile
import com.meera.core.utils.imagecapture.ui.model.ImageCaptureResultModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

object ImageCaptureUtils {

    @JvmStatic
    fun getCameraImageFile(): File? {
        return try {
            val datePattern = "yyyyMMddHHmmss"
            val currentDate = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date())
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            var postfix: Long = Random.nextLong()
            postfix = if (postfix == Long.MIN_VALUE) {
                0
            } else {
                abs(postfix)
            }
            val fileName = "JPEG_${currentDate}_$postfix.jpg"
            File(directory, fileName)
        } catch (e: IOException) {
            Timber.e("ImageCaptureUtils - Could not create imageFile for camera")
            null
        }
    }

    @JvmStatic
    fun getImageFromCamera(
        activity: Activity,
        listener: Listener,
        @CameraLensFacing cameraLensFacing: Int = CameraCharacteristics.LENS_FACING_BACK
    ) {
        activity.getImageFromCamera(listener, cameraLensFacing)
    }

    interface Listener {
        fun onResult(result: ImageCaptureResultModel)
        fun onFailed() = Unit
    }
}

private fun updateIntentForCameraFacing(cameraIntent: Intent, @CameraLensFacing cameraLensFacing: Int) {
    Timber.d("updateIntentForCameraFacing called; cameraLensFacing: $cameraLensFacing")
    if (cameraLensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        // на xiaomi/redmi нужен этот депрекейтнутый параметр для определения направления камеры
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", CAMERA_FACING_FRONT)
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        cameraIntent.putExtra("camerafacing", "front")
        cameraIntent.putExtra("previous_mode", "front")
    } else {
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_BACK", 1)
        // на xiaomi/redmi нужен этот депрекейтнутый параметр для определения направления камеры
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", CAMERA_FACING_BACK)
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
        cameraIntent.putExtra("camerafacing", "rear")
        cameraIntent.putExtra("previous_mode", "rear")
    }
}

fun Activity.getImageFromCamera(
    listener: ImageCaptureUtils.Listener,
    @CameraLensFacing cameraLensFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
) {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (cameraIntent.resolveActivity(packageManager) == null) {
        Timber.e("camera application not found")
        return
    }

    updateIntentForCameraFacing(cameraIntent, cameraLensFacing)

    getCameraImageFile()?.let { mediaFile ->
        val imageFileUri = Uri.fromFile(mediaFile)

        val imageContentUri = FileProvider.getUriForFile(this, this.getAuthority(), mediaFile)

        val resolvedIntentActivities = packageManager.queryIntentActivities(
            cameraIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        for (resolvedIntentInfo in resolvedIntentActivities) {
            grantUriPermission(
                resolvedIntentInfo.activityInfo.packageName,
                imageContentUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageContentUri)

        TedOnActivityResult.with(this)
            .setIntent(cameraIntent)
            .setListener { resultCode: Int, _: Intent? ->
                if (resultCode == Activity.RESULT_OK) {
                    imageFileUri?.let { imageFileUri ->
                        val connectionClient = object : MediaScannerConnection.MediaScannerConnectionClient {

                            override fun onMediaScannerConnected() = Unit

                            override fun onScanCompleted(s: String, uri: Uri) {
                                runOnUiThread {
                                    val result = ImageCaptureResultModel(
                                        fileUri = imageFileUri,
                                        contentUri = imageContentUri
                                    )
                                    listener.onResult(result)
                                }
                            }
                        }
                        MediaScannerConnection.scanFile(
                            this,
                            arrayOf(imageFileUri.path),
                            arrayOf("image/jpeg"),
                            connectionClient
                        )
                    } ?: run { listener.onFailed() }
                } else {
                    listener.onFailed()
                }
            }
            .startActivityForResult()
    }
}

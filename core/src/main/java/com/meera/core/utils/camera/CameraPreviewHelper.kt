package com.meera.core.utils.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber


class CameraPreviewHelper constructor(val context: Context) {

    fun attachPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        @CameraLensFacing lensFacing: Int
    ) {
        val cameraProviderFeature = ProcessCameraProvider.getInstance(context)
        cameraProviderFeature.addListener({
            try {
                val cameraProvider = cameraProviderFeature.get()
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                preview.setSurfaceProvider(previewView.surfaceProvider)
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            } catch (e: Exception) {
                Timber.e("Camera exception: $e")
            }
        }, ContextCompat.getMainExecutor(context))
    }
}


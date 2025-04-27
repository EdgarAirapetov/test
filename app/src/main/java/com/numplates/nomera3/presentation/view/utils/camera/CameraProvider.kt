package com.numplates.nomera3.presentation.view.utils.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

@Deprecated("Transited to core")
class CameraProvider private constructor(
        val context: Context?,
        val orientation: CameraOrientation?
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector? = null
    private var preview: Preview? = null

    // Default camera config
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    data class Builder(var context: Context) {
        private var cameraOrientation = CameraOrientation.FRONT

        fun cameraOrientation(orientation: CameraOrientation) =
                apply { cameraOrientation = orientation }

        fun build() =
                CameraProvider(context, cameraOrientation)
    }

    init {
        lensFacing = when (orientation) {
            CameraOrientation.BACK -> CameraSelector.LENS_FACING_BACK
            CameraOrientation.FRONT -> CameraSelector.LENS_FACING_FRONT
            else -> error("Back and front camera are unavailable")
        }
    }

    // @RequiresPermission(Manifest.permission.CAMERA)
    fun run(ready: () -> Unit) {
        context?.let {
            val cameraProviderFeature =
                    ProcessCameraProvider.getInstance(context)

            cameraProviderFeature.addListener(Runnable {
                cameraProvider = cameraProviderFeature.get()

                // Setup preview
                preview = Preview.Builder()
                        //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        // .setTargetResolution(Size(dpToPx(122), dpToPx(122)))
                        .build()

                // Select back camera
                cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                ready.invoke()
            }, ContextCompat.getMainExecutor(context))
        } ?: let {
            throw IllegalStateException("Context is NULL")
        }
    }

    fun startCameraPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        // Unbind use cases before rebinding
        // TODO: 22.07.2020 Crashlytics FIX - (maybe: runOnUiThread { cameraProvider.unbind (previewUseCase)})
        Timber.e("START Cam Preview")
        cameraProvider?.unbindAll()
        // https://t.codebug.vip/questions-2546318.htm
        //lifecycleOwner.lifecycle.currentState

        try {

            preview?.setSurfaceProvider(previewView.surfaceProvider)
            cameraSelector?.let {
                cameraProvider
                        ?.bindToLifecycle(lifecycleOwner, it, preview)
            }

        } catch (ex: Exception) {
            Timber.e("Camera exception: ${ex.message}")
        }
    }

    // For using in RecyclerView
    fun startCameraPreviewViewHolder(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        context?.let {
            val cameraProviderFeature =
                    ProcessCameraProvider.getInstance(context)

            cameraProviderFeature.addListener(Runnable {
                try {
                    cameraProvider = cameraProviderFeature.get()
                    cameraProvider?.unbindAll()
                } catch (e: Exception) {
                    Timber.e("Camera exception: ${e.message}")
                }

                // Setup preview (UseCase)
                preview = Preview.Builder().build()

                // Select back camera
                cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                try {

                    preview?.setSurfaceProvider(previewView.surfaceProvider)
                    cameraSelector?.let {
                        val camera = cameraProvider
                                ?.bindToLifecycle(lifecycleOwner, it, preview)
                    }

                } catch (ex: Exception) {
                    Timber.e("Camera exception: ${ex.message}")
                }
            }, ContextCompat.getMainExecutor(context))
        } ?: let {
            throw IllegalStateException("Context is NULL")
        }
    }

}


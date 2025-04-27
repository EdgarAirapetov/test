package com.meera.core.utils.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BlurHelper(
    context: Context,
    private var lifecycle: Lifecycle?,
    private val scaleFactor: Float = 0.3f,
) {

    private val blurKit: BlurKit
    private val appContext = context.applicationContext
    private var blurJob : Job? = null

    init {
        BlurKit.init(context)
        blurKit = BlurKit.getInstance()
    }

    fun updateLifecycle(lifecycle: Lifecycle) {
        this.lifecycle = lifecycle
    }

    fun blurByUrl(url: String?, isVideo: Boolean = false, onSuccess: (Bitmap?) -> Unit) {
        if (url.isNullOrEmpty() || isVideo) {
            onSuccess(null)
            return
        }
        Glide.with(appContext)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    doBlur(resource, onSuccess)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    onSuccess(null)
                }
            })
    }

    fun blurByBitmap(resource: Bitmap, onSuccess: (Bitmap?) -> Unit) {
        doBlur(resource, onSuccess)
    }

    private fun doBlur(resource: Bitmap, onSuccess: (Bitmap?) -> Unit) {
        blurJob = lifecycle?.coroutineScope?.launch {
            val result: Bitmap?
            withContext(Dispatchers.IO) {
                val scalableBitmap = Bitmap.createScaledBitmap(
                    resource,
                    (resource.width * scaleFactor).toInt(),
                    (resource.height * scaleFactor).toInt(),
                    false
                )

                val canvas = Canvas(scalableBitmap)
                val myPaint = Paint()
                myPaint.color = Color.parseColor("#99000000")
                canvas.drawRect(0f, 0f, (resource.width * scaleFactor), (resource.height * scaleFactor).toFloat(), myPaint)
                result = try {
                    blurKit.blur(scalableBitmap, 25)
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
            }
            onSuccess(result)
        }
    }

    fun cancel() {
        lifecycle = null
        blurJob?.cancel()
        blurJob = null
    }
}

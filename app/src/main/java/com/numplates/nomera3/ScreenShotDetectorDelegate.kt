package com.numplates.nomera3

import android.app.Activity
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

private const val SCREENSHOT_TIME_DELAY = 500

class ScreenShotDetectorDelegate(
    private val activityReference: WeakReference<Activity>,
    private val screenshotDetectedCallback: () -> Unit
) {

    private var lastScreenshotTime: Long = 0

    constructor(
        activity: Activity,
        screenshotDetectedListener: () -> Unit
    ) : this(WeakReference(activity), screenshotDetectedListener)

    private var job: Job? = null

    fun startScreenshotDetection() {
        job = CoroutineScope(Dispatchers.Main).launch {
            createContentObserverFlow()
                .collect {
                    if (System.currentTimeMillis() - lastScreenshotTime < SCREENSHOT_TIME_DELAY) return@collect
                    screenshotDetectedCallback.invoke()
                    lastScreenshotTime = System.currentTimeMillis()
                }
        }
    }

    fun stopScreenshotDetection() {
        job?.cancel()
    }

    private fun createContentObserverFlow() = channelFlow {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                uri?.let { trySend(it) }
            }
        }
        activityReference.get()
            ?.contentResolver
            ?.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )
        awaitClose {
            activityReference.get()
                ?.contentResolver
                ?.unregisterContentObserver(contentObserver)
        }
    }

}

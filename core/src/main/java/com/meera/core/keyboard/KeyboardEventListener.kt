package com.meera.core.keyboard

import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

class KeyboardEventListener(
    private val activity: AppCompatActivity,
    private val callback: (isOpen: Boolean) -> Unit
) : DefaultLifecycleObserver {

    private val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastState: Boolean = activity.isKeyboardOpen()

        override fun onGlobalLayout() {
            val isOpen = activity.isKeyboardOpen()
            if (isOpen == lastState) {
                return
            } else {
                dispatchKeyboardEvent(isOpen)
                lastState = isOpen
            }
        }
    }

    init {
        // Make the component lifecycle aware
        activity.lifecycle.addObserver(this)
    }

    @Suppress("KotlinConstantConditions")
    private fun dispatchKeyboardEvent(isOpen: Boolean) {
        when {
            isOpen -> callback(true)
            !isOpen -> callback(false)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        unregisterKeyboardListener()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Dispatch the current state of the keyboard
        dispatchKeyboardEvent(activity.isKeyboardOpen())
        registerKeyboardListener()
    }

    private fun registerKeyboardListener() {
        Timber.d("registerKeyboardListener")
        activity.getRootView().viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    private fun unregisterKeyboardListener() {
        Timber.d("unregisterKeyboardListener")
        activity.getRootView().viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}

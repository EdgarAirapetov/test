package com.numplates.nomera3.modules.maps.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.StyleRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.numplates.nomera3.R

/**
 * We remove and restore window animations to prevent slide animation from happening
 * when returning to app from launcher, fixes [BR-16994](https://nomera.atlassian.net/browse/BR-16994)
 */
open class MapBottomSheetDialog(
    private val activity: FragmentActivity,
    @StyleRes theme: Int? = null
    ) : BottomSheetDialog(activity, theme ?: R.style.BottomSheetDialogTheme) {

    private val handler = Handler(Looper.getMainLooper())
    private var windowAnimations: Int = NO_WINDOW_ANIMATIONS
    private var restoreWindowAnimationsRunnable: Runnable? = null
    private val parentLifecycleObserver = ParentLifecycleObserver()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        behavior.isHideable = true
        behavior.skipCollapsed = true
        behavior.isFitToContents = true
        windowAnimations = window?.attributes?.windowAnimations ?: NO_WINDOW_ANIMATIONS
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        activity.lifecycle.addObserver(parentLifecycleObserver)
    }

    override fun onStop() {
        super.onStop()
        activity.lifecycle.removeObserver(parentLifecycleObserver)
    }

    private inner class ParentLifecycleObserver : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            restoreWindowAnimationsRunnable = Runnable {
                window?.setWindowAnimations(windowAnimations)
            }.also {
                handler.postDelayed(it, RESTORE_ANIMATIONS_DELAY_MS)
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            restoreWindowAnimationsRunnable?.let(handler::removeCallbacks)
            window?.setWindowAnimations(NO_WINDOW_ANIMATIONS)
        }
    }

    companion object {
        private const val NO_WINDOW_ANIMATIONS = -1
        private const val RESTORE_ANIMATIONS_DELAY_MS = 100L
    }
}

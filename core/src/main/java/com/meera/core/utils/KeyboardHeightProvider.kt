package com.meera.core.utils

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.meera.core.extensions.displayHeight
import kotlin.properties.Delegates

/**
 * Ссылка на библиотеку -
 * https://github.com/turnsk/keyboardheight/blob/master/lib/src/main/java/sk/turn/keyboardheight/KeyboardHeightProvider.java
 * */
class KeyboardHeightProvider(
    private val rootView: View,
    private val useDisplayHeight: Boolean
) : PopupWindow(rootView.context) {

    constructor(rootView: View) : this(
        rootView,
        false
    )

    val context: Context = rootView.context
    var keyboardHeight: Int = 0
    var observer: ((height: Int) -> Unit)? = null

    private val popupView: View = View(context)
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        // Fix for Xiaomi
        heightDelegate = popupView.rootView.height
    }

    private var heightDelegate by Delegates.observable(0) { _, oldHeight, newHeight ->
        val currentHeight = if (useDisplayHeight) context.displayHeight else oldHeight
        if (newHeight < currentHeight && newHeight != oldHeight) {
            keyboardHeight = currentHeight - newHeight
        } else if (newHeight != oldHeight) {
            keyboardHeight = 0
        }
        observer?.invoke(keyboardHeight)
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun start() {
        if (!isShowing && rootView.windowToken != null) {
            showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0)
            popupView.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    fun release() {
        popupView.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        observer = null
        keyboardHeight = 0
        dismiss()
    }

    fun isOpened(): Boolean {
        return keyboardHeight > 0
    }

    init {
        popupView.layoutParams = ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        contentView = popupView
        softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED
        setBackgroundDrawable(null)
        width = 0
        height = LinearLayout.LayoutParams.MATCH_PARENT
        isTouchable = false
        start()
    }
}

package com.numplates.nomera3.modules.chat.views

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.Window
import androidx.core.graphics.ColorUtils

/**
 * `SupportDimView` is a custom `View` that overlays a semi-transparent dimming effect on the screen
 * and dynamically adjusts the status bar color to match.
 *
 * It's designed to be used as an overlay, such as during a modal dialog or while showing
 * temporary loading UI. When visible (`View.VISIBLE`), it dims the underlying content and applies a tint
 * to the status bar. When hidden (`View.INVISIBLE` or `View.GONE`), it restores the status bar to its original color.
 *
 * The status bar tinting logic is tied to the visibility state of the view. If the view is shown,
 * the status bar is tinted; if the view is hidden, the original status bar color is restored.
 *
 * @constructor Creates a new `SupportDimView`.
 * @param context The `Context` in which the view is running, through which it can
 *        access the current theme, resources, etc.
 * @param attributeSet Optional `AttributeSet` containing attributes supplied from an XML file.
 *        This is useful to pass attributes like `android:id`, `android:layout_width`, etc.
 * @param defStyleAtr An attribute in the current theme that contains a reference to a style
 *        resource that supplies default values for the `View`. Can be `0` to not look for
 *        defaults.
 */
class SupportDimView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : View(context, attributeSet, defStyleAtr) {

    /**
     * A reference to the `Window` object associated with the current `Activity`.
     * It is `null` if the context is not an `Activity`.
     */
    private val window: Window? get() = (context as? Activity)?.window

    /**
     * The color used for the dimming effect and the status bar tint.
     * It is obtained from the view's background, assuming it's a `ColorDrawable`.
     */
    private val tintColor: Int by lazy { (background as ColorDrawable).color }

    /**
     * Stores the original status bar color before the tint is applied.
     * It is `null` if the status bar has not been modified by this view.
     */
    private var statusBarColor: Int? = null

    /**
     * Overrides the `onVisibilityChanged` method of the `View` class.
     * This method is called when the visibility of the view changes.
     *
     * @param changedView The view whose visibility has changed. Although not used, it's part of the required signature.
     * @param visibility The new visibility of the view (e.g., `View.VISIBLE`, `View.INVISIBLE`, `View.GONE`).
     */
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // Check if the view is currently shown.
        when (isShown) {
            true -> tintStatusBar()
            else -> clearTintStatusBar()
        }
    }

    /**
     * Tints the status bar with the `tintColor`.
     * It saves the original status bar color before modification.
     */
    private fun tintStatusBar() {
        // Save the current status bar color before tinting.
        statusBarColor = window?.statusBarColor
        // If the status bar color is available and not already transparent, apply the tint.
        if (statusBarColor != null && statusBarColor != 0) {
            window?.statusBarColor = ColorUtils.compositeColors(tintColor, requireNotNull(statusBarColor))
        }
    }

    /**
     * Clears the tint from the status bar and restores the original color.
     * It sets the status bar color back to the original color that was saved when the tint was applied.
     */
    private fun clearTintStatusBar() {
        // If a status bar color was previously saved, restore it.
        if (statusBarColor != null) {
            window?.statusBarColor = requireNotNull(statusBarColor)
            statusBarColor = null
        }
    }
}

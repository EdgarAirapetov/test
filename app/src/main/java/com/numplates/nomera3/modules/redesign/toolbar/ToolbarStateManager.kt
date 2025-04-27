package com.numplates.nomera3.modules.redesign.toolbar

import android.app.Activity
import android.content.Context
import android.view.Window
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.meera.uikit.widgets.nav.UiKitToolbarView
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import timber.log.Timber

/**
 * `ToolbarStateManager` is responsible for saving and restoring the state of a
 * `UiKitToolbarView` across lifecycle events. It observes the lifecycle of an
 * Activity or Fragment and automatically saves the toolbar's visual configuration
 * when the specified "save" event occurs (defaulting to `ON_STOP`) and restores it
 * when the "restore" event occurs (defaulting to `ON_START`).
 *
 * This allows the toolbar to maintain its appearance (e.g., visibility of buttons,
 * title, background color) even when the user navigates away from and back to the
 * screen.
 *
 * @constructor Creates a new `ToolbarStateManager`.
 * @param saveEvent The `Lifecycle.Event` that triggers saving the toolbar state.
 *                  Defaults to `Lifecycle.Event.ON_STOP`.
 * @param restoreEvent The `Lifecycle.Event` that triggers restoring the toolbar
 *                     state. Defaults to `Lifecycle.Event.ON_START`.
 */
class ToolbarStateManager(
    private val saveEvent: Lifecycle.Event = Lifecycle.Event.ON_STOP,
    private val restoreEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
) : LifecycleEventObserver {

    private var statusBarColor: Int? = null
    private var darkMode: Boolean? = null
    private var hasSecondButton: Boolean? = null
    private var hasThirdButton: Boolean? = null
    private var notificationsCount: Int? = null
    private var secondButton: UiKitToolbarView.SecondButton? = null
    private var showButtons: Boolean? = null
    private var showLogo: Boolean? = null
    private var showShadow: Boolean? = null
    private var state: UiKitToolbarViewState? = null
    private var transparentBackground: Boolean? = null
    private var isChangeStateWithAnimation: Boolean? = null

    private var context: Context? = null

    private val window: Window? get() = (context as? Activity)?.window
    private val toolbar: UiKitToolbarView get() = NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar()

    /**
     *  Observes lifecycle events to trigger save and restore operations.
     *
     *  @param source The `LifecycleOwner` whose lifecycle is being observed.
     *  @param event The `Lifecycle.Event` that occurred.
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == saveEvent) saveToolbarState()
        if (event == restoreEvent) restoreToolbarState()
    }

    /**
     * Attaches the `ToolbarStateManager` to the lifecycle of a given `Context`
     * (usually an Activity or Fragment). This allows the manager to observe
     * lifecycle events and automatically handle toolbar state saving and restoring.
     *
     * @param context The `Context` to associate with the `ToolbarStateManager`.
     * @param lifecycle The `Lifecycle` of the `Context` to observe.
     */
    fun attachToLifecycle(context: Context, lifecycle: Lifecycle) {
        this.context = context
        lifecycle.addObserver(this)
    }

    /**
     * Saves the current state of the toolbar. This includes properties like
     * its background color, button visibility, and other visual attributes.
     * The saved state will be restored later when the `restoreEvent` occurs.
     *
     * The current values of the toolbar properties are also logged using Timber
     * for debugging purposes.
     */
    private fun saveToolbarState() {
        val logMessage = buildString {
            appendLine("Saving toolbar state:")
            appendLine("  darkMode: ${toolbar.darkMode}")
            appendLine("  hasSecondButton: ${toolbar.hasSecondButton}")
            appendLine("  hasThirdButton: ${toolbar.hasThirdButton}")
            appendLine("  notificationsCount: ${toolbar.notificationsCount}")
            appendLine("  secondButton: ${toolbar.secondButton}")
            appendLine("  showButtons: ${toolbar.showButtons}")
            appendLine("  showLogo: ${toolbar.showLogo}")
            appendLine("  showShadow: ${toolbar.showShadow}")
            appendLine("  state: ${toolbar.state}")
            appendLine("  transparentBackground: ${toolbar.transparentBackground}")
            appendLine("  isChangeStateWithAnimation: ${toolbar.isChangeStateWithAnimation}")
            appendLine("  statusBarColor: ${window?.statusBarColor}")
        }
        Timber.d(logMessage)

        darkMode = toolbar.darkMode
        hasSecondButton = toolbar.hasSecondButton
        hasThirdButton = toolbar.hasThirdButton
        notificationsCount = toolbar.notificationsCount
        secondButton = toolbar.secondButton
        showButtons = toolbar.showButtons
        showLogo = toolbar.showLogo
        showShadow = toolbar.showShadow
        state = toolbar.state
        transparentBackground = toolbar.transparentBackground
        isChangeStateWithAnimation = toolbar.isChangeStateWithAnimation
        statusBarColor = window?.statusBarColor
    }

    /**
     * Restores the toolbar's state to the values that were previously saved by
     * `saveToolbarState`.  This ensures that the toolbar maintains its visual
     * appearance across lifecycle events, such as screen rotations or navigating
     * back to a screen.
     *
     * The restored values are also logged using Timber for debugging.
     */
    private fun restoreToolbarState() {
        val logMessage = buildString {
            appendLine("Restoring toolbar state:")
            appendLine("  darkMode: $darkMode")
            appendLine("  hasSecondButton: $hasSecondButton")
            appendLine("  hasThirdButton: $hasThirdButton")
            appendLine("  notificationsCount: $notificationsCount")
            appendLine("  secondButton: $secondButton")
            appendLine("  showButtons: $showButtons")
            appendLine("  showLogo: $showLogo")
            appendLine("  showShadow: $showShadow")
            appendLine("  state: $state")
            appendLine("  transparentBackground: $transparentBackground")
            appendLine("  isChangeStateWithAnimation: $isChangeStateWithAnimation")
            appendLine("  statusBarColor: $statusBarColor")
        }
        Timber.d(logMessage)

        darkMode?.let { toolbar.darkMode = it }
        hasSecondButton?.let { toolbar.hasSecondButton = it }
        hasThirdButton?.let { toolbar.hasThirdButton = it }
        notificationsCount?.let { toolbar.notificationsCount = it }
        secondButton?.let { toolbar.secondButton = it }
        showButtons?.let { toolbar.showButtons = it }
        showLogo?.let { toolbar.showLogo = it }
        showShadow?.let { toolbar.showShadow = it }
        state?.let { toolbar.state = it }
        transparentBackground?.let { toolbar.transparentBackground = it }
        isChangeStateWithAnimation?.let { toolbar.isChangeStateWithAnimation = it }
        statusBarColor?.let { window?.statusBarColor = it }
    }
}

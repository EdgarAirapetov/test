package com.numplates.nomera3.presentation.utils.buttons

import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.numplates.nomera3.R
import com.meera.core.extensions.dp

/**
 * The idea of this class is to change buttons sizes and colors based on their priority.
 */
class FloatingButtonsManager(context: Context, lifecycleOwner: LifecycleOwner) : DefaultLifecycleObserver {

    private val appContext: Context
    private val accentStateList: ColorStateList
    private val defaultStateList: ColorStateList
    private val smallSize = 50.dp
    private val bigSize = 60.dp
    private val globalLayoutListener by lazy { provideGlobalLayoutListener() }
    private val fabVisibilityMap = mutableMapOf<Int, Int>()

    private val _wrappedButtons = mutableListOf<FabWrapper>()
    private var _parent: ViewGroup? = null

    init {
        appContext = context.applicationContext
        accentStateList = ColorStateList.valueOf(ContextCompat.getColor(appContext, R.color.ui_purple))
        defaultStateList = ColorStateList.valueOf(ContextCompat.getColor(appContext, R.color.white))
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * @param parent nearest [ViewGroup] which can track [wrappedButtons] state changes
     * @param wrappedButtons [FabWrapper] buttons with information about [FloatingActionButton]
     */
    fun trackFloatingButtonsStateChanges(parent: ViewGroup, wrappedButtons: Iterable<FabWrapper>) {
        if (parent != _parent) {
            parent.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
            _parent = parent
            _wrappedButtons.clear()
            _wrappedButtons.addAll(wrappedButtons)
            cacheButtonsVisibility()
            refreshFloatActionButtons()
        } else {
            error("This view has been used. Please make sure client don't use it second time.")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        _parent?.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        _parent = null
        _wrappedButtons.clear()
        fabVisibilityMap.clear()
    }

    private fun provideGlobalLayoutListener(): ViewTreeObserver.OnGlobalLayoutListener {
        return ViewTreeObserver.OnGlobalLayoutListener {
            if (hasVisibilityBeenChanged()) {
                refreshFloatActionButtons()
            }
        }
    }

    private fun refreshFloatActionButtons() {
        val visibleButtons = _wrappedButtons.filter { item -> item.button.isVisible }
        updateButtonsSizes(visibleButtons)
        updateButtonsColors(visibleButtons)
    }

    private fun cacheButtonsVisibility() {
        _wrappedButtons.forEach { fabWrapper ->
            fabVisibilityMap[fabWrapper.button.id] = fabWrapper.button.visibility
        }
    }

    /**
     * Check if buttons visibility were changed and update cached visibility values.
     *
     * @return 'true' if at least one button was changed and 'false' if nothing changed.
     */
    private fun hasVisibilityBeenChanged(): Boolean {
        var flag = false
        _wrappedButtons.forEach { fabWrapper ->
            if (fabVisibilityMap[fabWrapper.button.id] != fabWrapper.button.visibility) {
                fabVisibilityMap[fabWrapper.button.id] = fabWrapper.button.visibility
                flag = true
            }
        }
        return flag
    }

    /**
     * Change floating action buttons sizes based on the following logic:
     *  - 2 buttons always big
     *  - otherwise first and last buttons are small and buttons in the middle are big
     */
    private fun updateButtonsSizes(fabButtons: List<FabWrapper>) {
        if (fabButtons.size <= 2) {
            fabButtons.forEach { item -> item.button.customSize = bigSize }
        } else {
            fabButtons.forEachIndexed { index, fabWrapper ->
                val buttonSize = when (index) {
                    0 -> smallSize
                    fabButtons.size - 1 -> smallSize
                    else -> bigSize
                }
                fabWrapper.button.customSize = buttonSize
            }
        }
    }

    /**
     * Sort visible buttons by their priority and set accent color to the highest priority button.
     */
    private fun updateButtonsColors(fabButtons: List<FabWrapper>) {
        fabButtons.sortedByDescending { item -> item.colorPriority }
                .forEachIndexed { index, fabWrapper ->
                    val (iconTintList, backgroundTintList) = when (index) {
                        0 -> defaultStateList to accentStateList
                        else -> accentStateList to defaultStateList
                    }
                    fabWrapper.button.supportImageTintList = iconTintList
                    fabWrapper.button.supportBackgroundTintList = backgroundTintList
                }
    }
}
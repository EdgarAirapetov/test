package com.numplates.nomera3.modules.gifservice.ui.entity

/**
 * Represents the current state of media-related keyboards and input methods.
 *
 * This data class encapsulates information about whether the software keyboard
 * is open, whether an in-app media sheet (like a GIF picker) is open, and whether
 * there's an ongoing transition between different keyboard/input states.
 *
 * @property isSoftwareKeyboardOpened `true` if the standard software keyboard is currently visible, `false` otherwise.
 * @property isInAppMediaSheetOpened `true` if an in-app media sheet (e.g., for selecting GIFs or stickers) is currently visible, `false` otherwise.
 * @property isChangingKeyboard `true` if there's an ongoing transition between different keyboard states (e.g., opening, closing, switching between software keyboard and media sheet), `false` otherwise.  This can be useful to prevent UI glitches during transitions.
 */
data class MediaKeyboardState(
    val isSoftwareKeyboardOpened: Boolean = false,
    val isInAppMediaSheetOpened: Boolean = false,
    val isChangingKeyboard: Boolean = false,
) {

    /**
     * Checks if all keyboard-related input methods are currently hidden.
     *
     * @return `true` if neither the software keyboard nor the in-app media sheet is open, and there's no ongoing keyboard state change; `false` otherwise.
     */
    fun isHiddenKeyboards(): Boolean {
        return !isSoftwareKeyboardOpened && !isInAppMediaSheetOpened && !isChangingKeyboard
    }
}

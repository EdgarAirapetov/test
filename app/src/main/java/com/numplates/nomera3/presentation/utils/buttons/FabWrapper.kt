package com.numplates.nomera3.presentation.utils.buttons

import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Wrapper class which holds a link to the floating action button and extra information.
 *
 * @param button [FloatingActionButton] object.
 * @param colorPriority specify [button] priority. Higher value means higher priority.
 */
class FabWrapper(
        val button: FloatingActionButton,
        val colorPriority: Priority,
) {

    /**
     * Support enum class which specify buttons priority. Can be easily extended later. For example
     * we can potentially add VERY_LOW(-2) or VERY_HIGH(2) values.
     */
    enum class Priority(val value: Int) {
        LOW(-1),
        MEDIUM(0),
        HIGH(1),
    }
}
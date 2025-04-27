package com.meera.core.utils.listeners

import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Отделяем как отдельные события одиночный и двойной клики
 * отличается от DoubleClickListener тем, что срабатывает только одно событие
 */
abstract class DoubleOrOneClickListener(
    private val doubleClickInterval: Long = DOUBLE_CLICK_INTERVAL
) : View.OnClickListener {

    private val handler = Handler(Looper.getMainLooper())

    private var clicks: Int = 0
    private var isBusy: Boolean = false

    override fun onClick(view: View) {
        clicks++
        if (!isBusy) {
            isBusy = true
            handler.postDelayed({
                when {
                    clicks >= 2 -> onDoubleClick()
                    clicks == 1 -> onClick()
                }
                clicks = 0
                isBusy = false
            }, doubleClickInterval)
        }
    }

    open fun onClick() = Unit

    open fun onDoubleClick() = Unit

    companion object {
        const val DOUBLE_CLICK_INTERVAL = 300L
    }
}

package com.numplates.nomera3.presentation.view.utils.apphints

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration.COMMON_START_DELAY
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Основновной класс для работы с подсказками. Пример использования:
 *
 * 1. private var accountButtonTooltip: TooltipLifecycleAware? = null
 *
 * 2. override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *        super.onViewCreated(view, savedInstanceState)
 *        ...
 *        accountButtonTooltip = TooltipLifecycleAware(this)
 *    }
 *
 * 3. accountButtonTooltip?.showTooltip(tvAccount, getString(Tooltip.ACCOUNT_BUTTON.textResId))
 * */
class TooltipLifecycleAware(
        private val fragment: Fragment
) : LifecycleObserver {

    companion object {
        val makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val tooltipMargin = 6.dp
    }

    private var tooltipPopupWindow: PopupWindow? = null
    private var job: Job? = null

    init {
        fragment.lifecycle.addObserver(this)
    }

    fun showTooltip(targetView: View, tooltipType: Tooltip): Job? {
        hideTooltip()

        job = fragment.lifecycleScope.launch {
            tooltipPopupWindow = TooltipPopupWindowCreator.createTooltip(fragment.requireContext(), tooltipType)
            tooltipPopupWindow?.let { nonNullTooltipPopupWindow ->
                val targetViewVisibleRect = Rect().apply { targetView.getGlobalVisibleRect(this) }

                // вычисляем размеры подсказки
                nonNullTooltipPopupWindow.contentView.measure(makeMeasureSpec, makeMeasureSpec)
                val tooltipMeasuredHeight = nonNullTooltipPopupWindow.contentView.measuredHeight
                val tooltipMeasuredWidth = nonNullTooltipPopupWindow.contentView.measuredWidth

                // вычисляем позицию подсказки по оси Х
                val tooltipHorizontalGravity = when (tooltipType) {
                    Tooltip.ACCOUNT_BUTTON -> {
                        getTooltipHorizontalGravity(targetViewVisibleRect)
                    }
                    Tooltip.RATING_PROFILE -> {
                        0 //todo доделать
                    }
                }
                val x = when (tooltipHorizontalGravity) {
                    Gravity.START -> targetViewVisibleRect.left - tooltipMargin
                    Gravity.END -> targetViewVisibleRect.right - tooltipMeasuredWidth + tooltipMargin
                    else -> targetViewVisibleRect.centerX() - tooltipMeasuredWidth / 2
                }

                // вычисляем позицию подсказки по оси У
                val y = if (isEnoughSpaceAboveTargetViewForTooltip(targetViewVisibleRect, tooltipMeasuredHeight)) {
                    targetViewVisibleRect.top - tooltipMeasuredHeight - tooltipMargin
                } else {
                    targetViewVisibleRect.bottom + tooltipMargin
                }

                // вычисляем позицию указателя подсказки todo пока указатель смотрит только вниз
                val pointerLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                pointerLayoutParams.gravity = tooltipHorizontalGravity
                val pointer = nonNullTooltipPopupWindow.contentView.findViewById<ImageView>(R.id.tooltip_bottom_pointer)
                pointer.layoutParams = pointerLayoutParams
                when (tooltipHorizontalGravity) {
                    Gravity.START -> pointer.setMargins(start = targetView.width / 2 - tooltipMargin)
                    Gravity.END -> pointer.setMargins(end = targetView.width / 2 - tooltipMargin)
                }
                pointer.requestLayout()

                // показываем подсказку
                delay(COMMON_START_DELAY)

                nonNullTooltipPopupWindow.showAtLocation(fragment.view?.rootView, Gravity.NO_GRAVITY, x, y)

                delay(tooltipType.duration)

                nonNullTooltipPopupWindow.dismiss()

            }
        }

        job?.invokeOnCompletion {
            tooltipPopupWindow?.dismiss()
        }

        return job
    }

    /**
     * Определяем хватает ли места для отображения подсказки над целевой view. Из расстояния между
     * верхней границей экрана и верхней точкой целевой view вычитаем высоту подсказки, отступ от
     * границы экрана (16dp) и отступ подсказки до целевой view (6dp)
     * */
    private fun isEnoughSpaceAboveTargetViewForTooltip(targetViewRect: Rect, tooltipMeasuredHeight: Int): Boolean {
        return targetViewRect.top - tooltipMeasuredHeight - 16.dp - tooltipMargin >= 0
    }

    /**
     * Определяем куда смещена целевая view относительно центра экрана, для расчета позиции
     * подсказки и указателя.
     * */
    private fun getTooltipHorizontalGravity(targetViewRect: Rect): Int {
        val widthPixels = fragment.resources.displayMetrics.widthPixels
        val halfScreenWidthPixels = widthPixels / 2

        val nearToLeft = targetViewRect.left.toFloat().div(widthPixels.toFloat()).times(100) <= 15
        val nearToRight = targetViewRect.right.toFloat().div(widthPixels.toFloat()).times(100) >= 85

        // вью в левой части экрана
        if (targetViewRect.left < halfScreenWidthPixels && targetViewRect.right < halfScreenWidthPixels || nearToLeft) {
            return Gravity.START
        }

        // вью в правой части экрана
        if (targetViewRect.left > halfScreenWidthPixels && targetViewRect.right > halfScreenWidthPixels || nearToRight) {
            return Gravity.END
        }

        // вью в центральной экрана
        if (targetViewRect.left < halfScreenWidthPixels && targetViewRect.right > halfScreenWidthPixels || (!nearToLeft && !nearToRight)) {
            return Gravity.CENTER
        }

        return Gravity.CENTER
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun hideTooltipAndRemoveObserver() {
        fragment.lifecycle.removeObserver(this)
        hideTooltip()
    }

    fun hideTooltip() {
        job?.cancel()
        tooltipPopupWindow?.dismiss()
    }
}

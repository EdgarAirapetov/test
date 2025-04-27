package com.numplates.nomera3.presentation.view.utils.apphints

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import timber.log.Timber

/**
 * Создать всплывающее окно с подсказкой. В PopupWindow устанавливается вью
 * с макетом layoutResId
 * */
@SuppressLint("ClickableViewAccessibility")
fun createTooltip(context: Context?, layoutResId: Int): PopupWindow? {
    if (context != null) {
        val tooltipView: View = LayoutInflater
            .from(context)
            .inflate(layoutResId, null, false)
            ?: return null

        val popupWindow = PopupWindow(context)
        popupWindow.contentView = tooltipView
        popupWindow.isOutsideTouchable = true
        popupWindow.animationStyle = R.style.popup_window_animation
        popupWindow.setBackgroundDrawable(null)

        // здесь предреждение ClickableViewAccessibility, из-за setTouchInterceptor
        // у aboutUniqueNamePopup. Это решается реализацией performClick() у view,
        // но нам это не нужно - поэтому со спокойной совестью кладем болт на это
        // todo пофиксить ClickableViewAccessibility
        popupWindow.setTouchInterceptor { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                popupWindow.dismiss()
                return@setTouchInterceptor true
            }

            return@setTouchInterceptor false
        }

        return popupWindow
    } else {
        return null
    }
}

@SuppressLint("ClickableViewAccessibility")
fun createTooltipMatchParent(context: Context?, layoutResId: Int): PopupWindow? {
    if (context != null) {
        val tooltipView: View = LayoutInflater
            .from(context)
            .inflate(layoutResId, null, false)
            ?: return null

        val popupWindow = PopupWindow(tooltipView, MATCH_PARENT, WRAP_CONTENT, true)
        popupWindow.contentView = tooltipView
        popupWindow.isOutsideTouchable = true
        popupWindow.animationStyle = R.style.popup_window_animation
        popupWindow.setBackgroundDrawable(null)

        // здесь предреждение ClickableViewAccessibility, из-за setTouchInterceptor
        // у aboutUniqueNamePopup. Это решается реализацией performClick() у view,
        // но нам это не нужно - поэтому со спокойной совестью кладем болт на это
        // todo пофиксить ClickableViewAccessibility
        popupWindow.setTouchInterceptor { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                popupWindow.dismiss()
                return@setTouchInterceptor true
            }

            return@setTouchInterceptor false
        }

        return popupWindow
    } else {
        return null
    }
}

/**
 * Показать подсказку под view
 *
 * @param fragment нужен чтобы получить rootView для отображения popup window
 * @param view целевая view под которой показываем подсказку
 * @param offsetY отсуп по Y от view
 * @param offsetX отсуп по X от view
 * */
fun PopupWindow.showBelowView(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0, gravityModifier: Int? = null) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // под которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ под
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // параметры отображения подсказки
        var gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        if (gravityModifier != null) {
            gravity = Gravity.TOP or gravityModifier
        }

        val x = viewLocation[0] + offsetX
        val y = viewLocation[1] + this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

/**
 * Показать подсказку над view
 *
 * @param fragment нужен чтобы получить rootView для отображения popup window
 * @param view целевая view над которой показываем подсказку
 * @param offsetY отсуп по Y от view
 * @param offsetX отсуп по X от view
 * */
fun PopupWindow.showAboveView(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // параметры отображения подсказки
        val gravity = Gravity.TOP or Gravity.START
        val x = viewLocation[0] + offsetX
        val y = viewLocation[1] - this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

/**
 * Показать подсказку над view
 *
 * @param fragment нужен чтобы получить rootView для отображения popup window
 * @param view целевая view над которой показываем подсказку
 * @param offsetY отсуп по Y от view
 * @param offsetX отсуп по X от view
 * */
fun PopupWindow.showAboveViewAtStart(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // параметры отображения подсказки
        val gravity = Gravity.TOP or Gravity.START
        val x = viewLocation[0] - this.contentView.measuredWidth + offsetX
        val y = viewLocation[1] - this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

/**
 * Показать подсказку над view
 *
 * @param fragment нужен чтобы получить rootView для отображения popup window
 * @param view целевая view над которой показываем подсказку
 * @param offsetY отсуп по Y от view
 * @param offsetX отсуп по X от view
 * */
public fun PopupWindow.showAboveViewAtCenter(fragment: Fragment, view: View, xOffset: Int) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // параметры отображения подсказки
        val gravity = Gravity.TOP or Gravity.START
        val x = viewLocation[0] + (view.width - this.contentView.measuredWidth) / 2 + xOffset
        val y = viewLocation[1] - this.contentView.measuredHeight

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

/**
 * Показать подсказку под view
 *
 * @param fragment нужен чтобы получить rootView для отображения popup window
 * @param view целевая view под которой показываем подсказку
 * @param offsetY отсуп по Y от view
 * @param offsetX отсуп по X от view
 * */
fun PopupWindow.showBelowViewAtStart(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0, gravityModifier: Int? = null) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // под которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ под
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // параметры отображения подсказки
        var gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        if (gravityModifier != null) {
            gravity = Gravity.TOP or gravityModifier
        }

        val x = viewLocation[0] - this.contentView.measuredWidth + offsetX
        val y = viewLocation[1] + this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

public fun PopupWindow.showGroupChatTooltip(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // под которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ под
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // параметры отображения подсказки
        var gravity = Gravity.TOP or Gravity.END

        val x = offsetX
        val y = viewLocation[1] + this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.show(fragment: Fragment, view: View, gravity: Int, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val x = offsetX + 16.dp
        val y = offsetY + viewLocation[1] - view.height - 8.dp


        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.show(fragment: Fragment, gravity: Int, absX: Int = 0, absY: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        this.showAtLocation(fragmentRootView, gravity, absX, absY)
    }
}

fun PopupWindow.showForAccount(fragment: Fragment, view: View, gravity: Int, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val x = offsetX + 36.dp
        val y = offsetY + viewLocation[1] - view.height + 20.dp


        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showForCallSwitch(fragment: Fragment, view: View, gravity: Int, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val x = offsetX + 36.dp
        val y = offsetY + viewLocation[1]


        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showForSpeakerButton(fragment: Fragment, view: View, gravity: Int, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        this.contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val x = offsetX
        val y = offsetY + viewLocation[1] - 25.dp - 16.dp // https://zpl.io/V04831Q


        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showForRating(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            MATCH_PARENT,
            WRAP_CONTENT
        )

        // параметры отображения подсказки
        val gravity = Gravity.TOP or Gravity.CENTER
        val x = viewLocation[0] + offsetX
        val y = viewLocation[1] - this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showForUserInfoSubscribers(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)

        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            MATCH_PARENT,
            WRAP_CONTENT
        )

        // параметры отображения подсказки
        val gravity = Gravity.TOP or Gravity.CENTER
        val x = viewLocation[0] + offsetX
        val y = viewLocation[1] - this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showCreateAvatarAtUserInfo(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)
        Timber.i("Location ivPhoto: x${viewLocation[0]};y:${viewLocation[1]}")
        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            WRAP_CONTENT,
            WRAP_CONTENT
        )

        // параметры отображения подсказки
        val gravity = Gravity.START or Gravity.TOP
        val x = viewLocation[0] + offsetX
        val y = viewLocation[1] + this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showCreateAvatarAtUserPersonalInfo(fragment: Fragment, view: View, offsetY: Int = 0, offsetX: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)
        Timber.i("Location user personal:x:${viewLocation[0]}")
        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            WRAP_CONTENT,
            WRAP_CONTENT
        )

        // параметры отображения подсказки
        val gravity = Gravity.START or Gravity.TOP
        val x = viewLocation[0] + offsetX
        val y = viewLocation[1] + this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

fun PopupWindow.showCreateAvatarAtRegisterUser(fragment: Fragment, view: View, offsetY: Int = 0) {
    val fragmentRootView = fragment.view?.rootView
    if (fragmentRootView != null) {
        // поиск позиции view на экране
        // над которой будет показана подсказка
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)
        Timber.i("Location ivPhoto: x${viewLocation[0]};y:${viewLocation[1]}")
        // узнаем размеры подсказки заранее
        // чтобы правильно указать отступ над
        // view, иначе подсказка будет отображена
        // поверх view
        this.contentView.measure(
            WRAP_CONTENT,
            WRAP_CONTENT
        )

        // параметры отображения подсказки
        val gravity = Gravity.CENTER or Gravity.TOP
        val x = 0
        val y = viewLocation[1] + this.contentView.measuredHeight + offsetY

        this.showAtLocation(fragmentRootView, gravity, x, y)
    }
}

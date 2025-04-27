package com.numplates.nomera3.presentation.view.ui

import android.content.Context
import android.gesture.GestureOverlayView
import android.util.AttributeSet
import android.view.MotionEvent
import com.numplates.nomera3.modules.reaction.ui.ReactionBubbleViewController

/**
 * [ExtendedGestureOverlayView] расширяет [GestureOverlayView] возможностью блокировки жестов.
 * При этом возвращает событие, что жест произошёл.
 * Кроме того, [ExtendedGestureOverlayView] блокирует вызов метода [onTouchEvent], не блокируя
 * вызов метода [onTouchEvent] у своих потомков.
 *
 * Необходим для блокировки всех жестов, кроме выбора реакций.
 * @see ReactionBubbleViewController
 */
open class ExtendedGestureOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GestureOverlayView(context, attrs, defStyleAttr), GestureOverlayView.OnGestureListener {

    var gesturesInterceptorEnabled: Boolean = true
    private var gestureEventPasser: GestureEventPasser? = null

    override fun onGestureStarted(overlay: GestureOverlayView?, event: MotionEvent?) {
        if (!gesturesInterceptorEnabled) return
        gestureEventPasser?.onGesturePassed(view = overlay, event = event)
    }

    override fun onGesture(overlay: GestureOverlayView?, event: MotionEvent?) {
        if (!gesturesInterceptorEnabled) return
        gestureEventPasser?.onGesturePassed(view = overlay, event = event)
    }

    override fun onGestureEnded(overlay: GestureOverlayView?, event: MotionEvent?) {
        if (!gesturesInterceptorEnabled) return
        gestureEventPasser?.onGesturePassed(view = overlay, event = event)
    }

    override fun onGestureCancelled(overlay: GestureOverlayView?, event: MotionEvent?) {
        if (!gesturesInterceptorEnabled) return
        gestureEventPasser?.onGesturePassed(view = overlay, event = event)
    }

    fun setGestureEventPasser(gestureEventPasser: GestureEventPasser?) {
        this.gestureEventPasser = gestureEventPasser
    }

    fun addExtendedGestureListener() {
        super.addOnGestureListener(this)
    }
}

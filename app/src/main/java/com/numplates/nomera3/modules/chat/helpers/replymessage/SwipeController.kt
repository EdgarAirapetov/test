package com.numplates.nomera3.modules.chat.helpers.replymessage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.meera.core.extensions.dp
import com.meera.core.extensions.drawable
import com.numplates.nomera3.R
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.min


class ReplySwipeController(
    private val context: Context,
    private val type: SwipingItemType,
    private val swipeControllerActions: SwipeControllerActions
) : ItemTouchHelper.Callback() {

    var isSwipeEnabled = true

    private lateinit var imageDrawable: Drawable

    private var currentItemViewHolder: ISwipeableHolder? = null
    private lateinit var mView: View
    private var dX = 0f
    private lateinit var hldr: View

    private var replyButtonProgress: Float = 0.toFloat()
    private var lastReplyButtonAnimationTime: Long = 0
    private var swipeBack = false
    private var isVibrate = false
    private var startTracking = false
    private var screenWidth: Int = 0

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        if (!isSwipeEnabled) return 0
        if (viewHolder !is ISwipeableHolder || !viewHolder.canSwipe()) return 0
        hldr = viewHolder.itemView
        screenWidth = hldr.width
        mView = viewHolder.getSwipeContainer()
        //Саша сказал, что безопасно
        imageDrawable = context.drawable(R.drawable.ic_arrow_reply)!!
        return makeMovementFlags(ACTION_STATE_IDLE, LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) = Unit

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder !is ISwipeableHolder) return
        if (actionState == ACTION_STATE_SWIPE) setTouchListener(recyclerView, viewHolder)
        if (mView.translationX.absoluteValue < 130.dp || dX > this.dX) {
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                viewHolder.getSwipeContainer(),
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
            this.dX = dX
            startTracking = true
        }
        currentItemViewHolder = viewHolder
        drawReplyButton(c)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        if (viewHolder is ISwipeableHolder) {
            recyclerView.setOnTouchListener { _, event ->
                swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
                if (swipeBack) {
                    if (abs(mView.translationX) >= 100.dp) {
                        swipeControllerActions.onReply(viewHolder.absoluteAdapterPosition)
                    }
                }
                false
            }
        }
    }

    private fun drawReplyButton(canvas: Canvas) {
        if (currentItemViewHolder == null) {
            return
        }
        val translationX = mView.translationX.absoluteValue
        val newTime = System.currentTimeMillis()
        val dt = min(17, newTime - lastReplyButtonAnimationTime)
        lastReplyButtonAnimationTime = newTime
        val showing = translationX >= 30.dp
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f
                } else {
                    mView.invalidate()
                }
            }
        } else if (translationX <= 0.0f) {
            replyButtonProgress = 0f
            startTracking = false
            isVibrate = false
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f
                } else {
                    mView.invalidate()
                }
            }
        }
        val alpha: Int
        val scale: Float
        if (showing) {
            scale = if (replyButtonProgress <= 0.8f) {
                1.2f * (replyButtonProgress / 0.8f)
            } else {
                1.2f - 0.2f * ((replyButtonProgress - 0.8f) / 0.2f)
            }
            alpha = min(255f, 255 * (replyButtonProgress / 0.8f)).toInt()
        } else {
            scale = replyButtonProgress
            alpha = min(255f, 255 * replyButtonProgress).toInt()
        }

        imageDrawable.alpha = alpha
        if (startTracking) {
            if (!isVibrate && mView.translationX.absoluteValue >= 100.dp) {
                mView.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                isVibrate = true
            }
        }

        // Сдвиг по оси х. Ставим половину от того, на сколько отодвинули контейнер
        // если отступ по оси х больше 130 фиксируем его.
        val x: Int = if (mView.translationX.absoluteValue > 130.dp) {
            screenWidth - 130.dp / 2
        } else {
            screenWidth - (mView.translationX.absoluteValue / 2).toInt()
        }

        // Сдвиг по оси y. Так как контейнер, который мы двигаем, не всегда расположен в центре.
        // Для комментариев к посту - Отступ по размеру текстов под контейнером комметария.
        // Для сообщений в чате - (Сверху может находиться разделитель)
        // Мы должны высчитать размер отступа сверху, который равен
        // extraSizeY
        val extraSizeY = when (type) {
            SwipingItemType.POST_COMMENT -> EXTRA_SIZE_COMMENT.dp
            else -> (hldr.measuredHeight - mView.height) / 2
        }
        val y = (hldr.top + hldr.measuredHeight / 2 + extraSizeY)

        imageDrawable.setBounds(
            (x - 12.dp * scale).toInt(),
            (y - 11.dp * scale).toInt(),
            (x + 12.dp * scale).toInt(),
            (y + 10.dp * scale).toInt()
        )
        imageDrawable.draw(canvas)
        imageDrawable.alpha = 255
    }


    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        val v = viewHolder as? ISwipeableHolder
        v?.let {
            getDefaultUIUtil().clearView(v.getSwipeContainer())
        }
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        if (viewHolder != null && viewHolder is ISwipeableHolder) {
            getDefaultUIUtil().onSelected(viewHolder.getSwipeContainer())
        }
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder != null && viewHolder is ISwipeableHolder) {
            getDefaultUIUtil().onDrawOver(
                c, recyclerView, viewHolder.getSwipeContainer(), dX, dY, actionState, isCurrentlyActive
            )
        }
    }

    companion object {
        const val EXTRA_SIZE_COMMENT = -10
    }
}

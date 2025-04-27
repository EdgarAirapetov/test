package com.numplates.nomera3.presentation.view.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.viewholder.SeparatorViewHolder
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.UserGiftsHolidayAdapter
import kotlin.math.abs
import kotlin.properties.Delegates

private const val TEXT_FONT_SIZE = 12
private const val TEXT_HORIZONTAL_OFFSET = 72
private const val TEXT_VERTICAL_OFFSET = 16
private const val ICON_HORIZONTAL_OFFSET = 36
private const val ICON_VERTICAL_OFFSET = 8

// TODO: Переделать! т.к. жёстко привязан к типам view holders в методе isValidHolder()
@Deprecated("Should transit to CORE module")
class SwipeToDeleteUtils(
    private val context: Context,
    private val swipeType: SwipeType = SwipeType.PARTIALLY
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val paint = Paint()

    private var isSwiped = false

     private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_outlined_delete_m)
     private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
     private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0

    // Background for full swipe
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#ec4d36")

    var onClickRightButton: (Int) -> Unit = { }
    var onFullSwiped: (Int) -> Unit = { }

    var onSwipeProgress: (Boolean) -> Unit = { }

    var swipeActiveObservable by Delegates.observable(false) { prop, old, new ->
        if (old != new) {
            onSwipeProgress.invoke(new)
        }
    }

    var isSwipeEnable = true

    enum class SwipeType {
        PARTIALLY, FULL, MEERAFULL
    }


    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (!isValidHolder(viewHolder)) return 0
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    private fun isValidHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return when (viewHolder) {
            is SeparatorViewHolder -> false
            is UserGiftsHolidayAdapter.ViewHolder -> false
            is NotSwipeDeletable -> false
            else -> true
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        isSwiped = true // For partially
        if (swipeType == SwipeType.FULL || swipeType == SwipeType.MEERAFULL) {
            onFullSwiped.invoke(viewHolder.bindingAdapterPosition)
        }
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return isSwipeEnable
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (abs(dX) < viewHolder.itemView.width) {
            swipeActiveObservable = isCurrentlyActive
        }

        // Draw red
        when (swipeType) {
            SwipeType.PARTIALLY -> swipeTypePartially(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            SwipeType.FULL -> swipeTypeFull(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            SwipeType.MEERAFULL -> meeraSwipeTypeFull(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }


    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }


    private fun swipeTypePartially(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        try {
            // val icon: Bitmap
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val itemView = viewHolder.itemView
                // val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                // val width = height / 5
                viewHolder.itemView.translationX = dX / 4
                paint.color = Color.parseColor("#ec4d36")
                val background = RectF(
                    itemView.right.toFloat() + dX / 4,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                c.drawRect(background, paint)

                paint.color = Color.WHITE
                paint.textSize = 48f
                val yPos = (itemView.top + itemView.height / 2 - (paint.descent() + paint.ascent()) / 2)
                c.drawText(context.getString(R.string.general_delete), itemView.right.toFloat() - 220, yPos, paint)

                if (dX == 0f) {
                    /** STUB */
                } else {
                    setOnTouchListener(recyclerView, viewHolder, dX, itemView)
                }
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        itemView: View
    ) {

        recyclerView.setOnTouchListener { v, event ->
            if (isSwiped) {
                val buttonWidth = abs(dX / 4)
                // Click area
                if (event.rawX > itemView.width - buttonWidth) {
                    isSwiped = false
                    onClickRightButton.invoke(viewHolder.bindingAdapterPosition)
                }
            }
            false
        }
    }


    private fun swipeTypeFull(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        paint.color = Color.WHITE
        paint.textSize = dpToPx(16).toFloat()
        val yPos = (itemView.top + itemView.height / 2 - (paint.descent() + paint.ascent()) / 2)
        c.drawText(context.getString(R.string.general_delete), itemView.right.toFloat() - dpToPx(89), yPos, paint)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun meeraSwipeTypeFull(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val isCanceled = dX == 0f && !isCurrentlyActive
        val itemHeight = itemView.bottom - itemView.top

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        background.color = ContextCompat.getColor(context, R.color.uiKitColorBackgroundSecondary)
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        val deleteIconColor = ContextCompat.getColor(context, R.color.uiKitColorAccentWrong)
        paint.color = deleteIconColor
        paint.textSize = dpToPx(TEXT_FONT_SIZE).toFloat()
        val yPos = (itemView.top + itemView.height / 2 - (paint.descent() + paint.ascent()) / 2)
        c.drawText(
            context.getString(R.string.general_delete),
            (itemView.right negativeOffset TEXT_HORIZONTAL_OFFSET).toFloat(),
            yPos positiveOffset TEXT_VERTICAL_OFFSET,
            paint
        )

        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right  - intrinsicWidth
        val deleteIconRight = itemView.right
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        deleteIcon?.let { DrawableCompat.setTint(deleteIcon, deleteIconColor) }
        deleteIcon?.setBounds(
            deleteIconLeft negativeOffset ICON_HORIZONTAL_OFFSET,
            deleteIconTop negativeOffset ICON_VERTICAL_OFFSET,
            deleteIconRight negativeOffset ICON_HORIZONTAL_OFFSET,
            deleteIconBottom negativeOffset ICON_VERTICAL_OFFSET
        )
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private infix fun Float.positiveOffset(offset: Int): Float = this + dpToPx(offset)

    private infix fun Int.negativeOffset(offset: Int): Int = this - dpToPx(offset)



    ///////////////////////////////////////////////////////////////////////////
    // Full swipe
    ///////////////////////////////////////////////////////////////////////////

    // Full swipe
    /*override fun onChildDraw(
            c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }*/
}

/** Interface that marks viewHolder as ineligible for swipe-delete (a crutch). */
interface NotSwipeDeletable

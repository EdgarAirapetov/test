package com.numplates.nomera3.domain.interactornew

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.meera.core.extensions.dp
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.noomeera.nmravatarssdk.ui.view.AvatarView
import com.numplates.nomera3.R


const val ANIMATED_AVATAR_RESOLUTION = 600
const val WATERMARK_WIDTH = 53
const val WATERMARK_HEIGHT = 8
const val WATERMARK_SPACING = 8
const val WATERMARK_MARGIN_TOP = 12
const val WATERMARK_MARGIN_START = 16
const val WATERMARK_PADDING_VERTICAL = 4
const val WATERMARK_PADDING_HORIZONTAL = 8
const val LINE_PADDING_HORIZONTAL = 6
const val LINE_WIDTH = 1
private const val UNIQUE_NAME_PADDING = 5
private const val UNIQUE_NAME_TEXT_SIZE = 9f

class ProcessAnimatedAvatarImpl(
    private val context: Context,
    private val fileManager: FileManager,
    private val appSettings: AppSettings
) :
    ProcessAnimatedAvatar {

    override fun createBitmap(avatarState: String): Bitmap {
        return AvatarView.createBitmap(context, avatarState, ANIMATED_AVATAR_RESOLUTION)
    }

    override fun saveInFile(bitmap: Bitmap): String {
        return fileManager.saveBitmapInFile(bitmap)
    }

    override fun saveInFileWithWaterMark(bitmap: Bitmap): String {
        val canvas = Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_noomeera_watermark)
        drawable?.setBounds(0, 0, WATERMARK_WIDTH.dp, WATERMARK_HEIGHT.dp)
        canvas.save()
        canvas.translate(WATERMARK_SPACING.dp.toFloat(), WATERMARK_SPACING.dp.toFloat())
        drawable?.draw(canvas)

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = UNIQUE_NAME_TEXT_SIZE // Set the desired text size
            color = Color.WHITE // Set the text color
        }
        val textBounds = Rect()
        val text = "@${appSettings.readUniquieName()}"
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val xPosition = (drawable?.bounds?.width()?.div(2))?.minus(textBounds.width().div(2)?.toFloat() ?: 0f) ?: 0f
        val yPosition = (drawable?.bounds?.bottom?.minus(UNIQUE_NAME_PADDING.dp) ?: 0).toFloat()

        canvas.drawText(text, xPosition, yPosition, textPaint)
        canvas.restore()

        return fileManager.saveBitmapInFile(bitmap)
    }

    override fun saveInFileWithWaterMarkWithUniqueName(bitmap: Bitmap, uniqueName: String?): String {
        val username = uniqueName ?: appSettings.readUniquieName()

        val canvas = Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_logo_64_max)
        drawable?.setTint(Color.WHITE)
        drawable?.setBounds(0, 0, WATERMARK_WIDTH.dp, WATERMARK_HEIGHT.dp)
        canvas.save()
        canvas.translate(WATERMARK_MARGIN_START.dp.toFloat(), WATERMARK_MARGIN_TOP.dp.toFloat())

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = UNIQUE_NAME_TEXT_SIZE.dp
            color = Color.WHITE
        }
        val textBounds = Rect()
        val text = "@${username}"
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val logoHeight = drawable?.bounds?.height()?.toFloat() ?: 0f
        val logoWidth = (drawable?.bounds?.width()?.toFloat() ?: 0f)
        val nameHeight = textBounds.height().toFloat()

        val xPosition = logoWidth + LINE_PADDING_HORIZONTAL.dp + LINE_PADDING_HORIZONTAL.dp
        val yPosition = logoHeight.div(2) + nameHeight.div(2) - textPaint.fontMetrics.descent


        val rectLeft: Float = -WATERMARK_PADDING_HORIZONTAL.dp.toFloat()
        val rectTop: Float = -WATERMARK_PADDING_VERTICAL.dp.toFloat()
        val rectRight: Float =
            logoWidth + textBounds.width() + WATERMARK_PADDING_HORIZONTAL.dp + LINE_PADDING_HORIZONTAL.dp + LINE_PADDING_HORIZONTAL.dp
        val rectBottom: Float = (drawable?.bounds?.bottom?.toFloat() ?: 0f) + WATERMARK_PADDING_VERTICAL.dp


        val linePaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = LINE_WIDTH.dp.toFloat()
        }
        val lineX = logoWidth + LINE_PADDING_HORIZONTAL.dp

        val bgColor = ResourcesCompat.getColor(context.resources,R.color.uiKitColorBackgroundFadeBlack70,null)

        val paintBackground = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            rectLeft,
            rectTop,
            rectRight,
            rectBottom,
            WATERMARK_MARGIN_TOP.toFloat(),
            WATERMARK_MARGIN_TOP.toFloat(),
            paintBackground
        )

        drawable?.draw(canvas)
        canvas.drawLine(lineX, 0f, lineX, logoHeight, linePaint)
        canvas.drawText(text, xPosition, yPosition, textPaint)
        canvas.restore()

        return fileManager.saveBitmapInFile(bitmap)
    }
}

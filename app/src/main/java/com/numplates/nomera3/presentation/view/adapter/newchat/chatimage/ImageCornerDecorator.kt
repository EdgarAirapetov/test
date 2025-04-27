package com.numplates.nomera3.presentation.view.adapter.newchat.chatimage

import com.numplates.nomera3.modules.chat.views.RoundedFrameLayout

private const val EXCEPTION_POSITION_FOR_OTHER_MESSAGES = 0
class ImageCornerDecorator(private val cornerSizeInPx: Float) {

    private val exceptionsCornersMap = hashMapOf(
        Corner.TOP_LEFT to cornerSizeInPx,
        Corner.TOP_RIGHT to cornerSizeInPx,
        Corner.BOTTOM_RIGHT to cornerSizeInPx,
        Corner.BOTTOM_LEFT to cornerSizeInPx,
    )

    /**
     * For other images the first image always has top left corner equal 90 degree.
     * For 'my' images the image at the top right position has a 90 degree corner.
     * All the other corners (including other images) should be rounded.
     */
    @Suppress("LocalVariableName")
    fun bindImageCorners(
        rounded: RoundedFrameLayout,
        position: Int,
        imageCount: Int,
        isMe: Boolean
    ) {
        val exception = if (isMe) Corner.TOP_RIGHT else Corner.TOP_LEFT
        exceptionsCornersMap[exception] = 0f
        val _position = when (isMe) {
            true -> getExceptionPositionForMyMessages(imageCount)
            else -> EXCEPTION_POSITION_FOR_OTHER_MESSAGES
        }
        if (_position == position) {
            exceptionsCornersMap.forEach { (key, value) ->
                when (key) {
                    Corner.TOP_LEFT -> rounded.setTopLeftCornerRadius(value)
                    Corner.TOP_RIGHT -> rounded.setTopRightCornerRadius(value)
                    Corner.BOTTOM_RIGHT -> rounded.setBottomRightCornerRadius(value)
                    Corner.BOTTOM_LEFT -> rounded.setBottomLeftCornerRadius(value)
                }
            }
            rounded.apply()
        } else {
            rounded.setCornerRadius(cornerSizeInPx).apply()
        }
    }

    private fun getExceptionPositionForMyMessages(imageCount: Int): Int {
        return when (imageCount) {
            1 -> 0
            2 -> 1
            3 -> 1
            4 -> 1
            5 -> 2
            else -> error("We could have only 5 images. Please consider updating logic or fix an issue.")
        }
    }

    /**
     * Support class to define which corner requires a decoration
     */
    private enum class Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT;
    }
}

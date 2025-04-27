package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.meera.core.utils.layouts.LinearEnableTouchLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar

private const val DISABLED_ALPHA = 0.5f

interface MeeraReactionButtonColor {

    fun setButtonThemeByContent(contentActionBarType: MeeraContentActionBar.ContentActionBarType)

    @ColorInt
    fun getDefaultBackgroundTint(
        context: Context,
        contentActionBarType: MeeraContentActionBar.ContentActionBarType
    ): Int {
        val colorRes = when (contentActionBarType) {
            MeeraContentActionBar.ContentActionBarType.DEFAULT -> R.color.ui_gray_background
            MeeraContentActionBar.ContentActionBarType.DARK -> R.color.ui_dark_gray_background
            MeeraContentActionBar.ContentActionBarType.BLUR -> R.color.ui_blur_gray_background
        }
        return ContextCompat.getColor(context, colorRes)
    }

    @ColorInt
    fun getPressedBackgroundTint(
        context: Context,
        contentActionBarType: MeeraContentActionBar.ContentActionBarType
    ): Int {
        val colorRes = when (contentActionBarType) {
            MeeraContentActionBar.ContentActionBarType.DEFAULT -> R.color.gray_background_button
            MeeraContentActionBar.ContentActionBarType.DARK -> R.color.ui_black
            MeeraContentActionBar.ContentActionBarType.BLUR -> R.color.blur_gray_background_button
        }
        return ContextCompat.getColor(context, colorRes)
    }

    fun setButtonEnabled(
        enabled: Boolean,
        buttonLinearLayout: LinearEnableTouchLayout
    ) {
        val alpha = if (enabled) 1f else DISABLED_ALPHA
        buttonLinearLayout.enableTouchEvents = enabled
        buttonLinearLayout.alpha = alpha
    }

}

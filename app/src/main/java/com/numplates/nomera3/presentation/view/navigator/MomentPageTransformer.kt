package com.numplates.nomera3.presentation.view.navigator

import android.view.View
import androidx.viewpager.widget.ViewPager

class MomentPageTransformer : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val width = page.width.toFloat()
        page.translationX = -width * position
        page.alpha = 1f
    }
}

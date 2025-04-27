package com.numplates.nomera3.presentation.view.navigator

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class NavigatorPageTransformerVertical : ViewPager.PageTransformer {
    override fun transformPage(view: View, position: Float) {
            // PageTransformer используется для анимации между переходами экранов
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    // Все экраны в стеке справа от текущего
                    position <= -1 -> {
                        // Используем флаг INVISIBLE у всех экранов справа от текущего
                        // для оптимизации рендеринга
                        visibility = View.INVISIBLE
                        alpha = 1f
                    }
                    // Экран, который появляется справа от текущего при открытии нового фрагмента
                    position in 0.0..1.0 -> {
                        alpha = 1f
                        visibility = View.VISIBLE
                        translationX = -pageWidth * position
                        translationY =
                            pageHeight * position
                    }
                    // Анимация ухода текущего фрагмента влево при открытии нового
                    // (со смещением и изменением прозрачности, помните черный бэкграунд у NavigatorViewPager?)
                    position <= 0 -> {
                        alpha = 1.0F - abs(position) / 2
                        translationX =
                            -pageWidth * position
                        visibility = View.VISIBLE
                    }
                    //Все врагменты слева от текущего, убираем их из отрисовки
                    else -> {
                        visibility = View.INVISIBLE
                        alpha = 1f
                    }
                }
            }

    }

}

package com.numplates.nomera3.presentation.view.navigator

import android.view.View
import androidx.viewpager.widget.ViewPager

class NavigatorPageTransformerHorizontal : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {

        // PageTransformer используется для анимации между переходами экранов
        view.apply {
            val pageWidth = width
            when {
                // Все экраны в стеке справа от текущего
                position <= -1 -> {
                    // Используем флаг INVISIBLE у всех экранов справа от текущего
                    // для оптимизации рендеринга
                    visibility = View.INVISIBLE
                    alpha = 1f
                }

                // Экран, который появляется справа от текущего при открытии нового фрагмента
                position > 0 && position <= 1 -> {
                    alpha = 1f
                    visibility = View.VISIBLE
                    translationX = 0f
                }

                // Анимация ухода текущего фрагмента влево при открытии нового
                // (со смещением и изменением прозрачности, помните черный бэкграунд у NavigatorViewPager?)
                position <= 0 -> {
                    alpha = 1.0F - Math.abs(position) / 2
                    translationX = -pageWidth * position / 1.3F
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
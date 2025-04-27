package com.numplates.nomera3.modules.remotestyle.presentation

import android.content.Context
import androidx.core.content.ContextCompat
import com.numplates.nomera3.R

private const val BLACK = "black"
private const val RED = "red"
private const val WHITE = "white"

class RemoteColorMapper {
    fun mapToColor(context: Context, remoteColor: String): Int {
        return when (remoteColor) {
            BLACK -> {
                ContextCompat.getColor(context, R.color.black_85)
            }
            RED -> {
                ContextCompat.getColor(context, R.color.ui_red)
            }
            WHITE -> {
                ContextCompat.getColor(context, R.color.white_1000)
            }
            else -> {
                ContextCompat.getColor(context, R.color.black_85)
            }
        }
    }
}
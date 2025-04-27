package com.numplates.nomera3.modules.remotestyle.presentation

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle

private val remoteColorMapper = RemoteColorMapper()

fun AppCompatTextView.applyStyle(context: Context, isVip: Boolean, style: PostOnlyTextRemoteStyle.Style) {
    val styleColor = if (isVip) {
        style.fontColorVip
    } else {
        style.fontColor
    }

    this.textSize = style.fontSize.toFloat()
    this.setTextColor(remoteColorMapper.mapToColor(context, styleColor))
}

fun AppCompatEditText.applyStyle(context: Context, isVip: Boolean, style: PostOnlyTextRemoteStyle.Style) {
    val styleColor = if (isVip) {
        style.fontColorVip
    } else {
        style.fontColor
    }

    this.textSize = style.fontSize.toFloat()
    this.setTextColor(remoteColorMapper.mapToColor(context, styleColor))
}
package com.noomeera.nmravatarssdk.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.caverock.androidsvg.SVG

internal val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

fun SVG.drawable(context: Context, height: Int, width: Int): RoundedBitmapDrawable {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    this.renderToCanvas(canvas)
    return RoundedBitmapDrawableFactory.create(context.resources, bitmap)
}
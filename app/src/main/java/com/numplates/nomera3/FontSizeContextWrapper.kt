package com.numplates.nomera3

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class FontSizeContextWrapper(context: Context) : ContextWrapper(context) {

    companion object {
        @Suppress("NAME_SHADOWING")
        fun wrap(context: Context, fontScale: Float): ContextWrapper {
            var context: Context = context
            val config: Configuration = context.resources.configuration
            if (config.fontScale != fontScale) {
                config.fontScale = fontScale
            }
            val metrics = context.resources.displayMetrics
            val wm = context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = config.fontScale * metrics.density
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context = context.createConfigurationContext(config)
            } else {
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
            }
            return ContextWrapper(context)
        }
    }
}

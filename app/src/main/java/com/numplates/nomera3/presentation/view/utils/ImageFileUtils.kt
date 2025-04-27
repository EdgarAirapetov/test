package com.numplates.nomera3.presentation.view.utils

import timber.log.Timber
import java.io.File

object ImageFileUtils {

    const val GIF = ".gif"

    fun isAnimatedImage(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            val sample = File(path)
            val pos = sample.name.lastIndexOf('.')
            val ext = sample.name.substring(pos)
            ext.contains(GIF, ignoreCase = true)
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }
}
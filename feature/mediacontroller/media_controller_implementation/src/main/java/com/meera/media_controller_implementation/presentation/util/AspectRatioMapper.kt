package com.meera.media_controller_implementation.presentation.util

import com.meera.media_controller_common.CropInfo
import com.noomeera.nmrmediatools.utils.Ratio

internal class AspectRatioMapper(private val defaultRatioList: List<Ratio>) {
    fun map(cropInfo: CropInfo?) = mutableListOf<Ratio>()
        .apply {
            if (cropInfo?.allowOriginalCrop == true) {
                add(Ratio(0, 0))
            }

            cropInfo?.aspectList?.forEach { aspect ->
                val width = aspect.width
                val height = aspect.height

                if (width != null && height != null) {
                    add(Ratio(width, height))
                }
            }
        }
        .ifEmpty { defaultRatioList }
}

package com.meera.media_controller_implementation.presentation.util

import com.noomeera.nmrmediatools.utils.CropMode

internal class ForceCropMapper {
    fun map(
        isForceCrop: Boolean?,
        isCropAvatar: Boolean
    ): CropMode = when {
        isForceCrop == true && isCropAvatar -> CropMode.AVATAR
        isForceCrop == true -> CropMode.FORCE
        else -> CropMode.ALLOW
    }
}

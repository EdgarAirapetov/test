package com.numplates.nomera3.modules.viewvideo.presentation.data

import java.io.Serializable

data class ViewVideoInitialData(
    val id: String? = null,
    val position: Long,
    val duration: Long
): Serializable

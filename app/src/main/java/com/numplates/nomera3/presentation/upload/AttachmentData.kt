package com.numplates.nomera3.presentation.upload

import com.meera.core.extensions.empty

data class AttachmentData(
    var type: String = String.empty(),
    var mediaList: List<String> = listOf(),
    var duration: Int? = null,
    var isSilent: Boolean? = null,
    var lowQuality: String? = null,
    var preview: String? = null,
    var ratio: Double? = null
)


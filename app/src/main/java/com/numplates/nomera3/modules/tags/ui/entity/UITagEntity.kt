package com.numplates.nomera3.modules.tags.ui.entity

data class UITagEntity(
        var id: Long?,
        var image: String?,
        var uniqueName: String?,
        var userName: String?,
        var isMale: Int? = 1,
        var isVerified: Int? = 0
)

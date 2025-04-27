package com.numplates.nomera3.modules.music.ui.entity

import com.numplates.nomera3.modules.music.ui.adapter.MusicAdapterType
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity

const val ID_POST_DURING_CREATING = -500L

data class MusicCellUIEntity(
    val mediaEntity: MediaEntity = MediaEntity(),
    val type: MusicAdapterType = MusicAdapterType.ITEM_TYPE_MUSIC,
    var needToShowSeparator: Boolean = true,
    var needToShowAddBtn: Boolean = true,
    val idPost: Long = ID_POST_DURING_CREATING
)

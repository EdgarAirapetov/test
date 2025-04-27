package com.numplates.nomera3.modules.music.ui.entity.state

import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity

sealed class MusicViewModelState {
    data class AddMusic(val entity: MusicCellUIEntity): MusicViewModelState()
    data class ReplaceMusic(val entity: MusicCellUIEntity): MusicViewModelState()
}

package com.numplates.nomera3.modules.music.ui.entity.event

import android.view.View
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity

sealed class UserActionEvent {
    object CloseClicked : UserActionEvent()
    object ClearClicked : UserActionEvent()
    object UnSubscribe : UserActionEvent()

    class MoveMusicCell(
        val range: IntRange,
        val items: List<MusicCellUIEntity>
    ): UserActionEvent()
    class PlayClicked(val entity: MusicCellUIEntity, val musicView: View?) : UserActionEvent()
    class StopClicked(val entity: MusicCellUIEntity) : UserActionEvent()
    class AddClicked(val entity: MusicCellUIEntity) : UserActionEvent()
}

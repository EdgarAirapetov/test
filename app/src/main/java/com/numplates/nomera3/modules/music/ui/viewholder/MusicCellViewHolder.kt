package com.numplates.nomera3.modules.music.ui.viewholder

import android.view.ViewGroup
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.music.ui.adapter.MusicActionCallback
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.presentation.view.ui.customView.MediaPlayerListener
import com.numplates.nomera3.presentation.view.ui.customView.MusicPlayerCell

class MusicCellViewHolder(
    parent: ViewGroup,
    private val musicActionCallback: MusicActionCallback,
    private val isDarkMode: Boolean = false
) : BaseViewHolder(parent, R.layout.item_music_cell) {

    private val musicCell = itemView.findViewById<MusicPlayerCell>(R.id.mpc_media)

    fun bind(entity: MusicCellUIEntity) {
        initMediaListener(entity)
        initMusicCell(entity)
    }

    private fun initMusicCell(entity: MusicCellUIEntity) {
        musicCell.setMediaInformation(
            albumUrl = entity.mediaEntity.albumUrl,
            artistName = entity.mediaEntity.artist,
            musicTitle = entity.mediaEntity.track,
            isDarkMode = isDarkMode
        )
    }

    private fun initMediaListener(entity: MusicCellUIEntity) {
        musicCell.initMediaController(
                object : MediaPlayerListener {
                    override fun onPlay(withListener: Boolean) {
                        if (withListener) musicActionCallback.onPlayClicked(
                            entity = entity,
                            audioEventListener = audioEventListener,
                            adapterPosition = bindingAdapterPosition,
                            musicView = musicCell
                        )
                    }

                    override fun onStop(withListener: Boolean, isReset: Boolean) {
                        if (withListener) musicActionCallback.onStopClicked(entity, isReset)
                    }

                    override fun clickShare() {
                        musicActionCallback.onAddClicked(entity)
                    }
                })
    }

    private val audioEventListener: AudioEventListener =
            object : AudioEventListener {
                override fun onPlay(withListener: Boolean) {
                    musicCell.startPlaying(withListener)
                }

                override fun onPause(isReset: Boolean) {
                    musicCell.stopPlaying(false, isReset = isReset)
                }

                override fun onLoad(isDownload: Boolean) {
                    if (isDownload) musicCell.startDownloading()
                    else musicCell.stopDownloading()
                }

                override fun onProgress(percent: Int) {
                    musicCell.setProgress(percent)
                }
            }

}

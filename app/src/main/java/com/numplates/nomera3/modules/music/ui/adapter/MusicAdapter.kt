package com.numplates.nomera3.modules.music.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.music.ui.viewholder.EmptyViewHolder
import com.numplates.nomera3.modules.music.ui.viewholder.HeaderViewHolder
import com.numplates.nomera3.modules.music.ui.viewholder.MusicCellViewHolder
import com.numplates.nomera3.modules.music.ui.viewholder.ProgressViewHolder
import timber.log.Timber

class MusicAdapter(
    private val musicActionCallback: MusicActionCallback,
    private val isDarkMode: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val asyncDiffer =
        AsyncListDiffer(this,
            object : DiffUtil.ItemCallback<MusicCellUIEntity>() {
                override fun areItemsTheSame(
                    oldItem: MusicCellUIEntity,
                    newItem: MusicCellUIEntity
                ): Boolean = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: MusicCellUIEntity,
                    newItem: MusicCellUIEntity
                ): Boolean = oldItem == newItem
            })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        MusicAdapterType.ITEM_TYPE_MUSIC.itemType ->
            MusicCellViewHolder(parent = parent, musicActionCallback = musicActionCallback, isDarkMode = isDarkMode)
        MusicAdapterType.ITEM_TYPE_HEADER_RECOMMENDATION.itemType -> HeaderViewHolder(parent = parent, isDarkMode = isDarkMode)
        MusicAdapterType.ITEM_TYPE_HEADER_SEARCH.itemType -> HeaderViewHolder(parent = parent, isDarkMode = isDarkMode)
        MusicAdapterType.ITEM_TYPE_PROGRESS.itemType -> ProgressViewHolder(parent)
        else -> EmptyViewHolder(parent)
    }

    override fun getItemCount() = asyncDiffer.currentList.size

    override fun getItemViewType(position: Int): Int = asyncDiffer.currentList[position].type.itemType

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is MusicCellViewHolder -> holder.bind(asyncDiffer.currentList[position])
        is HeaderViewHolder -> holder.bind(asyncDiffer.currentList[position])
        is ProgressViewHolder -> holder.bind()
        else -> Unit
    }

    fun submitList(musicList: List<MusicCellUIEntity>, submitReady: () -> Unit = {}) {
        asyncDiffer.submitList(musicList, submitReady)
    }

    fun getRangeItems(range: IntRange): List<MusicCellUIEntity> {
        return try {
            asyncDiffer.currentList.subList(range.first, range.last)
        } catch (e: Exception) {
            Timber.e("Get range items failed ${e.message}")
            emptyList()
        }
    }
}

interface MusicActionCallback {
    fun onPlayClicked(
        entity: MusicCellUIEntity,
        audioEventListener: AudioEventListener,
        adapterPosition: Int,
        musicView: View?
    )

    fun onStopClicked(entity: MusicCellUIEntity, isReset: Boolean)
    fun onCellClicked(entity: MusicCellUIEntity)
    fun onAddClicked(entity: MusicCellUIEntity)
}

enum class MusicAdapterType(val itemType: Int) {
    ITEM_TYPE_MUSIC(0),
    ITEM_TYPE_HEADER_RECOMMENDATION(1),
    ITEM_TYPE_PROGRESS(2),
    ITEM_TYPE_HEADER_SEARCH(3)
}

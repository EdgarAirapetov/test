package com.numplates.nomera3.modules.music.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.music.ui.adapter.MusicAdapterType
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity

class HeaderViewHolder(
    parent: ViewGroup,
    isDarkMode: Boolean = false
) : BaseViewHolder(parent, R.layout.item_music_header) {

    private val header = itemView.findViewById<TextView>(R.id.tv_header)

    init {
        header.setTextColor(
            ContextCompat.getColor(
                header.context,
                if (isDarkMode) {
                    R.color.uiKitColorForegroundInvers
                } else {
                    R.color.uiKitColorForegroundPrimary
                }
            )
        )
    }

    fun bind(item: MusicCellUIEntity) {
        if (item.type == MusicAdapterType.ITEM_TYPE_HEADER_SEARCH) {
            header.text = itemView.context?.getString(R.string.general_search_results)
        } else {
            header.text = itemView.context?.getString(R.string.apple_music_top)
        }
    }
}

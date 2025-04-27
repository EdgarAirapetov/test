package com.numplates.nomera3.modules.tags.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSearchTagItemBinding
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity


class MeeraTagViewHolder(val binding: MeeraSearchTagItemBinding, val isDarkMode: Boolean) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: UITagEntity, onTagClick: (UITagEntity) -> Unit) {
        binding.vUserTag.apply {
            if (isDarkMode) {
                cellBackgroundColor = R.color.ui_black
                cellLeftIconAndTitleColor = R.color.uiKitColorBackgroundPrimary
            }
            cellCityText = false
            setTitleValue(data.userName ?: "")
            cellTitleVerified = data.isVerified == 0
            setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = data.image
                )
            )
        }

        binding.root.setThrottledClickListener {
            onTagClick(data)
        }
    }
}

package com.numplates.nomera3.modules.share.ui.holder

import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraShareItemBinding
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem

class MeeraShareItemHolder(
    private val callback: ShareItemsCallback,
    val binding: MeeraShareItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: UIShareItem, lastItem: Boolean) {
        binding.vShareItem.apply {
            if (item.avatar is String) {
                setLeftUserPicConfig(
                    config = UserpicUiModel(
                        userAvatarUrl = item.avatar,
                        userAvatarErrorPlaceholder = identifyAvatarByGender(item.gender)
                    )
                )
            } else {
                setLeftUserPicConfig(
                    config = UserpicUiModel(userAvatarRes = R.drawable.ic_empty_avatar)
                )
            }

            setTitleValue(item.title ?: "")
            cellCityText = true
            setCityValue(item.subTitle ?: "")
            setCellRightElementChecked(item.isChecked)

            if (lastItem) cellPosition = CellPosition.BOTTOM
            setupCheckBox(item)
            cellTitleVerified = item.verified
        }
    }

    private fun identifyAvatarByGender(gender: Int?): Int {
        return if (gender.toBoolean()) {
            R.drawable.ic_man_avatar_placeholder
        } else {
            R.drawable.ic_woman_avatar_placeholder
        }
    }

    private fun setupCheckBox(item: UIShareItem) {
        binding.vShareItem.apply {
            setRightElementContainerClickable(false)
            setThrottledClickListener {
                if (isCheckButton) {
                    callback.onChecked(item, !item.isChecked)
                } else {
                    if (callback.canBeChecked()) {
                        callback.onChecked(item, !item.isChecked)
                    }
                }
            }
        }
    }
}

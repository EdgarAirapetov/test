package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMomentSettingsUserAddItemBinding

class MeeraBaseSettingsListSearchUsersAdapter(
    private val callback: (MeeraBaseSettingsSearchUserAction) -> Unit
) : ListAdapter<UserSimple, MeeraBaseSettingsListSearchUsersAdapter.MeeraUserSettingsSearchHolder>(DiffCallback()) {
    private val listCheckUserId = mutableSetOf<Long>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraUserSettingsSearchHolder {
        return MeeraUserSettingsSearchHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: MeeraUserSettingsSearchHolder, position: Int) {
        holder.bind(
            currentList[position], position == 0, currentList.lastIndex == position)
    }

    fun clearCheckListUser(){
        listCheckUserId.clear()
    }

    inner class MeeraUserSettingsSearchHolder(
        val binding: MeeraMomentSettingsUserAddItemBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: UserSimple, firstPosition: Boolean, lastPosition: Boolean) {
            binding.cellWithRightIcon.apply {
                cellPosition = when {
                    firstPosition -> CellPosition.TOP
                    lastPosition -> CellPosition.BOTTOM
                    else -> CellPosition.MIDDLE
                }
                setRightElementContainerClickable(false)
                setTitleValue(data.name ?: "")
                setDescriptionValue((resources.getString(R.string.uniquename_prefix) + data.uniqueName))
                cellCityText = true
                setCityValue(data.city?.name ?: "")
                cellTitleVerified = data.profileVerified == 0
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = data.avatarSmall))
                if (listCheckUserId.contains(data.userId)) {
                    setCellRightElementChecked(true)
                } else {
                    setCellRightElementChecked(false)
                }
                setThrottledClickListener {
                    setCellRightElementChecked(!isCheckButton)
                    if (listCheckUserId.contains(data.userId)) {
                        listCheckUserId.remove(data.userId)
                    } else {
                        listCheckUserId.add(data.userId)
                    }
                    callback.invoke(
                        MeeraBaseSettingsSearchUserAction.UserChecked(data.copy(isChecked = isCheckButton))
                    )
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<UserSimple>() {
        override fun areItemsTheSame(oldItem: UserSimple, newItem: UserSimple): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserSimple, newItem: UserSimple): Boolean {
            return oldItem == newItem
        }
    }
}

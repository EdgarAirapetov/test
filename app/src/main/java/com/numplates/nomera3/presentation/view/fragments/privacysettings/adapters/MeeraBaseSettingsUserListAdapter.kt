package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraProfileSettingsUserCellBinding

sealed class MeeraBaseSettingsUserListAction {
    class DeleteUserAction(
        val userName: String?,
        val userId: Long,
        val adapterPosition: Int
    ) : MeeraBaseSettingsUserListAction()
}

class MeeraBaseSettingsUserListAdapter(
    private val actionListener: (MeeraBaseSettingsUserListAction) -> Unit
) : ListAdapter<UserSimple, MeeraBaseSettingsUserListAdapter.ExclusionUserViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExclusionUserViewHolder {
        return ExclusionUserViewHolder(parent.toBinding(), parent.resources)
    }

    override fun onBindViewHolder(holder: ExclusionUserViewHolder, position: Int) {
        holder.bind(currentList[position], position)
    }

    inner class ExclusionUserViewHolder(
        private val binding: MeeraProfileSettingsUserCellBinding,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: UserSimple, adapterPosition: Int) {
            val uniqueNamePrefix = resources.getString(R.string.uniquename_prefix)
            binding.vProfileSettingsUserItem.run {
                cellCityText = false
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = data.avatarSmall))
                setTitleValue(data.name ?: "")
                setDescriptionValue((uniqueNamePrefix + data.uniqueName))
                cellTitleVerified = data.profileVerified == 0
                cellRightIconClickListener = {
                    actionListener.invoke(
                        MeeraBaseSettingsUserListAction.DeleteUserAction(
                            userName = data.name,
                            userId = data.userId,
                            adapterPosition = adapterPosition
                        )
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

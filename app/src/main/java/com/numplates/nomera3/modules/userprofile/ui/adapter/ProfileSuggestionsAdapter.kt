package com.numplates.nomera3.modules.userprofile.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemProfileContactSyncBinding
import com.numplates.nomera3.databinding.ItemProfileSuggestionBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.viewholder.ProfileContactSyncHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.ProfileSuggestionViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class ProfileSuggestionsAdapter(
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { _ -> },
) : ListAdapter<ProfileSuggestionUiModels, RecyclerView.ViewHolder>(ProfileSuggestionDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is ProfileSuggestionUiModels.ProfileSuggestionUiModel -> ViewType.SUGGESTION_PROFILE.ordinal
            is ProfileSuggestionUiModels.SuggestionSyncContactUiModel -> ViewType.CONTACT_SYNC.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.SUGGESTION_PROFILE.ordinal -> {
                createSuggestionHolder(parent)
            }
            ViewType.CONTACT_SYNC.ordinal -> {
                createContactSyncHolder(parent)
            }
            else -> error("Unknown View type!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProfileSuggestionViewHolder -> {
                holder.bind(currentList[position] as ProfileSuggestionUiModels.ProfileSuggestionUiModel)
            }
            is ProfileContactSyncHolder -> {
                holder.bind(currentList[position] as ProfileSuggestionUiModels.SuggestionSyncContactUiModel)
            }
        }
    }

    private fun createSuggestionHolder(parent: ViewGroup): ProfileSuggestionViewHolder {
        return ProfileSuggestionViewHolder(
            binding = parent.inflateBinding(ItemProfileSuggestionBinding::inflate),
            profileUIActionHandler = profileUIActionHandler
        )
    }

    private fun createContactSyncHolder(parent: ViewGroup): ProfileContactSyncHolder {
        return ProfileContactSyncHolder(
            binding = parent.inflateBinding(ItemProfileContactSyncBinding::inflate),
            profileUIActionHandler = profileUIActionHandler
        )
    }

    class ProfileSuggestionDiffCallback : DiffUtil.ItemCallback<ProfileSuggestionUiModels>() {
        override fun areItemsTheSame(
            oldItem: ProfileSuggestionUiModels,
            newItem: ProfileSuggestionUiModels
        ): Boolean {
            return oldItem.getUserId() == newItem.getUserId()
        }

        override fun areContentsTheSame(
            oldItem: ProfileSuggestionUiModels,
            newItem: ProfileSuggestionUiModels
        ): Boolean {
            return oldItem == newItem
        }
    }

    private enum class ViewType {
        SUGGESTION_PROFILE,
        CONTACT_SYNC
    }
}

package com.numplates.nomera3.modules.userprofile.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.MeeraItemProfileContactSyncBinding
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraProfileContactSyncHolder
import com.numplates.nomera3.modules.userprofile.ui.viewholder.MeeraProfileSuggestionViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraProfileSuggestionsAdapter(
    private var profileUIActionHandler: ((UserProfileUIAction) -> Unit)? = null,
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
            is MeeraProfileSuggestionViewHolder -> {
                holder.bind(currentList[position] as ProfileSuggestionUiModels.ProfileSuggestionUiModel)
                holder.setProfileUIActionHandler(profileUIActionHandler)
            }
            is MeeraProfileContactSyncHolder -> {
                holder.bind(currentList[position] as ProfileSuggestionUiModels.SuggestionSyncContactUiModel)
                holder.setProfileUIActionHandler(profileUIActionHandler)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is MeeraProfileSuggestionViewHolder) {
            holder.setProfileUIActionHandler(profileUIActionHandler)
        }
        if (holder is MeeraProfileContactSyncHolder) {
            holder.setProfileUIActionHandler(profileUIActionHandler)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is MeeraProfileSuggestionViewHolder) {
            holder.clearResources()
        }
        if (holder is MeeraProfileContactSyncHolder) {
            holder.clearResources()
        }
    }

    fun clearResources() {
        profileUIActionHandler = null
    }

    private fun createSuggestionHolder(parent: ViewGroup): MeeraProfileSuggestionViewHolder {
        return MeeraProfileSuggestionViewHolder(
            binding = parent.inflateBinding(MeeraItemProfileSuggestionBinding::inflate)
        )
    }

    private fun createContactSyncHolder(parent: ViewGroup): MeeraProfileContactSyncHolder {
        return MeeraProfileContactSyncHolder(
            binding = parent.inflateBinding(MeeraItemProfileContactSyncBinding::inflate)
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

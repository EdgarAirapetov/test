package com.numplates.nomera3.presentation.view.utils.sharedialog.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraGroupShareMenuItemBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunitiesListItemEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity


class MeeraShareGroupAdapter(val groupClick: (community: CommunityEntity) -> Unit) :
    ListAdapter<CommunitiesListItemEntity, MeeraShareGroupAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MeeraGroupShareMenuItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, parent.resources)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(
        val binding: MeeraGroupShareMenuItemBinding,
        val resource: Resources
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CommunitiesListItemEntity) {
            binding.vGroupItem.apply {
                setTitleValue(data.community?.name ?: "")
                setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarUrl = data.community?.avatar
                    )
                )
                cellCityText = true

                setCityValue(binding.vGroupItem.context.getString(R.string.groups_members, data.community?.users))
                setThrottledClickListener {
                    data.community?.let {
                        groupClick.invoke(data.community)
                    }
                }
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommunitiesListItemEntity>() {
    override fun areContentsTheSame(oldItem: CommunitiesListItemEntity, newItem: CommunitiesListItemEntity): Boolean {
        return oldItem.community == newItem.community
    }

    override fun areItemsTheSame(oldItem: CommunitiesListItemEntity, newItem: CommunitiesListItemEntity): Boolean {
        return oldItem == newItem
    }
}

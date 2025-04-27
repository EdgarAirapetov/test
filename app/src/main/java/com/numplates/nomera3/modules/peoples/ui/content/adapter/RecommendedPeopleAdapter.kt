package com.numplates.nomera3.modules.peoples.ui.content.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.ItemRecommendedPeopleBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.holder.RecommendedPeopleHolder

class RecommendedPeopleAdapter(
    private val actionListener: (FriendsContentActions) -> Unit
) : ListAdapter<RecommendedPeopleUiEntity, RecommendedPeopleHolder>(RecommendedPeopleDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedPeopleHolder {
        val binding = ItemRecommendedPeopleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendedPeopleHolder(
            binding = binding,
            actionListener = actionListener
        )
    }

    override fun onBindViewHolder(holder: RecommendedPeopleHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun getItemByPosition(position: Int) = try {
        currentList[position]
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private class RecommendedPeopleDiff : DiffUtil.ItemCallback<RecommendedPeopleUiEntity>() {

        override fun areItemsTheSame(
            oldItem: RecommendedPeopleUiEntity,
            newItem: RecommendedPeopleUiEntity
        ): Boolean = oldItem.userId == newItem.userId

        override fun areContentsTheSame(
            oldItem: RecommendedPeopleUiEntity,
            newItem: RecommendedPeopleUiEntity
        ): Boolean = oldItem == newItem

    }
}

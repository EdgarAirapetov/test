package com.numplates.nomera3.modules.services.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesRecommendedUserViewHolder

class MeeraServicesRecommendedPeopleAdapter(
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ListAdapter<RecommendedPeopleUiEntity, MeeraServicesRecommendedUserViewHolder>(RecommendedPeopleDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraServicesRecommendedUserViewHolder {
        val binding = MeeraItemRecommendedPeopleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MeeraServicesRecommendedUserViewHolder(
            binding = binding,
            actionListener = actionListener
        )
    }

    override fun onBindViewHolder(holder: MeeraServicesRecommendedUserViewHolder, position: Int) {
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

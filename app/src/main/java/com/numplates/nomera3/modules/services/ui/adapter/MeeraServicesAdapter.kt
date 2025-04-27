package com.numplates.nomera3.modules.services.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.numplates.nomera3.databinding.MeeraItemRecentUsersBinding
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleListBinding
import com.numplates.nomera3.databinding.MeeraItemServicesButtonsBinding
import com.numplates.nomera3.databinding.MeeraItemServicesCommunitiesPlaceholderBinding
import com.numplates.nomera3.databinding.MeeraItemServicesUserBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.services.ui.content.equalTo
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesContentType
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecentUsersUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecommendedPeopleUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiUpdate
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUserUiModel
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesButtonsViewHolder
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesCommunitiesPlaceholderViewHolder
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesCommunityListViewHolder
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesRecentUsersListViewHolder
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesRecommendedUsersListViewHolder
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesUserViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraServicesAdapter(
    private val actionListener: (MeeraServicesUiAction) -> Unit,
    private val recommendedPeoplePaginationHandler: RecommendedPeoplePaginationHandler,
    private val communnitiesPaginationHandler: RecommendedPeoplePaginationHandler
) : ListAdapter<MeeraServicesUiModel, ViewHolder>(MeeraServicesDiffUtil()) {

    override fun getItemViewType(position: Int): Int {
        if (position == -1) return MeeraServicesContentType.USER.ordinal
        return getItem(position).getServicesContentType().ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            MeeraServicesContentType.USER.ordinal -> {
                val binding = parent.inflateBinding(MeeraItemServicesUserBinding::inflate)
                MeeraServicesUserViewHolder(binding, actionListener)
            }
            MeeraServicesContentType.BUTTONS.ordinal -> {
                val binding = parent.inflateBinding(MeeraItemServicesButtonsBinding::inflate)
                MeeraServicesButtonsViewHolder(binding, actionListener)
            }
            MeeraServicesContentType.RECOMMENDED_PEOPLE.ordinal -> {
                val binding = parent.inflateBinding(MeeraItemRecommendedPeopleListBinding::inflate)
                MeeraServicesRecommendedUsersListViewHolder(binding, actionListener, recommendedPeoplePaginationHandler)
            }
            MeeraServicesContentType.RECENT_USERS.ordinal -> {
                val binding = parent.inflateBinding(MeeraItemRecentUsersBinding::inflate)
                MeeraServicesRecentUsersListViewHolder(binding, actionListener)
            }
            MeeraServicesContentType.COMMUNITIES.ordinal -> {
                val binding = parent.inflateBinding(MeeraItemRecentUsersBinding::inflate)
                MeeraServicesCommunityListViewHolder(binding, actionListener, communnitiesPaginationHandler)
            }
            MeeraServicesContentType.COMMUNITIES_PLACEHOLDER.ordinal -> {
                val binding = parent.inflateBinding(MeeraItemServicesCommunitiesPlaceholderBinding::inflate)
                MeeraServicesCommunitiesPlaceholderViewHolder(binding, actionListener)
            }
            else -> error("View Type is not supported")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is MeeraServicesUserViewHolder -> holder.bind(getItem(position) as MeeraServicesUserUiModel)
            is MeeraServicesRecommendedUsersListViewHolder -> holder.bind((getItem(position) as MeeraServicesRecommendedPeopleUiModel))
            is MeeraServicesRecentUsersListViewHolder -> holder.bind(getItem(position) as MeeraServicesRecentUsersUiModel)
            is MeeraServicesCommunityListViewHolder -> holder.bind(getItem(position) as MeeraServicesCommunitiesUiModel)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()
            && payloads[0] is MeeraServicesUiUpdate
        ) {
            when (val payload = payloads[0] as MeeraServicesUiUpdate) {
                is MeeraServicesUiUpdate.UpdateBloggersList -> {
                    (holder as? MeeraServicesRecommendedUsersListViewHolder?)?.submitRecommendations(payload.recommendationsList)
                }

                is MeeraServicesUiUpdate.UpdateCommunitiesList -> {
                    (holder as? MeeraServicesCommunityListViewHolder?)?.updateCommunities(payload.newModel)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private class MeeraServicesDiffUtil : DiffUtil.ItemCallback<MeeraServicesUiModel>() {
        override fun areItemsTheSame(oldItem: MeeraServicesUiModel, newItem: MeeraServicesUiModel): Boolean {
            return oldItem.getServicesContentType() == newItem.getServicesContentType()
        }

        override fun areContentsTheSame(oldItem: MeeraServicesUiModel, newItem: MeeraServicesUiModel): Boolean {
            return oldItem.equalTo(newItem)
        }

        override fun getChangePayload(oldItem: MeeraServicesUiModel, newItem: MeeraServicesUiModel): Any? {
            if (newItem.getServicesContentType() == MeeraServicesContentType.RECOMMENDED_PEOPLE) {
                val isEquals = oldItem.equalTo(newItem)
                val newItemEntity = (newItem as? MeeraServicesRecommendedPeopleUiModel) ?: return super.getChangePayload(oldItem, newItem)
                if (!isEquals) return MeeraServicesUiUpdate.UpdateBloggersList(newItemEntity.users)
            }
            if (newItem.getServicesContentType() == MeeraServicesContentType.COMMUNITIES) {
                val isEquals = oldItem.equalTo(newItem)
                val newItemEntity = (newItem as? MeeraServicesCommunitiesUiModel) ?: return super.getChangePayload(oldItem, newItem)
                if (!isEquals) return MeeraServicesUiUpdate.UpdateCommunitiesList(newItemEntity)
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}

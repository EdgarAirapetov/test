package com.numplates.nomera3.modules.peoples.ui.content.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemBloggerMediaContentListBinding
import com.numplates.nomera3.databinding.ItemPeoplesShimmerBinding
import com.numplates.nomera3.databinding.MeeraItemFindPeoplesActionBinding
import com.numplates.nomera3.databinding.MeeraItemLabelBinding
import com.numplates.nomera3.databinding.MeeraItemPeopleBloggersPlaceholderBinding
import com.numplates.nomera3.databinding.MeeraItemPeopleInfoBinding
import com.numplates.nomera3.databinding.MeeraItemPeopleRecommendedUsersShimmerBinding
import com.numplates.nomera3.databinding.MeeraItemPeopleSyncContactsBinding
import com.numplates.nomera3.databinding.MeeraItemPeoplesRecentShimmerBinding
import com.numplates.nomera3.databinding.MeeraItemRecentUsersBinding
import com.numplates.nomera3.databinding.MeeraItemRecommendedPeopleListBinding
import com.numplates.nomera3.databinding.MeeraItemSearchResultTitleBinding
import com.numplates.nomera3.databinding.MeeraItemSearchResultUserBinding
import com.numplates.nomera3.databinding.MeeraItemSearchUserShimmerBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.RecommendedPeoplePaginationHandler
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggersPlaceHolderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FindPeoplesUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.HeaderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleSyncContactsUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedUsersShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.TitleSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UiPeopleUpdate
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.equalTo
import com.numplates.nomera3.modules.peoples.ui.content.holder.BasePeoplesViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraBloggerMediaContentListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraBloggersPlaceholderViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraFindPeoplesViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraLabelHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraPeopleInfoViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraPeopleSyncContactsHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecentUsersShimmerHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecentUsersViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecommendedPeopleListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecommendedUsersShimmerHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraTitleSearchResultViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraUserSearchResultShimmerViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraUserSearchResultViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.PeoplesShimmerHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraPeoplesContentAdapter(
    private val actionListener: (actions: FriendsContentActions) -> Unit,
    private val mediaContentScrollListener: (innerPosition: Int, rootPosition: Int) -> Unit,
    private val recommendedPeoplePaginationHandler: RecommendedPeoplePaginationHandler
) : ListAdapter<PeoplesContentUiEntity, RecyclerView.ViewHolder>(PeoplesContentDiffUtil()) {

    override fun getItemViewType(position: Int): Int {
        return currentList[position].getPeoplesActionType().ordinal
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            PeoplesContentType.HEADER_TYPE.ordinal -> {
                createLabelHolder(parent)
            }
            PeoplesContentType.FIND_FRIENDS_TYPE.ordinal -> {
                createFindPeoplesViewHolder(parent)
            }
            PeoplesContentType.SHIMMER_TYPE.ordinal -> {
                createShimmerHolder(parent)
            }
            PeoplesContentType.PEOPLE_INFO_TYPE.ordinal -> {
                createPeopleInfoHolder(parent)
            }
            PeoplesContentType.BLOGGER_MEDIA_CONTENT_TYPE.ordinal -> {
                createMediaContentHolder(parent)
            }
            PeoplesContentType.RECOMMENDED_PEOPLE.ordinal -> {
                createRecommendedPeopleHolder(parent)
            }
            PeoplesContentType.CONTACT_SYNC_TYPE.ordinal -> {
                createSyncContactsHolder(parent)
            }
            PeoplesContentType.RECENT_USERS.ordinal -> {
                createRecentUsersHolder(parent)
            }
            PeoplesContentType.USER_SEARCH_RESULT.ordinal -> {
                createUserSearchResultHolder(parent)
            }
            PeoplesContentType.TITLE_SEARCH_RESULT.ordinal -> {
                createTitleSearchResultHolder(parent)
            }
            PeoplesContentType.BLOGGERS_PLACEHOLDER.ordinal -> {
                createBloggersPlaceholderViewHolder(parent)
            }
            PeoplesContentType.RECENT_SHIMMER_TYPE.ordinal -> {
                createRecentUsersShimmerHolder(parent)
            }
            PeoplesContentType.RECOMMENDED_USERS_SHIMMER_TYPE.ordinal -> {
                createRecommendedUsersShimmerHolder(parent)
            }
            PeoplesContentType.SEARCH_RESULT_SHIMMER_TYPE.ordinal -> {
                createSearchResultUserShimmerHolder(parent)
            }
            else -> error("Unknown view type!")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is MeeraLabelHolder -> holder.bind(currentList[position] as HeaderUiEntity)
            is MeeraFindPeoplesViewHolder -> holder.bind(currentList[position] as FindPeoplesUiEntity)
            is PeoplesShimmerHolder -> holder.bind(currentList[position] as PeoplesShimmerUiEntity)
            is MeeraPeopleInfoViewHolder -> holder.bind(currentList[position] as PeopleInfoUiEntity)
            is MeeraBloggerMediaContentListHolder -> holder.bind(currentList[position] as BloggerMediaContentListUiEntity)
            is MeeraRecommendedPeopleListHolder -> holder.bind(currentList[position] as RecommendedPeopleListUiEntity)
            is MeeraPeopleSyncContactsHolder -> holder.bind(currentList[position] as PeopleSyncContactsUiModel)
            is MeeraRecentUsersViewHolder -> holder.bind(currentList[position] as RecentUsersUiEntity)
            is MeeraUserSearchResultViewHolder -> holder.bind(currentList[position] as UserSearchResultUiEntity)
            is MeeraTitleSearchResultViewHolder -> holder.bind(currentList[position] as TitleSearchResultUiEntity)
            is MeeraBloggersPlaceholderViewHolder -> holder.bind(currentList[position] as BloggersPlaceHolderUiEntity)
            is MeeraRecentUsersShimmerHolder -> holder.bind(currentList[position] as RecentUsersShimmerUiEntity)
            is MeeraRecommendedUsersShimmerHolder -> holder.bind(currentList[position] as RecommendedUsersShimmerUiEntity)
            is MeeraUserSearchResultShimmerViewHolder -> holder.bind(currentList[position] as UserSearchResultShimmerUiEntity)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()
            && payloads[0] is UiPeopleUpdate
            && holder is MeeraRecommendedPeopleListHolder
        ) {
            when (val payload = payloads[0] as UiPeopleUpdate) {
                is UiPeopleUpdate.UpdateBloggersList -> {
                    holder.submitRecommendations(payload.recommendationsList)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is BasePeoplesViewHolder<*, *>) {
            holder.onViewDetached()
        }
    }

    fun getItemByPosition(position: Int): PeoplesContentUiEntity? {
        return try {
            currentList[position]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createLabelHolder(parent: ViewGroup) =
        MeeraLabelHolder(
            binding = parent.inflateBinding(MeeraItemLabelBinding::inflate),
            actionListener = actionListener
        )


    private fun createFindPeoplesViewHolder(parent: ViewGroup) =
        MeeraFindPeoplesViewHolder(
            binding = parent.inflateBinding(MeeraItemFindPeoplesActionBinding::inflate),
            actionListener = actionListener
        )


    private fun createShimmerHolder(parent: ViewGroup) =
        PeoplesShimmerHolder(parent.inflateBinding(ItemPeoplesShimmerBinding::inflate))

    private fun createPeopleInfoHolder(viewGroup: ViewGroup) =
        MeeraPeopleInfoViewHolder(
            binding = viewGroup.inflateBinding(MeeraItemPeopleInfoBinding::inflate),
            actionListener = actionListener
        )

    private fun createMediaContentHolder(viewGroup: ViewGroup) =
        MeeraBloggerMediaContentListHolder(
            binding = viewGroup.inflateBinding(ItemBloggerMediaContentListBinding::inflate),
            actionListener = actionListener,
            scrollListener = mediaContentScrollListener
        )

    private fun createRecommendedPeopleHolder(viewGroup: ViewGroup) =
        MeeraRecommendedPeopleListHolder(
            binding = viewGroup.inflateBinding(MeeraItemRecommendedPeopleListBinding::inflate),
            actionListener = actionListener,
            paginationHandler = recommendedPeoplePaginationHandler
        )

    private fun createSyncContactsHolder(viewGroup: ViewGroup) =
        MeeraPeopleSyncContactsHolder(
            binding = viewGroup.inflateBinding(MeeraItemPeopleSyncContactsBinding::inflate),
            actionListener = actionListener
        )

    private fun createRecentUsersHolder(viewGroup: ViewGroup) =
        MeeraRecentUsersViewHolder(
            binding = viewGroup.inflateBinding(MeeraItemRecentUsersBinding::inflate),
            actionListener = actionListener
        )

    private fun createUserSearchResultHolder(viewGroup: ViewGroup) =
        MeeraUserSearchResultViewHolder(
            binding = viewGroup.inflateBinding(MeeraItemSearchResultUserBinding::inflate),
            actionListener = actionListener
        )

    private fun createTitleSearchResultHolder(viewGroup: ViewGroup) =
        MeeraTitleSearchResultViewHolder(
            binding = viewGroup.inflateBinding(MeeraItemSearchResultTitleBinding::inflate)
        )

    private fun createBloggersPlaceholderViewHolder(viewGroup: ViewGroup) =
        MeeraBloggersPlaceholderViewHolder(
            binding = viewGroup.inflateBinding(MeeraItemPeopleBloggersPlaceholderBinding::inflate)
        )

    private fun createRecommendedUsersShimmerHolder(parent: ViewGroup) =
        MeeraRecommendedUsersShimmerHolder(parent.inflateBinding(MeeraItemPeopleRecommendedUsersShimmerBinding::inflate))

    private fun createRecentUsersShimmerHolder(parent: ViewGroup) =
        MeeraRecentUsersShimmerHolder(parent.inflateBinding(MeeraItemPeoplesRecentShimmerBinding::inflate))

    private fun createSearchResultUserShimmerHolder(parent: ViewGroup) =
        MeeraUserSearchResultShimmerViewHolder(parent.inflateBinding(MeeraItemSearchUserShimmerBinding::inflate))

    private class PeoplesContentDiffUtil : DiffUtil.ItemCallback<PeoplesContentUiEntity>() {
        override fun areItemsTheSame(
            oldItem: PeoplesContentUiEntity,
            newItem: PeoplesContentUiEntity
        ) = oldItem.getUserId() == newItem.getUserId()
            && oldItem.getPeoplesActionType() == newItem.getPeoplesActionType()

        override fun areContentsTheSame(
            oldItem: PeoplesContentUiEntity,
            newItem: PeoplesContentUiEntity
        ) = oldItem.equalTo(newItem)

        override fun getChangePayload(
            oldItem: PeoplesContentUiEntity,
            newItem: PeoplesContentUiEntity
        ): Any? {
            if (newItem.getPeoplesActionType() == PeoplesContentType.RECOMMENDED_PEOPLE) {
                val isEquals = oldItem.equalTo(newItem)
                val newItemEntity =
                    (newItem as? RecommendedPeopleListUiEntity) ?: return super.getChangePayload(oldItem, newItem)
                if (!isEquals) return UiPeopleUpdate.UpdateBloggersList(newItemEntity.recommendedPeopleList)
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}

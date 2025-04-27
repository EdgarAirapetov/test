package com.numplates.nomera3.modules.peoples.ui.content.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemBloggerMediaContentListBinding
import com.numplates.nomera3.databinding.ItemFindPeoplesActionBinding
import com.numplates.nomera3.databinding.ItemLabelBinding
import com.numplates.nomera3.databinding.ItemPeopleBloggersPlaceholderBinding
import com.numplates.nomera3.databinding.ItemPeopleInfoBinding
import com.numplates.nomera3.databinding.ItemPeopleSyncContactsBinding
import com.numplates.nomera3.databinding.ItemPeoplesShimmerBinding
import com.numplates.nomera3.databinding.ItemRecommendedPeopleListBinding
import com.numplates.nomera3.databinding.SearchRecentBlockBinding
import com.numplates.nomera3.databinding.SearchResultTitleItemBinding
import com.numplates.nomera3.databinding.SearchResultUserItemBinding
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
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.TitleSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UiPeopleUpdate
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.equalTo
import com.numplates.nomera3.modules.peoples.ui.content.holder.BasePeoplesViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggerMediaContentListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggersPlaceHolderViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.FindPeoplesViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.LabelHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.PeopleInfoViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.PeopleSyncContactsHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.PeoplesShimmerHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.RecentUsersViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.RecommendedPeopleListHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.TitleSearchResultViewHolder
import com.numplates.nomera3.modules.peoples.ui.content.holder.UserSearchResultViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class PeoplesContentAdapter constructor(
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
            else -> error("Unknown view type!")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is LabelHolder -> holder.bind(currentList[position] as HeaderUiEntity)
            is FindPeoplesViewHolder -> holder.bind(currentList[position] as FindPeoplesUiEntity)
            is PeoplesShimmerHolder -> holder.bind(currentList[position] as PeoplesShimmerUiEntity)
            is PeopleInfoViewHolder -> holder.bind(currentList[position] as PeopleInfoUiEntity)
            is BloggerMediaContentListHolder -> holder.bind(currentList[position] as BloggerMediaContentListUiEntity)
            is RecommendedPeopleListHolder -> holder.bind(currentList[position] as RecommendedPeopleListUiEntity)
            is PeopleSyncContactsHolder -> holder.bind(currentList[position] as PeopleSyncContactsUiModel)
            is RecentUsersViewHolder -> holder.bind(currentList[position] as RecentUsersUiEntity)
            is UserSearchResultViewHolder -> holder.bind(currentList[position] as UserSearchResultUiEntity)
            is TitleSearchResultViewHolder -> holder.bind(currentList[position] as TitleSearchResultUiEntity)
            is BloggersPlaceHolderViewHolder -> holder.bind(currentList[position] as BloggersPlaceHolderUiEntity)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()
            && payloads[0] is UiPeopleUpdate
            && holder is RecommendedPeopleListHolder
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
        LabelHolder(
            binding = parent.inflateBinding(ItemLabelBinding::inflate),
            actionListener = actionListener
        )


    private fun createFindPeoplesViewHolder(parent: ViewGroup) =
        FindPeoplesViewHolder(
            binding = parent.inflateBinding(ItemFindPeoplesActionBinding::inflate),
            actionListener = actionListener
        )


    private fun createShimmerHolder(parent: ViewGroup) =
        PeoplesShimmerHolder(parent.inflateBinding(ItemPeoplesShimmerBinding::inflate))

    private fun createPeopleInfoHolder(viewGroup: ViewGroup) =
        PeopleInfoViewHolder(
            binding = viewGroup.inflateBinding(ItemPeopleInfoBinding::inflate),
            actionListener = actionListener
        )

    private fun createMediaContentHolder(viewGroup: ViewGroup) =
        BloggerMediaContentListHolder(
            binding = viewGroup.inflateBinding(ItemBloggerMediaContentListBinding::inflate),
            actionListener = actionListener,
            scrollListener = mediaContentScrollListener
        )

    private fun createRecommendedPeopleHolder(viewGroup: ViewGroup) =
        RecommendedPeopleListHolder(
            binding = viewGroup.inflateBinding(ItemRecommendedPeopleListBinding::inflate),
            actionListener = actionListener,
            paginationHandler = recommendedPeoplePaginationHandler
        )

    private fun createSyncContactsHolder(viewGroup: ViewGroup) =
        PeopleSyncContactsHolder(
            binding = viewGroup.inflateBinding(ItemPeopleSyncContactsBinding::inflate),
            actionListener = actionListener
        )

    private fun createRecentUsersHolder(viewGroup: ViewGroup) =
        RecentUsersViewHolder(
            binding = viewGroup.inflateBinding(SearchRecentBlockBinding::inflate),
            actionListener = actionListener
        )

    private fun createUserSearchResultHolder(viewGroup: ViewGroup) =
        UserSearchResultViewHolder(
            binding = viewGroup.inflateBinding(SearchResultUserItemBinding::inflate),
            actionListener = actionListener
        )

    private fun createTitleSearchResultHolder(viewGroup: ViewGroup) =
        TitleSearchResultViewHolder(
            binding = viewGroup.inflateBinding(SearchResultTitleItemBinding::inflate)
        )

    private fun createBloggersPlaceholderViewHolder(viewGroup: ViewGroup) =
        BloggersPlaceHolderViewHolder(
            binding = viewGroup.inflateBinding(ItemPeopleBloggersPlaceholderBinding::inflate)
        )

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

enum class PeoplesContentType {
    HEADER_TYPE,
    FIND_FRIENDS_TYPE,
    SHIMMER_TYPE,
    PEOPLE_INFO_TYPE,
    BLOGGER_MEDIA_CONTENT_TYPE,
    RECOMMENDED_PEOPLE,
    CONTACT_SYNC_TYPE,
    RECENT_USERS,
    USER_SEARCH_RESULT,
    TITLE_SEARCH_RESULT,
    BLOGGERS_PLACEHOLDER,
    RECENT_SHIMMER_TYPE,
    RECOMMENDED_USERS_SHIMMER_TYPE,
    SEARCH_RESULT_SHIMMER_TYPE
}

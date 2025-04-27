package com.numplates.nomera3.modules.peoples.ui.content

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FindPeoplesUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.HeaderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.TitleSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity

fun PeoplesContentUiEntity.equalTo(newItem: PeoplesContentUiEntity): Boolean {
    return when (newItem.getPeoplesActionType()) {
        PeoplesContentType.HEADER_TYPE -> {
            val thatEntity = this as? HeaderUiEntity
            val newEntity = newItem as? HeaderUiEntity
            thatEntity == newEntity
        }
        PeoplesContentType.FIND_FRIENDS_TYPE -> {
            val thatEntity = this as? FindPeoplesUiEntity
            val newEntity = newItem as? FindPeoplesUiEntity
            thatEntity == newEntity
        }
        PeoplesContentType.PEOPLE_INFO_TYPE -> {
            val thatEntity = this as? PeopleInfoUiEntity
            val newEntity = newItem as? PeopleInfoUiEntity
            thatEntity == newEntity
        }
        PeoplesContentType.BLOGGER_MEDIA_CONTENT_TYPE -> {
            val thatEntity = this as? BloggerMediaContentListUiEntity
            val newEntity = newItem as? BloggerMediaContentListUiEntity
            if (thatEntity?.bloggerPostList?.size != newEntity?.bloggerPostList?.size) return false
            newEntity?.bloggerPostList?.forEachIndexed { index, bloggerMediaContentUiEntity ->
                val thatInnerEntity: BloggerMediaContentUiEntity =
                    thatEntity?.bloggerPostList?.get(index) ?: return true
                val isCompared = thatInnerEntity.compare(bloggerMediaContentUiEntity)
                if (!isCompared) return false
            }
            true
        }
        PeoplesContentType.RECOMMENDED_PEOPLE -> {
            val thatEntity = this as? RecommendedPeopleListUiEntity
            val newEntity = newItem as? RecommendedPeopleListUiEntity
            if (thatEntity?.recommendedPeopleList?.size != newEntity?.recommendedPeopleList?.size) return false
            newEntity?.recommendedPeopleList?.forEachIndexed { index, recommendedPeopleUiEntity ->
                val thatInnerEntity = thatEntity?.recommendedPeopleList?.get(index) ?: return true
                val isCompared = thatInnerEntity == recommendedPeopleUiEntity
                if (!isCompared) return false
            }
            true
        }
        PeoplesContentType.RECENT_USERS -> {
            val thatEntity = this as? RecentUsersUiEntity
            val newEntity = newItem as? RecentUsersUiEntity
            if (thatEntity?.users?.size != newEntity?.users?.size) return false
            return thatEntity?.users == newEntity?.users
        }
        PeoplesContentType.USER_SEARCH_RESULT -> {
            val thatEntity = this as? UserSearchResultUiEntity
            val newEntity = newItem as? UserSearchResultUiEntity
            return thatEntity == newEntity
        }
        PeoplesContentType.TITLE_SEARCH_RESULT -> {
            val thatEntity = this as? TitleSearchResultUiEntity
            val newEntity = newItem as? TitleSearchResultUiEntity
            return thatEntity == newEntity
        }
        PeoplesContentType.SEARCH_RESULT_SHIMMER_TYPE,
        PeoplesContentType.RECENT_SHIMMER_TYPE,
        PeoplesContentType.RECOMMENDED_USERS_SHIMMER_TYPE,
        PeoplesContentType.BLOGGERS_PLACEHOLDER,
        PeoplesContentType.SHIMMER_TYPE,
        PeoplesContentType.CONTACT_SYNC_TYPE-> true
    }
}

fun BloggerMediaContentUiEntity.compare(newItem: BloggerMediaContentUiEntity): Boolean {
    if (this.getItemViewType != newItem.getItemViewType) return false
    return when (newItem) {
        is BloggerMediaContentUiEntity.BloggerVideoContentUiEntity -> {
            val thatEntity = this as? BloggerMediaContentUiEntity.BloggerVideoContentUiEntity
            val newEntity = newItem as? BloggerMediaContentUiEntity.BloggerVideoContentUiEntity
            thatEntity == newEntity
        }
        is BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity -> {
            val thatEntity = this as? BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity
            val newEntity = newItem as? BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity
            thatEntity == newEntity
        }
        is BloggerMediaContentUiEntity.BloggerImageContentUiEntity -> {
            val thatEntity = this as? BloggerMediaContentUiEntity.BloggerImageContentUiEntity
            val newEntity = newItem as? BloggerMediaContentUiEntity.BloggerImageContentUiEntity
            thatEntity == newEntity
        }
    }
}

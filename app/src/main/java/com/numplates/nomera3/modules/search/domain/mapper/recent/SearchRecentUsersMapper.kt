package com.numplates.nomera3.modules.search.domain.mapper.recent

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.search.data.entity.RecentUserEntityResponse
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel

class SearchRecentUsersMapper : Mapper<List<RecentUserEntityResponse>, SearchItem.RecentBlock?> {

    fun mapForPeoples(entity: List<RecentUserEntityResponse>): List<RecentUserUiModel> {
        return entity.map { recentUser ->
            RecentUserUiModel(
                uid = recentUser.data?.userId ?: 0,
                image = recentUser.data?.avatarSmall ?: String.empty(),
                name = recentUser.data?.name ?: String.empty(),
                gender = 0,
                accountType = createAccountTypeEnum(recentUser.data?.accountType),
                accountColor = recentUser.data?.accountColor ?: 0,
                approved = recentUser.data?.approved.toBoolean(),
                topContentMaker = recentUser.data?.topContentMaker.toBoolean(),
                hasMoments = recentUser.data?.moments?.hasMoments.toBoolean(),
                hasNewMoments = recentUser.data?.moments?.hasNewMoments.toBoolean()
            )
        }
    }

    override fun map(entity: List<RecentUserEntityResponse>): SearchItem.RecentBlock? {
        val items = entity.map { user -> userToRecentItem(user) }

        return if (items.isNullOrEmpty().not()) {
            SearchItem.RecentBlock(items)
        } else {
            null
        }
    }

    private fun userToRecentItem(user: RecentUserEntityResponse): SearchItem.RecentBlock.RecentBaseItem.RecentUser {
        val recentUser = user.data

        return SearchItem.RecentBlock.RecentBaseItem.RecentUser(
            uid = recentUser?.userId ?: 0,
            image = recentUser?.avatarSmall ?: String.empty(),
            name = recentUser?.name ?: String.empty(),
            gender = 0,
            accountType = createAccountTypeEnum(recentUser?.accountType),
            accountColor = recentUser?.accountColor ?: 0,
            approved = recentUser?.approved.toBoolean(),
            topContentMaker = recentUser?.topContentMaker.toBoolean(),
            hasMoments = recentUser?.moments?.hasMoments.toBoolean(),
            hasNewMoments = recentUser?.moments?.hasNewMoments.toBoolean()
        )
    }
}

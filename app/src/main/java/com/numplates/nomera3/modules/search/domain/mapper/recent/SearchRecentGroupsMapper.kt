package com.numplates.nomera3.modules.search.domain.mapper.recent

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.data.entity.RecentGroupEntityResponse
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.meera.core.extensions.empty

class SearchRecentGroupsMapper : Mapper<List<RecentGroupEntityResponse>, SearchItem.RecentBlock?> {

    override fun map(entity: List<RecentGroupEntityResponse>): SearchItem.RecentBlock? {
        val items = entity.map { user -> groupToRecentItem(user) }

        return if (items.isNullOrEmpty().not()) {
            SearchItem.RecentBlock(items)
        } else {
            null
        }
    }

    private fun groupToRecentItem(group: RecentGroupEntityResponse): SearchItem.RecentBlock.RecentBaseItem.RecentGroup {
        val recentGroup = group.data

        return SearchItem.RecentBlock.RecentBaseItem.RecentGroup(
            recentGroup?.id ?: 0,
            recentGroup?.avatar ?: String.empty(),
            recentGroup?.name ?: String.empty()
        )
    }
}

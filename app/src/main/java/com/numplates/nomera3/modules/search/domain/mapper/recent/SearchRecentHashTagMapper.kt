package com.numplates.nomera3.modules.search.domain.mapper.recent

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.data.entity.RecentHashtagEntityResponse
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

private const val MAX_HASHTAG_RECENT_ITEMS = 3

class SearchRecentHashTagMapper(private val title: SearchItem.Title) :
    Mapper<List<RecentHashtagEntityResponse>, List<SearchItem>> {

    override fun map(entity: List<RecentHashtagEntityResponse>): List<SearchItem> {
        val hashTagItems = entity
            .map { domainHashTag -> hashTagToRecyclerItem(domainHashTag) } as List<SearchItem>

        return if (hashTagItems.isNotEmpty()) {
            val cutTo = (hashTagItems.size).coerceAtMost(MAX_HASHTAG_RECENT_ITEMS)

            listOf(title, *hashTagItems.subList(0, cutTo).toTypedArray())
        } else {
            emptyList()
        }
    }

    private fun hashTagToRecyclerItem(domainHashTag: RecentHashtagEntityResponse): SearchItem.HashTag {
        return SearchItem.HashTag(
            name = domainHashTag.data?.text ?: "",
            count = domainHashTag.data?.count
        )
    }
}
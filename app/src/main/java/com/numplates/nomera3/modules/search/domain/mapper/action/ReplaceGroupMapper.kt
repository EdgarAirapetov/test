package com.numplates.nomera3.modules.search.domain.mapper.action

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState

class ReplaceGroupMapper(
    val currentData: SearchResultViewState.Data
) : Mapper<SearchItem.Group, SearchResultViewState.Data> {

    override fun map(entity: SearchItem.Group): SearchResultViewState.Data {
        val resultListData = currentData.value.map { listItem ->
            if ((listItem as? SearchItem.Group)?.groupId == entity.groupId) {
                entity
            } else {
                listItem
            }
        }

        return SearchResultViewState.Data(resultListData)
    }
}
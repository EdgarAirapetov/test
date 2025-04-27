package com.numplates.nomera3.modules.search.domain.mapper.action

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState

class ReplaceUserMapper(
    val currentData: SearchResultViewState.Data
) : Mapper<SearchItem.User, SearchResultViewState.Data> {

    override fun map(entity: SearchItem.User): SearchResultViewState.Data {
        val resultListData = currentData.value.map { listItem ->
            if ((listItem as? SearchItem.User)?.uid == entity.uid) {
                entity
            } else {
                listItem
            }
        }

        return SearchResultViewState.Data(resultListData)
    }
}
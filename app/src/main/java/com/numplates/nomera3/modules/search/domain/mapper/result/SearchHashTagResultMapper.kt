package com.numplates.nomera3.modules.search.domain.mapper.result

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.tags.data.entity.HashtagModel

class SearchHashTagResultMapper :
    Mapper<HashtagModel, SearchItem> {

    override fun map(entity: HashtagModel): SearchItem {
        return SearchItem.HashTag(
            name = entity.text,
            count = entity.count.toInt()
        )
    }
}
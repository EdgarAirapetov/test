package com.numplates.nomera3.modules.search.domain.mapper.result

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.data.entity.GroupEntityResponse
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.meera.core.extensions.empty

private const val TYPE_NOT_APPROVED = 2

class SearchGroupResultMapper :
    Mapper<GroupEntityResponse, SearchItem> {

    override fun map(entity: GroupEntityResponse): SearchItem {
        val status =
            if (entity.isSubscribed == 0 && entity.userStatus != TYPE_NOT_APPROVED) {
                SearchItem.Group.ButtonState.Show
            } else {
                SearchItem.Group.ButtonState.Hide
            }

        return SearchItem.Group(
            groupId = entity.groupId ?: 0,
            name = entity.name ?: String.empty(),
            image = entity.avatar,
            participantCount = entity.users ?: 0,
            isClosedGroup = entity.private == 1,
            buttonState = status
        )
    }
}
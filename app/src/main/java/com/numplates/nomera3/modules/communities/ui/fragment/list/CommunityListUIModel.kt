package com.numplates.nomera3.modules.communities.ui.fragment.list

import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel

sealed class CommunityListUIModel {
    data class CommunityListTitle(var title: String?): CommunityListUIModel()
    data class Community(val community: CommunityListItemUIModel): CommunityListUIModel()
}

package com.numplates.nomera3.modules.communities.ui.viewmodel.list

import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel

sealed class CommunityListEvent {
    class CommunityListLoaded(
        val isNewList: Boolean?,
        val totalCount: Int?,
        val uiModelList: List<CommunityListItemUIModel>
    ) : CommunityListEvent()

    class CommunityListLoadingProgress(val inProgress: Boolean) : CommunityListEvent()

    class CommunityChanges(val communityListEvents: CommunityListEvents) : CommunityListEvent()

    object CommunityListLoadingFailed : CommunityListEvent()

    object CommunityDeletionFailed : CommunityListEvent()
    object CommunityDeletionSuccess : CommunityListEvent()
}

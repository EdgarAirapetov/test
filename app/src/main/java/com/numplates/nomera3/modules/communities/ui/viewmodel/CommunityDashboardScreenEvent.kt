package com.numplates.nomera3.modules.communities.ui.viewmodel

import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel

sealed class CommunityDashboardScreenEvent {
    class CommunityInfoLoadingStart : CommunityDashboardScreenEvent()
    class CommunityInfoLoadingFailed : CommunityDashboardScreenEvent()
    class CommunityInfoLoadingSuccess(val uiModel: CommunityInformationScreenUIModel?) : CommunityDashboardScreenEvent()

    class CommunityDeletionStart : CommunityDashboardScreenEvent()
    class CommunityDeletionFailed : CommunityDashboardScreenEvent()
    class CommunityDeletionSuccess : CommunityDashboardScreenEvent()
}
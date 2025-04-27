package com.numplates.nomera3.modules.feedviewcontent.presentation.viewstates

import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentGroupUiModel

sealed class ViewContentState {

    data class ContentDataReceived(val contentGroup: ContentGroupUiModel) : ViewContentState()

    data class PostSubscriptionUpdated(val contentGroup: ContentGroupUiModel) : ViewContentState()
}

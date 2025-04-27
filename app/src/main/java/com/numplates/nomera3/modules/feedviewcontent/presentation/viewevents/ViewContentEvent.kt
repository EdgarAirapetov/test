package com.numplates.nomera3.modules.feedviewcontent.presentation.viewevents

import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

sealed class ViewContentEvent {

    data class GetContent(val post: PostUIEntity?) : ViewContentEvent()

    data class SubscribePost(val postId: Long?) : ViewContentEvent()

    data class UnsubscribePost(val postId: Long?) : ViewContentEvent()

    data class AddPostComplaint(val postId: Long?) : ViewContentEvent()

    data class SendAnalytic(
        val post: PostUIEntity?,
        val where: AmplitudePropertyWhere,
        val actionType: AmplitudePropertyMenuAction? = null
    ) : ViewContentEvent()
}

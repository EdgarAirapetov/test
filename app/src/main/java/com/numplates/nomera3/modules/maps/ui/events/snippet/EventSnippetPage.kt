package com.numplates.nomera3.modules.maps.ui.events.snippet

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage

interface EventSnippetPage : MapSnippetPage {
    fun updateEventSnippetPageContent(postUIEntity: PostUIEntity)
    fun onEventPostUpdated(postUIEntity: PostUIEntity)
    fun onUserDeletedOwnPost()
    fun onBackFromEventClicked() = Unit
}

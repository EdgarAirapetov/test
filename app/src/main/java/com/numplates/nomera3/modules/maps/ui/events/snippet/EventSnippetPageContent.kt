package com.numplates.nomera3.modules.maps.ui.events.snippet

import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPageContent

interface EventSnippetPageContent : MapSnippetPageContent {
    fun onEventPostUpdated(postUIEntity: PostUIEntity)
    fun onSnippetStateChanged(isCollapsed: Boolean) = Unit
}

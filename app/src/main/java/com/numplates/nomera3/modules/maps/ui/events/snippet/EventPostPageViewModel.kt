package com.numplates.nomera3.modules.maps.ui.events.snippet

import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.maps.ui.MapParametersCache
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetItem
import javax.inject.Inject

class EventPostPageViewModel @Inject constructor(
    private val mapParametersCache: MapParametersCache
) : ViewModel() {

    fun getEventPostItem(postId: Long): EventSnippetItem.EventPostItem? = mapParametersCache.getEventPostItem(postId)
}

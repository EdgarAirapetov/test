package com.numplates.nomera3.modules.search.ui.entity.event

import com.numplates.nomera3.modules.search.ui.entity.SearchItem

sealed class HashTagSearchViewEvent : SearchBaseViewEvent() {
    data class OpenHashTag(val item: SearchItem.HashTag) : HashTagSearchViewEvent()
}
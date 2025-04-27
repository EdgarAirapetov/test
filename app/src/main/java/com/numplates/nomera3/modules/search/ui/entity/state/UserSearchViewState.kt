package com.numplates.nomera3.modules.search.ui.entity.state

import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity

data class UserSearchViewState(
    val contentList: List<PeoplesContentUiEntity>?,
    val isRefreshing: Boolean? = false,
    val showProgressBar: Boolean? = false,
    val showPlaceholder: Boolean? = false
)

package com.numplates.nomera3.modules.reactionStatistics.ui.entity

data class ReactionTabUiEntity(val reactions: List<String> = listOf(), val count: Int = 0, val isViewersTab: Boolean = false)

data class ReactionTabsUiEntity(val items: List<ReactionTabUiEntity> = listOf(), val selectedTab: Int? = null)

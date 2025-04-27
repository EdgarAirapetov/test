package com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers

data class ViewersRootModel(
    val count: Long,
    val more: Int,
    val viewers: List<ViewerModel>)

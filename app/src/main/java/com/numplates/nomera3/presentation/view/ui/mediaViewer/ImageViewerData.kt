package com.numplates.nomera3.presentation.view.ui.mediaViewer

import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter

data class ImageViewerData(
        val imageUrl: String?,
        val description: String? = null,
        val photoID: Long? = null,
        var viewType: Int = RecyclingPagerAdapter.VIEW_TYPE_IMAGE
){
    var isSelected = false
    var cnt: Int = 0 // порядковый номер медиа файла
}



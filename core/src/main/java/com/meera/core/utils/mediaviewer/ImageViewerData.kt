package com.meera.core.utils.mediaviewer

import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter
import com.meera.core.utils.tedbottompicker.models.MediaUriModel

data class ImageViewerData(
    val mediaUriModel: MediaUriModel,
    val description: String? = null,
    val photoID: Long? = null,
    var viewType: Int = RecyclingPagerAdapter.VIEW_TYPE_IMAGE
){
    var isSelected = false
    var cnt: Int = 0 // порядковый номер медиа файла

    fun getInitialStringUri() = mediaUriModel.initialUri.toString()

    fun getActualStringUri() = (mediaUriModel.editedUri?: mediaUriModel.initialUri).toString()
}

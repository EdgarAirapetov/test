package com.meera.core.utils.mediaviewer

interface MediaViewerViewCallback {

    fun onClickDotsMenu(result: MediaViewerViewDotsClickResult)

    interface MediaViewerViewDotsClickResult {
        fun onClickSaveImage()
        fun onClickDeleteImage()
        fun isShowDeleteMenuItem(): Boolean
    }
}

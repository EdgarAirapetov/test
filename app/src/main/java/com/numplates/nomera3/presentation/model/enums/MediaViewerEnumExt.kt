package com.numplates.nomera3.presentation.model.enums

sealed class MediaViewerEnumExt {
    object Common: MediaViewerEnumExt()
    object Gallery: MediaViewerEnumExt()
    object Avatar: MediaViewerEnumExt()
    class Chat(val roomId: Long?): MediaViewerEnumExt()
    object Post: MediaViewerEnumExt()
    object Profile: MediaViewerEnumExt()
    object VideoPost: MediaViewerEnumExt()
}

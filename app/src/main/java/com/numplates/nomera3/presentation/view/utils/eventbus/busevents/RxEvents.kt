package com.numplates.nomera3.presentation.view.utils.eventbus.busevents

sealed class BusEvents {

    class UploadProgress(
            val total: Int,
            val progress: Int
    ): BusEvents()

}

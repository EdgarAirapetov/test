package com.numplates.nomera3.modules.gifservice.ui

import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType

interface GifSearchMenuView {

    fun dismiss()

    fun dismiss(type: TagType)

    fun onBackPressed(): Boolean?
}

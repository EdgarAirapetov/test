package com.numplates.nomera3.modules.uploadpost.ui

import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel

interface AttachmentPostActions {
    fun onItemClicked(model: UIAttachmentPostModel)
    fun onItemEditClick(model: UIAttachmentPostModel)
    fun onAddStickerClick(model: UIAttachmentPostModel) = Unit
    fun onItemCloseClick(model: UIAttachmentPostModel)
}

interface AttachmentMediaActions {
    fun onItemClicked(uiMediaModel: UIAttachmentMediaModel)
    fun onItemEditClick(uiMediaPosition: Int)
    fun onItemCloseClick(uiMediaPosition: Int)
    fun onAddStickerClick(uiMediaPosition: Int) = Unit
    fun onItemPositionChange(uiMediaModel: UIAttachmentMediaModel, x: Double, y: Double)
}


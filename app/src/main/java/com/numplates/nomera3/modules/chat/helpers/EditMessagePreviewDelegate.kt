package com.numplates.nomera3.modules.chat.helpers

import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.meera.core.extensions.clearText
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.databinding.LayoutChatEditPreviewBinding
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum

class EditMessagePreviewDelegate(private val binding: LayoutChatEditPreviewBinding) {

    fun showPreview(message: MessageEntity, onPreviewClicked: () -> Unit, onCancelled: () -> Unit) {
        setPreviewImage(message)
        setContentText(message)
        binding.apply {
            root.visible()
            root.setOnClickListener {
                onPreviewClicked.invoke()
            }
            ivCancelEdit.setOnClickListener {
                root.gone()
                onCancelled.invoke()
            }
        }
    }

    fun clearAndHide() {
        binding.apply {
            tvEditMessage.clearText()
            ivReplyImage.glideClear()
            ivCancelEdit.setOnClickListener(null)
            root.setOnClickListener(null)
            root.gone()
        }
    }

    private fun setContentText(message: MessageEntity) {
        binding.tvEditMessage.text = message.tagSpan?.text?.takeIf { it.isNotBlank() }
            ?: message.content.takeIf { it.isNotBlank() } ?: message.getFallbackText()
    }

    private fun setPreviewImage(message: MessageEntity) {
        binding.vgImageContainer.visible()
        if (message.eventCode == ChatEventEnum.REPOST.state) {
            binding.ivReplyImage.loadGlide(R.drawable.ic_repost_list_dark)
        } else {
            val preview = message.getMediaPreviewUri()
            binding.vgImageContainer.isVisible = preview != null
            if (preview != null) binding.ivReplyImage.loadGlide(preview)
        }
    }

    private fun MessageEntity.getFallbackText(): String? {
        return when (eventCode) {
            ChatEventEnum.VIDEO.state -> getString(R.string.chat_edit_preview_video)
            ChatEventEnum.IMAGE.state -> getString(R.string.chat_edit_preview_photo)
            ChatEventEnum.REPOST.state -> getString(R.string.chat_edit_preview_repost)
            ChatEventEnum.TEXT.state -> getSeveralMediaString(message = this)
            else -> null
        }
    }

    private fun getSeveralMediaString(message: MessageEntity): String? {
        return if (message.attachments.isNotEmpty()) {
            getContext().getString(R.string.chat_edit_preview_photos, message.attachments.size)
        } else {
            null
        }
    }

    private fun getString(@StringRes stringId: Int) =
        getContext().getString(stringId)

    private fun MessageEntity.getMediaPreviewUri(): String? =
        attachment.getAttachmentPreviewUri() ?: attachments.firstOrNull()?.getAttachmentPreviewUri()

    private fun MessageAttachment.getAttachmentPreviewUri(): String? {
        return when (type) {
            TYPING_TYPE_IMAGE -> url
            TYPING_TYPE_VIDEO -> metadata["preview"] as? String
            else -> null
        }
    }

    private fun getContext() = binding.root.context
}

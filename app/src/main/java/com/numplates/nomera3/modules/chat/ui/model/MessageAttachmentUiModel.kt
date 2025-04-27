package com.numplates.nomera3.modules.chat.ui.model

import com.google.gson.internal.LinkedTreeMap
import com.numplates.nomera3.modules.chat.ChatPayloadKeys

data class MessageAttachmentsUiModel(
    val isMultiple: Boolean = false,
    val attachments: List<AttachmentUiModel>?
)

data class AttachmentUiModel(
    val id: Long? = null,
    val favoriteId: Int? = null,
    val url: String? = null,
    val lottieUrl: String? = null,
    val webpUrl: String? = null,
    val type: String,
    val isShowImageBlur: Boolean? = false,
    val metadata: HashMap<String, Any> = hashMapOf()
) {
    val repost: LinkedTreeMap<String, Any>?
        get() = metadata[ChatPayloadKeys.ATTACHMENT_METADATA_POST.key] as? LinkedTreeMap<String, Any>

    val moment: LinkedTreeMap<String, Any>?
        get() = metadata[ChatPayloadKeys.ATTACHMENT_METADATA_MOMENT.key] as? LinkedTreeMap<String, Any>

    val ratio: Double?
        get() = metadata[ChatPayloadKeys.ATTACHMENT_METADATA_RATIO.key] as? Double

    val waveForm: List<Int>
        get() = metadata[ChatPayloadKeys.WAVE_FORM.key] as? List<Int> ?: listOf()

    val duration: Double
        get() = metadata[ChatPayloadKeys.ATTACHMENT_METADATA_DURATION.key] as? Double ?: 0.0

    val audioRecognizedText: String
        get() = metadata[ChatPayloadKeys.VOICE_RECOGNIZED_TEXT.key] as? String ?: ""

    val customTitle: String
        get() = metadata[ChatPayloadKeys.CUSTOM_TITLE.key] as? String ?: ""
}

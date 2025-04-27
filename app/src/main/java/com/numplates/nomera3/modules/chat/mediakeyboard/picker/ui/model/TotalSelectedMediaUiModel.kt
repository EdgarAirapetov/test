package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.model

data class TotalSelectedMediaUiModel(
    val mediaFromMessage: Set<String>,
    val mediaFromPicker: Set<String>
) {

    fun totalMedias(): Set<String> {
        val total = mutableSetOf<String>()
        total.addAll(mediaFromMessage)
        total.addAll(mediaFromPicker)
        return total
    }
}

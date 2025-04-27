package com.numplates.nomera3.modules.appDialogs

data class DialogEntity(
    var type: DialogType
) {
    var state: DialogState = DialogState.NOT_SHOWN
}
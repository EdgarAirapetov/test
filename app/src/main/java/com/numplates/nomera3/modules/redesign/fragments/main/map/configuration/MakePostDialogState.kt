package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

sealed class MakePostDialogState {
    object Show : MakePostDialogState()
    object Hide : MakePostDialogState()
}

package com.numplates.nomera3.presentation.model.enums

//Enum для выбора типа дороги (при отправки поста можем выбрать тип дороги)
enum class RoadSelectionEnum(var state: Int) {
    MAIN(0), MY(1)
}

fun getRoadTypeForState(state: Int?) =
    when (state) {
        0 -> RoadSelectionEnum.MAIN
        1 -> RoadSelectionEnum.MY
        else -> RoadSelectionEnum.MAIN
    }

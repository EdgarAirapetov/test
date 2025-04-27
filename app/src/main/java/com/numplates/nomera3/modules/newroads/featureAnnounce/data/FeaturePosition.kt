package com.numplates.nomera3.modules.newroads.featureAnnounce.data

enum class FeaturePosition(val position: Int) {
    POSITION5(5),
    POSITION25(25),
    POSITION50(50),
    POSITION75(75),
    CALCUlATED_AT3(3),
    CALCUlATED_AT4(4),
    CALCUlATED_AT7(7),
    CALCUlATED_AT11(11),
    NONE(0);

    companion object {

        fun calculateNextPosition(pos: Int): FeaturePosition {
            return when(pos) {
                POSITION5.position ->  CALCUlATED_AT4
                POSITION25.position -> CALCUlATED_AT3
                POSITION50.position -> CALCUlATED_AT7
                POSITION75.position -> CALCUlATED_AT11
                else -> NONE
            }
        }

    }
}
package com.numplates.nomera3.modules.featuretoggles

class TimeOfDayReactionsFeatureToggle {
    var localValue: Boolean? = null
    var remoteValue: Boolean = false
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

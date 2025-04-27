package com.numplates.nomera3.modules.featuretoggles

class HiddenAgeAndSexFeatureToggle {
    var localValue: Boolean? = null
    var remoteValue: Boolean = true
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

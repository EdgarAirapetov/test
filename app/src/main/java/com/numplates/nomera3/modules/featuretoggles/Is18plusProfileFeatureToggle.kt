package com.numplates.nomera3.modules.featuretoggles

class Is18plusProfileFeatureToggle {
    var localValue: Boolean? = null
    var remoteValue: Boolean = false
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

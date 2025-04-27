package com.numplates.nomera3.modules.featuretoggles

class PostMediaPositioningFeatureToggle {
    var localValue: Boolean? = null
    var remoteValue: Boolean = false
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

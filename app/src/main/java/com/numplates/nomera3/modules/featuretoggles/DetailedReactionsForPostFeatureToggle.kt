package com.numplates.nomera3.modules.featuretoggles

class DetailedReactionsForPostFeatureToggle {
    var localValue: Boolean? = null
    var remoteValue: Boolean = false
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

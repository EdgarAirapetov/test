package com.numplates.nomera3.modules.featuretoggles

class AvatarCarouselFeatureToggle {
    var localValue: Boolean? = true
    var remoteValue: Boolean = true
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

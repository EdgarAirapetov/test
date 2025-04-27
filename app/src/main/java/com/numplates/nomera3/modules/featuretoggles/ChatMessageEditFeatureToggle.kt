package com.numplates.nomera3.modules.featuretoggles

/** LocalValue - forcibly enables chat message edit feature. Can be changed locally.
 *  RemoteValue - value set from Firebase Remote Config.
 */
class ChatMessageEditFeatureToggle {
    var localValue: Boolean? = null
    var remoteValue: Boolean = false
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

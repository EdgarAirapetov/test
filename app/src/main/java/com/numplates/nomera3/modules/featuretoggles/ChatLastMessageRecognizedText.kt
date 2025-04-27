package com.numplates.nomera3.modules.featuretoggles

/** LocalValue - forcibly enables group chat complaints feature. Can be changed locally.
 *  RemoteValue - value set from Firebase Remote Config.
 */
class ChatLastMessageRecognizedText {
    var localValue: Boolean? = null
    var remoteValue: Boolean = false
    val isEnabled: Boolean
        get() = localValue ?: remoteValue
}

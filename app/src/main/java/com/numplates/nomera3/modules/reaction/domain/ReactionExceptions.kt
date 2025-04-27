package com.numplates.nomera3.modules.reaction.domain

class AlreadyDeletedException(override val message: String) : IllegalStateException(message) {
    companion object {
        const val CODE = 4920
    }
}

class MomentDeletedException(override val message: String) : IllegalStateException() {
    companion object {
        const val CODE = 1
    }
}

package com.numplates.nomera3.modules.auth.data

sealed class SendCodeErrors {

    object SendCodeFail: SendCodeErrors()

    class UserNotFound(val reason: String?): SendCodeErrors()

    class UserIsBlockedWithoutHideContent(
            val reason: String?,
            val blockExpired: Long?
    ): SendCodeErrors()

    class UserIsBlockedWithHideContent(val reason: String?): SendCodeErrors()
}

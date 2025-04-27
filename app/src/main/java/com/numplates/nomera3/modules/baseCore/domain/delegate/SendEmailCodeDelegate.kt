package com.numplates.nomera3.modules.baseCore.domain.delegate

import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.auth.domain.AuthSendCodeEmailParams
import com.numplates.nomera3.modules.auth.domain.AuthSendCodeSendCodeEmailUseCase
import com.numplates.nomera3.modules.baseCore.domain.validator.PhoneEmailValidator
import javax.inject.Inject

class SendEmailCodeDelegate @Inject constructor(
    private val authSendCodeEmailUseCase: AuthSendCodeSendCodeEmailUseCase,
    private val phoneEmailValidator: PhoneEmailValidator
) {

    private var email: String? = null

    fun setEmail(email: String) {
        this.email = email
    }

    fun getEmail() = email

    fun validateEmail(): Boolean {
        val currentEmail = email ?: return false
        return phoneEmailValidator.validateEmail(currentEmail)
    }

    suspend fun sendEmailCode(success: (Boolean, String) -> Unit, fail: (SendCodeErrors) -> Unit) {
        email?.let { email ->
            val params = AuthSendCodeEmailParams(email)
            authSendCodeEmailUseCase.execute(params, { result, _, _ -> success.invoke(result, email) }, fail)
        }
    }

}

package com.numplates.nomera3.modules.baseCore.domain.delegate

import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.auth.domain.AuthSendCodePhoneParams
import com.numplates.nomera3.modules.auth.domain.AuthSendCodeSendCodePhoneUseCase
import com.numplates.nomera3.modules.baseCore.domain.validator.PhoneEmailValidator
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import javax.inject.Inject

class SendPhoneCodeDelegate @Inject constructor(
    private val authSendCodePhoneUseCase: AuthSendCodeSendCodePhoneUseCase,
    private val phoneEmailValidator: PhoneEmailValidator
) {
    private var phone: String? = null
    var currentCountry: RegistrationCountryModel? = null

    fun setPhone(phone: String) {
        val clearPhone = phone.replace(" ", "").replace("-", "")
        this.phone = clearPhone
    }

    fun getPhone() = phone
    fun getCountry() = currentCountry

    fun validatePhone(): Boolean {
        val currentPhone = phone ?: return false
        return phoneEmailValidator.validatePhone(currentPhone, currentCountry)
    }

    fun getCodeTransformed(phone: String): String {
        val clearPhone = phone.replace(" ", "")
            .replace("-", "")
        return if (!clearPhone.startsWith(currentCountry?.code ?: PHONE_CODE)) {
            "${currentCountry?.code ?: PHONE_CODE}$clearPhone"
        } else clearPhone
    }

    suspend fun sendPhoneCode(success: (Boolean, Long?, Long?, String) -> Unit, fail: (SendCodeErrors) -> Unit) {
        phone?.let { phone ->
            val params = AuthSendCodePhoneParams(phone)
            authSendCodePhoneUseCase.execute(
                params,
                { result, timeout, blockTime -> success.invoke(result, timeout, blockTime, phone) },
                fail
            )
        }
    }

    companion object {
        private const val PHONE_CODE = "+7"
    }
}

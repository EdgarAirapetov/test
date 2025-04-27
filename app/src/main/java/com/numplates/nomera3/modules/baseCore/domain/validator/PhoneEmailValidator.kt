package com.numplates.nomera3.modules.baseCore.domain.validator

import android.util.Patterns
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import javax.inject.Inject

class PhoneEmailValidator @Inject constructor() {


    fun validatePhone(phone: String, currentCountry: RegistrationCountryModel?): Boolean {
        return when {
            phone.length < getPhoneLength(currentCountry) -> false
            !phone.startsWith(PHONE_FIRST_SYMBOL) -> false
            else -> true
        }
    }

    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun getPhoneLength(currentCountry: RegistrationCountryModel?): Int {
        val countryCodeLength = currentCountry?.code?.length ?: PHONE_CODE_LENGTH
        val phoneWithoutCountryCodeLength = currentCountry?.mask
            ?.replace("-", "")
            ?.replace(" ", "")
            ?.length ?: return DEFAULT_PHONE_LENGTH
        return countryCodeLength + phoneWithoutCountryCodeLength
    }

    companion object {
        private const val PHONE_FIRST_SYMBOL = "+"
        private const val DEFAULT_PHONE_LENGTH = 12
        private const val PHONE_CODE_LENGTH = 2
    }

}

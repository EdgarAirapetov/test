package com.numplates.nomera3.modules.registration.data

import android.content.Context
import android.telephony.TelephonyManager
import javax.inject.Inject

class RegistrationCountryCodeMapper @Inject constructor(
    private val appContext: Context
) {

    private val COUNTRIES_DICT = mapOf(
        "ru" to "Россия",
        "az" to "Азербайджан",
        "am" to "Армения",
        "by" to "Беларусь",
        "ge" to "Грузия",
        "kz" to "Казахстан",
        "kg" to "Киргизия",
        "md" to "Молдова",
        "tj" to "Таджикистан",
        "tm" to "Туркменистан",
        "uz" to "Узбекистан"
    )

    private val COUNTRIES_DICT_EN = mapOf(
        "ru" to "Russia",
        "az" to "Azerbaijan",
        "am" to "Armenia",
        "by" to "Belarus",
        "ge" to "Georgia",
        "kz" to "Kazakhstan",
        "kg" to "Kyrgyzstan",
        "md" to "Moldova",
        "tj" to "Tajikistan",
        "tm" to "Turkmenistan",
        "uz" to "Uzbekistan"
    )

    fun getCountryCode(): String? {
        val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager?
        return telephonyManager?.networkCountryIso ?: telephonyManager?.simCountryIso
    }

    fun getCountryNameByCode(code: String): String? {
        return COUNTRIES_DICT[code]
    }

    private fun getCountryCodeByName(name: String?): String? {
        return COUNTRIES_DICT.entries.firstOrNull { it.value == name }?.key
    }

    fun getCountryNameEnByCode(code: String?): String? {
        return COUNTRIES_DICT_EN[code]
    }

    fun translateCountryNameRuToEn(name: String?): String? {
        val code = getCountryCodeByName(name)
        return getCountryNameEnByCode(code)
    }

    companion object {
        const val DEFAULT_COUNTRY_NAME = "Россия"
    }

}

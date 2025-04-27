package com.meera.core.network.utils

import android.content.Context
import android.os.Build
import java.util.Locale
import javax.inject.Inject

private const val RU = "ru"

interface LocaleManager {

    fun getCurrentLocaleName(): String

    fun getLocale(): Locale?

    fun isRusLanguage() : Boolean
}

class LocaleManagerImpl @Inject constructor(
    private var context: Context
) : LocaleManager {

    override fun getCurrentLocaleName() = getLocale().let {
        return@let it?.language?.toString() ?: RU
    }

    override fun getLocale() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        context.resources.configuration.locale
    }

    override fun isRusLanguage(): Boolean {
        return getLocale().language.equals(Locale(RU).language)
    }
}

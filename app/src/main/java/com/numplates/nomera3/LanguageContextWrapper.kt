package com.numplates.nomera3

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class LanguageContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        @Suppress("NAME_SHADOWING")
        fun wrap(context: Context, language: String): ContextWrapper {
            var context: Context = context
            val config: Configuration = context.resources.configuration
            val sysLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            if (language != "" && !sysLocale.language.equals(language)) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    context = context.createConfigurationContext(config)
                } else {
                    context.resources.updateConfiguration(config, context.resources.displayMetrics)
                }
            }
            return LanguageContextWrapper(context)
        }

        fun getLocale(context: Context): Locale {
            val config: Configuration = context.resources.configuration
            val sysLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            return sysLocale
        }

        fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }
    }
}

package com.numplates.nomera3.modules.newroads.data.entities

import java.io.Serializable

class FilterSettingsProvider(
    val mainFilterSettings: FilterSettings,
    val customFilterSettings: FilterSettings
) {
    enum class FilterType : Serializable {
        Custom,
        Main;

        companion object {
            const val BUNDLE_KEY = "filter_settings"
        }
    }

    fun get(type: FilterType): FilterSettings {
        return when (type) {
            FilterType.Custom -> customFilterSettings
            FilterType.Main -> mainFilterSettings
        }
    }
}
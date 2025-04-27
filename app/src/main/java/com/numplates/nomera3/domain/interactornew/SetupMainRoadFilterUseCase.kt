package com.numplates.nomera3.domain.interactornew

import com.meera.core.extensions.toBooleanOrNull
import com.meera.core.extensions.toInt
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.AppSettingsValue
import com.numplates.nomera3.modules.appInfo.data.entity.RecSystemType
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudePropertyRecSystemChangeMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudePropertyRecSystemType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudeRecSystemAnalytics
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import javax.inject.Inject

class SetupMainRoadFilterUseCase @Inject constructor(
    private val filterSettingsProvider: FilterSettingsProvider,
    private val appSettings: AppSettings,
    private val amplitudeRecSystemAnalytics: AmplitudeRecSystemAnalytics
) {
    fun invoke(settings: Settings) {
        val filterSettings = filterSettingsProvider.get(FilterSettingsProvider.FilterType.Main)

        val isRecommendedRoadEnabled: Boolean = settings.appInfo.find { item ->
            item.name == FeedViewModel.RECOMMENDED_ROAD_ENABLED_SETTING_NAME
        }?.value?.toBoolean() ?: false

        appSettings.isRecSystemAvailable = isRecommendedRoadEnabled

        if (filterSettings.data.showSort != isRecommendedRoadEnabled) {
            filterSettings.data.showSort = isRecommendedRoadEnabled
            filterSettings.data.isRecommended = null
        }

        val defaultRoadType = settings.appInfo.find { item ->
            item.name == FeedViewModel.DEFAULT_ROAD_TYPE_SETTING_NAME
        }?.value ?: RecSystemType.TIMELINE.value

        val isRecommendedDefault: Boolean = when (defaultRoadType) {
            RecSystemType.RECOMMENDED.value -> true
            else -> false
        }

        filterSettings.data.isRecommendedByDefault = isRecommendedDefault

        var isAutoRecSystemChanged = AppSettingsValue.FALSE

        if (filterSettings.isFilterDefaultState().not()) {
            filterSettings.data.isRecommended = false
        } else {
            if (filterSettings.data.isRecommended == null) {
                filterSettings.data.isRecommended = isRecommendedDefault
                isAutoRecSystemChanged = AppSettingsValue.TRUE

                if (filterSettings.data.isRecommendedByDefault) {
                    amplitudeRecSystemAnalytics.logRecSystemChanged(
                        how = AmplitudePropertyRecSystemChangeMethod.AUTOMATICALLY,
                        type = AmplitudePropertyRecSystemType.REC,
                        userId = appSettings.readUID()
                    )
                }
            }
        }

        if (appSettings.isAutoRecSystemChangedSingle == AppSettingsValue.NOT_SET.value) {
            appSettings.isAutoRecSystemChangedSingle = isAutoRecSystemChanged.value
        }

        val oldChangeValue = appSettings.isAutoRecSystemChanged.toBooleanOrNull()

        if (oldChangeValue != null && oldChangeValue != filterSettings.data.isRecommended) {
            appSettings.isAutoRecSystemChanged = filterSettings.data.isRecommended.toInt()
        }

        filterSettings.commit()
    }
}

package com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.model.MapVisibilitySettingsListType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.model.toAmplitudePropertyMapPrivacyListType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistChangeCountParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistInitParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistLogDataParams
import com.numplates.nomera3.modules.maps.ui.entity.toAmplitudePropertyWhereMapPrivacy
import javax.inject.Inject

@AppScope
class MapVisibilitySettingsAnalyticsRepositoryImpl @Inject constructor(
    private val analyticsInteractor: AnalyticsInteractor
) : MapVisibilitySettingsAnalyticsRepository {

    private var initParams: MapVisibilityBlacklistInitParams? = null
    private var startCount: Int? = null
    private var addCount = 0
    private var removeCount = 0
    private var logged = false

    override fun init(params: MapVisibilityBlacklistInitParams) {
        reset()
        initParams = params
    }

    override fun changeCount(params: MapVisibilityBlacklistChangeCountParams) {
        if (startCount == null) {
            startCount = params.setCount
        }
        addCount += params.addCount
        removeCount += params.removeCount

    }

    override fun log(params: MapVisibilityBlacklistLogDataParams) {
        if (logged) return
        initParams?.let { initParams ->
            if (params.deleteAll) {
                startCount?.let { startCount ->
                    analyticsInteractor.logMapPrivacySettingsDeleteAll(
                        startCount,
                        initParams.listType.toAmplitudePropertyMapPrivacyListType()
                    )
                }
            } else {
                initParams.origin?.let {
                    when (initParams.listType) {
                        MapVisibilitySettingsListType.BLACKLIST -> {
                            analyticsInteractor.logMapPrivacySettingsBlacklist(
                                initParams.origin.toAmplitudePropertyWhereMapPrivacy(),
                                addCount,
                                removeCount
                            )
                        }
                        MapVisibilitySettingsListType.WHITELIST -> {
                            analyticsInteractor.logMapPrivacySettingsWhitelist(
                                initParams.origin.toAmplitudePropertyWhereMapPrivacy(),
                                addCount,
                                removeCount
                            )
                        }
                    }
                }
            }
            logged = true
        }
    }

    private fun reset() {
        initParams = null
        startCount = null
        addCount = 0
        removeCount = 0
        logged = false
    }
}

package com.meera.application_api

import android.content.Context
import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.application_api.media.domain.ShouldForceResizeVideoUseCase
import com.meera.core.navigation.ActivityNavigator
import com.meera.core.network.utils.LocaleManager

interface ApplicationApi {
    fun getMediaControllerMetaDataDelegate(): MediaFileMetaDataDelegate
    fun getMediaCropInfoUseCase(): GetMediaCropInfoUseCase
    fun getCropInfoUseCase(): GetCropInfoUseCase
    fun getShouldForceResizeVideoUseCase(): ShouldForceResizeVideoUseCase
    fun getContext(): Context
    fun getAmplitudeEventDelegate(): AmplitudeEventDelegate
    fun getActivityNavigator(): ActivityNavigator
    fun getLocaleManager(): LocaleManager
}

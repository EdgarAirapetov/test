package com.meera.media_controller_implementation.di

import android.content.Context
import com.meera.application_api.ApplicationApi
import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.application_api.media.domain.ShouldForceResizeVideoUseCase
import com.meera.core.navigation.ActivityNavigator
import dagger.Module
import dagger.Provides

@Module
internal class MediaControllerApplicationApiGateModule {
    @Provides
    internal fun provideGetMediaCropInfoUseCase(applicationApi: ApplicationApi): GetMediaCropInfoUseCase {
        return applicationApi.getMediaCropInfoUseCase()
    }

    @Provides
    internal fun provideGetCropInfoUseCase(applicationApi: ApplicationApi): GetCropInfoUseCase {
        return applicationApi.getCropInfoUseCase()
    }

    @Provides
    internal fun provideShouldForceResizeVideoUseCase(applicationApi: ApplicationApi): ShouldForceResizeVideoUseCase {
        return applicationApi.getShouldForceResizeVideoUseCase()
    }

    @Provides
    internal fun provideMediaFileMetaDataDelegate(applicationApi: ApplicationApi): MediaFileMetaDataDelegate {
        return applicationApi.getMediaControllerMetaDataDelegate()
    }

    @Provides
    internal fun provideContext(applicationApi: ApplicationApi): Context {
        return applicationApi.getContext()
    }

    @Provides
    internal fun getAmplitudeEventDelegate(applicationApi: ApplicationApi): AmplitudeEventDelegate {
        return applicationApi.getAmplitudeEventDelegate()
    }

    @Provides
    internal fun getActivityNavigator(applicationApi: ApplicationApi): ActivityNavigator {
        return applicationApi.getActivityNavigator()
    }
}

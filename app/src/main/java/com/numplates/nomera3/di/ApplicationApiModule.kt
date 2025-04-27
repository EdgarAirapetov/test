package com.numplates.nomera3.di

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.media.domain.GetCropInfoUseCase
import com.meera.application_api.media.domain.GetMediaCropInfoUseCase
import com.meera.application_api.media.domain.ShouldForceResizeVideoUseCase
import com.meera.core.di.scopes.AppScope
import com.meera.core.navigation.ActivityNavigator
import com.meera.core.network.utils.LocaleManager
import com.meera.core.network.utils.LocaleManagerImpl
import com.numplates.nomera3.domain.interactornew.ShouldForceResizeVideoUseCaseImpl
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetCropInfoUseCaseImpl
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetMediaCropInfoUseCaseImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeEventDelegateImpl
import com.numplates.nomera3.presentation.router.ActivityNavigatorImpl
import dagger.Binds
import dagger.Module

@Module
interface ApplicationApiModule {
    @Binds
    fun bindShouldForceResizeVideoUseCase(useCase: ShouldForceResizeVideoUseCaseImpl): ShouldForceResizeVideoUseCase

    @Binds
    fun bindGetMediaCropInfoUseCase(useCase: GetMediaCropInfoUseCaseImpl): GetMediaCropInfoUseCase

    @Binds
    fun bindGetCropInfoUseCase(useCase: GetCropInfoUseCaseImpl): GetCropInfoUseCase

    @Binds
    fun provideLocaleManger(impl: LocaleManagerImpl): LocaleManager

    @Binds
    @AppScope
    fun bindAmplitudeEventDelegate(impl: AmplitudeEventDelegateImpl): AmplitudeEventDelegate

    @Binds
    fun bindActivityNavigator(impl: ActivityNavigatorImpl): ActivityNavigator
}

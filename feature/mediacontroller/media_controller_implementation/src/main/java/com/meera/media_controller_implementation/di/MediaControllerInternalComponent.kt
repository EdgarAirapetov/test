package com.meera.media_controller_implementation.di

import androidx.lifecycle.ViewModelProvider
import com.meera.application_api.ApplicationApi
import com.meera.core.navigation.ActivityNavigator
import com.meera.media_controller_implementation.domain.analytic.MediaControllerAmplitude
import com.meera.media_controller_implementation.presentation.util.MediaControllerNewPostNeedEditUtil
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        MediaControllerViewModelModule::class,
        MediaControllerApplicationApiGateModule::class,
        MediaControllerModule::class
    ]
)
internal interface MediaControllerInternalComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance applicationApi: ApplicationApi,
        ): MediaControllerInternalComponent
    }

    fun getViewModelFactory(): ViewModelProvider.Factory

    fun getMediaControllerAmplitude(): MediaControllerAmplitude

    fun getMediaControllerNewPostNeedEditUtil(): MediaControllerNewPostNeedEditUtil

    fun getActivityNavigator(): ActivityNavigator
}

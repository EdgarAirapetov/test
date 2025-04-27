package com.meera.media_controller_implementation.di

import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.media_controller_implementation.domain.analytic.MediaControllerAmplitude
import com.meera.media_controller_implementation.domain.analytic.MediaControllerAmplitudeImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class MediaControllerModule {
    @Binds
    internal abstract fun bindFileManager(factory: FileUtilsImpl): FileManager

    @Binds
    internal abstract fun bindAmplitude(implementation: MediaControllerAmplitudeImpl): MediaControllerAmplitude
}

package com.meera.media_controller_implementation.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meera.core.di.common.ViewModelFactory
import com.meera.core.di.common.ViewModelKey
import com.meera.media_controller_implementation.presentation.MediaControllerWrapperViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class MediaControllerViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MediaControllerWrapperViewModel::class)
    abstract fun bindsMediaControllerWrapperViewModel(roomsViewModel: MediaControllerWrapperViewModel): ViewModel
}

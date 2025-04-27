package com.meera.referrals.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.meera.core.di.common.ViewModelFactory
import com.meera.core.di.common.ViewModelKey
import com.meera.referrals.ui.ReferralViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ReferralFeatureModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ReferralViewModel::class)
    abstract fun bindReferralViewModel(viewModel: ReferralViewModel): ViewModel


}

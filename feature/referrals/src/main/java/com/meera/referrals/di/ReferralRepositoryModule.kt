package com.meera.referrals.di

import com.meera.referrals.data.repository.ReferralRepositoryImpl
import com.meera.referrals.domain.ReferralRepository
import dagger.Binds
import dagger.Module

@Module
interface ReferralRepositoryModule {

    @Binds
    fun provideReferralRepository(repository: ReferralRepositoryImpl): ReferralRepository

}


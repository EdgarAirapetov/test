package com.meera.referrals.di

import com.meera.referrals.data.api.ReferralApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class ReferralApiModule {

    @Provides
    fun provideReferralApi(retrofit: Retrofit): ReferralApi =
        retrofit.create(ReferralApi::class.java)

}

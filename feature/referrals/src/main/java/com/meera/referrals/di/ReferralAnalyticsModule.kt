package com.meera.referrals.di

import com.meera.analytics.referrals.ReferralAnalyticsImpl
import com.meera.analytics.referrals.ReferralsAnalytics
import dagger.Binds
import dagger.Module

@Module
interface ReferralAnalyticsModule {
    @Binds
    fun bindReferralAmplitudeAnalytics(helper: ReferralAnalyticsImpl): ReferralsAnalytics
}

package com.meera.referrals.di

import com.meera.core.di.CoreComponent
import com.meera.core.di.scopes.FeatureScope
import com.meera.referrals.ui.PopUpGetVipDialogFragment
import com.meera.referrals.ui.ReferralFragment
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        CoreComponent::class
    ],
    modules = [
        ReferralFeatureModule::class,
        ReferralApiModule::class,
        ReferralRepositoryModule::class,
        ReferralAnalyticsModule::class
    ],
)
interface ReferralFeatureComponent {

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): ReferralFeatureComponent
    }

    fun inject(fragment: ReferralFragment)

    fun inject(dialogFragment: PopUpGetVipDialogFragment)

}

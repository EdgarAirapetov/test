package com.numplates.nomera3.modules.registration.di

import com.numplates.nomera3.modules.registration.ui.avatar.RegistrationAvatarViewModel
import com.numplates.nomera3.modules.registration.ui.birthday.RegistrationBirthdayViewModel
import com.numplates.nomera3.modules.registration.ui.gender.RegistrationGenderViewModel
import com.numplates.nomera3.modules.registration.ui.location.RegistrationLocationViewModel
import com.numplates.nomera3.modules.registration.ui.name.RegistrationNameViewModel
import dagger.Subcomponent

@RegistrationScope
@Subcomponent(modules = [RegistrationModule::class])
interface RegistrationComponent {

    fun inject(viewModel: RegistrationNameViewModel)
    fun inject(viewModel: RegistrationBirthdayViewModel)
    fun inject(viewModel: RegistrationGenderViewModel)
    fun inject(viewModel: RegistrationLocationViewModel)
    fun inject(viewModel: RegistrationAvatarViewModel)
}

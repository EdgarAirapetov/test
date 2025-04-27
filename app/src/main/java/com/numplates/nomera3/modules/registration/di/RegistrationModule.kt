package com.numplates.nomera3.modules.registration.di

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.registration.data.RegistrationApi
import com.numplates.nomera3.modules.registration.data.repository.RegistrationAvatarUniqueNameRepository
import com.numplates.nomera3.modules.registration.data.repository.RegistrationAvatarUniqueNameRepositoryImpl
import com.numplates.nomera3.modules.registration.data.repository.RegistrationGenerateUniqueNameRepository
import com.numplates.nomera3.modules.registration.data.repository.RegistrationGenerateUniqueNameRepositoryImpl
import com.numplates.nomera3.modules.registration.data.repository.RegistrationLocationRepository
import com.numplates.nomera3.modules.registration.data.repository.RegistrationLocationRepositoryImpl
import com.numplates.nomera3.modules.registration.data.repository.RegistrationUserDataRepository
import com.numplates.nomera3.modules.registration.data.repository.RegistrationUserDataRepositoryImpl
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class RegistrationModule {

    @RegistrationScope
    @Provides
    fun provideRegistrationUserDataUseCase(repository: ProfileRepository): UserDataUseCase {
        return UserDataUseCase(repository)
    }

    @RegistrationScope
    @Provides
    fun provideRegistrationLocationRepository(api: RegistrationApi): RegistrationLocationRepository =
        RegistrationLocationRepositoryImpl(api)

    @RegistrationScope
    @Provides
    fun registrationApi(retrofit: Retrofit): RegistrationApi =
        retrofit.create(RegistrationApi::class.java)

    @RegistrationScope
    @Provides
    fun provideRegistrationAvatarUniqueNameRepository(
        api: RegistrationApi,
        appSettings: AppSettings
    ): RegistrationAvatarUniqueNameRepository {
        return RegistrationAvatarUniqueNameRepositoryImpl(api, appSettings)
    }

    @RegistrationScope
    @Provides
    fun provideRegistrationUploadUserData(api: RegistrationApi): RegistrationUserDataRepository {
        return RegistrationUserDataRepositoryImpl(api)
    }

    @RegistrationScope
    @Provides
    fun provideGenerateUniqueNameRepository(api: RegistrationApi): RegistrationGenerateUniqueNameRepository {
        return RegistrationGenerateUniqueNameRepositoryImpl(api)
    }
}

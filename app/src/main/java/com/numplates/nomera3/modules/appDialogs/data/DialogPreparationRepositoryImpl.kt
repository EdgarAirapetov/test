package com.numplates.nomera3.modules.appDialogs.data

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.auth.util.isAuthorizedUser
import javax.inject.Inject

class DialogPreparationRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings
): DialogPreparationRepository {

    override fun isOnBoardingReady(): Boolean {
        return appSettings.readNeedOnBoarding()
    }

    override fun isOutCallsReady(): Boolean {
        return appSettings.isAuthorizedUser() &&
                appSettings.isRegistrationCompleted
    }
}

package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

class SetSubscriptionDismissedUseCase @Inject constructor(
    private val appSettings: AppSettings
) {

    suspend operator fun invoke(userId: Long) {
        val oldSet = appSettings.subscriptionDismissedUserIdSet.get() ?: emptySet()
        appSettings.subscriptionDismissedUserIdSet.set(oldSet + userId.toString())
    }

}

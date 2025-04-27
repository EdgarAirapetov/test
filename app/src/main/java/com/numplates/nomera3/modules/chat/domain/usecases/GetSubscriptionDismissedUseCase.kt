package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

class GetSubscriptionDismissedUseCase @Inject constructor(
    private val appSettings: AppSettings
) {

    suspend operator fun invoke(): List<Long> {
        return appSettings.subscriptionDismissedUserIdSet.get()?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }
}

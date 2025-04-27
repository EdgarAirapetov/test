package com.numplates.nomera3.modules.rateus.domain

import com.numplates.nomera3.modules.rateus.data.RateUsRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class IsNeedToGetRateUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val rateUsRepository: RateUsRepository
) {
    fun invoke(): Boolean {
        if (!userRepository.isUserAuthorized()) return false

        val lastRatedTime = rateUsRepository.getLastRatedTime()
        val timeLeft = System.currentTimeMillis() - lastRatedTime

        return if (!rateUsRepository.isRated()) {
            timeLeft > DAY_MILS
        } else {
            timeLeft > MONTH_MILS
        }
    }

    companion object {
        private const val MONTH_MILS = 2592000000L
        private const val DAY_MILS = 86400000L
    }
}

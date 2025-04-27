package com.numplates.nomera3.modules.moments.util

import com.numplates.nomera3.modules.moments.show.domain.MomentItemModel
import java.util.concurrent.TimeUnit

private const val DAY_LENGTH_MS = 86400000
const val MOMENT_LIMIT = 50

class CheckMomentsLimitUtil {

    /**
     * Проверить – приведёт ли публикация новых моментов к перетиранию текущих
     * @param newMomentCount – кол-во новых момент предполагаемых к публикации
     *
     * @return true – сколько осталось моментов
     */
    fun momentsLeft(moments: List<MomentItemModel>?, newMomentCount: Int = 0): Int {
        if (moments.isNullOrEmpty()) {
            return MOMENT_LIMIT
        }

        val currentDayMoments = getCurrentDayMoments(moments)

        return MOMENT_LIMIT - currentDayMoments.size + newMomentCount
    }

    fun getOverLimitMoments(moments: List<MomentItemModel>?): List<MomentItemModel> {
        if (moments.isNullOrEmpty()) {
            return emptyList()
        }

        val currentDayMoments = getCurrentDayMoments(moments)

        return if (currentDayMoments.size > MOMENT_LIMIT) {
            moments.subList(0, currentDayMoments.size - MOMENT_LIMIT)
        } else emptyList()
    }

    private fun getCurrentDayMoments(moments: List<MomentItemModel>): List<MomentItemModel> {
        return moments.filter { moment ->
            val isMomentPublishedToday = TimeUnit.SECONDS.toMillis(moment.createdAt) > System.currentTimeMillis() - DAY_LENGTH_MS

            isMomentPublishedToday
        }
    }
}

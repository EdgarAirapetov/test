package com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeCreatePostWhichButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeProfileStatistics {
    fun onIntroOpened(userId: Long, week: String, year: String)
    fun onVisitorsOpened(
        visitorsCount: Long,
        visitorsGrowth: Long,
        visitorsTrend: AmplitudePropertyProfileStatisticsVisitorsTrendType
    )
    fun onViewsOpened(viewsCount: Long, viewsGrowth: Long, viewsTrend: AmplitudePropertyProfileStatisticsViewsTrendType)
    fun onStatisticsClosed(closeType: AmplitudePropertyProfileStatisticsCloseType, screenNumber: Int)
    fun onCreatePostClick()
}

class AmplitudeProfileStatisticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeProfileStatistics {

    override fun onIntroOpened(
        userId: Long,
        week: String,
        year: String
    ) {
        delegate.logEvent(
            eventName = AmplitudeProfileStatisticsEventName.INTRO,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_USER_ID, userId)
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_WEEK, week)
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_YEAR, year)
                }
            }
        )
    }

    override fun onVisitorsOpened(
        visitorsCount: Long,
        visitorsGrowth: Long,
        visitorsTrend: AmplitudePropertyProfileStatisticsVisitorsTrendType
    ) {
        delegate.logEvent(
            eventName = AmplitudeProfileStatisticsEventName.VISITORS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_VISITORS_COUNT, visitorsCount)
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_VISITOR_GROWTH, visitorsGrowth)
                    addProperty(visitorsTrend)
                }
            }
        )
    }

    override fun onViewsOpened(
        viewsCount: Long,
        viewsGrowth: Long,
        viewsTrend: AmplitudePropertyProfileStatisticsViewsTrendType
    ) {
        delegate.logEvent(
            eventName = AmplitudeProfileStatisticsEventName.VIEWS,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_VIEWS_COUNT, viewsCount)
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_VIEW_GROWTH, viewsGrowth)
                    addProperty(viewsTrend)
                }
            }
        )
    }

    override fun onStatisticsClosed(
        closeType: AmplitudePropertyProfileStatisticsCloseType,
        screenNumber: Int
    ) {
        delegate.logEvent(
            eventName = AmplitudeProfileStatisticsEventName.CLOSE,
            properties = {
                it.apply {
                    addProperty(closeType)
                    addProperty(AmplitudePropertyProfileStatisticsConst.PROPERTY_SCREEN_NUMBER, screenNumber)
                }
            }
        )
    }

    override fun onCreatePostClick() {
        delegate.logEvent(
            eventName = AmplitudeProfileStatisticsEventName.BUTTON_POST_CREATE_TAP,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyWhere.PROFILE_STATISTICS)
                    addProperty(AmplitudeCreatePostWhichButton.BUTTON_IN_STATISTICS)
                }
            }
        )
    }
}

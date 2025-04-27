package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudeProfileStatistics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsCloseType
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.ProfileStatisticsViewState
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.ProfileStatisticsTrend
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.TYPE_VIEWS
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.TYPE_VISITORS
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.mapper.SlidesMapper
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.GetProfileStatisticsSlidesUseCase
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.SetProfileStatisticsAsReadUseCase
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.SetProfileStatisticsParams
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.SetProfileStatisticsSlidesUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.absoluteValue

class ProfileStatisticsContainerViewModel : BaseViewModel() {

    @Inject
    lateinit var getProfileStatisticsSlidesUseCase: GetProfileStatisticsSlidesUseCase

    @Inject
    lateinit var setProfileStatisticsSlidesUseCase: SetProfileStatisticsSlidesUseCase

    @Inject
    lateinit var setProfileStatisticsAsReadUseCase: SetProfileStatisticsAsReadUseCase

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var analytics: AmplitudeProfileStatistics

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var slidesMapper: SlidesMapper

    private val viewStateMutableLiveData = MutableLiveData<ProfileStatisticsViewState>()
    val viewStateLiveData: LiveData<ProfileStatisticsViewState> get() = viewStateMutableLiveData

    init {
        App.component.inject(this)
        getSlides()
        removeProfileNotificationIcon()
        setProfileStatisticsAsRead()
    }

    fun dialogIsBeingClosed(closeType: AmplitudePropertyProfileStatisticsCloseType, screenNumber: Int) {
        analytics.onStatisticsClosed(closeType, screenNumber)
    }

    fun navigatingToCreatingPost() {
        analytics.onCreatePostClick()
    }

    fun pageChanged(position: Int) {
        sendAnalyticsForCurrentSlide(position)
        val viewState = viewStateMutableLiveData.value as? ProfileStatisticsViewState.Data? ?: return
        viewStateMutableLiveData.postValue(viewState.copy(currentSlideIndex = position))
    }

    private fun sendAnalyticsForCurrentSlide(index: Int) {
        val data = viewStateMutableLiveData.value as? ProfileStatisticsViewState.Data? ?: return
        val slides = data.slidesListModel.slides
        val currentSlide = slides.getOrNull(index) ?: return
        when (currentSlide.type) {
            null -> {
                introOpened()
            }
            TYPE_VIEWS -> {
                viewsOpened(
                    currentSlide.count ?: return,
                    currentSlide.growth ?: return,
                    currentSlide.trend
                )
            }
            TYPE_VISITORS -> {
                visitorsOpened(
                    currentSlide.count ?: return,
                    currentSlide.growth ?: return,
                    currentSlide.trend
                )
            }
        }
    }

    private fun introOpened() {
        val userId = appSettings.readUID()
        val calendar = Calendar.getInstance()
        val data = viewStateMutableLiveData.value as? ProfileStatisticsViewState.Data? ?: return
        calendar.timeInMillis = data.slidesListModel.createdAt * 1000
        val week = calendar.get(Calendar.WEEK_OF_YEAR).toString()
        val year = calendar.get(Calendar.YEAR).toString().takeLast(2)
        val weekString = "W$week"
        val yearString = "Y$year"
        analytics.onIntroOpened(userId, weekString, yearString)
    }

    private fun viewsOpened(count: Long, growth: Long, trend: ProfileStatisticsTrend) {
        analytics.onViewsOpened(count, growth.absoluteValue, trend.toViewsAmplitudeProperty())
    }

    private fun visitorsOpened(count: Long, growth: Long, trend: ProfileStatisticsTrend) {
        analytics.onVisitorsOpened(count, growth.absoluteValue, trend.toVisitorsAmplitudeProperty())
    }

    private fun getSlides() {
        val slidesListResponse = getProfileStatisticsSlidesUseCase.execute()
        val slidesListModel = slidesMapper.mapSlidesListModel(slidesListResponse ?: return)
        val viewState = when {
            slidesListModel.slides.isEmpty() -> ProfileStatisticsViewState.Empty
            else -> ProfileStatisticsViewState.Data(slidesListModel)
        }
        viewStateMutableLiveData.postValue(viewState)
    }

    private fun removeProfileNotificationIcon() {
        viewModelScope.launch {
            appSettings.profileNotification.set(false)
        }
    }

    private fun setProfileStatisticsAsRead() {
        setProfileStatisticsSlidesUseCase.execute(SetProfileStatisticsParams(null))
        viewModelScope.launch {
            setProfileStatisticsAsReadUseCase.execute({
                Timber.d("Set profile statistics as read success")
            }, {
                Timber.d("Set profile statistics as read fail")
            })
        }
    }

}

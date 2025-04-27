package com.numplates.nomera3.modules.maps.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.baseCore.domain.exception.ResponseException
import com.numplates.nomera3.modules.maps.data.mapper.MapDataMapper
import com.numplates.nomera3.modules.maps.domain.model.FilterGender
import com.numplates.nomera3.modules.maps.domain.model.FilterOnlyFriends
import com.numplates.nomera3.modules.maps.domain.model.FilterOnlySubscriptions
import com.numplates.nomera3.modules.maps.domain.model.FilterPedestrians
import com.numplates.nomera3.modules.maps.domain.model.FilterUserState
import com.numplates.nomera3.modules.maps.domain.model.FilterVehicleType
import com.numplates.nomera3.modules.maps.domain.model.FilterWithoutFriends
import com.numplates.nomera3.modules.maps.domain.model.GetMapObjectsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.GetUserSnippetsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.MapObjectsModel
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.repository.MapDataRepository
import com.numplates.nomera3.presentation.view.utils.eventbus.busevents.RxEventsJava
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
@AppScope
class MapDataRepositoryImpl @Inject constructor(
    private val apiHiWayKt: ApiHiWayKt,
    private val apiMain: ApiMain,
    private val appSettings: AppSettings,
    private val mapper: MapDataMapper,
) : MapDataRepository {

    private var geoPopupShown = false

    override suspend fun getMapObjects(params: GetMapObjectsParamsModel): MapObjectsModel {
        val mapObjectsResponse = apiHiWayKt
            .getMapObjects(
                params.gpsXMin,
                params.gpsXMax,
                params.gpsYMin,
                params.gpsYMax,
                params.zoom,
                getShowOnlyFriendsValue(),
                getShowWithoutFriendsValue(),
                FilterOnlySubscriptions.DISABLED.value,
                FilterGender.allValue(),
                FilterVehicleType.allValue(),
                FilterUserState.allValue(),
                MIN_AGE,
                MAX_AGE,
                FilterPedestrians.SHOW.value,
                EVENTS_COUNT,
                MAX_USER_COUNT,
                ROAD_EVENTS,
                USER_TYPE
            )
        val mapObject = mapObjectsResponse?.data ?: throw ResponseException(mapObjectsResponse?.err)
        return mapper.mapObjects(mapObject)
    }

    override suspend fun getUserSnippets(params: GetUserSnippetsParamsModel): List<UserSnippetModel> {
        val mapTapeResponse = try {
            apiMain.getUserCards(
                uid = appSettings.readUID(),
                userType = USER_TYPE_MAPTAPE,
                selectedUserId = params.selectedUserId,
                excludedUserIds = params.excludedUserIds.joinToString(","),
                showOnlyFriends = getShowOnlyFriendsValue(),
                showWithoutFriends = getShowWithoutFriendsValue(),
                gender = FilterGender.allValue(),
                lat = params.lat,
                lon = params.lon,
                limit = params.limit
            )
        } catch (httpException: HttpException) {
            handleAndRethrow(httpException)
        }
        return when {
            mapTapeResponse.data != null -> mapper.mapUserSnippets(mapTapeResponse.data)
            else -> throw ResponseException(mapTapeResponse.err)
        }
    }

    override fun setUserSnippetOnboardingShown() {
        appSettings.writeSnippetOnboardingShown()
    }

    override fun needToShowUserSnippetOnboarding(): Boolean {
        return !appSettings.readSnippetOnboardingShown()
    }

    override fun setGeoPopupShown() {
        geoPopupShown = true
        val geoPopupShownCount = appSettings.readGeoPopupShownCount()
        appSettings.writeGeoPopupShownCount(geoPopupShownCount + 1)
    }

    override fun needToShowGeoPopup(): Boolean =
        !geoPopupShown && appSettings.readGeoPopupShownCount() < MAX_TIMES_GEO_POPUP_CAN_BE_SHOWN

    override fun resetGeoPopupShownCount() {
        appSettings.writeGeoPopupShownCount(0)
    }

    private fun getShowOnlyFriendsValue(): Int {
        val showOnlyFriends = appSettings.readShowFriends() && appSettings.readShowPeople().not()
        return if (showOnlyFriends) {
            FilterOnlyFriends.ENABLED.value
        } else {
            FilterOnlyFriends.DISABLED.value
        }
    }

    private fun getShowWithoutFriendsValue(): Int {
        val showWithoutFriends = appSettings.readShowFriends().not() && appSettings.readShowPeople()
        return if (showWithoutFriends) {
            FilterWithoutFriends.ENABLED.value
        } else {
            FilterWithoutFriends.DISABLED.value
        }
    }

    private fun handleAndRethrow(e: Exception): Nothing {
        (e as? HttpException)?.response()?.code()?.let { code ->
            Timber.e("ERROR Response code: $code")
            if (code == AUTH_ERROR_CODE) {
                App.bus.send(RxEventsJava.MustRefreshToken())
            }
        }
        throw e
    }

    companion object {
        private const val EVENTS_COUNT = 0
        private const val ROAD_EVENTS = ""
        private const val USER_TYPE = "UserMap"
        private const val USER_TYPE_MAPTAPE = "UserMapCard"
        private const val MIN_AGE = 14
        private const val MAX_AGE = 99
        private const val MAX_USER_COUNT = 50
        private const val MAX_TIMES_GEO_POPUP_CAN_BE_SHOWN = 2

        private const val AUTH_ERROR_CODE = 401
    }
}

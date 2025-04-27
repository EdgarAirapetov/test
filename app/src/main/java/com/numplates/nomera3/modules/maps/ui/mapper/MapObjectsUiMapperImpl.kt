package com.numplates.nomera3.modules.maps.ui.mapper

import com.google.android.gms.maps.model.LatLng
import com.meera.core.extensions.isTrue
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.maps.domain.model.MapObjectsModel
import com.numplates.nomera3.modules.maps.ui.model.MapClusterUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapObjectsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.model.NearestFriendUiModel
import javax.inject.Inject

class MapObjectsUiMapperImpl @Inject constructor(
    private val holidayInfoHelper: HolidayInfoHelper,
    private val featureToggle: FeatureTogglesContainer
) : MapObjectsUiMapper {
    override fun mapMapObjects(mapObjectsModel: MapObjectsModel): MapObjectsUiModel {
        val users = mapObjectsModel.users.map { mapUserModel ->
            MapUserUiModel(
                id = mapUserModel.id,
                accountType = mapUserModel.accountType,
                avatar = mapUserModel.avatar,
                hatLink = holidayInfoHelper.getHatLink(mapUserModel.accountType),
                gender = mapUserModel.gender,
                accountColor = mapUserModel.accountColor,
                latLng = LatLng(mapUserModel.coordinates.lat, mapUserModel.coordinates.lon),
                name = mapUserModel.name,
                uniqueName = mapUserModel.uniqueName,
                isFriend = mapUserModel.isFriend,
                blacklistedByMe = mapUserModel.blacklistedByMe,
                blacklistedMe = mapUserModel.blacklistedMe,
                hasMoments = mapUserModel.moments?.hasMoments.isTrue() && featureToggle.momentsFeatureToggle.isEnabled,
                hasNewMoments = mapUserModel.moments?.hasNewMoments.isTrue() && featureToggle.momentsFeatureToggle.isEnabled,
                moments = mapUserModel.moments
            )
        }
        val clusters = mapObjectsModel.clusters.map { model ->
            MapClusterUiModel(
                id = model.id,
                latLng = LatLng(model.gpsX, model.gpsY),
                capacity = model.capacity,
                userAvatars = model.users.map { it.avatar ?: "" }
            )
        }
        val nearestFriend = mapObjectsModel.nearestFriend?.let {
            NearestFriendUiModel(
                id = it.id,
                location = LatLng(it.location.lat, it.location.lon)
            )
        }
        return MapObjectsUiModel(
            users = users,
            clusters = clusters,
            nearestFriend = nearestFriend
        )
    }
}

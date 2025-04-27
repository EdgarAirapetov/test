package com.numplates.nomera3.modules.userprofile.ui.mapper

import com.google.android.gms.maps.model.LatLng
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import javax.inject.Inject

class UserDetailsMapper @Inject constructor(
    private val holidayInfoHelper: HolidayInfoHelper
) {
    fun mapUserUiModel(userModel: UserProfileUIModel, latitude: Double, longitude: Double): MapUserUiModel {
        val accountType = createAccountTypeEnum(userModel.accountDetails.accountType)
        return MapUserUiModel(
            id = userModel.userId,
            accountType = accountType,
            avatar = userModel.avatarDetails.avatarSmall,
            hatLink = holidayInfoHelper.getHatLink(accountType),
            gender = Gender.fromValue(userModel.gender),
            accountColor = userModel.accountDetails.accountColor,
            latLng = LatLng(latitude, longitude),
            name = userModel.name,
            uniqueName = userModel.uniquename,
            isFriend = userModel.friendStatus == FriendStatus.FRIEND_STATUS_CONFIRMED,
            blacklistedByMe = userModel.blacklistedByMe,
            blacklistedMe = userModel.blacklistedMe,
            hasMoments = userModel.moments?.hasMoments.isTrue(),
            hasNewMoments = userModel.moments?.hasNewMoments.isTrue(),
            moments = userModel.moments,
            isShowOnMap = userModel.showOnMap.toBoolean()
        )
    }
}

package com.meera.referrals.data.mapper

import com.meera.db.models.userprofile.UserProfileNew
import com.meera.referrals.domain.model.UserProfileModel
import javax.inject.Inject

class UserProfileMapper @Inject constructor() {

    fun mapUserProfile(user: UserProfileNew?): UserProfileModel {
        return UserProfileModel(
            userId = user?.userId ?: 0,
            accountTypeExpiration = user?.accountTypeExpiration ?: -1,
            uniqueName = user?.uniquename.orEmpty()
        )
    }

}

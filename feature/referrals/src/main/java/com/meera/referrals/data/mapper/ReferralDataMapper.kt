package com.meera.referrals.data.mapper

import com.meera.referrals.data.model.ReferralDataDto
import com.meera.referrals.domain.model.ReferralModel
import com.meera.referrals.domain.model.ReferralDataModel
import javax.inject.Inject

class ReferralDataMapper @Inject constructor() {

    fun mapReferralData(data: ReferralDataDto): ReferralDataModel {
        return ReferralDataModel(
            availableVips = data.availableVips,
            code = data.code,
            referrals = ReferralModel(
                count = data.referrals.count,
                limit = data.referrals.limit
            ),
            text = data.text,
            title = data.title
        )
    }

}

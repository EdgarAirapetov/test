package com.meera.referrals.ui.mapper

import com.meera.referrals.domain.model.ReferralDataModel
import com.meera.referrals.ui.model.ReferralDataUIModel
import com.meera.referrals.ui.model.ReferralUIModel
import javax.inject.Inject

class ReferralDataUIMapper @Inject constructor() {

    fun mapReferralData(data: ReferralDataModel): ReferralDataUIModel {
        return ReferralDataUIModel(
            availableVips = data.availableVips,
            code = data.code,
            referrals = ReferralUIModel(
                count = data.referrals.count,
                limit = data.referrals.limit
            ),
            text = data.text,
            title = data.title
        )
    }

}

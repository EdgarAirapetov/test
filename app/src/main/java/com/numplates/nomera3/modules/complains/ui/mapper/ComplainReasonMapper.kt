package com.numplates.nomera3.modules.complains.ui.mapper

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import javax.inject.Inject

class ComplainReasonMapper @Inject constructor() {

    fun mapReasonToModel(reasonId: ComplainReasonId): UserComplainUiModel {
        val titleRes = when (reasonId) {
            ComplainReasonId.SPAM -> R.string.user_complain_spam
            ComplainReasonId.HACKING -> R.string.user_complain_account_hacking
            ComplainReasonId.FRAUD -> R.string.user_complain_fraud
            ComplainReasonId.HOSTILE_SPEECH -> R.string.user_complain_hostile_speech
            ComplainReasonId.NUDES -> R.string.user_complain_nudes
            ComplainReasonId.PROSTITUTION -> R.string.user_complain_prostitution
            ComplainReasonId.ILLEGAL_GOODS -> R.string.user_complain_illegal_goods
            ComplainReasonId.ADVERTISING -> R.string.user_complain_advertising
            ComplainReasonId.OTHER -> R.string.user_complain_other
            else -> -1
        }
        return UserComplainUiModel(titleRes, reasonId)
    }
}

package com.numplates.nomera3.modules.chat.common.utils

import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.ComplainType
import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId

fun getComplaintById(reasonId: Int) = when (reasonId) {
    ComplainReasonId.ABUSE.key -> ComplainType.INSULTS
    ComplainReasonId.VIOLENCE.key -> ComplainType.THREATS
    ComplainReasonId.DANGEROUS_PEOPLE.key -> ComplainType.DANGEROUS_ORG
    ComplainReasonId.ANIMAL_ABUSE.key -> ComplainType.ANIMALS
    ComplainReasonId.NUDES.key -> ComplainType.EIGHTEEN_PLUS
    ComplainReasonId.SPAM.key -> ComplainType.SPAM
    ComplainReasonId.ADVERTISING.key -> ComplainType.ADVERTISING
    ComplainReasonId.FRAUD.key -> ComplainType.FRAUD
    ComplainReasonId.PROSTITUTION.key -> ComplainType.PROSTITUTION
    ComplainReasonId.ILLEGAL_GOODS.key -> ComplainType.UNLEAGAL_ITEMS
    ComplainReasonId.HACKING.key -> ComplainType.HACKING
    else -> null
}

package com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints

import com.numplates.nomera3.modules.user.ui.entity.ComplainReasonId
import javax.inject.Inject

class ComplaintsMapper @Inject constructor() {

    fun mapReasonToType(reason: ComplainReasonId): ComplainType {
        return when (reason) {
            ComplainReasonId.ABUSE -> ComplainType.INSULTS
            ComplainReasonId.VIOLENCE -> ComplainType.THREATS
            ComplainReasonId.DANGEROUS_PEOPLE -> ComplainType.DANGEROUS_ORG
            ComplainReasonId.ANIMAL_ABUSE -> ComplainType.ANIMALS
            ComplainReasonId.NUDES -> ComplainType.EIGHTEEN_PLUS
            ComplainReasonId.SPAM -> ComplainType.SPAM
            ComplainReasonId.ADVERTISING -> ComplainType.ADVERTISING
            ComplainReasonId.FRAUD -> ComplainType.FRAUD
            ComplainReasonId.PROSTITUTION -> ComplainType.PROSTITUTION
            ComplainReasonId.ILLEGAL_GOODS -> ComplainType.UNLEAGAL_ITEMS
            ComplainReasonId.HACKING -> ComplainType.HACKING
            ComplainReasonId.HOSTILE_SPEECH -> ComplainType.HOSTILE_SPEECH
            else -> ComplainType.OTHER
        }
    }
}

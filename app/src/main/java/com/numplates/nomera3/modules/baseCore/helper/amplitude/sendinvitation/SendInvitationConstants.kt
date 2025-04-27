package com.numplates.nomera3.modules.baseCore.helper.amplitude.sendinvitation

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeSendInvitationEvent(
    private val event: String
) : AmplitudeName {
    SEND_INVITATION_TAP("send invitation tap");

    override val eventName: String
        get() = event
}

/**
 * Данный enum озночает, что какое действие сделал user
 * button - Данный определяет, что юзер нажал на кнопку "Отправить приглашение"
 * code - Данный озночает, что юзер скопировал код
 */
enum class AmplitudeSendInvitationType(
    private val property: String
) : AmplitudeProperty {

    BUTTON("button"),
    CODE("code");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WAY
}

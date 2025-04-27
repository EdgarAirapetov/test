package com.meera.analytics.referrals

import com.meera.analytics.amplitude.AmplitudeName
import com.meera.analytics.amplitude.AmplitudeProperty

const val VIP_COLOR = "color"
const val DURATION = "duration"
const val HAVE_VIP_BEFORE = "have vip before" // Отмечаем, приобретался ли VIP статус ранее (любой и на любой срок)
const val TRUE_VALUE = "true"
const val FALSE_VALUE = "false"
const val WAY = "way"


enum class AmplitudeReferralEventName(val event: String): AmplitudeName {
    VIP_BUYING("vip buying");

    override val eventName: String
        get() = event
}

enum class AmplitudeReferralSendInvitationEvent(
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
enum class AmplitudeReferralSendInvitationType(
    private val property: String
) : AmplitudeProperty {

    BUTTON("button"),
    CODE("code");

    override val _value: String
        get() = property

    override val _name: String
        get() = WAY
}

enum class AmplitudePropertyColor(val property: String) : AmplitudeProperty {
    RED("red"),
    GREEN("green"),
    BLUE("blue"),
    PINK("pink"),
    PURPLE("purple"),
    GOLD("gold"),
    NONE("none");

    override val _name: String
        get() = VIP_COLOR

    override val _value: String
        get() = property
}

enum class AmplitudePropertyDuration(val property: String) : AmplitudeProperty {
    WEEK("week"),
    MONTH("month"),
    THREE_MONTH("three month"),
    YEAR("year"),
    NONE("none");

    override val _name: String
        get() = DURATION

    override val _value: String
        get() = property
}

enum class AmplitudePropertyHaveVIPBefore(val property: String) : AmplitudeProperty {
    TRUE(TRUE_VALUE),
    FALSE(FALSE_VALUE);

    override val _name: String
        get() = HAVE_VIP_BEFORE

    override val _value: String
        get() = property
}

/**
 * Данный эвент отправляет информацию получения VIP аккаунта
 */
enum class AmplitudePropertyWay(
    private val property: String
) : AmplitudeProperty {

    BUY("buy"),
    FRIENDS_INVITATION("friends invitation");

    override val _name: String
        get() = WAY

    override val _value: String
        get() = property
}

object AmplitudeReferralPropertyNameConst {
    const val EXPIRATION_DATE = "expiration date"                   // Отмечаем, когда у пользователя закончится VIP статус
}

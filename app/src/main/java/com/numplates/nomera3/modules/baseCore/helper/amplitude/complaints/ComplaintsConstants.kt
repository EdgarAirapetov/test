package com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeComplaintEventName(val event: String) : AmplitudeName {
    PROFILE_REPORT_START("profile report start"),
    PROFILE_REPORT_FINISH("profile report finish"),
    RULES_OPEN("rules open");

    override val eventName: String
        get() = event
}

enum class ComplainType(val property: String) : AmplitudeProperty {
    INSULTS("insults"),  //- оскобления и травля
    THREATS("threats"),  //  - угроза насилия
    DANGEROUS_ORG("dangerous org"), // - опасные организации и люди
    ANIMALS("animals"), // - жестокое обращение с животными
    EIGHTEEN_PLUS("18+"), // - изображения обнаженного тела и материалы сексуального
    SPAM("spam"), // - спам
    ADVERTISING("advertising"),//  - реклама
    FRAUD("fraud"),// - мошенничество
    PROSTITUTION("prostitution"),// - проституция
    UNLEAGAL_ITEMS("unlegal items"),// - продажа незаконных товаров
    HACKING("hacking"), // - продажа незаконных товаров
    HOSTILE_SPEECH("enmity"), // - враждебные высказывания
    OTHER("other"); // - другая причина

    override val _name: String
        get() = AmplitudePropertyNameConst.GENERAL_TYPE

    override val _value: String
        get() = property
}

enum class RulesOpenWhere(val property: String) : AmplitudeProperty {
    ONBOARDING_EVENTS("onboarding events"),
    REPORT("report"),
    SETTINGS("settings"),
    OTHER("other");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}

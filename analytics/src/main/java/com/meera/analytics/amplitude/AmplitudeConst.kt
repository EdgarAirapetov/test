package com.meera.analytics.amplitude

interface AmplitudeName {
    val eventName: String
}

interface AmplitudeProperty {
    val _value: String
    val _name: String
}

object AmplitudePropertyNameConst {
    const val USER_ID = "user id"
}

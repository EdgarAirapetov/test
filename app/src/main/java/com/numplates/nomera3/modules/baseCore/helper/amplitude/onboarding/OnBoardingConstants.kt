package com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeOnBoardingEventName(
    private val event: String
) : AmplitudeName {
    JOIN("onb1 join"),
    FEED("onb2 feed"),
    MAP("onb3 map"),
    COMMUNICATION("onb4 communication"),
    JOIN_PHONE("onb5 join"),
    WELCOME("welcome"),
    CONTINUE("second onboarding tap");

    override val eventName: String
        get() = event
}

/**
 * "Отмечаем, какое действие совершил пользователь
 * enter - нажал на кнопку ""Войти/зарегестрироваться""
 * close - нажал на кнопку ""Крестик""
 * close swipe - закрыл свайпом вниз
 * after - нажал на кнопку ""Позже""
 * swipe - перешел на следующий экран свайпом в сторону"
 * next - переход на следующий слайд
 * */
enum class AmplitudePropertyOnBoardingActionType(val property: String) : AmplitudeProperty {
    ENTER("enter"),
    CLOSE("close"),
    CLOSE_SWIPE("close swipe"),
    AFTER("after"),
    SWIPE("swipe"),
    NEXT("next");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

object AmplitudePropertyOnBoardingConst {
    const val CONTINUE_CLICKED = "1th"
}

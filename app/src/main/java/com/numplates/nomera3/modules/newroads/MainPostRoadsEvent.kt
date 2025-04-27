package com.numplates.nomera3.modules.newroads

sealed class MainPostRoadsEvent {
    object CloseOnBoarding : MainPostRoadsEvent()
    object OnBoardingCollapsed: MainPostRoadsEvent()
    object ShowOnBoardingWelcome: MainPostRoadsEvent()
    object CheckHolidays : MainPostRoadsEvent()
    class ShowBirthdayDialog(val isBirthdayToday: Boolean) : MainPostRoadsEvent()
    object ShowSubscribersPrivacyDialog : MainPostRoadsEvent()
}

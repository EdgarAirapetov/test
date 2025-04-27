package com.meera.core.navigation

sealed class Screens {

    class ScreenUserInfo(val userId: Long): Screens()

}

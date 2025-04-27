package com.meera.core.navigation

class NavigationRouter {

    fun addScreen(navigationAdapter: NavigationRouterAdapter, screen: Screens) {
        when (screen) {
            is Screens.ScreenUserInfo -> gotoUserInfoScreen(navigationAdapter, screen.userId)
        }
    }

    private fun gotoUserInfoScreen(navigationAdapter: NavigationRouterAdapter, userId: Long) {
        navigationAdapter.gotoUserInfoScreen(userId)
    }

}

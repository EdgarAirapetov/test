package com.numplates.nomera3.presentation.router

import com.meera.core.navigation.NavigationRouterAdapter
import com.numplates.nomera3.Act
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment

class NavigationRouterAdapterActImpl(
    private val act: Act
) : NavigationRouterAdapter {

    override fun gotoUserInfoScreen(userId: Long) {
        val args = arrayListOf(
            Arg(IArgContainer.ARG_USER_ID, userId)
        )
        act.addFragment(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            *args.toTypedArray()
        )
    }

}

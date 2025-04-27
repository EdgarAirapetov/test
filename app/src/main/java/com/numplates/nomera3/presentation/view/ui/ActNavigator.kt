package com.numplates.nomera3.presentation.view.ui

import com.numplates.nomera3.presentation.router.Arg
import com.meera.core.base.BaseFragment

interface ActNavigator {
    fun addFragment(fragment: BaseFragment, vararg args: Arg<*, *>)
}

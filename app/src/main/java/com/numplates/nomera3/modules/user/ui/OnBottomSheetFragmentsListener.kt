package com.numplates.nomera3.modules.user.ui

import android.os.Bundle
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.empty

interface OnBottomSheetFragmentsListener {

    fun onNextFragment(
        clazz: Class<out BaseFragment>,
        tag: String = String.empty(),
        bundle: Bundle? = null
    )

    fun onBackFragment()

    fun onCloseMenu()

    fun <T> onReturnResult(result: T)

}

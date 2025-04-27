package com.numplates.nomera3.modules.viewvideo.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import com.numplates.nomera3.databinding.ViewVideoFragmentBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class ViewVideoFragment : BaseFragmentNew<ViewVideoFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ViewVideoFragmentBinding
        get() = ViewVideoFragmentBinding::inflate

}

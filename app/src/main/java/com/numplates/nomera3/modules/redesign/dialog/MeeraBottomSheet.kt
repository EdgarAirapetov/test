package com.numplates.nomera3.modules.redesign.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.numplates.nomera3.R

class MeeraBottomSheet : MeeraBaseBottomSheet() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dynamic_bottom_sheet_layout, container, false)

}

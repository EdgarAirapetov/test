package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRoadSelectBottomSheetBinding
import com.numplates.nomera3.presentation.model.enums.RoadSelectionEnum
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.meera.core.extensions.simpleName

class RoadSelectBottomSheet : BaseBottomSheetDialogFragment<FragmentRoadSelectBottomSheetBinding>() {

    var mainRoadClickedListener: () -> Unit = {}
    var myRoadClickedListener: () -> Unit = {}
    var onDismiss: () -> Unit = {}
    var roadState = RoadSelectionEnum.MAIN

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRadioButtons()
        when (roadState) {
            RoadSelectionEnum.MY -> {
                binding?.rbToMainRoad?.setChecked(false)
                binding?.rbToMyRoad?.setChecked(true)
            }
            RoadSelectionEnum.MAIN -> {
                binding?.rbToMainRoad?.setChecked(true)
                binding?.rbToMyRoad?.setChecked(false)
            }
        }
        initClickListeners()
    }

    private fun initRadioButtons() {
        binding?.rbToMainRoad?.setTitle(R.string.to_main_road)
        binding?.rbToMainRoad?.setDescription(R.string.to_main_road_desc)
        binding?.rbToMainRoad?.setIcon(R.drawable.ic_main_road)
        binding?.rbToMainRoad?.setSeparatorVisibility(true)
        binding?.rbToMyRoad?.setTitle(R.string.to_my_road)
        binding?.rbToMyRoad?.setDescription(R.string.to_my_road_desc)
        binding?.rbToMyRoad?.setIcon(R.drawable.ic_my_road)
    }

    private fun initClickListeners() {
        binding?.rbToMainRoad?.setOnCheckedChangeListener(object : BottomItemImageRadioButton.OnCheckedChangeListener {
            override fun onCheckedChanged(isChecked: Boolean) {
                if (isChecked) {
                    binding?.rbToMyRoad?.setChecked(false)
                    mainRoadClickedListener()
                }
            }
        })
        binding?.rbToMyRoad?.setOnCheckedChangeListener(object : BottomItemImageRadioButton.OnCheckedChangeListener {
            override fun onCheckedChanged(isChecked: Boolean) {
                if (isChecked) {
                    binding?.rbToMainRoad?.setChecked(false)
                    myRoadClickedListener()
                }
            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss()
    }

    fun show(manager: FragmentManager?) {
        if (manager == null) return
        if (manager.findFragmentByTag(simpleName) == null) {
            super.show(manager, simpleName)
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRoadSelectBottomSheetBinding
        get() = FragmentRoadSelectBottomSheetBinding::inflate
}
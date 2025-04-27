package com.numplates.nomera3.presentation.view.fragments.market.configurevehicle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.AppBarLayout
import com.numplates.nomera3.data.network.market.Field
import com.numplates.nomera3.data.network.market.Value
import com.numplates.nomera3.databinding.FragmentVehicleParamFillBinding
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleBrandModel
import com.numplates.nomera3.presentation.model.adaptermodel.ExpandedCheckedData
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleBrandAdapter
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleColorAdapter
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleModelAdpterExpanded
import com.numplates.nomera3.presentation.view.adapter.vehicleparam.VehicleParamSelectAdapter
import com.numplates.nomera3.presentation.viewmodel.VehicleParamFillViewModel

class VehicleParamFillFragment: BaseFragmentNew<FragmentVehicleParamFillBinding>() {

    private lateinit var vehicleParamFillModel: VehicleParamFillModel
    private lateinit var viewModel: VehicleParamFillViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)
                .get(VehicleParamFillViewModel::class.java)

        arguments?.let { args ->
            args.get(IArgContainer.ARG_VEHICLE_PARAM_FILL)?.let { argMode->
                vehicleParamFillModel = argMode as VehicleParamFillModel
            }
        }

    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVehicleParamFillBinding
        get() = FragmentVehicleParamFillBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initClickListeners()
        initObservers()
        doDelayed(150){ //иначе фризит анимация открытия
            viewModel.init(vehicleParamFillModel)
        }


    }

    private fun initClickListeners() {
        binding?.ivCloseFill?.setOnClickListener {
            act.onBackPressed()
        }
        binding?.ivAddGroupFill?.setOnClickListener {
            act.onBackPressed()
        }

    }

    private fun initToolbar() {
        val layoutParamsStatusBar = binding?.statusBarParamFill?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarParamFill?.layoutParams = layoutParamsStatusBar
    }

    private fun initObservers() {
        viewModel.liveField.observe(viewLifecycleOwner, Observer {
            handleField(it)
        })

        viewModel.liveBrand.observe(viewLifecycleOwner, Observer {
            handleBrandExpandable(it)
        })

        viewModel.liveModel.observe(viewLifecycleOwner, Observer {
            handleModel(it)
        })

        viewModel.liveBrandNotExpanded.observe(viewLifecycleOwner, Observer {
            handleBrandNotExpandable(it)
        })
    }

    private fun handleBrandNotExpandable(values: List<Value>?) {
        values?.let {
            binding?.rvVehicleParamFill?.layoutManager = LinearLayoutManager(context)
            val listAdapter = VehicleParamSelectAdapter(mutableListOf(), true)
            listAdapter.replaceData(it)
            binding?.rvVehicleParamFill?.adapter = listAdapter
            binding?.pbVehicleFill?.gone()
            setSelectorMode()
        }
    }

    private fun handleModel(model: MutableList<ExpandedCheckedData>?) {
        model?.let {
            binding?.rvVehicleParamFill?.layoutManager = LinearLayoutManager(context)
            val modelAdapter = VehicleModelAdpterExpanded(it)
            binding?.rvVehicleParamFill?.adapter = modelAdapter
            binding?.pbVehicleFill?.gone()
            setSelectorMode()
        }
    }

    private fun handleBrandExpandable(brands: List<VehicleBrandModel>?) {
        brands?.let {
            binding?.rvVehicleParamFill?.layoutManager = LinearLayoutManager(context)
            val brandAdapter = VehicleBrandAdapter(brands.toMutableList())
            binding?.rvVehicleParamFill?.adapter = brandAdapter
            binding?.pbVehicleFill?.gone()
            setSelectorMode()
        }
    }

    private fun handleField(field: Field?) {
        field?.let {
            binding?.tvTitle?.text = it.name
            binding?.pbVehicleFill?.gone()
            when(it.type){
                Field.TYPE_TEXT ->{
                    setTextMode()
                    binding?.tvVehicleRightTextFill?.text = it.units
                }
                Field.TYPE_LIST -> {
                    setSelectorMode()
                    binding?.rvVehicleParamFill?.layoutManager = LinearLayoutManager(context)
                    val listAdapter = VehicleParamSelectAdapter(mutableListOf(), true)
                    listAdapter.replaceData(it.value)
                    binding?.rvVehicleParamFill?.adapter = listAdapter
                }
                Field.TYPE_COLOR -> {
                    setColorMode()
                    val columns = calculateNumberOfColumns(102)
                    binding?.rvVehicleParamFill?.layoutManager = GridLayoutManager(context, columns)
                    val colorAdapter = VehicleColorAdapter(mutableListOf())
                    colorAdapter.replaceData(it.value)
                    binding?.rvVehicleParamFill?.adapter = colorAdapter
                    (binding?.rvVehicleParamFill?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                }
            }
        }
    }

    private fun calculateNumberOfColumns(columnWidthDp: Int): Int {
        val displayMetrics = context?.resources?.displayMetrics
        displayMetrics?.let {
            val screenWidthDp = it.widthPixels / it.density
            return (screenWidthDp / columnWidthDp + 0.5).toInt()
        } ?: kotlin.run {
            return 3
        }
    }


    private fun setColorMode() {
        binding?.rvVehicleParamFill?.visible()
        binding?.clVehicleTextContainer?.gone()
    }


    private fun setTextMode() {
        binding?.rvVehicleParamFill?.gone()
        binding?.clVehicleTextContainer?.visible()

    }

    private fun setSelectorMode() {
        binding?.rvVehicleParamFill?.visible()
        binding?.clVehicleTextContainer?.gone()
    }

    companion object{
        const val VIEW_TYPE_FIELD = 0
        const val VIEW_TYPE_BRAND = 1
        const val VIEW_TYPE_MODEL = 2
    }

}

package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.getStatusBarHeight
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentVehicleListNewBinding
import com.numplates.nomera3.presentation.model.VipStatus
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.VehicleListAdapter
import com.numplates.nomera3.presentation.view.utils.NToast
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.numplates.nomera3.presentation.viewmodel.VehicleListViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.VehicleListEvent

class VehicleListFragmentNew : BaseFragmentNew<FragmentVehicleListNewBinding>() {

    private lateinit var viewModel: VehicleListViewModel
    private lateinit var adapter: VehicleListAdapter

    private var userID: Long = -1
    private var vipStatus: VipStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(VehicleListViewModel::class.java)

        arguments?.let {
            userID = it.getLong(IArgContainer.ARG_USER_ID, -1)
            vipStatus = it.getParcelable(IArgContainer.ARG_USER_VIP_STATUS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initToolbar()
        initObservers()
        binding?.srlVehicleList?.isRefreshing = true

        binding?.srlVehicleList?.setOnRefreshListener {
            viewModel.refreshData()
        }

        if (userID != -1L)
            viewModel.init(userID)
        else showError()
    }

    private fun initToolbar() {
        val layoutParamsStatusBar = binding?.statusBarVehicleList?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarVehicleList?.layoutParams = layoutParamsStatusBar

        binding?.ivVehicleListBack?.setOnClickListener {
            act?.onBackPressed()
        }

        if (userID == viewModel.getUserUid()) {
            binding?.ivVehicleListAddButton?.visible()
            binding?.ivVehicleListAddButton?.setOnClickListener {
                add(VehicleSelectTypeFragment(), Act.LIGHT_STATUSBAR)
            }
        } else {
            binding?.ivVehicleListAddButton?.gone()
        }

        binding?.tvCaption?.text = if (userID == viewModel.getUserUid()) getString(R.string.garage_my) else getString(R.string.garage_my_garage)
    }

    private fun showError() {
        NToast.with(view)
                .text(getString(R.string.error_try_later))
                .show()
    }

    private fun initObservers() {
        viewModel.liveVehicles.observe(viewLifecycleOwner, Observer {
            adapter.addItems(it)
            binding?.srlVehicleList?.isRefreshing = false
            if (adapter.itemCount == 0 || it.size == 0)
                showNoItemsPlaceHolder()
            else binding?.placeholderEmptyList?.llEmptyListContainer?.gone()
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            when(it){
                is VehicleListEvent.VehicleError -> {
                    showError()
                    binding?.srlVehicleList?.isRefreshing = false
                }
                is VehicleListEvent.VehicleClearAdapter -> {
                    adapter.clearItems()
                }
                is VehicleListEvent.VehicleUpdate -> {
                    adapter.updateItem(it.vehicle)
                }
            }
        })
    }

    private fun showNoItemsPlaceHolder() {
        binding?.placeholderEmptyList?.llEmptyListContainer?.visible()
        binding?.placeholderEmptyList?.ivEmptyList?.loadGlide(R.drawable.empty_vehicles)
        if (userID == viewModel.getUserUid()) {
            binding?.placeholderEmptyList?.tvEmptyList?.text = getString(R.string.garage_no_vehicles_yet)
            binding?.placeholderEmptyList?.tvButtonEmptyList?.text = getString(R.string.add_vehicle)
            binding?.placeholderEmptyList?.tvButtonEmptyList?.setOnClickListener {
                add(VehicleSelectTypeFragment(), Act.LIGHT_STATUSBAR)
            }
            binding?.placeholderEmptyList?.tvButtonEmptyList?.visible()
        } else {
            binding?.placeholderEmptyList?.tvEmptyList?.text = getString(R.string.profile_empty_user_garage)
            binding?.placeholderEmptyList?.tvButtonEmptyList?.gone()
        }

    }

    private fun initRecycler() {
        binding?.rvVehicleList?.layoutManager = LinearLayoutManager(context)

        val isNeedToShowMainVehicle = userID == viewModel.getUserUid()
        adapter = VehicleListAdapter(vipStatus, mutableListOf(), isNeedToShowMainVehicle) {
            // init itemClickListener
            handleItemClick(it)
        }
        binding?.rvVehicleList?.adapter = adapter
    }

    private fun handleItemClick(vehicleModel: VehicleModel) {
//        //if (userID == ownId)
            add(VehicleInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR
                    , Arg(IArgContainer.ARG_CAR_ID, vehicleModel.vehicle?.vehicleId.toString())
                    , Arg(IArgContainer.ARG_USER_ID, userID))
//        add(EditVehicleMarketFragment(), Act.LIGHT_STATUSBAR,
//                 Arg(ARG_CAR_MODEL, vehicleModel.vehicle))
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVehicleListNewBinding
        get() = FragmentVehicleListNewBinding::inflate
}

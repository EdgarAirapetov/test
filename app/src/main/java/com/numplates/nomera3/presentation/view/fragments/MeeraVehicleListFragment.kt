package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentVehicleListBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment.Companion.DELAY_DELETE_SNACK_BAR_SEC
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_ID
import com.numplates.nomera3.presentation.view.adapter.MeeraVehicleListAdapter
import com.numplates.nomera3.presentation.view.adapter.MeeraVehicleListShimmerAdapter
import com.numplates.nomera3.presentation.view.fragments.MeeraVehicleInfoFragment.Companion.VEHICLE_INFO_BOTTOM_DIALOG_DELETED
import com.numplates.nomera3.presentation.view.fragments.MeeraVehicleInfoFragment.Companion.VEHICLE_INFO_BOTTOM_DIALOG_KEY
import com.numplates.nomera3.presentation.view.fragments.MeeraVehicleInfoFragment.Companion.VEHICLE_INFO_BOTTOM_DIALOG_UPDATED
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.VehicleListViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.VehicleListEvent

class MeeraVehicleListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_vehicle_list, behaviourConfigState = ScreenBehaviourState.Full
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentVehicleListBinding::bind)
    private lateinit var viewModel: VehicleListViewModel
    private var undoSnackBar: UiKitSnackBar? = null
    private val adapter: MeeraVehicleListAdapter by lazy {
        val isNeedToShowMainVehicle = userID == viewModel.getUserUid()
        return@lazy MeeraVehicleListAdapter(mutableListOf(), isNeedToShowMainVehicle) {
            handleItemClick(it)
        }
    }
    private val shimmerAdapter = MeeraVehicleListShimmerAdapter()

    private val userID: Long by lazy {
        requireArguments().getLong(IArgContainer.ARG_USER_ID, -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VehicleListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initToolbar()
        initObservers()
        binding?.srlVehicleList?.setRefreshing(false)
        binding?.srlVehicleList?.setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                viewModel.refreshData()
            }
        })
        childFragmentManager.setFragmentResultListener(
            VEHICLE_INFO_BOTTOM_DIALOG_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val shouldDelete = bundle.getBoolean(VEHICLE_INFO_BOTTOM_DIALOG_DELETED, false)
            if (shouldDelete) {
                val vehicleID = bundle.getString(ARG_CAR_ID) ?: return@setFragmentResultListener
                viewModel.handleHideVehicle(vehicleID)
                undoSnackBar?.dismiss()
                undoSnackBar = UiKitSnackBar.make(
                    view = requireView(),
                    params = SnackBarParams(
                        snackBarViewState = SnackBarContainerUiState(
                            messageText = getText(R.string.vehicle_deleted),
                            loadingUiState = SnackLoadingUiState.DonutProgress(
                                timerStartSec = DELAY_DELETE_SNACK_BAR_SEC.toLong(),
                                onTimerFinished = {
                                    viewModel.handleDeleteVehicle(vehicleID)
                                    undoSnackBar?.dismiss()
                                }),
                            buttonActionText = getText(R.string.cancel),
                            buttonActionListener = {
                                viewModel.handleShowVehicle(vehicleID)
                                undoSnackBar?.dismiss()
                            }),
                        duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                        dismissOnClick = true,
                        dismissListeners = DismissListeners(dismissListener = {
                            viewModel.handleShowVehicle(vehicleID)
                            undoSnackBar?.dismiss()
                        })
                    )
                )
                undoSnackBar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
                undoSnackBar?.show()
                return@setFragmentResultListener
            }
            val shouldUpdate = bundle.getBoolean(VEHICLE_INFO_BOTTOM_DIALOG_UPDATED, false)
            if (shouldUpdate) viewModel.refreshData()
        }
        if (userID != -1L) viewModel.init(userID)
        else showError()
    }

    private fun initToolbar() {
        binding?.apply {
            navViewGarage.title =
                if (userID == viewModel.getUserUid()) getString(R.string.garage_my) else getString(R.string.garage_my_garage)
            navViewGarage.backButtonClickListener = { findNavController().popBackStack() }
            rvVehicleList.let { navViewGarage.addScrollableView(it) }

            if (userID == viewModel.getUserUid()) {
                btnAdd.visible()
                btnAdd.setThrottledClickListener {
                    findNavController().safeNavigate(R.id.action_meeraVehicleListFragment_to_meeraVehicleSelectTypeFragment)
                }
            } else {
                btnAdd.gone()
            }
        }
    }

    private fun showError() {
        NToast.with(view).text(getString(R.string.error_try_later)).show()
    }

    private fun initObservers() {
        viewModel.liveVehicles.observe(viewLifecycleOwner, Observer {
            binding.rvShimmerVehicleList.gone()
            adapter.addItems(it.filterNot { it.hidden })
            binding?.srlVehicleList?.setRefreshing(false)
            if (adapter.itemCount == 0 || it.isEmpty()) showNoItemsPlaceHolder()
            else binding?.groupVehicleListEmpty?.gone()
        })

        viewModel.liveEvent.observe(viewLifecycleOwner, Observer {
            when (it) {
                is VehicleListEvent.VehicleError -> {
                    showError()
                    binding?.srlVehicleList?.setRefreshing(false)
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
        binding?.groupVehicleListEmpty?.visible()
        if (userID == viewModel.getUserUid()) {
            binding?.buttonAddVehicle?.setOnClickListener {
                findNavController().safeNavigate(R.id.action_meeraVehicleListFragment_to_meeraVehicleSelectTypeFragment)
            }
            binding?.buttonAddVehicle?.visible()
        } else {
            binding?.buttonAddVehicle?.gone()
        }
    }

    private fun initRecycler() {
        binding?.apply {
            rvVehicleList.adapter = adapter
            rvShimmerVehicleList.adapter = shimmerAdapter
            rvShimmerVehicleList.visible()
        }
    }

    private fun handleItemClick(vehicleModel: VehicleModel) {
        MeeraVehicleInfoFragment.show(childFragmentManager, userID, vehicleModel.vehicle?.vehicleId.toString())
    }

}

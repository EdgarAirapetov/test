package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.CarsMakes
import com.numplates.nomera3.data.network.CarsModels
import com.numplates.nomera3.data.network.CarsYears
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.VehicleType
import com.numplates.nomera3.data.network.VehicleTypes
import com.numplates.nomera3.data.network.Vehicles
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.MeeraFragmentVehicleSelectTypeBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.presenter.GaragePresenter
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.MeeraVehicleTypeAdapter
import com.numplates.nomera3.presentation.view.view.IGarageView

const val VEHICLE_EDIT_FRAGMENT_STOP = "VEHICLE_EDIT_FRAGMENT_STOP"

class MeeraVehicleSelectTypeFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_vehicle_select_type, behaviourConfigState = ScreenBehaviourState.Full
), IGarageView {
    val presenter: GaragePresenter by lazy { GaragePresenter() }
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentVehicleSelectTypeBinding::bind)
    private val vehicle: Vehicle by lazy {
        val defCountry = Country(countryId = Country.ID_RUSSIA)
        Vehicle(
            0, VehicleType(), null, null, 0, defCountry, null, null, CarsMakes.Make(), CarsModels.Model(), "", null
        )
    }

    private fun onPublish() {
        findNavController().safeNavigate(resId = R.id.meeraVehicleEditFragment, bundle = Bundle().apply {
            putSerializable(IArgContainer.ARG_CAR_MODEL, vehicle)
        })
    }

    var adapter: MeeraVehicleTypeAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = binding
        if (binding != null) {
            presenter.setView(this)
            presenter.vehicleTypeList()

            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                binding.clVehicleSelectTypeContainer.setMargins(
                    start = CONTAINER_MARGIN.dp,
                    top = CONTAINER_MARGIN.dp + statusBar,
                    bottom = CONTAINER_MARGIN.dp,
                    end = CONTAINER_MARGIN.dp
                )
                insets
            }

            binding.rvVehicleTypes.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.rvVehicleTypes.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    adapter = MeeraVehicleTypeAdapter()
                    binding.rvVehicleTypes.adapter = adapter
                }
            })
            val snapHelper: SnapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.rvVehicleTypes)
            binding.rvVehicleTypes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var oldCenter: Int = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = Unit

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val centeredItemPosition = layoutManager.findFirstVisibleItemPosition()
                        if (oldCenter != centeredItemPosition) {
                            oldCenter = centeredItemPosition
                            adapter?.getVehicleItem(centeredItemPosition)?.let {
                                updateVehicleType(it)
                            }
                            binding.sliderTypes.selectTab(binding.sliderTypes.getTabAt(centeredItemPosition))
                        }
                    }
                }
            })

            binding.buttonContinue.setThrottledClickListener {
                onPublish()
            }
            binding.buttonClose.setThrottledClickListener {
                findNavController().popBackStack()
            }
        }
        initFragmentResultListener()
    }

    private fun updateVehicleType(vehicleType: VehicleType) {
        vehicle.type = vehicleType
    }


    override fun onCountryList(countries: List<Country>) = Unit


    override fun onYears(years: CarsYears) = Unit

    override fun onMakes(makes: List<CarsMakes.Make>) = Unit

    override fun onModels(models: List<CarsModels.Model>) = Unit

    override fun onAddVehicle() = Unit

    override fun onUpdateVehicle() = Unit

    override fun onVehicleList(vehicles: Vehicles) = Unit

    override fun onVehicle(vehicle: Vehicle) = Unit

    override fun onDeleteVehicle() = Unit

    override fun onMainVehicle() = Unit

    override fun onShowErrorMessage(message: String) = Unit

    override fun onTypes(res: VehicleTypes) {
        val typesList = res.getList()?.filter {
            when (it?.typeId) {
                INetworkValues.VEHICLE_TYPE_CAR.toString(), INetworkValues.VEHICLE_TYPE_MOTO.toString() -> true
                else -> false
            }
        }
        typesList?.let { items ->
            adapter?.submitList(items)
            vehicle.type = items.first()
            binding?.sliderTypes?.apply {
                removeAllTabs()
                repeat(items.size) {
                    addTab(newTab())
                }
            }
        }
    }

    override fun onFail(msg: String) = Unit

    private fun initFragmentResultListener() {
        setFragmentResultListener(VEHICLE_EDIT_FRAGMENT_STOP) { _, bundle ->
            val result = bundle.getBoolean(VEHICLE_EDIT_FRAGMENT_STOP, false)
            if (result) {
                requireContext().hideKeyboard(requireView())
                binding?.rvVehicleTypes?.requestLayout()
            }
        }
    }

    companion object {
        const val CONTAINER_MARGIN = 16
    }
}



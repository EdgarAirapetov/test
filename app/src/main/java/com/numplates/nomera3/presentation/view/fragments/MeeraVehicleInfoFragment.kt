package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.widgets.VehiclePlateTypeSize
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.CarsMakes.Make
import com.numplates.nomera3.data.network.CarsModels
import com.numplates.nomera3.data.network.CarsYears
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.VehicleTypes
import com.numplates.nomera3.data.network.Vehicles
import com.numplates.nomera3.databinding.MeeraFragmentVehicleInfoStaticBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_AUTO
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_BICYCLE
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_JETSKI
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_KICKSCOOTER
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_MOTO
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_PLANE
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_ROLLERSKATES
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_SKATEBOARD
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_SNOWMOBILE
import com.numplates.nomera3.presentation.presenter.GaragePresenter
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.view.IGarageView

class MeeraVehicleInfoFragment : UiKitBottomSheetDialog<MeeraFragmentVehicleInfoStaticBinding>(), IGarageView {

    private var garagePresenter: GaragePresenter? = null
    private var userId: Long = 0
    private var vehicle: Vehicle? = null
    private var vehicleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && requireArguments().containsKey(ARG_CAR_ID)) {
            vehicleId = requireArguments().getString(ARG_CAR_ID)
        }
        if (arguments != null && requireArguments().containsKey(ARG_USER_ID)) {
            userId = requireArguments().getLong(ARG_USER_ID)
        }
    }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentVehicleInfoStaticBinding
        get() = MeeraFragmentVehicleInfoStaticBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        garagePresenter = GaragePresenter()
        garagePresenter?.setView(this)
        garagePresenter?.getVehicle(vehicleId)
        initBehavior()
        firstSetup()

        childFragmentManager.setFragmentResultListener(
            MeeraVehicleInfoMenuFragment.ARG_VEHICLE_INFO_MENU_REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString(MeeraVehicleInfoMenuFragment.ARG_VEHICLE_INFO_MENU)
            when (action) {
                MeeraVehicleInfoMenuFragment.ARG_VEHICLE_INFO_MENU_ACTION_EDIT -> {
                    findNavController().safeNavigate(
                        resId = R.id.meeraVehicleEditFragment,
                        bundle = Bundle().apply {
                            putSerializable(ARG_CAR_MODEL, vehicle)
                        }
                    )
                }

                MeeraVehicleInfoMenuFragment.ARG_VEHICLE_INFO_MENU_ACTION_DELETE -> {
                    setFragmentResult(
                        VEHICLE_INFO_BOTTOM_DIALOG_KEY,
                        bundleOf(
                            VEHICLE_INFO_BOTTOM_DIALOG_UPDATED to true,
                            VEHICLE_INFO_BOTTOM_DIALOG_DELETED to true,
                            ARG_CAR_ID to vehicleId
                        )
                    )
                    dismiss()
                }

                else -> Unit
            }
        }
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false, needShowGrabberView = false, dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    private fun initBehavior() {
        val dialog = dialog as BottomSheetDialog
        val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        mainContainer?.let { frameLayout ->
            val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun firstSetup() {
        contentBinding?.buttonCloseVehicleInfo?.setThrottledClickListener {
            dismiss()
        }

        if (userId == garagePresenter?.userUid || userId == 0L) {
            contentBinding?.cellMainVehicle?.visibility = View.VISIBLE
            contentBinding?.buttonMenuVehicleInfo?.visibility = View.VISIBLE

            contentBinding?.buttonMenuVehicleInfo?.setThrottledClickListener {
                MeeraVehicleInfoMenuFragment.show(childFragmentManager)
            }

        } else {
            contentBinding?.cellMainVehicle?.visibility = View.GONE
            contentBinding?.buttonMenuVehicleInfo?.visibility = View.GONE
        }
    }

    override fun onCountryList(res: List<Country>) = Unit
    override fun onYears(years: CarsYears) = Unit
    override fun onMakes(makes: List<Make>) = Unit
    override fun onModels(models: List<CarsModels.Model>) = Unit
    override fun onAddVehicle() = Unit
    override fun onUpdateVehicle() {
        setFragmentResult(
            VEHICLE_INFO_BOTTOM_DIALOG_KEY,
            bundleOf(VEHICLE_INFO_BOTTOM_DIALOG_UPDATED to true)
        )
    }

    override fun onVehicleList(vehicles: Vehicles) = Unit
    override fun onShowErrorMessage(message: String) = Unit

    override fun onVehicle(res: Vehicle) {
        vehicle = res
        typeOfVehicles()

        contentBinding?.apply {
            if (vehicle?.number.isNullOrEmpty().not()) {
                vehiclePlateView.visible()
                val plateType =
                    if (vehicle?.type?.typeId == TYPE_AUTO.toString()) VehiclePlateTypeSize.LARGE_AUTO else VehiclePlateTypeSize.LARGE_MOTO
                vehiclePlateView.setTypeSize(plateType)
                vehiclePlateView.text = vehicle?.number ?: ""
            } else {
                vehiclePlateView.invisible()
            }

            cellMainVehicle.setCellRightElementChecked(vehicle?.mainVehicle?.toBoolean().isTrue())
            val makerAndModel = StringBuilder()
            if (vehicle?.make != null) {
                makerAndModel.append(vehicle?.make?.name)
            }
            if (vehicle?.model != null) {
                if (makerAndModel.isNotEmpty()) makerAndModel.append(getString(R.string.space))
                makerAndModel.append(vehicle?.model?.name)
            }
            tvMake.text = makerAndModel.toString()

            cellMainVehicle.setRightElementContainerClickable(true)
            cellMainVehicle.setCellRightElementClickable(false)
            cellMainVehicle.let {
                it.cellRightIconClickListener = {
                    it.setCellRightElementChecked(it.getCellRightElementChecked().not())
                    garagePresenter?.mainVehicle(vehicleId, if (it.getCellRightElementChecked()) 1 else 0)

                }
            }
        }
    }

    override fun onMainVehicle() {
        setFragmentResult(
            VEHICLE_INFO_BOTTOM_DIALOG_KEY,
            bundleOf(VEHICLE_INFO_BOTTOM_DIALOG_UPDATED to true)
        )
    }

    override fun onDeleteVehicle() = Unit
    override fun onTypes(types: VehicleTypes) = Unit
    override fun onFail(msg: String) = Unit

    private fun typeOfVehicles() {
        if (vehicle?.type != null) {

            val photoThumb = when (vehicle?.type?.typeId?.toIntOrNull()) {
                TYPE_AUTO -> R.drawable.meera_vehicle_placeholder_car
                TYPE_MOTO -> R.drawable.meera_vehicle_placeholder_moto
                else -> R.drawable.meera_vehicle_placeholder_car
            }

            val makerThumb = when (vehicle?.type?.typeId?.toIntOrNull()) {
                TYPE_AUTO -> R.drawable.icon_car
                TYPE_MOTO -> R.drawable.icon_motorbike
                TYPE_BICYCLE -> R.drawable.icon_bicycle
                TYPE_KICKSCOOTER -> R.drawable.icon_kickscooter
                TYPE_SKATEBOARD -> R.drawable.icon_skateboard
                TYPE_ROLLERSKATES -> R.drawable.icon_rollerskates
                TYPE_SNOWMOBILE -> R.drawable.icon_snowmobile
                TYPE_JETSKI -> R.drawable.icon_jetski
                TYPE_PLANE -> R.drawable.icon_plane
                else -> R.drawable.icon_boat
            }

            contentBinding?.apply {
                if (vehicle?.picture.isNullOrEmpty().not()) {
                    Glide.with(requireActivity()).load(vehicle?.picture).into(ivAvatar)
                } else {
                    Glide.with(requireActivity()).load(photoThumb).into(ivAvatar)
                }

                if (vehicle?.make?.makeLogo.isNullOrEmpty().not()) {
                    Glide.with(requireActivity()).load(vehicle?.make?.makeLogo).into(ivCarMaker)
                } else {
                    Glide.with(requireActivity()).load(makerThumb).into(ivCarMaker)
                }
            }
        }
    }


    companion object {
        const val VEHICLE_INFO_BOTTOM_DIALOG_TAG = "vehicleInfoBottomDialog"
        const val VEHICLE_INFO_BOTTOM_DIALOG_KEY = "vehicleInfoBottomKey"
        const val VEHICLE_INFO_BOTTOM_DIALOG_UPDATED = "vehicleInfoBottomUpdated"
        const val VEHICLE_INFO_BOTTOM_DIALOG_DELETED = "vehicleInfoBottomDeleted"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, userID: Long, vehicleId: String): MeeraVehicleInfoFragment {
            val instance = MeeraVehicleInfoFragment()
            instance.arguments = bundleOf(ARG_CAR_ID to vehicleId, ARG_USER_ID to userID)
            instance.show(fragmentManager, VEHICLE_INFO_BOTTOM_DIALOG_TAG)
            return instance
        }
    }

}

package com.numplates.nomera3.presentation.view.fragments

import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.CarsMakes.Make
import com.numplates.nomera3.data.network.CarsModels
import com.numplates.nomera3.data.network.CarsYears
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.VehicleTypes
import com.numplates.nomera3.data.network.Vehicles
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.FragmentVehicleInfoStaticBinding
import com.numplates.nomera3.presentation.presenter.GaragePresenter
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.view.IGarageView
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView


class VehicleInfoFragment : BaseFragmentNew<FragmentVehicleInfoStaticBinding>(), IGarageView {

    var garagePresenter: GaragePresenter? = null
    private var userId: Long = 0
    private var vehicle: Vehicle? = null
    private var vehicleId: String? = null
    var layerDrawable: LayerDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && requireArguments().containsKey(ARG_CAR_ID)) {
            vehicleId = requireArguments().getString(ARG_CAR_ID)
        }
        if (arguments != null && requireArguments().containsKey(ARG_USER_ID)) {
            userId = requireArguments()[ARG_USER_ID] as Long
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentVehicleInfoStaticBinding
        get() = FragmentVehicleInfoStaticBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        garagePresenter = GaragePresenter()
        garagePresenter?.setView(this)
        garagePresenter?.getVehicle(vehicleId)
        val layers = arrayOfNulls<Drawable>(1)
        val colors = intArrayOf(act.resources.getColor(R.color.ui_black),
                act.resources.getColor(R.color.ui_placeholder_gradient),
                act.resources.getColor(R.color.ui_black))
        layers[0] = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
        layerDrawable = LayerDrawable(layers)
        val avatarHeight = act.window.decorView.height
        binding?.ivAvatar?.background = layerDrawable
        binding?.ivAvatar?.layoutParams?.height = (avatarHeight * 0.45).toInt()

        firstSetup()
    }

    private fun firstSetup() {
        val statusBarVehicleInfo = binding?.statusBarVehicleInfo
        val params = statusBarVehicleInfo?.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBarVehicleInfo.layoutParams = params

        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback_white)
        binding?.toolbar?.setNavigationOnClickListener { v: View? -> act.onBackPressed() }

        if (userId == garagePresenter?.userUid || userId == 0L) {
            binding?.toolbar?.inflateMenu(R.menu.menu_vehicle)
            binding?.etDescription?.isEnabled = false
            binding?.scMainVehicle?.visibility = View.VISIBLE
            binding?.toolbar?.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.action_settings -> add(VehicleEditFragment(), Act.LIGHT_STATUSBAR, Arg<Any?, Any?>(ARG_CAR_MODEL, vehicle))
                    R.id.action_delete -> AlertDialog.Builder(requireContext())
                        .setMessage(R.string.delete_vehicle_confirmation)
                        .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton
                            -> garagePresenter!!.deleteVehicle(vehicleId) })
                        .setNegativeButton(android.R.string.no, null).show()
                    else -> {
                    }
                }
                true
            }
        } else {
            binding?.etDescription?.isEnabled = false
            binding?.scMainVehicle?.visibility = View.GONE
            binding?.viewSeparator1?.visibility = View.GONE
            binding?.viewSeparator2?.visibility = View.GONE
        }
    }

    override fun onCountryList(res: List<Country>) = Unit
    override fun onYears(years: CarsYears) = Unit
    override fun onMakes(makes: List<Make>) = Unit
    override fun onModels(models: List<CarsModels.Model>) = Unit
    override fun onAddVehicle() = Unit
    override fun onUpdateVehicle() = Unit
    override fun onVehicleList(vehicles: Vehicles) = Unit
    override fun onShowErrorMessage(message: String) = Unit

    override fun onVehicle(res: Vehicle) {
        vehicle = res
        typeOfVehicles()
        if (vehicle?.description == null || vehicle?.description.isNullOrEmpty()) binding?.etDescription?.hint = getString(R.string.no_description)
        else {
            binding?.etDescription?.setText(vehicle?.description)
        }
        if (vehicle != null && vehicle!!.type != null && vehicle!!.type!!.hasNumber != 0) {
            binding?.nvNumberNew?.visibility = View.GONE
            binding?.nvNumber?.visibility = View.VISIBLE
            binding?.nvNumber?.let {
                NumberPlateEditView.Builder(it)
                        .setVehicle(vehicle!!)
                        .build()
            }

            if (vehicle?.type?.typeId == "1") {
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(
                        dpToPx(-60),  //LEFT
                        dpToPx(-12),  //TOP
                        dpToPx(-60),  //RIGHT
                        dpToPx(-12) //BOTTOM
                )
                binding?.nvNumber?.layoutParams = params
            } else if (vehicle!!.type!!.typeId == "2") {
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(
                        dpToPx(-30),  //LEFT
                        dpToPx(-22),  //TOP
                        dpToPx(-30),  //RIGHT
                        dpToPx(-22) //BOTTOM
                )
                binding?.nvNumber?.layoutParams = params
            }
            binding?.nvNumber?.scaleX = 0.6f
            binding?.nvNumber?.scaleY = 0.6f
        } else {
            binding?.nvNumberNew?.visibility = View.VISIBLE
            binding?.nvNumber?.visibility = View.GONE
            binding?.nvNumberNew?.setType(INetworkValues.ACCOUNT_TYPE_REGULAR, null)
            if (vehicle!!.make != null) binding?.nvNumberNew?.setName(vehicle?.make?.name ?: "")
            if (vehicle!!.model != null) binding?.nvNumberNew?.setModel(vehicle?.model?.name ?: "")
        }
        binding?.scMainVehicle?.isChecked = vehicle?.mainVehicle == 1
        if (vehicle!!.make != null) {
            val screenWidth = NGraphics.getScreenWidth(act)
            val makeName = vehicle!!.make!!.name
            if (makeName != null && makeName.length > 15) {
                binding?.tvMake?.maxWidth = screenWidth / 2
            }
            binding?.tvMake?.text = vehicle?.make?.name
        }
        if (vehicle!!.model != null) {
            binding?.tvModel?.text = vehicle?.model?.name
        }
        binding?.scMainVehicle?.setOnCheckedChangeListener { v: CompoundButton?, isChecked: Boolean ->
            garagePresenter!!.mainVehicle(vehicleId, if (isChecked) 1 else 0)
        }
    }

    override fun onDeleteVehicle() {
        act.returnToTargetFragment(0, true)
    }

    override fun onMainVehicle() = Unit
    override fun onTypes(types: VehicleTypes) = Unit
    override fun onFail(msg: String) = Unit

    private fun typeOfVehicles() {
        if (vehicle?.type != null) {
            when (vehicle?.type?.typeId) {
                "1" -> {
                    if (vehicle?.picture == null || vehicle?.picture!!.isEmpty()) {
                        binding?.ivAvatar?.let {
                            Glide.with(act.applicationContext).load(R.drawable.bg_car).into(it)
                        }
                    } else {
                        binding?.ivAvatar?.let {
                            Glide.with(this).load(vehicle?.picture).into(it)
                        }
                    }
                    if (vehicle?.make?.makeLogo == null || vehicle!!.make!!.makeLogo!!.isEmpty()) {
                        binding?.ivCarMaker?.let {
                            Glide.with(this)
                                    .load(R.drawable.icon_car)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(it)
                        }
                    } else {
                        binding?.ivCarMaker?.let {
                            Glide.with(this)
                                    .load(vehicle!!.make!!.makeLogo) //
                                    .into(it)
                        }

                        binding?.ivCarMaker?.background = binding?.content?.resources?.getDrawable(R.drawable.background_road_button)
                    }
                    binding?.nvNumber?.visibility = View.VISIBLE
                }
                "2" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(this).load(R.drawable.bg_motorbike).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(this).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(this).load(R.drawable.icon_motorbike).into(binding?.ivCarMaker!!)
                    binding?.nvNumber?.visibility = View.VISIBLE
                }
                "3" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(this).load(R.drawable.bg_bicycle).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(this).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(this).load(R.drawable.icon_bicycle).into(binding?.ivCarMaker!!)
                }
                "4" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_kickscooter).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_kickscooter).into(binding?.ivCarMaker!!)
                }
                "5" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_skateboard).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_skateboard).into(binding?.ivCarMaker!!)
                }
                "6" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_rollerskates).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_rollerskates).into(binding?.ivCarMaker!!)
                }
                "7" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_snowmobile).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_snowmobile).into(binding?.ivCarMaker!!)
                }
                "8" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_jetski).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_jetski).into(binding?.ivCarMaker!!)
                }
                "9" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_plane).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_plane).into(binding?.ivCarMaker!!)
                }
                "10" -> {
                    if (vehicle?.picture == null || vehicle!!.picture!!.isEmpty()) {
                        Glide.with(act.applicationContext).load(R.drawable.bg_boat).into(binding?.ivAvatar!!)
                    } else {
                        Glide.with(act.applicationContext).load(vehicle!!.picture).into(binding?.ivAvatar!!)
                    }
                    Glide.with(act.applicationContext).load(R.drawable.icon_boat).into(binding?.ivCarMaker!!)
                }
            }
        }
    }

}

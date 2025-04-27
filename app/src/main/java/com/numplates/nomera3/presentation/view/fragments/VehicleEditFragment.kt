package com.numplates.nomera3.presentation.view.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.CarsMakes
import com.numplates.nomera3.data.network.CarsModels
import com.numplates.nomera3.data.network.CarsYears
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.ProfileListItemImp
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.VehicleTypes
import com.numplates.nomera3.data.network.Vehicles
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_REGULAR
import com.numplates.nomera3.databinding.FragmentEditVehivleBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.presenter.GaragePresenter
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.ProfileListAdapter
import com.numplates.nomera3.presentation.view.callback.ProfileListCallback
import com.numplates.nomera3.presentation.view.holder.CityHolder
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NGraphics
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.view.IGarageView
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEnum
import timber.log.Timber
import java.util.Objects

class VehicleEditFragment :
    BaseFragmentNew<FragmentEditVehivleBinding>(),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    IGarageView,
    TedBottomSheetPermissionActionsListener {


    internal lateinit var rvGender: RecyclerView
    internal lateinit var pb_garage_search_dialog: ProgressBar
    internal var plate: NumberPlateEnum? = null
    internal lateinit var carMakerAdapter: ProfileListAdapter
    internal var carMakerDialog: Dialog? = null
    internal var carModelDialog: Dialog? = null
    private var garagePresenter: GaragePresenter? = null
    private var vehicle: Vehicle? = null

    private var imagePath: String? = null
    private var holder: View? = null
    private var mediaPicker: TedBottomSheetDialogFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments?.containsKey(IArgContainer.ARG_CAR_MODEL) == true) {
            vehicle = arguments?.get(IArgContainer.ARG_CAR_MODEL) as Vehicle?
        } else {
            vehicle = Vehicle()
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEditVehivleBinding
        get() = FragmentEditVehivleBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        garagePresenter = GaragePresenter()
        garagePresenter?.setView(this)


        val params = binding?.statusBarEditVehicle?.layoutParams as? AppBarLayout.LayoutParams?
        params?.height = context.getStatusBarHeight()
        params?.let { binding?.statusBarEditVehicle?.layoutParams = it }
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbar?.setNavigationOnClickListener { _ ->
            context?.hideKeyboard(requireView())
            act.onBackPressed()
        }

        if (vehicle != null && vehicle!!.type != null && vehicle!!.type!!.hasNumber == 0) {
            binding?.npdNumber?.visibility = View.INVISIBLE
            binding?.etModel?.isEnabled = true
        } else if (vehicle != null && vehicle!!.type != null && vehicle!!.type!!.hasNumber != 0) {
            binding?.npdNumber?.visibility = View.VISIBLE
            binding?.npdNumber?.let {
                NumberPlateEditView.Builder(it)
                    .setVehicle(vehicle!!)
                    .build()
            }
        }




        if (vehicle?.make != null) {
            vehicle?.make?.name?.length?.let {
                if (it > 30) {
                    val filter = arrayOf(InputFilter.LengthFilter(vehicle?.make?.name?.length ?: 30))
                    binding?.etMaker?.filters = filter // нужно для отображения больших названий
                }
            }
            binding?.etMaker?.setText(vehicle?.make?.name)
        }
        if (vehicle != null && vehicle!!.type != null && vehicle!!.type!!.hasMakes != 0) {
            binding?.etMaker?.isFocusable = false
            binding?.etMaker?.setOnClickListener { v -> onSelectMake() }

        } else {
            binding?.etMaker?.isFocusable = true
            binding?.etMaker?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) = Unit

                override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                    if (charSequence != null)
                        vehicle?.make?.brandName = charSequence.toString()
                    else
                        vehicle?.model?.modelName = ""
                }

                override fun afterTextChanged(editable: Editable) {
                    binding?.etModel?.isEnabled = true
                }
            })
        }


        if (vehicle?.model != null) {
            vehicle?.model?.name?.length?.let {
                if (it > 30) {
                    val filter = arrayOf(
                        InputFilter.LengthFilter(
                            vehicle?.model?.name?.length
                                ?: 30
                        )
                    )
                    binding?.etModel?.filters = filter // нужно для отображения больших названий
                }
            }
            binding?.etModel?.setText(vehicle!!.model!!.name)
        }

        vehicle?.type?.hasNumber?.let {
            if (it == 0) {
                binding?.npNoNumber?.visible()
                binding?.npNoNumber?.setNameHint(R.string.vehicle_brand_txt)
                binding?.npNoNumber?.setModelHint(R.string.vehicle_model_txt)
                binding?.npNoNumber?.setType(ACCOUNT_TYPE_REGULAR)
                binding?.npNoNumber?.setModel(vehicle?.model?.name ?: "")
                binding?.npNoNumber?.setName(vehicle?.make?.name ?: "")

            } else binding?.npNoNumber?.gone()

        } ?: kotlin.run {
            binding?.npNoNumber?.gone()
        }

        if (vehicle != null && vehicle!!.type != null && vehicle!!.type!!.hasModels != 0) {
            binding?.etModel?.isFocusable = false
            binding?.etModel?.setOnClickListener { v -> onSelectModel() }

        } else {
            binding?.etModel?.isFocusable = true
            binding?.etModel?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) = Unit

                override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                    if (charSequence != null)
                        vehicle!!.model!!.modelName = charSequence.toString()
                    else
                        vehicle!!.model!!.modelName = ""
                }

                override fun afterTextChanged(editable: Editable) = Unit
            })
        }

        if (vehicle?.type != null) {
            vehicle?.type?.getSelectedIcon(NGraphics.getVehicleTypeMap())
                ?.let {
                    RequestOptions.placeholderOf(it)
                }
                ?.let { options ->
                    if (!vehicle?.picture.isNullOrEmpty()) {
                        handleNotEmptyVehicleAvatar()
                    } else {
                        handleEmptyVehicleAvatar()
                    }
                    binding?.ivImage?.let { iv ->
                        Glide.with(this)
                            .load(vehicle?.picture)
                            .apply(options)
                            .apply(RequestOptions.circleCropTransform())
                            .into(iv)
                    }

                }
        } else {
            binding?.ivImage?.let {
                Glide.with(this)
                    .load(vehicle?.picture)
                    .apply(RequestOptions.placeholderOf(R.drawable.vehicle_car))
                    .into(it)
            }
        }

        binding?.etDescription?.setText(vehicle?.description ?: "")

        binding?.tvAddPhoto?.setThrottledClickListener {
            checkMediaPermissions(
                object : PermissionDelegate.Listener {

                    override fun onGranted() {
                        showMediaPickerWithPermissionState(PermissionState.GRANTED)
                    }

                    override fun onDenied() {
                        showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
                    }

                    override fun needOpenSettings() {
                        showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                    }
                }
            )
        }

        setDescriptionListener()
        createVehicle()
        setUpEditVehicleListeners()

        act.permissionListener.add(listener)
    }

    val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        }

    override fun onDestroyView() {
        act.permissionListener.remove(listener)

        super.onDestroyView()
    }

    private fun showMediaPickerWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Avatar,
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            needWithVideo = false,
            showGifs = false,
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imagePathFromPicker ->
                    act.logEditorOpen(
                        uri = imagePathFromPicker,
                        where = AmplitudePropertyWhere.PROFILE,
                        automaticOpen = true
                    )
                    act.getMediaControllerFeature().open(
                        uri = imagePathFromPicker,
                        openPlace = MediaControllerOpenPlace.Avatar,
                        callback = object : MediaControllerCallback {
                            override fun onPhotoReady(
                                resultUri: Uri,
                                nmrAmplitude: NMRPhotoAmplitude?
                            ) {
                                if (nmrAmplitude != null) {
                                    act.logPhotoEdits(
                                        nmrAmplitude = nmrAmplitude,
                                        where = AmplitudePropertyWhere.PROFILE,
                                        automaticOpen = true
                                    )
                                }
                                resultUri.path?.let {
                                    imagePath = it
                                    binding?.ivImage?.let { iv ->
                                        Glide.with(act.applicationContext)
                                            .load(imagePath)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(iv)
                                    }
                                    handleNotEmptyVehicleAvatar()
                                } ?: kotlin.run {
                                    showMediaEditingError()
                                }
                            }

                            override fun onError() {
                                showMediaEditingError()
                            }
                        }
                    )
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK
        )
    }

    private fun showMediaEditingError() {
        NToast.with(view)
            .typeError()
            .text(getString(R.string.error_editing_media))
            .show()
    }


    private fun setUpEditVehicleListeners() {
        binding?.etMaker?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkVehicleIsFilled()
                vehicle?.type?.hasNumber?.let {
                    if (it == 0) binding?.npNoNumber?.setName(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding?.etModel?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkVehicleIsFilled()
                vehicle?.type?.hasNumber?.let {
                    if (it == 0) binding?.npNoNumber?.setModel(s.toString())
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })

        binding?.npdNumber?.addOnValidateListener {
            checkVehicleIsFilled()
        }
        checkVehicleIsFilled()

        binding?.etMaker?.text = binding?.etMaker?.text
        binding?.etModel?.text = binding?.etModel?.text

    }

    private fun handleNotEmptyVehicleAvatar() {
        binding?.tvAddPhoto?.text = getString(R.string.chose_another_photo)
        context?.let {
            binding?.tvAddPhoto?.setTextColor(ContextCompat.getColor(it, R.color.ui_gray))
        }
    }

    private fun handleEmptyVehicleAvatar() {
        binding?.tvAddPhoto?.text = getString(R.string.profile_add_photo)
        context?.let {
            binding?.tvAddPhoto?.setTextColor(ContextCompat.getColor(it, R.color.ui_purple))
        }
    }

    private fun checkVehicleIsFilled() {
        if (vehicle?.type?.hasNumber == 1) {
            if (binding?.etMaker?.text.isNullOrEmpty()
                || binding?.etModel?.text.isNullOrEmpty()
                || binding?.npdNumber?.validate() == false
            ) {
                binding?.tvSend?.setBackgroundResource(R.drawable.btn_not_active_gray_nomera)
            } else {
                binding?.tvSend?.setBackgroundResource(R.drawable.selector_button_vehicle)
            }
        } else {
            if (binding?.etMaker?.text.isNullOrEmpty() || binding?.etModel?.text.isNullOrEmpty()) {
                binding?.tvSend?.setBackgroundResource(R.drawable.btn_not_active_gray_nomera)
            } else {
                binding?.tvSend?.setBackgroundResource(R.drawable.selector_button_vehicle)
            }
        }
    }

    private fun setDescriptionListener() {
        binding?.etDescription?.setOnFocusChangeListener { _, b ->
            if (b) {
                doDelayed(100) {
                    binding?.nsvEditVehicle?.post {
                        run {
                            binding?.nsvEditVehicle?.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                }
            }
        }

        binding?.etDescription?.setOnClickListener {
            doDelayed(100) {
                binding?.nsvEditVehicle?.post {
                    run {
                        binding?.nsvEditVehicle?.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }
    }

    /**
     * Validate fields (should not be empty) and create vehicle
     */
    private fun createVehicle() {
        binding?.tvSend?.setOnClickListener { view ->
            val vType = vehicle!!.type

            if (vType?.typeId != null && (vType.typeId == "1" || vType.typeId == "2")) {
                if (binding?.npdNumber?.validate() == false) {
                    NToast.with(view)
                        .text(getString(R.string.garage_empty_number))
                        .typeAlert()
                        .show()
                    return@setOnClickListener
                }
            }
            // Maker
            if (binding?.etMaker?.text.toString() == "") {
                NToast.with(view)
                    .text(getString(R.string.garage_empty_maker))
                    .typeAlert()
                    .show()
                return@setOnClickListener
            }
            // Model
            if (binding?.etModel?.text.toString() == "") {
                NToast.with(view)
                    .text(getString(R.string.garage_empty_model))
                    .typeAlert()
                    .show()
                return@setOnClickListener
            }
            //  Start update -----------
            binding?.pbEditVehicle?.visibility = View.VISIBLE
            binding?.tvSend?.visibility = View.GONE
            context?.hideKeyboard(requireView())

            if (binding?.npdNumber?.getFullNumberString() != null && ("1" == vehicle?.type?.typeId || "2" == vehicle?.type?.typeId)) {
                vehicle?.number = binding?.npdNumber?.getFullNumberString()
            } else vehicle?.number = null

            if (vehicle?.model != null) {
                if (vehicle?.model?.modelId == null && vehicle?.model?.name != null) {
                    vehicle?.model?.modelName = vehicle?.model?.name
                    vehicle?.modelName = vehicle?.make?.name
                }
            }

            if (vehicle?.make != null) {
                if (vehicle?.make?.makeId == null && vehicle?.make?.name != null) {
                    vehicle?.make?.brandName = vehicle?.make?.name
                    vehicle?.brandName = vehicle?.make?.name
                }
            }

            vehicle?.description = binding?.etDescription?.text.toString()


            if (vehicle != null && vehicle?.vehicleId != null && vehicle?.vehicleId != 0) {
                garagePresenter?.updateVehicle(vehicle, imagePath)
            } else {
                garagePresenter?.addVehicle(vehicle, imagePath)
            }
        }
    }

    override fun onDestroy() {
        if (garagePresenter != null)
            garagePresenter!!.destroy()
        super.onDestroy()
    }

    override fun onCountryList(res: List<Country>) = Unit

    override fun onFail(msg: String) {
        garagePresenter?.deleteVehicleImage(imagePath)
        binding?.pbEditVehicle?.visibility = View.GONE
        binding?.tvSend?.visibility = View.VISIBLE
        Timber.d("VehicleEditFragment onFail $msg")
        NToast.with(view)
            .text(msg)
            .typeError()
            .show()
    }

    override fun onYears(years: CarsYears) = Unit

    override fun onMakes(makes: List<CarsMakes.Make>) {
        val list = ArrayList<ProfileListItem>()

        for (make in makes) {
            val item =
                ProfileListItemImp(make.caption, Objects.requireNonNull<Int>(make.makeId).toString(), make.makeLogo)
            list.add(item)
        }
        carMakerAdapter.setData(list)
        pb_garage_search_dialog.visibility = View.GONE
    }

    fun onSelectMake() {

        garagePresenter?.carMakes("")

        carMakerDialog = Dialog(act, R.style.Theme_AppCompat_Light_Dialog_Alert)
        carMakerDialog?.setContentView(R.layout.dialog_garage_picker)


        rvGender = carMakerDialog!!.findViewById(R.id.rvGender)
        pb_garage_search_dialog = carMakerDialog!!.findViewById(R.id.pb_garage_search_dialog)
        val svSearch = carMakerDialog?.findViewById<SearchView>(R.id.svSearch)
        pb_garage_search_dialog.visibility = View.VISIBLE
        svSearch?.queryHint = act.getString(R.string.garage_dialog_makes_caption)
        svSearch?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                garagePresenter?.carMakes(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                garagePresenter?.carMakes(newText)
                return true
            }
        })

        val llManager = LinearLayoutManager(act)


        carMakerAdapter = ProfileListAdapter.Builder<Any>(act)
            .callback(object : ProfileListCallback() {
                override fun onClick(holder: RecyclerView.ViewHolder) {
                    binding?.etMaker?.filters = arrayOf(InputFilter.LengthFilter(1000))
                    binding?.etMaker?.setText(carMakerAdapter.getItem(holder.adapterPosition)!!.caption)
                    vehicle?.make?.makeId = Integer.parseInt(carMakerAdapter.getItem(holder.adapterPosition)!!.num!!)
                    vehicle?.model?.modelId = null
                    binding?.etModel?.text = null
                    binding?.etModel?.isEnabled = true
                    context?.hideKeyboard(requireView())
                    carMakerDialog?.dismiss()
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val v = LayoutInflater.from(parent.context).inflate(R.layout.item_car_params, parent, false)
                    return CityHolder(v, this)
                }


            })
            .data(ArrayList<Any>())
            .build()


        rvGender.setHasFixedSize(true)
        rvGender.layoutManager = llManager
        rvGender.adapter = carMakerAdapter
        carMakerDialog?.setOnCancelListener { v -> carMakerDialog = null }
        carMakerDialog?.setOnDismissListener { v ->
            context?.hideKeyboard(requireView())
            carMakerDialog = null
        }
        carMakerDialog?.show()
    }

    override fun onModels(models: List<CarsModels.Model>) {
        val list = ArrayList<ProfileListItem>()

        for (model in models) {
            val item = ProfileListItemImp(model.name, model.modelId!!.toString(), model.modelId!!.toString())
            list.add(item)

        }
        pb_garage_search_dialog.visibility = View.GONE
        carMakerAdapter.setData(list)
    }

    override fun onAddVehicle() {
        garagePresenter?.deleteVehicleImage(imagePath)
        act.returnToTargetFragment(0, true)
        act.setUserProfileTab()
    }

    override fun onUpdateVehicle() {
        garagePresenter?.deleteVehicleImage(imagePath)
        act?.onBackPressed()
        act?.onBackPressed()
    }

    override fun onShowErrorMessage(message: String?) {
        binding?.pbEditVehicle?.gone()
        binding?.tvSend?.visible()
        NToast.with(view)
            .typeError()
            .text(message)
            .show()
    }

    override fun onVehicleList(vehicles: Vehicles) = Unit
    override fun onVehicle(vehicle: Vehicle) = Unit
    override fun onDeleteVehicle() = Unit
    override fun onMainVehicle() = Unit
    override fun onTypes(types: VehicleTypes) = Unit

    override fun onGalleryRequestPermissions() {
        setMediaPermissions()
    }

    override fun onGalleryOpenSettings() {
        requireContext().openSettingsScreen()
    }

    override fun onCameraRequestPermissions(fromMediaPicker: Boolean) {
        setPermissionsWithSettingsOpening(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    mediaPicker?.openCamera()
                }

                override fun needOpenSettings() {
                    onCameraOpenSettings()
                }

                override fun onError(error: Throwable?) {
                    onCameraOpenSettings()
                }
            },
            Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    private fun showCameraSettingsDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.camera_settings_dialog_title))
            .setDescription(getString(R.string.camera_settings_dialog_description))
            .setLeftBtnText(getString(R.string.camera_settings_dialog_cancel))
            .setRightBtnText(getString(R.string.camera_settings_dialog_action))
            .setCancelable(false)
            .setRightClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    fun onSelectModel() {
        if (vehicle?.make == null || vehicle?.make?.makeId == null) return

        garagePresenter?.carModels(vehicle?.make?.makeId?.toString() ?: "", "")

        carModelDialog = Dialog(act, R.style.Theme_AppCompat_Light_Dialog_Alert)
        carModelDialog?.setContentView(R.layout.dialog_garage_picker)

        val rvGender = carModelDialog!!.findViewById<RecyclerView>(R.id.rvGender)
        pb_garage_search_dialog = carModelDialog!!.findViewById(R.id.pb_garage_search_dialog)
        val svSearch = carModelDialog!!.findViewById<SearchView>(R.id.svSearch)
        pb_garage_search_dialog.visibility = View.VISIBLE
        svSearch.queryHint = act.getString(R.string.garage_dialog_models_caption)
        svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                garagePresenter!!.carModels(vehicle!!.make!!.makeId!!.toString(), query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                garagePresenter!!.carModels(vehicle!!.make!!.makeId!!.toString(), newText)
                return true
            }
        })
        val llManager = LinearLayoutManager(act)

        carMakerAdapter = ProfileListAdapter.Builder<Any>(act)
            .callback(object : ProfileListCallback() {
                override fun onClick(holder: RecyclerView.ViewHolder) {
                    vehicle?.model?.modelId = Integer.parseInt(carMakerAdapter.getItem(holder.adapterPosition)!!.num!!)
                    binding?.etModel?.filters = arrayOf(InputFilter.LengthFilter(1000))// setting max length to etModel
                    binding?.etModel?.setText(carMakerAdapter.getItem(holder.adapterPosition)!!.caption)
                    context?.hideKeyboard(requireView())
                    carModelDialog?.dismiss()
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val v = LayoutInflater.from(parent.context).inflate(R.layout.item_car_params, parent, false)
                    return CityHolder(v, this)
                }
            })
            .data(ArrayList<Any>())
            .build()
        rvGender.adapter = carMakerAdapter
        rvGender.setHasFixedSize(true)
        rvGender.layoutManager = llManager
        carModelDialog?.setOnCancelListener { v -> carModelDialog = null }
        carModelDialog?.setOnDismissListener { v -> carModelDialog = null }
        carModelDialog?.show()
    }
}

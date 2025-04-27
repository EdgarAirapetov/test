package com.numplates.nomera3.presentation.view.fragments.vehicleedit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedListBuilder
import com.meera.core.extensions.clearText
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.input.BottomTextState
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_CAR
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_MOTO
import com.numplates.nomera3.databinding.MeeraFragmentEditVehicleBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.router.CountriesEnum
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_MODEL
import com.numplates.nomera3.presentation.view.fragments.VEHICLE_EDIT_FRAGMENT_STOP
import com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect.MeeraVehicleBrandModelSelectFragment
import com.numplates.nomera3.presentation.view.fragments.vehicleedit.usecase.MeeraVehicleEditState
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEnum
import com.numplates.nomera3.presentation.view.widgets.numberplateview.getNumberPlateEnum
import com.numplates.nomera3.presentation.view.widgets.numberplateview.maskformatter.MeeraMaskFormatter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


class MeeraVehicleEditFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_edit_vehicle, behaviourConfigState = ScreenBehaviourState.Full
), BasePermission by BasePermissionDelegate(), BaseLoadImages by BaseLoadImagesDelegate(),
    TedBottomSheetPermissionActionsListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentEditVehicleBinding::bind)

    private val vehicle: Vehicle by lazy {
        val argVehicle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getSerializable(ARG_CAR_MODEL, Vehicle::class.java)
        } else {
            @Suppress("DEPRECATION") requireArguments().getSerializable(ARG_CAR_MODEL) as Vehicle?
        }
        argVehicle ?: Vehicle()
    }
    private var imagePath: String? = null
    private var mediaPicker: TedBottomSheetDialogFragment? = null
    private var infoSnackbar: UiKitSnackBar? = null

    var numPlateEnum: NumberPlateEnum? = null
    var currentInput: String = ""
    var maskStr = ""
    private var maskTextWatcher: TextWatcher? = null

    private val viewModel by viewModels<MeeraVehicleEditViewModel> {
        App.component.getViewModelFactory()
    }
    private val act by lazy { requireActivity() as MeeraAct }

    private val glideRequestOption by lazy {
        RequestOptions.circleCropTransform().placeholder(R.drawable.gray_circle_transparent_shape)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initKeyboardBehavior()
        initObservers()
        binding.apply {
            navViewVehicleEdit.backButtonClickListener = {
                findNavController().popBackStack()
            }
            viewModel.loadCountries(vehicle?.type?.typeId == VEHICLE_TYPE_CAR.toString())
            when (vehicle?.type?.typeId) {
                VEHICLE_TYPE_CAR.toString() -> {
                    inputVehicleEditBrand.setHint(getString(R.string.meera_garage_auto_maker_hint))
                    inputVehicleEditModel.setHint(getString(R.string.meera_garage_auto_model_hint))
                    ivBrandArrow.visible()
                    ivModelArrow.visible()
                }

                VEHICLE_TYPE_MOTO.toString() -> {
                    inputVehicleEditBrand.setHint(getString(R.string.meera_garage_moto_maker_hint))
                    inputVehicleEditModel.setHint(getString(R.string.meera_garage_moto_model_hint))
                    val filter = arrayOf(LengthFilterWithError(MODEL_DEFAULT_MAX_LENGTH, onLimitExceeded = {
                        inputVehicleEditModel.setBottomTextState(BottomTextState.Error(getString(R.string.maximal_symbos_30)))
                    }))
                    inputVehicleEditModel.etInput.filters = filter // нужно для отображения больших названий
                    inputVehicleEditModel.etInput.doAfterTextChanged { text ->
                        if ((text?.length ?: 0) < MODEL_DEFAULT_MAX_LENGTH) {
                            inputVehicleEditModel.setBottomTextState(BottomTextState.Empty)
                        }
                    }
                }

                else -> Unit
            }

            vehicle?.country?.let { existCountry ->
                val flagIcon = inputVehicleEditRegPlate.ivIcon
                existCountry.flag?.let { existFlag ->
                    Glide.with(requireContext()).load(existFlag).apply(glideRequestOption)
                        .transition(DrawableTransitionOptions.withCrossFade(200)).into(flagIcon)
                } ?: run {
                    CountriesEnum.entries.firstOrNull { it.id == existCountry.countryId }?.let {
                        Glide.with(requireContext()).load(it.flag).apply(glideRequestOption)
                            .transition(DrawableTransitionOptions.withCrossFade(200)).into(flagIcon)
                    }
                }
                updateNumPlateMask()
            }


            if (vehicle?.type?.hasNumber == 1) {
                inputVehicleEditRegPlate.visibility = View.VISIBLE
                updateNumPlateMask()
                inputVehicleEditRegPlate.etInput.setText(vehicle.number)
                vehicle.number?.let { currentInput = it }
            } else {
                inputVehicleEditRegPlate.visibility = View.INVISIBLE
                inputVehicleEditModel.etInput.isEnabled = true
            }


            if (vehicle?.type?.typeId == VEHICLE_TYPE_CAR.toString() || vehicle?.type?.typeId == VEHICLE_TYPE_MOTO.toString()) {
                inputVehicleEditRegPlate.ivIcon.apply {
                    setThrottledClickListener {
                        onSelectCountry()
                    }
                }
            }

            binding.inputVehicleEditRegPlate.etInput.doAfterTextChanged {
                binding.inputVehicleEditRegPlate.setBottomTextState(BottomTextState.Empty)
                checkVehicleIsFilled()
            }

            if (vehicle?.make != null) {
                vehicle?.make?.name?.length?.let {
                    if (it > MODEL_DEFAULT_MAX_LENGTH) {
                        val filter =
                            arrayOf(InputFilter.LengthFilter(vehicle?.make?.name?.length ?: MODEL_DEFAULT_MAX_LENGTH))
                        inputVehicleEditBrand.etInput.filters = filter // нужно для отображения больших названий
                    }
                }
                inputVehicleEditBrand.etInput.setText(vehicle?.make?.name)
            }
            if (vehicle?.type?.hasMakes?.toBoolean() == true) {
                inputVehicleEditBrand.etInput.isFocusable = false
                inputVehicleEditModel.etInput.isFocusable = false
                inputVehicleEditModel.isEnabled = vehicle.make?.makeId!=null
                inputVehicleEditBrand.etInput.setOnClickListener { v -> onSelectMake() }

            } else {
                inputVehicleEditBrand.etInput.isFocusable = true
                inputVehicleEditModel.etInput.isFocusable = true
                inputVehicleEditBrand.etInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) = Unit

                    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                        if (charSequence != null) vehicle?.make?.brandName = charSequence.toString()
                        else vehicle?.model?.modelName = ""
                    }

                    override fun afterTextChanged(editable: Editable) {
                        inputVehicleEditModel.etInput.isEnabled = true
                    }
                })
            }


            if (vehicle?.model != null) {
                vehicle?.model?.name?.length?.let {
                    if (it > MODEL_DEFAULT_MAX_LENGTH) {
                        val filter = arrayOf(
                            InputFilter.LengthFilter(
                                vehicle?.model?.name?.length ?: MODEL_DEFAULT_MAX_LENGTH
                            )
                        )
                        inputVehicleEditModel.etInput.filters = filter // нужно для отображения больших названий
                    }
                }
                inputVehicleEditModel.etInput.setText(vehicle?.model?.name)
            }

            if (vehicle?.type?.hasModels?.toBoolean() == true) {
                inputVehicleEditModel.isFocusable = false
                inputVehicleEditModel.etInput.setOnClickListener { v -> onSelectModel() }

            } else {
                inputVehicleEditModel.isFocusable = true
                inputVehicleEditModel.etInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) = Unit

                    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                        if (charSequence != null) vehicle?.model?.modelName = charSequence.toString()
                        else vehicle?.model?.modelName = ""
                    }

                    override fun afterTextChanged(editable: Editable) = Unit
                })
            }

            if (vehicle?.type != null) {
                picIconVehicleEdit.let { iv ->
                    Glide.with(requireContext()).load(vehicle?.picture).apply(RequestOptions.circleCropTransform())
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                iv.updateState(true)
                                return false
                            }
                        }).into(iv.imageView)
                }
            } else {
                picIconVehicleEdit.let {
                    Glide.with(requireContext()).load(vehicle?.picture).addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            it.updateState(true)
                            return false
                        }
                    }).into(it.imageView)
                }
            }

            picIconVehicleEdit.findViewById<UiKitButton>(R.id.button_bottom)?.setThrottledClickListener {
                onPickImageClicked()
            }
            picIconVehicleEdit.setThrottledClickListener {
              onPickImageClicked()
            }
            createVehicle()
            setUpEditVehicleListeners()

            childFragmentManager.setFragmentResultListener(
                MeeraVehicleBrandModelSelectFragment.ARG_VEHICLE_BRAND_MODEL_SELECT_REQUEST_KEY, viewLifecycleOwner
            ) { _, bundle ->
                val listType = bundle.getString(MeeraVehicleBrandModelSelectFragment.ARG_LIST_TYPE)
                val selectedId = bundle.getInt(MeeraVehicleBrandModelSelectFragment.ARG_SELECTED_ID)
                val selectedName = bundle.getString(MeeraVehicleBrandModelSelectFragment.ARG_SELECTED_NAME)

                when (listType) {
                    MeeraVehicleBrandModelSelectFragment.ARG_LIST_TYPE_BRANDS -> {
                        inputVehicleEditBrand.etInput.filters =
                            arrayOf(InputFilter.LengthFilter(BRAND_DEFAULT_MAX_LENGTH))
                        inputVehicleEditBrand.etInput.setText(selectedName)
                        vehicle?.make?.makeId = selectedId
                        vehicle?.model?.modelId = null
                        inputVehicleEditModel.etInput.text = null
                        inputVehicleEditModel.isEnabled = true
                        context?.hideKeyboard(requireView())
                    }

                    MeeraVehicleBrandModelSelectFragment.ARG_LIST_TYPE_MODELS -> {
                        vehicle?.model?.modelId = selectedId
                        inputVehicleEditModel.etInput.filters =
                            arrayOf(InputFilter.LengthFilter(BRAND_DEFAULT_MAX_LENGTH))// setting max length to etModel
                        inputVehicleEditModel.etInput.setText(selectedName)
                        context?.hideKeyboard(requireView())
                    }
                }
                checkVehicleIsFilled()
            }
            act.permissionListener.add(listener)
        }
    }

    private fun onPickImageClicked() {
        checkMediaPermissions(object : PermissionDelegate.Listener {

            override fun onGranted() {
                showMediaPickerWithPermissionState(PermissionState.GRANTED)
            }

            override fun onDenied() {
                showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
            }

            override fun needOpenSettings() {
                showMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
            }
        })
    }

    private fun initObservers() {
        viewModel.state.onEach(::handleStates).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleStates(state: MeeraVehicleEditState) {
        when (state) {
            MeeraVehicleEditState.OnAddVehicleSuccess -> onAddVehicle()
            MeeraVehicleEditState.OnUpdateVehicleSuccess -> onUpdateVehicle()
            is MeeraVehicleEditState.MessageError -> onShowErrorMessage(state.message)
            is MeeraVehicleEditState.NumPlateError -> handleNumPlateError(state.message)
            MeeraVehicleEditState.OnLoading -> binding.buttonReady.isEnabled = false
        }
    }

    private fun handleNumPlateError(message: String?) {
        binding.inputVehicleEditRegPlate.setBottomTextState(BottomTextState.Error(errorText = message))
        binding.buttonReady.isEnabled = true
    }

    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
        setFragmentResult(VEHICLE_EDIT_FRAGMENT_STOP, bundleOf(VEHICLE_EDIT_FRAGMENT_STOP to true))
    }


    private fun initKeyboardBehavior() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.clRoot.setMargins(bottom = if (imeVisible) imeHeight else 0)
            insets
        }
    }

    private fun updateNumPlateMask(selectedNumPlate: NumberPlateEnum? = null) {
        numPlateEnum = selectedNumPlate ?: run {
            val existVehicle = vehicle ?: return
            val typeId = existVehicle.type?.typeId?.toIntOrNull() ?: 0
            val countryId = existVehicle.country?.countryId?.toLong() ?: 0L
            getNumberPlateEnum(typeId, countryId)
        }

        val existNumPlateEnum = numPlateEnum ?: return clearNumPlateMasks()

        maskStr = StringBuilder().apply {
            existNumPlateEnum.prefixPattern?.let { append(it) }
            append(existNumPlateEnum.numPattern)
            existNumPlateEnum.suffixPattern?.let { append(it) }
            existNumPlateEnum.regionPattern?.let { append(it) }
        }.toString().uppercase()

        val etRegPlate = binding.inputVehicleEditRegPlate.etInput
        if (etRegPlate.hint.toString() == maskStr) return

        clearNumPlateMasks()
        etRegPlate.apply {
            hint = maskStr
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    setSelection(currentInput.length)
                } else {
                    currentInput = etRegPlate.text.toString()
                }
            }
            setOnTouchListener { v, event ->
                requestFocus()
                showKeyboard()
                true
            }

            maskTextWatcher = MeeraMaskFormatter(
                mask = maskStr.lowercase(),
                maskedField = etRegPlate,
                dynamicMask = binding.inputVehicleEditRegPlate.etInputMask,
                pattern = existNumPlateEnum.regexPattern
            )
            addTextChangedListener(maskTextWatcher)
            setText("")
        }
    }

    private fun clearNumPlateMasks() {
        binding.inputVehicleEditRegPlate.etInput.apply {
            removeTextChangedListener(maskTextWatcher)
            currentInput = ""
            clearText()
        }
        binding.inputVehicleEditRegPlate.etInputMask.text = ""
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

    private fun onSelectCountry() {
        MeeraConfirmDialogUnlimitedListBuilder().setListItems(viewModel.countriesList())
            .setHeader(R.string.garage_select_country).setItemsWithMargins(false).setItemListener {
                if (it is MeeraVehicleCountryItem) {
                    onCountrySelected(it.countryModel)
                    updateNumPlateMask(it.numPlateMask)
                }
            }.show(childFragmentManager)
    }

    private fun onCountrySelected(countryUiModel: RegistrationCountryModel) {
        val country = Country()
        country.name = countryUiModel.name
        country.flag = countryUiModel.flag
        country.countryId = countryUiModel.id
        vehicle?.country = country
        binding.inputVehicleEditRegPlate.ivIcon.let { ivFlag ->
            Glide.with(requireContext()).load(country.flag).apply(glideRequestOption)
                .transition(DrawableTransitionOptions.withCrossFade(200)).into(ivFlag)
        }
    }

    override fun onDestroyView() {
        act.permissionListener.remove(listener)

        super.onDestroyView()
    }

    private fun showMediaPickerWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadSingleImageUri(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Avatar,
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            needWithVideo = false,
            showGifs = false,
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imagePathFromPicker ->
                    act.getMediaControllerFeature().open(uri = imagePathFromPicker,
                        openPlace = MediaControllerOpenPlace.Avatar,
                        callback = object : MediaControllerCallback {
                            override fun onPhotoReady(
                                resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?
                            ) {
                                resultUri.path?.let {
                                    imagePath = it
                                    binding.picIconVehicleEdit.let { iv ->
                                        Glide.with(requireActivity().applicationContext).load(imagePath)
                                            .apply(RequestOptions.circleCropTransform())
                                            .addListener(object : RequestListener<Drawable> {
                                                override fun onLoadFailed(
                                                    e: GlideException?,
                                                    model: Any?,
                                                    target: Target<Drawable>?,
                                                    isFirstResource: Boolean
                                                ): Boolean {
                                                    return false
                                                }

                                                override fun onResourceReady(
                                                    resource: Drawable?,
                                                    model: Any?,
                                                    target: Target<Drawable>?,
                                                    dataSource: DataSource?,
                                                    isFirstResource: Boolean
                                                ): Boolean {
                                                    iv.updateState(true)
                                                    return false
                                                }
                                            }).into(iv.imageView)
                                    }
                                } ?: kotlin.run {
                                    showMediaEditingError()
                                }
                            }

                            override fun onError() {
                                showMediaEditingError()
                            }
                        })
                }),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK
        )
    }

    private fun showMediaEditingError() {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.error_editing_media),
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        )
        infoSnackbar?.show()
    }


    private fun setUpEditVehicleListeners() {
        binding.inputVehicleEditBrand.etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkVehicleIsFilled()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.inputVehicleEditModel.etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkVehicleIsFilled()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })

        checkVehicleIsFilled()

        binding.inputVehicleEditBrand.etInput.text = binding.inputVehicleEditBrand.etInput.text
        binding.inputVehicleEditModel.etInput.text = binding.inputVehicleEditModel.etInput.text

    }

    private fun checkVehicleIsFilled() {
        binding.apply {

            val numberPlateIsValid =
                vehicle?.type?.hasNumber?.toBoolean() == false || inputVehicleEditRegPlate.etInput.text.isNullOrEmpty()
                    .not()

            val brandIsValid = inputVehicleEditBrand.etInput.text.isNullOrEmpty().not()
            val modelIsValid = inputVehicleEditModel.etInput.text.isNullOrEmpty().not()

            buttonReady.isEnabled = numberPlateIsValid && brandIsValid && modelIsValid


        }
    }


    /**
     * Validate fields (should not be empty) and create vehicle
     */
    private fun createVehicle() {
        binding.buttonReady.setOnClickListener { view ->
            val vType = vehicle?.type

            if (vType?.typeId != null && (vType.typeId == "1" || vType.typeId == "2")) {
                if (currentInput.isEmpty()) {
                    UiKitSnackBar.make(
                        view = requireView(), params = SnackBarParams(
                            snackBarViewState = SnackBarContainerUiState(
                                messageText = getText(R.string.garage_empty_number),
                                avatarUiState = AvatarUiState.ErrorIconState,
                            )
                        )
                    ).show()
                    return@setOnClickListener
                }
            }
            // Maker
            if (binding.inputVehicleEditBrand.etInput.text.toString() == "") {
                UiKitSnackBar.make(
                    view = requireView(), params = SnackBarParams(
                        snackBarViewState = SnackBarContainerUiState(
                            messageText = getText(R.string.garage_empty_maker),
                            avatarUiState = AvatarUiState.ErrorIconState,
                        )
                    )
                ).show()
                return@setOnClickListener
            }
            // Model
            if (binding.inputVehicleEditModel.etInput.text.toString() == "") {
                UiKitSnackBar.make(
                    view = requireView(), params = SnackBarParams(
                        snackBarViewState = SnackBarContainerUiState(
                            messageText = getText(R.string.garage_empty_model),
                            avatarUiState = AvatarUiState.ErrorIconState,
                        )
                    )
                ).show()
                return@setOnClickListener
            }
            //  Start update -----------
            context?.hideKeyboard(requireView())

            val regPlate = currentInput
            if (regPlate != null && vehicle?.type?.hasNumber?.toBoolean() == true) {
                vehicle?.number = regPlate
            }

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

            if (vehicle.vehicleId != null && vehicle.vehicleId != 0) {
                viewModel.updateVehicle(vehicle, imagePath)
            } else {
                viewModel.addVehicle(vehicle, imagePath)
            }
        }
    }

    fun onFail(msg: String) {
        binding.buttonReady.isEnabled = true
        Timber.d("VehicleEditFragment onFail $msg")
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = msg,
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        ).show()
    }

    private fun onSelectMake() {
        MeeraVehicleBrandModelSelectFragment.show(
            childFragmentManager,
            R.string.garage_dialog_makes_caption,
            MeeraVehicleBrandModelSelectFragment.ARG_LIST_TYPE_BRANDS,
        )
    }


    private fun onAddVehicle() {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.meera_garage_success_added),
                    avatarUiState = AvatarUiState.SuccessIconState,
                )
            )
        ).show()
        findNavController().popBackStack(R.id.userInfoFragment, false)
    }

    private fun onUpdateVehicle() {
        findNavController().popBackStack(R.id.userInfoFragment, false)
    }

    private fun onShowErrorMessage(message: String?) {
        binding.buttonReady.isEnabled = true
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = message,
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        ).show()
    }

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
            }, Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    private fun showCameraSettingsDialog() {
        ConfirmDialogBuilder().setHeader(getString(R.string.camera_settings_dialog_title))
            .setDescription(getString(R.string.camera_settings_dialog_description))
            .setLeftBtnText(getString(R.string.camera_settings_dialog_cancel))
            .setRightBtnText(getString(R.string.camera_settings_dialog_action)).setCancelable(false)
            .setRightClickListener {
                requireContext().openSettingsScreen()
            }.show(childFragmentManager)
    }

    private fun onSelectModel() {
        val existMakeId = vehicle?.make?.makeId ?: return

        MeeraVehicleBrandModelSelectFragment.show(
            childFragmentManager,
            R.string.garage_dialog_models_caption,
            MeeraVehicleBrandModelSelectFragment.ARG_LIST_TYPE_MODELS,
            existMakeId
        )
    }

    companion object {
        const val MODEL_DEFAULT_MAX_LENGTH = 30
        const val BRAND_DEFAULT_MAX_LENGTH = 1000
    }

}

package com.numplates.nomera3.presentation.view.fragments.userprofileinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.empty
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.NToast
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.getAge
import com.meera.core.utils.showCommonError
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.text.LetterSpacingSpan
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.visible
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.AnimatedAvatarUtils
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoFragmentBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryState
import com.numplates.nomera3.modules.registration.ui.avatar.MeeraSelectAvatarBottomSheetDialog
import com.numplates.nomera3.modules.registration.ui.country.fragment.MeeraCountryPickerBottomSheetDialog
import com.numplates.nomera3.modules.registration.ui.country.fragment.MeeraCountryPickerBottomSheetDialogBuilder
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import com.numplates.nomera3.modules.registration.ui.country.viewmodel.RegistrationCountryViewModel
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.MeeraCityPickerBottomSheetDialog
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.MeeraCityPickerBottomSheetDialogBuilder
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoViewModel
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidationResult
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

const val AVATAR_GENDER_MALE = 0
const val AVATAR_GENDER_FEMALE = 1
private const val DOT_SPACING = 0.14f

enum class UserPersonalInfoItemType(val position: Int) {
    AVATAR(0),
    FULL_NAME(1),
    UNIQUE_NAME(2),
    BIRTHDAY(3),
    GENDER(4),
    COUNTRY(5),
    CITY(6),
    ACCOUNT_MANAGEMENT(7),
    PHONE_NUMBER(8),
    EMAIL(9)
}

class MeeraUserPersonalInfoFragment :
    MeeraBaseDialogFragment(R.layout.meera_user_personal_info_fragment, ScreenBehaviourState.Full),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    TedBottomSheetPermissionActionsListener {

    @Inject
    lateinit var fileManager: FileManager

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraUserPersonalInfoFragmentBinding::bind)

    private val userPersonalInfoViewModel: UserPersonalInfoViewModel by viewModels {
        App.component.getViewModelFactory()
    }
    private val registrationCountryViewModel by viewModels<RegistrationCountryViewModel> {
        App.component.getViewModelFactory()
    }
    private val act: MeeraAct by lazy {
        activity as MeeraAct
    }

    private var recyclerView: RecyclerView? = null
    private var isCalledFromProfile: Boolean = false
    private var doSilentProfileCheckJob: Job? = null

    private val fullNameErrorState = MutableStateFlow<String?>(null)
    private val uniqueNameErrorState = MutableStateFlow<String?>(null)
    private val emailHiddenState = MutableStateFlow<UserEmail?>(null)
    private val phoneHiddenState = MutableStateFlow<UserPhone?>(null)
    private val avatarState = MutableStateFlow<Pair<String?, String?>?>(null)

    private var userPersonalInfoAdapter: MeeraUserPersonalInfoAdapter? = null

    private val listCountry = mutableListOf<RegistrationCountryModel>()
    private var mediaPicker: TedBottomSheetDialogFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initParams(arguments)
        initClickListeners()
        initExistingUserProfile()
        initLiveDataObservers()

        activity?.onBackPressedDispatcher?.addCallback(this) {
            context?.hideKeyboard(requireView())
            if (isCalledFromProfile) findNavController().popBackStack()
        }
        act.permissionListener.add(listener)
    }

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, _, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        act.permissionListener.remove(listener)
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
            },
            Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    private fun createRecyclerView(userProfileModel: UserPersonalInfoContainer) {
        binding.recyclerUserProfileParam.let {
            recyclerView = it
            userPersonalInfoAdapter = MeeraUserPersonalInfoAdapter(
                items = getUserInfoItems(),
                userInfoFromContainer = userPersonalInfoViewModel.getNewUserInfo(),
                lifecycleScope = lifecycleScope,
                actionListener = this::initUserPersonalInfoClickActionListener
            )

            recyclerView?.adapter = userPersonalInfoAdapter
            userPersonalInfoAdapter?.updatePersonalInfoContainer(userProfileModel)
        }
    }

    private fun getUserInfoItems(): List<UserPersonalInfoItemType> {
        val resultList = UserPersonalInfoItemType.entries.toMutableList()
        if (userPersonalInfoViewModel.getNewUserInfo().email != null) {
            resultList.removeAt(UserPersonalInfoItemType.PHONE_NUMBER.position)
        } else {
            resultList.removeAt(UserPersonalInfoItemType.EMAIL.position)
        }
        return resultList
    }

    private fun choosingDateBirth(previousSelectedBirthday: Long?) {
        MeeraBirthdayDatePickerDialog.show(
            context = requireContext(),
            previousSelectedDate = previousSelectedBirthday,
            onDateSelected = this@MeeraUserPersonalInfoFragment::onBirthdayDateSelected
        )
    }

    private fun setupToolbar() {
        binding?.apply {
            personalInfoNavView.title = getString(R.string.editing)
            personalInfoNavView.backButtonClickListener = {
                context?.hideKeyboard(requireView())
                if (isCalledFromProfile) findNavController().popBackStack()
            }
        }
    }

    private fun choosingGender(isMale: Boolean) {
        if (isMale) {
            userPersonalInfoViewModel.setUserGender(isMale = true)
            tryActivateSaveButton()
        } else {
            userPersonalInfoViewModel.setUserGender(isMale = false)
            tryActivateSaveButton()
        }
    }

    private fun changeFullName(fullName: String, errorAction: (errorText: String?) -> Unit) {
        userPersonalInfoViewModel.validateNick(fullName)

        viewLifecycleOwner.lifecycleScope.launch {
            fullNameErrorState.collectLatest { textError ->
                errorAction.invoke(textError)
            }
        }
    }

    private fun changeUniqueName(uniqueName: String, errorAction: (errorText: String?) -> Unit) {
        userPersonalInfoViewModel.validateUserName(uniqueName)

        viewLifecycleOwner.lifecycleScope.launch {
            uniqueNameErrorState.collectLatest { textError ->
                errorAction.invoke(textError)
            }
        }
    }

    private fun clickEmailItem(mail: (userEmail: UserEmail) -> Unit) {
        userPersonalInfoViewModel.updateEmail()
        viewLifecycleOwner.lifecycleScope.launch {
            emailHiddenState.collectLatest {
                it?.let { userEmail ->
                    if (!userEmail.isHidden) {
                        mail.invoke(
                            userEmail.copy(email = getSpannedUserData(userEmail.email ?: "").toString())
                        )
                    } else {
                        mail.invoke(userEmail)
                    }
                }
            }
        }
    }

    private fun clickPhoneItem(phone: (userPhone: UserPhone) -> Unit) {
        userPersonalInfoViewModel.updatePhone()
        viewLifecycleOwner.lifecycleScope.launch {
            phoneHiddenState.collectLatest {
                it?.let { userPhone ->
                    if (!userPhone.isHidden) {
                        phone.invoke(
                            userPhone.copy(
                                phoneNumber = getSpannedUserData(userPhone.phoneNumber ?: "")
                                    .toString()
                            )
                        )
                    } else {
                        phone.invoke(userPhone)
                    }
                }
            }
        }
    }

    private fun loadAvatarItem(avatar: (Pair<String?, String?>?) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            avatarState.collectLatest {
                it?.let { avatar.invoke(it) }
            }
        }
    }

    private fun initUserPersonalInfoClickActionListener(action: UserPersonalInfoAction) {
        when (action) {
            is UserPersonalInfoAction.BirthdayItemClick -> {
                choosingDateBirth(action.tag as Long?)
            }

            is UserPersonalInfoAction.CityItemClick -> {
                val countryId = userPersonalInfoViewModel.getNewUserInfo().countryId//userInfoFromContainer.countryId
                if (countryId != null && countryId > 0) {
                    userPersonalInfoViewModel.getCitySuggestion(countryId.toLong())
                }
            }

            is UserPersonalInfoAction.CountryItemClick -> {
                userPersonalInfoViewModel.countryClicked()
            }

            is UserPersonalInfoAction.DeleteItemClick -> {
                findNavController().safeNavigate(R.id.action_meeraUserPersonalInfoFragment_to_meeraRemoveAccountReasonsFragment)
            }

            is UserPersonalInfoAction.GenderItemClick -> {
                choosingGender(action.isMale)
            }

            is UserPersonalInfoAction.InputFullName -> {
                changeFullName(action.fullName, action.errorAction)
            }

            is UserPersonalInfoAction.InputUniqueName -> {
                changeUniqueName(action.uniqueName, action.errorAction)
            }

            is UserPersonalInfoAction.MailItemClick -> {
                clickEmailItem(action.mail)
            }

            is UserPersonalInfoAction.PhoneItemClick -> {
                clickPhoneItem(action.phone)
            }

            is UserPersonalInfoAction.AvatarItemClick -> {
                showPhotoSourceBottomMenu()
                loadAvatarItem(action.avatar)
            }
        }
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

    private fun initParams(args: Bundle?) {
        args?.let { bundle ->
            isCalledFromProfile = bundle.getBoolean(IArgContainer.ARG_CALLED_FROM_PROFILE)
        }
        userPersonalInfoViewModel.setIsRegistered(isCalledFromProfile)
    }

    private fun initClickListeners() {
        binding.tvSavePersonalInfo.setThrottledClickListener {
            binding.confirmProgressBar.visible()
            binding.tvSavePersonalInfo.invisible()
            userPersonalInfoViewModel.saveProfileOnServer(binding.recyclerUserProfileParam.tag as? String?)
        }
    }

    private fun initLiveDataObservers() {
        userPersonalInfoViewModel.events.observe(viewLifecycleOwner) { newEvent: UserPersonalInfoFragmentEvents? ->
            if (newEvent != null) {
                when (newEvent) {
                    is UserPersonalInfoFragmentEvents.OnNicknameValidated -> onNicknameValidated(
                        newEvent.result,
                        newEvent.nickname
                    )

                    is UserPersonalInfoFragmentEvents.OnUniquenameValidated -> onUniqueNameValidated(
                        newEvent.result,
                        newEvent.userName
                    )

                    is UserPersonalInfoFragmentEvents.OnShowCityDialog -> onShowCityPickerDialog(newEvent.cities)
                    is UserPersonalInfoFragmentEvents.OnShowCountryDialog -> onShowCountryPickerDialog()
                    is UserPersonalInfoFragmentEvents.OnProfileUploaded -> onProfileUploaded(
                        newEvent.result,
                        newEvent.exit
                    )

                    is UserPersonalInfoFragmentEvents.OnAccountEmailUpdated -> onEmailUpdated(newEvent.userEmail)
                    is UserPersonalInfoFragmentEvents.OnAccountPhoneUpdated -> onPhoneUpdated(newEvent.userPhone)
                    is UserPersonalInfoFragmentEvents.OnProfileSaveResult ->
                        handleSaveProfileResult(newEvent.isSuccess, newEvent.isExit)

                    else -> {
                        Timber.i("An event that was not expected")
                    }
                }
            }
        }
    }

    private fun onEmailUpdated(userEmail: UserEmail) {
        emailHiddenState.value = userEmail
    }

    private fun onPhoneUpdated(userPhone: UserPhone) {
        phoneHiddenState.value = userPhone
    }

    private fun getSpannedUserData(userData: String): SpannableString {
        val spannableString = SpannableString(userData)
        try {
            spannableString.setSpan(
                LetterSpacingSpan(DOT_SPACING),
                userData.indexOfFirst { it == '•' || it == ' ' },
                userData.indexOfLast { it == '•' || it == ' ' } + 1,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        } catch (exception: IndexOutOfBoundsException) {
            Timber.e(exception)
        }
        return spannableString
    }

    private fun onProfileUploaded(isUploaded: Boolean, exit: Boolean) {
        if (isUploaded) {
            userPersonalInfoViewModel.getLiveProfile().removeObservers(viewLifecycleOwner)
            userPersonalInfoViewModel.loadProfileAndSaveInDatabase(exit)
        } else {
            showCommonError(getText(R.string.profile_saving_failed), requireView())
            binding.confirmProgressBar.invisible()
            binding.tvSavePersonalInfo.visible()
        }
    }

    private fun handleSaveProfileResult(isSavedInDatabase: Boolean, exit: Boolean) {
        binding.confirmProgressBar.visible()
        binding.tvSavePersonalInfo.invisible()

        if (isSavedInDatabase && exit) {
            context?.hideKeyboard(requireView())
            findNavController().popBackStack()
        }
    }

    private fun onShowCityPickerDialog(cities: List<City>) {
        val countryId = userPersonalInfoViewModel.getNewUserInfo().countryId//userInfoFromContainer.countryId
        if (countryId != null && countryId > 0 && cities.isNotEmpty()) {
            showCityPickerDialog(cities)
        }
    }

    private fun onUniqueNameValidated(result: UniqueUsernameValidationResult, userName: String?) {
        uniqueNameErrorState.value = when (result) {
            UniqueUsernameValidationResult.IsEmpty -> getString(R.string.required_field)
            UniqueUsernameValidationResult.IsTooShort -> getString(R.string.minimal_symbols_3)
            UniqueUsernameValidationResult.IsTooLong -> getString(R.string.maximal_symbols_25)
            UniqueUsernameValidationResult.IsStartedByDot -> getString(R.string.name_cannot_start_with_dot)
            UniqueUsernameValidationResult.IsEndedByDot -> getString(R.string.name_cannot_finish_with_dot)
            UniqueUsernameValidationResult.IsTwoDotOneByOne -> getString(R.string.name_cannot_be_dots)
            UniqueUsernameValidationResult.IsTookByAnotherUser -> getString(R.string.name_already_taken)
            UniqueUsernameValidationResult.IsNotAllowed -> getString(R.string.you_cant_use_this_name)
            UniqueUsernameValidationResult.IsContainsInvalidCharacters -> {
                getString(R.string.meera_contains_invalid_characters_this_name)
            }

            UniqueUsernameValidationResult.IsUnknownError -> getString(R.string.community_general_error)
            UniqueUsernameValidationResult.IsValid -> {
                Timber.i("Validation result IsValid")
                null
            }
        }
        userPersonalInfoViewModel.setUserUsername(userName)
        tryActivateSaveButton()
    }

    private fun onNicknameValidated(validationResult: NicknameValidationResult, nickname: String?) {
        fullNameErrorState.value = when (validationResult) {
            is NicknameValidationResult.IsEmpty -> getString(R.string.required_field)
            is NicknameValidationResult.IsTooLong -> getString(R.string.maximal_symbos_30)
            is NicknameValidationResult.IsUnknownError -> getString(R.string.community_general_error)
            is NicknameValidationResult.IsValid -> {
                userPersonalInfoViewModel.setUserNickname(nickname)
                Timber.i("Validation result IsValid")
                null
            }
        }
        tryActivateSaveButton()
    }

    private fun showCityPickerDialog(cities: List<City>) {
        if (
            childFragmentManager.findFragmentByTag(MeeraCountryPickerBottomSheetDialog::class.simpleName) != null
        ) return
        if (childFragmentManager.findFragmentByTag(MeeraCityPickerBottomSheetDialog::class.simpleName) == null) {
            childFragmentManager.let { manager ->

                val userCitySelectorBottomSheet = MeeraCityPickerBottomSheetDialogBuilder()
                    .setPredefinedCityList(cities)
                    .setDismissListener(object : MeeraCityPickerBottomSheetDialog.UserCitySelectorDismissListener {
                        override fun onDismiss(city: City?) {
                            this@MeeraUserPersonalInfoFragment.onCitySelected(city)
                        }
                    })
                userCitySelectorBottomSheet.show(childFragmentManager)
            }
        }
    }

    private fun onCitySelected(city: City?) {
        city?.let {
            userPersonalInfoViewModel.setUserCity(
                cityId = it.cityId.toLong(),
                cityName = it.title_,
                cityNameTextError = null
            )
            userPersonalInfoAdapter?.updatePersonalInfoContainer(userPersonalInfoViewModel.getNewUserInfo())
            tryActivateSaveButton()
        }
    }

    private fun initExistingUserProfile() {
        val profile = userPersonalInfoViewModel.getNewUserInfo()
        registrationCountryViewModel.loadCountries(RegistrationCountryFromScreenType.Profile)
        lifecycleScope.launch {
            userPersonalInfoViewModel.getLiveProfile().asFlow().combine(
                registrationCountryViewModel.stateLiveData.asFlow()
            ) { userProfile, listCountryFlag ->
                if (profile.nickname.isNullOrEmpty()) {
                    updateCountryFlag(listCountryFlag as RegistrationCountryState.RegistrationCountryList)
                    val gender = userProfile?.gender ?: 1
                    val isMale = gender == 1
                    val birthday = userProfile?.birthday?.times(1000)
                    val countryModel = listCountry.find {
                        it.id == userProfile?.coordinates?.countryId?.toInt()
                    }
                    userProfile?.let {
                        val personalInfo = mapperUserPersonalInfoContainer(userProfile, birthday, isMale, countryModel)
                        initPersonal(userProfile, personalInfo)
                    }
                }
                createRecyclerView(userPersonalInfoViewModel.getNewUserInfo())
            }.first()
        }
    }

    private fun updateCountryFlag(listCountryFlag: RegistrationCountryState.RegistrationCountryList?) {
        listCountryFlag?.let { countries ->
            listCountry.addAll(countries.countries)
        }
        if (userPersonalInfoViewModel.getNewUserInfo().countryFlag == null) {
            val flag = listCountry.find {
                it.id == userPersonalInfoViewModel.getNewUserInfo().countryId?.toInt()
            }?.flag
            userPersonalInfoViewModel.setUserCountryFlag(countryFlag = flag)
            userPersonalInfoAdapter?.updatePersonalInfoContainer(userPersonalInfoViewModel.getNewUserInfo())
        }
    }

    private fun initPersonal(userProfile: UserProfileModel, personalInfo: UserPersonalInfoContainer) {
        userPersonalInfoViewModel.setDefaultUserName(userProfile.uniquename)
        userPersonalInfoViewModel.setOldUserInfo(personalInfo)
        userPersonalInfoViewModel.setNewUserInfo(personalInfo.copy())

        userPersonalInfoViewModel.setHiddenUserPhone(userProfile.phoneNumber)
        userPersonalInfoViewModel.setHiddenUserEmail(userProfile.email)
    }

    private fun mapperUserPersonalInfoContainer(
        userProfile: UserProfileModel,
        birthday: Long?,
        isMale: Boolean,
        countryModel: RegistrationCountryModel?
    ): UserPersonalInfoContainer {
        return UserPersonalInfoContainer(
            photo = userProfile.avatarSmall,
            nickname = userProfile.name,
            username = userProfile.uniquename,
            birthday = birthday,
            birthdayStr = getFormattedBirthdayDate(birthday),
            isMale = isMale,
            countryId = userProfile.coordinates?.countryId,
            countryName = userProfile.coordinates?.countryName,
            countryFlag = countryModel?.flag,
            cityId = userProfile.coordinates?.cityId,
            cityName = userProfile.coordinates?.cityName,
            cityNameTextError = null,
            phoneNumber = userProfile.phoneNumber,
            email = userProfile.email
        )
    }

    private fun onShowCountryPickerDialog() {
        if (parentFragmentManager.findFragmentByTag(MeeraCityPickerBottomSheetDialog::class.simpleName) != null) return
        if (parentFragmentManager.findFragmentByTag(MeeraCountryPickerBottomSheetDialog::class.simpleName) == null) {
            val fragment = MeeraCountryPickerBottomSheetDialogBuilder()
                .setPredefinedCountryList(listCountry)
                .setOnDismissListener(object :
                    MeeraCountryPickerBottomSheetDialog.UserCountrySelectorDismissListener {
                    override fun onDismiss(country: RegistrationCountryModel?) {
                        country?.let {
                            onCountrySelected(country)
                        }
                    }
                })
            fragment.show(parentFragmentManager)
        }
    }

    private fun onCountrySelected(country: RegistrationCountryModel) {
        if (country.id != null) {
            userPersonalInfoViewModel.setUserCountry(
                countryId = country.id.toLong(),
                countryName = country.name,
                countryFlag = country.flag
            )
            userPersonalInfoViewModel.setUserCity(
                cityId = 0L,
                cityName = null,
                cityNameTextError = getString(R.string.required_field)
            )
            userPersonalInfoAdapter?.updatePersonalInfoContainer(userPersonalInfoViewModel.getNewUserInfo())
            tryActivateSaveButton()
        }
    }

    private fun tryActivateSaveButton() {
        doSilentProfileCheckJob?.cancel()
        doSilentProfileCheckJob = userPersonalInfoViewModel.doSilentProfileCheck { isFieldsCorrect ->
            val isProfileChanged = userPersonalInfoViewModel.isProfileChanged()
            if (isFieldsCorrect && isProfileChanged) {
                binding.tvSavePersonalInfo.visible()
                binding.tvSavePersonalInfo.isClickable = true
            } else {
                binding.tvSavePersonalInfo.invisible()
                binding.tvSavePersonalInfo.isClickable = false
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun onBirthdayDateSelected(calendar: Calendar) {
        val userAgeMillis: Long = calendar.timeInMillis
        val userAge: String = getAge(userAgeMillis / 1000)

        try {
            val userAgeInt = if (userAge.isNotEmpty()) userAge.toInt() else 0

            if (userAgeInt < 17) {
                showCommonError(R.string.age_should_not_be_less_than_17)
                tryActivateSaveButton()
                return
            }
            userPersonalInfoViewModel.setUserBirthday(
                birthday = userAgeMillis,
                birthdayStr = getFormattedBirthdayDate(userAgeMillis)
            )
            userPersonalInfoAdapter?.updatePersonalInfoContainer(userPersonalInfoViewModel.getNewUserInfo())
            tryActivateSaveButton()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showCommonError(errorTextRes: Int) {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(errorTextRes),
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
            )

        ).show()
    }

    private fun showPhotoSourceBottomMenu() {
        val photoClickListener = object : MeeraSelectAvatarBottomSheetDialog.PhotoSelectorDismissListener {
            override fun selectPhoto() {
                this@MeeraUserPersonalInfoFragment.openMediaPicker()
            }

            override fun selectAvatar() {
                openAvatarCreator()
            }
        }
        val avatarBottomSheetDialog = MeeraSelectAvatarBottomSheetDialog(photoClickListener)
        avatarBottomSheetDialog.show(parentFragmentManager, MeeraSelectAvatarBottomSheetDialog::class.simpleName)
    }

    private fun openAvatarCreator() {
        observeAvatarChange()
        val avatarState = if (userPersonalInfoViewModel.getNewUserInfo().isMale) {
            AnimatedAvatarUtils.DEFAULT_MALE_STATE
        } else {
            AnimatedAvatarUtils.DEFAULT_FEMALE_STATE
        }
        findNavController().safeNavigate(
            R.id.action_meeraUserPersonalInfoFragment_to_meeraContainerAvatarFragment, bundle = bundleOf(
                IArgContainer.ARG_AVATAR_STATE to avatarState
            )
        )
    }

    private fun observeAvatarChange() {
        setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR
        ) { _, bundle ->
            val avatarState: String = bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener

            userPersonalInfoViewModel.logAvatarCreated(isCalledFromProfile.not())
            userPersonalInfoViewModel.logPhotoSelection(false)
            userPersonalInfoViewModel.saveAvatarInFile(avatarState)
            userPersonalInfoViewModel.setUserAvatarState(avatarState)
            userPersonalInfoViewModel.setAvatarAnimation(avatarState)
            userPersonalInfoViewModel.setUserPhoto(null)
            this@MeeraUserPersonalInfoFragment.avatarState.value = Pair(null, avatarState)
            tryActivateSaveButton()
        }
    }

    private fun openMediaPicker() {
        userPersonalInfoViewModel.openMediaPickerClicked()
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    openMediaPickerWithPermissionState(
                        PermissionState.GRANTED
                    )
                }

                override fun onDenied() {
                    openMediaPickerWithPermissionState(
                        PermissionState.NOT_GRANTED_CAN_BE_REQUESTED
                    )
                }

                override fun needOpenSettings() {
                    openMediaPickerWithPermissionState(
                        PermissionState.NOT_GRANTED_OPEN_SETTINGS
                    )
                }
            }
        )
    }

    private fun openMediaPickerWithPermissionState(
        permissionState: PermissionState
    ) {
        mediaPicker = loadSingleImageUri(
            activity = requireActivity(),
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            cameraType = com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
            suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imageUri -> openImageEditor(imageUri) }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_FRONT
        )
    }

    private fun openImageEditor(selectedImageUri: Uri) {
        act.openPhotoEditorForProfile(
            uri = selectedImageUri,
            listener = object : MediaControllerCallback {
                override fun onPhotoReady(
                    resultUri: Uri,
                    nmrAmplitude: NMRPhotoAmplitude?
                ) {
                    loadUserImage(resultUri.path, null)
                }
            }
        )
    }

    private fun loadUserImage(editedImagePath: String?, animateAvatar: String?) {
        if (editedImagePath != null) {
            avatarState.value = Pair(editedImagePath, animateAvatar)
            userPersonalInfoViewModel.setUserPhoto(editedImagePath)
            userPersonalInfoViewModel.logPhotoSelection(true)
            tryActivateSaveButton()
        } else {
            NToast.with(view)
                .typeError()
                .text(getString(R.string.error_editing_media))
                .show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getFormattedBirthdayDate(birthday: Long?): String {
        return birthday?.let {
            SimpleDateFormat("dd MMMM yyyy").format(it)
        } ?: String.empty()
    }
}

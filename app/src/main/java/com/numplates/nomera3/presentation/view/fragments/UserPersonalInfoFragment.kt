package com.numplates.nomera3.presentation.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.loadGlideRoundedCorner
import com.meera.core.extensions.observeOnce
import com.meera.core.extensions.observeOnceButSkipNull
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setImageDrawable
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.getAge
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.text.LetterSpacingSpan
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.referrals.ui.PopUpGetVipDialogFragment
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.Act
import com.numplates.nomera3.AnimatedAvatarUtils
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.UserPersonalInfoFragmentBinding
import com.numplates.nomera3.modules.avatar.ContainerAvatarFragment
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.country.fragment.KEY_COUNTRY
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFragment
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_AVATAR_STATE
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment.Companion.setupAvatarQuality
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.CityPickerBottomSheetDialog
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.CityPickerBottomSheetDialog.UserCitySelectorDismissListener
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.BirthdayDatePickerDialog
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showCreateAvatarAtRegisterUser
import com.numplates.nomera3.presentation.view.utils.apphints.showCreateAvatarAtUserPersonalInfo
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnAccountEmailUpdated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnAccountPhoneUpdated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnNicknameValidated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnPhotoUploaded
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnProfileUploaded
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnRandomAvatarGenerated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnShowCityDialog
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnShowCountryDialog
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnUniquenameValidated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoViewModel
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidationResult
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Обновленный экран "Ваш профиль" / "Регистрация"
 * макет: https://zpl.io/a8gxv7g
 * переходы: https://overflow.io/s/VSE9HW?node=82a49e01
 * документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1321795792
 */
private const val RADIUS_8 = 8
private const val POSITION_200 = 200
private const val POSITION_20 = 20
const val AVATAR_GENDER_MALE = 0
const val AVATAR_GENDER_FEMALE = 1
private const val DOT_SPACING = 0.14f

class UserPersonalInfoFragment :
    BaseFragmentNew<UserPersonalInfoFragmentBinding>(),
    BasePermission by BasePermissionDelegate(),
    BaseLoadImages by BaseLoadImagesDelegate(),
    IOnBackPressed,
    TedBottomSheetPermissionActionsListener {

    private val viewModel: UserPersonalInfoViewModel by viewModels { App.component.getViewModelFactory() }

    private var isCalledFromProfile: Boolean = false
    private var delayedNicknameValidation: Disposable? = null
    private var delayedUsernameValidation: Disposable? = null
    private var doSilentProfileCheckJob: Job? = null
    private var avatarAnimation: String? = null
    private var shouldGenerateRandomAvatar = false

    private var createAvatarRegisterUserTooltipJob: Job? = null
    private val createAvatarRegisterUserTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_create_avatar_register_user)
    }

    private var createAvatarUserPersonalInfoTooltipJob: Job? = null
    private val createAvatarUserPersonalInfoTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_create_avatar_user_personal)
    }

    private var mediaPicker: TedBottomSheetDialogFragment? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> UserPersonalInfoFragmentBinding
        get() = UserPersonalInfoFragmentBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        makeSquareAvatar()
        initParams(arguments)
        initClickListeners()
        initExistingUserProfile()
        initLiveDataObservers()

        act.permissionListener.add(listener)
    }

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(KEY_COUNTRY) { _, bundle ->
            val country = bundle.getParcelable<RegistrationCountryModel>(KEY_COUNTRY)
            country?.let(this::onCountrySelected)
        }
    }

    override fun onHideHints() {
        hideAllHints()
    }

    override fun onStopFragment() {
        super.onStopFragment()
        binding?.vAvatarView?.stopParallaxEffect()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        binding?.vAvatarView?.startParallaxEffect()
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
        viewModel.setIsRegistered(isCalledFromProfile)
    }

    private fun initClickListeners() {
        binding?.addPhoto?.setOnClickListener {
            showPhotoSourceBottomMenu()
            tryActivateSaveButton()
        }

        binding?.flPhotoContainer?.setOnClickListener {
            showPhotoSourceBottomMenu()
            tryActivateSaveButton()
        }

        binding?.birthday?.setOnClickListener {
            val previousSelectedBirthday = binding?.birthday?.tag as? Long?
            BirthdayDatePickerDialog.show(
                context = requireContext(),
                previousSelectedDate = previousSelectedBirthday,
                onDateSelected = this::onBirthdayDateSelected
            )
        }

        binding?.maleCheckbox?.setOnClickListener {
            binding?.maleCheckbox?.isChecked = true
            binding?.femaleCheckbox?.isChecked = false
            viewModel.setUserGender(isMale = true)
            tryActivateSaveButton()
            generateRandomAvatarIfNeeded(AVATAR_GENDER_MALE)
        }

        binding?.femaleCheckbox?.setOnClickListener {
            binding?.maleCheckbox?.isChecked = false
            binding?.femaleCheckbox?.isChecked = true
            viewModel.setUserGender(isMale = false)
            tryActivateSaveButton()
            generateRandomAvatarIfNeeded(AVATAR_GENDER_FEMALE)
        }

        binding?.countryEdittext?.setThrottledClickListener {
            viewModel.countryClicked()
        }

        binding?.cityEdittext?.setThrottledClickListener {
            val countryId = binding?.countryEdittext?.tag
            if (countryId != null && countryId is Long && countryId > 0) {
                viewModel.getCitySuggestion(countryId.toLong())
            }
        }

        binding?.referralCodeContainer?.setOnClickListener {
            showVipDialog()
        }

        binding?.confirmButton?.setOnClickListener {
            binding?.confirmProgressBar?.visible()
            binding?.confirmButton?.gone()
            viewModel.saveProfileOnServer(binding?.referralCodeContainer?.tag as? String?)
        }

        binding?.backArrow?.setOnClickListener {
            val isRegisteredUser = isCalledFromProfile
            if (isRegisteredUser) {
                exitScreen()
            } else {
                logoutAndBackToMainRoad()
            }
        }

        binding?.createAvatar?.click { openAvatarCreator() }
        binding?.randomAvatar?.click {
            viewModel.generateRandomAvatar(if (isMale()) AVATAR_GENDER_MALE else AVATAR_GENDER_FEMALE)
        }
        binding?.tvDeleteAccount?.click {
            checkAppRedesigned(
                isRedesigned = {},
                isNotRedesigned = { add(RemoveAccountReasonsFragment(), Act.LIGHT_STATUSBAR) }
            )
        }

        binding?.phoneContainer?.click {
            viewModel.updatePhone()
        }
        binding?.emailContainer?.click {
            viewModel.updateEmail()
        }
    }

    private fun generateRandomAvatarIfNeeded(gender: Int) {
        if (shouldGenerateRandomAvatar) {
            viewModel.generateRandomAvatar(gender)
        }
    }

    private fun isMale(): Boolean {
        return binding?.maleCheckbox?.isChecked ?: false
    }

    private fun showVipDialog() {
        val getVipDialog = PopUpGetVipDialogFragment()
        getVipDialog.show(act.supportFragmentManager, "check_code_dialog")
        getVipDialog.successCheckCodeCallback = { code: String ->
            binding?.referralCodeContainer?.tag = code
            binding?.tvReferralCodeLabel?.text = getString(R.string.referral_register_one_day_free)
            binding?.referralCodeContainer?.isClickable = false
            act.hideKeyboard(requireView())
            getVipDialog.dismiss()
        }
    }

    private fun initNicknameInputField() {
        val nicknameFilters = arrayOf(InputFilter.LengthFilter(30))
        binding?.nickname?.editText?.filters = nicknameFilters
        binding?.nickname?.editText?.doAfterTextChanged { typedNick: Editable? ->
            typedNick?.toString()?.let { nickname: String ->
                delayedNicknameValidation?.dispose()
                delayedNicknameValidation = Observable.just(nickname)
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        viewModel.validateNick(it)
                    }
            }
        }
    }


    private fun initUniqueNameInputField() {
        val usernameFilters = arrayOf(InputFilter.LengthFilter(25))
        binding?.username?.editText?.filters = usernameFilters
        binding?.username?.editText?.doAfterTextChanged { typedUserName: Editable? ->
            typedUserName?.toString()?.let { username: String ->
                delayedUsernameValidation?.dispose()
                delayedUsernameValidation = Observable
                    .just(username)
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        viewModel.validateUserName(it)
                    }
            }
        }

        val lowerCaseChanger = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val sourceText = s?.toString() ?: ""
                val sourceLowerCased = sourceText.lowercase(Locale.getDefault())
                if (sourceText == sourceLowerCased) return
                binding?.username?.editText?.removeTextChangedListener(this)
                val typedUniqueName = s?.toString() ?: ""
                binding?.username?.editText?.setText(typedUniqueName.lowercase(Locale.getDefault()))
                binding?.username?.editText?.setSelection(typedUniqueName.length)
                binding?.username?.editText?.addTextChangedListener(this)
            }
        }
        binding?.username?.editText?.addTextChangedListener(lowerCaseChanger)
    }

    private fun initLiveDataObservers() {
        viewModel.events.observe(viewLifecycleOwner) { newEvent: UserPersonalInfoFragmentEvents? ->
            if (newEvent != null) {
                when (newEvent) {
                    is OnNicknameValidated -> onNicknameValidated(newEvent.result)
                    is OnUniquenameValidated -> onUniqueNameValidated(newEvent.result)
                    is OnShowCityDialog -> onShowCityPickerDialog(newEvent.cities)
                    is OnShowCountryDialog -> onShowCountryPickerDialog()
                    is OnPhotoUploaded -> onPhotoUploaded(newEvent.userId)
                    is OnProfileUploaded -> onProfileUploaded(newEvent.result, newEvent.exit)
                    is OnRandomAvatarGenerated -> onRandomAvatarGenerated(newEvent.animatedAvatar)
                    is OnAccountEmailUpdated -> onEmailUpdated(newEvent.userEmail)
                    is OnAccountPhoneUpdated -> onPhoneUpdated(newEvent.userPhone)
                    is UserPersonalInfoFragmentEvents.OnProfileSaveResult ->
                        handleSaveProfileResult(newEvent.isSuccess, newEvent.isExit)
                }
            }
        }
    }

    private fun onEmailUpdated(userEmail: UserEmail) {
        if (userEmail.email.isNullOrBlank()) {
            binding?.emailContainer?.gone()
            binding?.emailDivider?.gone()
        } else {
            if (userEmail.isHidden) {
                binding?.tvEmail?.text = getSpannedUserData(userEmail.email)
                binding?.ivEyeEmail?.setImageDrawable(R.drawable.ic_eye_open)
            } else {
                binding?.tvEmail?.text = userEmail.email
                binding?.ivEyeEmail?.setImageDrawable(R.drawable.ic_eye_close)
            }
        }
    }

    private fun onPhoneUpdated(userPhone: UserPhone) {
        if (userPhone.phoneNumber.isNullOrBlank()) {
            binding?.phoneContainer?.gone()
            binding?.phoneDivider?.gone()
        } else {
            if (userPhone.isHidden) {
                binding?.tvPhone?.text = getSpannedUserData(userPhone.phoneNumber)
                binding?.ivEyePhone?.setImageDrawable(R.drawable.ic_eye_open)
            } else {
                binding?.tvPhone?.text = userPhone.phoneNumber
                binding?.ivEyePhone?.setImageDrawable(R.drawable.ic_eye_close)
            }
        }
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

    private fun observeAvatarChange() {
        (act?.supportFragmentManager as? FragmentManager)?.setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR,
            viewLifecycleOwner
        ) { _, bundle ->
            // get json state
            val avatarState: String =
                bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
            viewModel.logAvatarCreated(isCalledFromProfile.not())
            viewModel.logPhotoSelection(false)
            shouldGenerateRandomAvatar = false
            binding?.randomAvatar?.gone()
            viewModel.saveAvatarInFile(avatarState)
            viewModel.setUserAvatarState(avatarState)
            avatarAnimation = avatarState
            binding?.vAvatarView?.visible()
            binding?.userPhoto?.gone()
            binding?.vAvatarView?.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
            binding?.vAvatarView?.startParallaxEffect()
            binding?.llCreateAvatarBntContainer?.gone()
            binding?.addPhoto?.text = context?.string(R.string.chose_another)
            viewModel.setAvatarAnimation(avatarState)
            viewModel.setUserPhoto(null)
            tryActivateSaveButton()
            //exitScreenWithoutCheck()
        }
    }

    private fun hideAllHints() {
        createAvatarRegisterUserTooltipJob?.cancel()
        createAvatarRegisterUserTooltip?.dismiss()
        createAvatarUserPersonalInfoTooltipJob?.cancel()
        createAvatarUserPersonalInfoTooltip?.dismiss()
    }

    private fun onRandomAvatarGenerated(avatarState: String) {
        viewModel.saveAvatarInFile(avatarState)
        viewModel.setUserAvatarState(avatarState)
        avatarAnimation = avatarState
        binding?.vAvatarView?.visible()
        binding?.userPhoto?.gone()
        binding?.vAvatarView?.setStateAsync(avatarState, viewLifecycleOwner.lifecycleScope)
        binding?.vAvatarView?.startParallaxEffect()
        binding?.addPhoto?.text = context?.string(R.string.chose_another)
        viewModel.setAvatarAnimation(avatarState)
        viewModel.setUserPhoto(null)
    }

    private fun onProfileUploaded(isUploaded: Boolean, exit: Boolean) {
        if (isUploaded) {
            viewModel.getLiveProfile().removeObservers(viewLifecycleOwner)
            viewModel.loadProfileAndSaveInDatabase(exit)
        } else {
            binding?.confirmProgressBar?.visible()
            binding?.confirmButton?.gone()

            NToast.with(view)
                .typeError()
                .text(getString(R.string.profile_saving_failed))
                .show()
        }
    }

    private fun handleSaveProfileResult(isSavedInDatabase: Boolean, exit: Boolean) {
        binding?.confirmProgressBar?.visible()
        binding?.confirmButton?.gone()

        if (isSavedInDatabase && exit) {
            context?.hideKeyboard(requireView())
            exitScreen()
        }
    }

    private fun onPhotoUploaded(userId: Long) {
        setFragmentResult(
            requestKey = ProfilePhotoViewerFragment.PHOTO_VIEWER_RESULT,
            result = bundleOf(ProfilePhotoViewerFragment.CURRENT_POSITION to 0, IArgContainer.ARG_USER_ID to userId)
        )
    }

    private fun onShowCityPickerDialog(cities: List<City>) {
        val countryId = binding?.countryEdittext?.tag
        if (countryId != null && countryId is Long && countryId > 0 && cities.isNotEmpty()) {
            showCityPickerDialog(cities, countryId)
        }
    }

    private fun onUniqueNameValidated(result: UniqueUsernameValidationResult) {
        binding?.username?.reset()
        when (result) {
            UniqueUsernameValidationResult.IsEmpty -> binding?.username?.setError(getString(R.string.required_field))
            UniqueUsernameValidationResult.IsTooShort -> binding?.username?.setError(getString(R.string.minimal_symbols_3))
            UniqueUsernameValidationResult.IsTooLong -> binding?.username?.setError(getString(R.string.maximal_symbols_25))
            UniqueUsernameValidationResult.IsStartedByDot -> binding?.username?.setError(getString(R.string.name_cannot_start_with_dot))
            UniqueUsernameValidationResult.IsEndedByDot -> binding?.username?.setError(getString(R.string.name_cannot_finish_with_dot))
            UniqueUsernameValidationResult.IsTwoDotOneByOne -> binding?.username?.setError(getString(R.string.name_cannot_be_dots))
            UniqueUsernameValidationResult.IsTookByAnotherUser -> binding?.username?.setError(getString(R.string.name_already_taken))
            UniqueUsernameValidationResult.IsNotAllowed -> binding?.username?.setError(getString(R.string.you_cant_use_this_name))
            UniqueUsernameValidationResult.IsUnknownError -> binding?.username?.setError(getString(R.string.community_general_error))
            UniqueUsernameValidationResult.IsValid -> {

            }
            else -> Unit
        }

        viewModel.setUserUsername(binding?.username?.editText?.text.toString())
        tryActivateSaveButton()
    }

    private fun onNicknameValidated(validationResult: NicknameValidationResult) {
        binding?.nickname?.reset()
        when (validationResult) {
            NicknameValidationResult.IsEmpty -> binding?.nickname?.setError(getString(R.string.required_field))
            NicknameValidationResult.IsTooLong -> binding?.nickname?.setError(getString(R.string.maximal_symbos_30))
            NicknameValidationResult.IsUnknownError -> binding?.nickname?.setError(getString(R.string.community_general_error))
            NicknameValidationResult.IsValid -> {

            }
        }

        viewModel.setUserNickname(binding?.nickname?.editText?.text.toString())
        tryActivateSaveButton()
    }

    private fun exitScreen() {
        if (act.getAuthenticationNavigator().isAuthScreenOpen()) {
            viewModel.getLiveProfile().observeOnceButSkipNull(viewLifecycleOwner, { profile ->
                act.getAuthenticationNavigator().completeOnPersonalScreen()
            })
            return
        } else {
            act.getAuthenticationNavigator().completeOnPersonalScreen()
        }
        act.isSubscribeFloorFragment = true
        act.navigatorViewPager.setCurrentItem(act.navigatorViewPager.currentItem - 1, true)
    }

    private fun showCityPickerDialog(cities: List<City>, countryId: Long) {
        if (act.supportFragmentManager.findFragmentByTag(RegistrationCountryFragment::class.simpleName) != null) return
        if (act.supportFragmentManager.findFragmentByTag(CityPickerBottomSheetDialog::class.simpleName) == null) {
            act.supportFragmentManager.let { manager ->

                val userCitySelectorBottomSheet = CityPickerBottomSheetDialog(cities, countryId)

                val onDismissListener = object : UserCitySelectorDismissListener {
                    override fun onDismiss(city: City?) {
                        this@UserPersonalInfoFragment.onCitySelected(city)
                    }
                }

                userCitySelectorBottomSheet.setOnDismissListener(onDismissListener)
                userCitySelectorBottomSheet.show(manager, CityPickerBottomSheetDialog::class.simpleName)
            }
        }
    }

    private fun showCreateAvatarRegisterUserTooltipIfNeeded() {
        if (viewModel.isCreateAvatarRegisterUserHintShown()) return
        createAvatarRegisterUserTooltipJob?.cancel()
        createAvatarRegisterUserTooltip?.dismiss()

        createAvatarRegisterUserTooltipJob = lifecycleScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)
            viewModel.createAvatarRegisterInfoTooltipWasShown()
            binding?.llCreateAvatarBntContainer?.let { view ->
                createAvatarRegisterUserTooltip?.showCreateAvatarAtRegisterUser(
                    fragment = this@UserPersonalInfoFragment,
                    view = view,
                )
            }
            delay(TooltipDuration.COMMON_END_DELAY)
            createAvatarRegisterUserTooltip?.dismiss()
        }

    }

    private fun showCreateAvatarUserPersonalInfoTooltipIfNeeded() {
        if (viewModel.isCreateAvatarUserPersonalInfoHintShown()) return
        createAvatarUserPersonalInfoTooltipJob?.cancel()
        createAvatarUserPersonalInfoTooltip?.dismiss()

        createAvatarUserPersonalInfoTooltipJob = lifecycleScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)
            viewModel.createAvatarUserPersonalInfoTooltipWasShown()
            binding?.addPhoto?.let { view ->
                createAvatarUserPersonalInfoTooltip?.showCreateAvatarAtUserPersonalInfo(
                    fragment = this@UserPersonalInfoFragment,
                    view = view,
                    offsetX = -POSITION_200.dp,
                    offsetY = -POSITION_20.dp
                )
            }
            delay(TooltipDuration.COMMON_END_DELAY)
            createAvatarUserPersonalInfoTooltip?.dismiss()
        }

    }

    private fun onCitySelected(city: City?) {
        city?.let {
            binding?.cityEdittext?.tag = it.cityId.toLong()
            binding?.cityEdittext?.setText(it.title_ ?: it.name ?: "")

            viewModel.setUserCityId(it.cityId.toLong())

            tryActivateSaveButton()
        }
    }

    private fun initExistingUserProfile() {
        setupAvatarQuality(binding?.vAvatarView)
        val isRegisteredUser = isCalledFromProfile
        viewModel.getLiveProfile().observeOnce(viewLifecycleOwner) {
            avatarAnimation = it.avatarAnimation
            val gender = it.gender ?: 1
            val isMale = gender == 1
            val birthday = it.birthday?.times(1000)

            val userInfoFromDatabase = UserPersonalInfoContainer(
                photo = it.avatarSmall,
                nickname = it.name,
                username = it.uniquename,
                birthday = birthday,
                isMale = isMale,
                countryId = it?.coordinates?.countryId,
                cityId = it?.coordinates?.cityId,
                avatarAnimation = it.avatarAnimation
            )

            viewModel.setDefaultUserName(it.uniquename)
            viewModel.setOldUserInfo(userInfoFromDatabase)
            viewModel.setNewUserInfo(userInfoFromDatabase.copy())

            if (it.avatarSmall != null) {
                binding?.addPhoto?.text = getString(R.string.select_other)
            }

            if (isRegisteredUser) {

                val avatarSmall = it.avatarSmall ?: ""
                if (it.avatarAnimation != null && (it.avatarAnimation?.isNotEmpty() ?: false)) {
                    binding?.userPhoto?.gone()
                    binding?.vAvatarView?.visible()
                    binding?.vAvatarView?.setStateAsync(it.avatarAnimation ?: "", viewLifecycleOwner.lifecycleScope)
                    binding?.vAvatarView?.startParallaxEffect()
                    binding?.llCreateAvatarBntContainer?.gone()
                } else if (avatarSmall.isNotEmpty()) {
                    binding?.userPhoto?.loadGlideRoundedCorner(it.avatarBig, RADIUS_8)
                    binding?.userPhoto?.visible()
                    binding?.vAvatarView?.gone()
                    binding?.llCreateAvatarBntContainer?.gone()
                } else {
                    binding?.addPhoto?.text = getString(R.string.user_personal_info_add_photo_text)
                }

                binding?.nickname?.editText?.setText(it.name ?: "")
                binding?.username?.editText?.setText(it.uniquename)
                binding?.usernameLimitationDescription?.text =
                    getString(R.string.username_limitation_for_registered_user)

                binding?.birthday?.setText(getFormattedBirthdayDate(birthday))
                binding?.birthday?.tag = birthday
                viewModel.setUserBirthday(birthday)

                binding?.maleCheckbox?.isChecked = isMale
                binding?.femaleCheckbox?.isChecked = !isMale

                binding?.countryEdittext?.setText(it?.coordinates?.countryName ?: "")
                binding?.countryEdittext?.tag = it?.coordinates?.countryId

                binding?.cityEdittext?.setText(it?.coordinates?.cityName ?: "")
                binding?.cityEdittext?.tag = it?.coordinates?.cityId

                binding?.referralCodeContainer?.gone()
                binding?.backArrow?.visible()
                viewModel.validateNick(it.name ?: "")

                viewModel.setHiddenUserPhone(it.phoneNumber)
                viewModel.setHiddenUserEmail(it.email)

                showCreateAvatarUserPersonalInfoTooltipIfNeeded()
            } else {
                binding?.addPhoto?.text = getString(R.string.user_personal_info_add_photo_text)
                binding?.username?.editText?.setText(it.uniquename)
                binding?.usernameLimitationDescription?.text =
                    getString(R.string.username_limitation_for_unregistered_user)

                binding?.birthday?.setHint(getString(R.string.user_personal_info_birth_hint))

                binding?.maleCheckbox?.isChecked = true
                binding?.femaleCheckbox?.isChecked = false

                binding?.backArrow?.visible()
                act?.navigatorViewPager?.setAllowedSwipeDirection(NavigatorViewPager.SwipeDirection.NONE)

                showCreateAvatarRegisterUserTooltipIfNeeded()
                viewModel.generateRandomAvatar(AVATAR_GENDER_MALE)
                shouldGenerateRandomAvatar = true
                binding?.llCreateAvatarBntContainer?.visible()
            }

            binding?.confirmButton?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_check_mark_inactive
                )
            )
            binding?.confirmButton?.isClickable = false

            initNicknameInputField()
            initUniqueNameInputField()
        }
    }

    private fun makeSquareAvatar() {
        binding?.apply {
            userPhoto.measure(MATCH_PARENT, WRAP_CONTENT)
            val width = userPhoto.measuredWidth
            val params = userPhoto.layoutParams as? FrameLayout.LayoutParams
            params?.height = width
            params?.let { binding?.userPhoto?.layoutParams = it }
        }
    }

    private fun onShowCountryPickerDialog() {
        if (parentFragmentManager.findFragmentByTag(CityPickerBottomSheetDialog::class.simpleName) != null) return
        if (parentFragmentManager.findFragmentByTag(RegistrationCountryFragment::class.simpleName) == null) {
            val fragment = RegistrationCountryFragment.newInstance(
                RegistrationCountryFromScreenType.Profile
            )
            fragment.show(parentFragmentManager, RegistrationCountryFragment::class.simpleName)
        }
    }

    private fun onCountrySelected(country: RegistrationCountryModel) {
        val countryName = country.name
        val countryId = country.id

        if (countryId != null) {
            binding?.countryEdittext?.tag = countryId.toLong()
            binding?.countryEdittext?.setText(countryName)

            viewModel.setUserCountryId(countryId.toLong())

            binding?.cityEdittext?.resetView()
            binding?.cityEdittext?.setHint(getString(R.string.user_personal_info_city_hint))
            binding?.cityEdittext?.tag = 0L

            viewModel.setUserCityId(0L)

            tryActivateSaveButton()
        }
    }

    private fun tryActivateSaveButton() {
        doSilentProfileCheckJob?.cancel()
        doSilentProfileCheckJob = viewModel.doSilentProfileCheck { isFieldsCorrect ->
            val isProfileChanged = viewModel.isProfileChanged()
            if (isFieldsCorrect && isProfileChanged) {
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_mark_active)
                binding?.confirmButton?.setImageDrawable(icon)
                binding?.confirmButton?.isClickable = true
            } else {
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_mark_inactive)
                binding?.confirmButton?.setImageDrawable(icon)
                binding?.confirmButton?.isClickable = false
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun onBirthdayDateSelected(calendar: Calendar) {
        val userAgeMillis: Long = calendar.timeInMillis
        val userAge: String = getAge(userAgeMillis / 1000)

        try {
            val birthdayFormat = SimpleDateFormat("dd.MM.yyyy")
            val userAgeInt = if (userAge.isNotEmpty()) userAge.toInt() else 0

            if (userAgeInt < 17) {
                NToast.with(view)
                    .text(getString(R.string.age_should_not_be_less_than_17))
                    .typeAlert()
                    .show()

                tryActivateSaveButton()
                return
            }

            binding?.birthday?.tag = userAgeMillis
            binding?.birthday?.setText(birthdayFormat.format(calendar.time))
            viewModel.setUserBirthday(userAgeMillis)

            tryActivateSaveButton()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showPhotoSourceBottomMenu() {
        val menu = MeeraMenuBottomSheet(context)
        with(menu) {
            addItem(
                title = R.string.user_personal_info_bottom_menu_select_photo,
                icon = R.drawable.ic_open_gallery,
                click = this@UserPersonalInfoFragment::openMediaPicker
            )
            addItem(
                title = R.string.user_personal_info_bottom_menu_create_avatar,
                icon = R.drawable.ic_avatar_photo,
                click = this@UserPersonalInfoFragment::openAvatarCreator,
            )

            show(act.supportFragmentManager)
        }
    }

    private fun openAvatarCreator() {
        // Такой костыль сделано для того что бы передать пол пользователя, к либе не возможно передать сам пол, ты можешь передать только состояния аватара.
        val avatarState = if (avatarAnimation.isNullOrEmpty()) {
            if (isMale()) AnimatedAvatarUtils.DEFAULT_MALE_STATE else AnimatedAvatarUtils.DEFAULT_FEMALE_STATE
        } else {
            avatarAnimation ?: ""
        }
        Timber.i("UserPersonal AvatarState:${avatarState}")
        viewModel.logAvatarOpen(isCalledFromProfile.not())
        viewModel.noteTimeWhenAvatarOpened()
        add(ContainerAvatarFragment(), Act.LIGHT_STATUSBAR, Arg(ARG_AVATAR_STATE, avatarState))
        observeAvatarChange()
    }

    private fun openMediaPicker() {
        viewModel.openMediaPickerClicked()
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    openMediaPickerWithPermissionState(PermissionState.GRANTED)
                }

                override fun onDenied() {
                    openMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
                }

                override fun needOpenSettings() {
                    openMediaPickerWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        )
    }

    private fun openMediaPickerWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Common,
            cameraType = com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
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
                    val editedImagePath = resultUri.path
                    if (editedImagePath != null) {
                        shouldGenerateRandomAvatar = false
                        binding?.randomAvatar?.gone()
                        avatarAnimation = null
                        viewModel.setAvatarAnimationPhoto(null)
                        binding?.userPhoto?.visible()
                        binding?.vAvatarView?.gone()
                        binding?.userPhoto?.loadGlideRoundedCorner(editedImagePath, 8)
                        viewModel.setUserPhoto(editedImagePath)
                        binding?.addPhoto?.text = getString(R.string.select_other)
                        viewModel.setAvatarAnimation(null)
                        binding?.addPhoto?.text = context?.string(R.string.chose_another)
                        viewModel.logPhotoSelection(true)
                        tryActivateSaveButton()
                        nmrAmplitude?.let(viewModel::logPhotoEdits)
                    } else {
                        NToast.with(view)
                            .typeError()
                            .text(getString(R.string.error_editing_media))
                            .show()
                    }
                }
            }
        )
    }

    override fun onBackPressed(): Boolean {
        context?.hideKeyboard(requireView())
        return if (isCalledFromProfile) {
            exitScreen()
            true
        } else {
            logoutAndBackToMainRoad()
            true
        }
    }

    private fun logoutAndBackToMainRoad() {
        act?.getAuthenticationNavigator()?.backNavigatePersonalScreenByBack()
        act.logOutWithDelegate()
    }

    fun getFormattedBirthdayDate(birthday: Long?): String {
        return birthday?.let {
            SimpleDateFormat("dd.MM.yyyy").format(it)
        } ?: ""
    }
}

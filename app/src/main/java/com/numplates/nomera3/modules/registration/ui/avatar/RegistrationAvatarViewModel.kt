package com.numplates.nomera3.modules.registration.ui.avatar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.extensions.getTimeDifference
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.meera.db.DataStore
import com.meera.db.models.userprofile.UserProfileNew
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.App
import com.numplates.nomera3.data.websocket.STATUS_ERROR
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.di.CACHE_DIR
import com.numplates.nomera3.domain.interactornew.GenerateRandomAvatarParam
import com.numplates.nomera3.domain.interactornew.GenerateRandomAvatarUseCase
import com.numplates.nomera3.domain.interactornew.GetReferralsUseCase
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationAvatarHaveReferral
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationAvatarPhotoType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationGender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.registration.data.Avatar
import com.numplates.nomera3.modules.registration.data.RegistrationCountryCodeMapper
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.domain.GenerateUniqueNameParams
import com.numplates.nomera3.modules.registration.domain.GenerateUniqueNameUseCase
import com.numplates.nomera3.modules.registration.domain.GetInviterIdUseCase
import com.numplates.nomera3.modules.registration.domain.UploadAvatarParams
import com.numplates.nomera3.modules.registration.domain.UploadAvatarUseCase
import com.numplates.nomera3.modules.registration.domain.UploadUserDataUseCase
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.modules.registration.ui.RegistrationBaseViewModel
import com.numplates.nomera3.modules.registration.ui.birthday.getValidAge
import com.numplates.nomera3.presentation.utils.isEditorTempFile
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.AVATAR_GENDER_FEMALE
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.AVATAR_GENDER_MALE
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class RegistrationAvatarViewModel : RegistrationBaseViewModel() {

    val eventLiveData = MutableLiveData<RegistrationAvatarViewEvent>()

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var generateRandomAvatarUseCase: GenerateRandomAvatarUseCase

    @Inject
    lateinit var websocketChannel: WebSocketMainChannel

    @Inject
    lateinit var generateUniqueNameUseCase: GenerateUniqueNameUseCase

    @Inject
    lateinit var processAnimatedAvatar: ProcessAnimatedAvatar

    @Inject
    lateinit var uploadAvatarUseCase: UploadAvatarUseCase

    @Inject
    lateinit var referralUseCase: GetReferralsUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var uploadUserDataUseCase: UploadUserDataUseCase

    @Inject
    lateinit var userUseCase: UserDataUseCase

    @Inject
    lateinit var registrationCountryCodeMapper: RegistrationCountryCodeMapper

    @Inject
    lateinit var getInviterIdUseCase: GetInviterIdUseCase

    @Inject
    @Named(CACHE_DIR)
    lateinit var cacheDir: File

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var amplitudeEditor: AmplitudeEditor

    private var avatarEditStarted: Long = 0
    private var avatarEditTime: String = ""
    private var isMediaPickerOpened = false

    private var generatedUniqueName: String? = null
    private var authType: String? = null
    private var countryNumber: String? = null

    override val userDataUseCase: UserDataUseCase
        get() = userUseCase

    override val uploadUserUseCase: UploadUserDataUseCase
        get() = uploadUserDataUseCase

    override fun userDataInitialized() {
        if (isAvatarNotExist()) generateRandomAvatar()
        else showAvatarOrPhotoIfExist()
    }

    init {
        App.getRegistrationComponent().inject(this)
    }

    fun getGender() = userUseCase.userData?.gender

    fun avatarEditorStarted() {
        avatarEditStarted = System.currentTimeMillis()
    }

    fun avatarEditorFinished() {
        avatarEditTime = getTimeDifference(avatarEditStarted)
    }

    fun setAvatarState(state: String) {
        appSettings.userAvatarState = state
    }

    fun onAvatarEdits(nmrAmplitude: NMRPhotoAmplitude) =
        viewModelScope.launch {
            amplitudeEditor.photoEditorAction(nmrAmplitude)
        }

    fun generateRandomAvatar() {
        viewModelScope.launch(Dispatchers.IO) {
            generateRandomAvatarUseCase.execute(GenerateRandomAvatarParam(
                getGenderForAvatarGenerator()
            ), {
                it.data?.also { data ->
                    Timber.i("Generate Random Avatar success: ${data.animation}, ${data.imageUrl}")
                    deletePhoto(getPhotoToUpload())
                    userUseCase.userData?.avatarAnimation = data.animation
                    userUseCase.userData?.avatarGender = userUseCase.userData?.gender
                    userUseCase.userData?.photo = null
                    event(RegistrationAvatarViewEvent.ShowAvatar(data.animation))
                }
            }, { Timber.e("Generate Random Avatar fail: $it") })
        }
    }

    fun setAuthType(argAuthType: String?) {
        authType = argAuthType
    }

    fun setCountryNumber(argCountryNumber: String?) {
        countryNumber =   registrationCountryCodeMapper.translateCountryNameRuToEn(argCountryNumber)
    }

    fun setAvatarAnimationPhoto(saveAvatarAnimationPath: String?) {
        if (!saveAvatarAnimationPath.isNullOrEmpty() && userUseCase.userData?.photo != null) {
            deletePhoto(userUseCase.userData?.photo)
            userUseCase.userData?.photo = null
        }
        userUseCase.userData?.animatedPhoto = saveAvatarAnimationPath
        userUseCase.userData?.avatarGender = userUseCase.userData?.gender
    }

    fun setAvatarAnimation(avatarState: String?) {
        userUseCase.userData?.avatarAnimation = avatarState
        userUseCase.userData?.avatarGender = userUseCase.userData?.gender
    }

    fun setUserPhoto(editedImagePath: String?) {
        if (!editedImagePath.isNullOrEmpty() && userUseCase.userData?.animatedPhoto != null) {
            deletePhoto(userUseCase.userData?.animatedPhoto)
            userUseCase.userData?.animatedPhoto = null
            userUseCase.userData?.avatarAnimation = null
        }
        userUseCase.userData?.photo = editedImagePath
    }

    fun avatarSet() {
        if (userUseCase.userData?.uniqueName.isNullOrEmpty()) generateUniqueName()
        else userUseCase.userData?.uniqueName?.let {
            event(RegistrationAvatarViewEvent.SetUniqueName(it))
        }
    }

    fun saveAvatarInFile(avatarState: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = processAnimatedAvatar.createBitmap(avatarState)
                val path = processAnimatedAvatar.saveInFile(bitmap)
                Timber.i("Avatar saved path:${path}")
                if (path.isNotEmpty()) {
                    userUseCase.userData?.animatedPhoto = path
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun generateUniqueName() {
        userUseCase.userData?.name?.let { name ->
            viewModelScope.launch(Dispatchers.IO) {
                generateUniqueNameUseCase.execute(params = GenerateUniqueNameParams(name), success = {
                    generatedUniqueName = it
                    event(RegistrationAvatarViewEvent.SetUniqueName(it))
                }, fail = {
                    event(RegistrationAvatarViewEvent.SetUniqueName(null))
                    Timber.e(it)
                })
            }
        }
    }

    fun validateUserName(newUsername: String) {
        userUseCase.userData?.uniqueName = newUsername
        userUseCase.userData?.isUniqueNameValid = false
        viewModelScope.launch(Dispatchers.IO) {
            val offlineValidationResult = UniqueUsernameValidator.validate(newUsername)
            if (offlineValidationResult == UniqueUsernameValidationResult.IsValid) {
                val params = hashMapOf(UNIQNAME to newUsername)
                val response = websocketChannel.isUsernameUnique(params)
                val status = response.payload[STATUS]
                when (status) {
                    STATUS_OK -> {
                        userUseCase.userData?.isUniqueNameValid = true
                        event(RegistrationAvatarViewEvent.UniqueNameValid)
                    }

                    STATUS_ERROR -> {
                        try {
                            val errorExplanation: Any? =
                                (response.payload[RESPONSE] as LinkedTreeMap<*, *>)[USER_MESSAGE]
                            when {
                                errorExplanation != null && errorExplanation is String && errorExplanation.contains(
                                    RESERVED
                                ) -> {
                                    event(RegistrationAvatarViewEvent.UniqueNameNotAllowed)
                                }

                                errorExplanation is String && errorExplanation.contains(ALREADY_BEEN_TAKEN) -> {
                                    event(RegistrationAvatarViewEvent.UniqueNameAlreadyTaken)
                                }

                                else -> {
                                    event(RegistrationAvatarViewEvent.UniqueNameNotAllowed)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                            event(RegistrationAvatarViewEvent.UniqueNameNotAllowed)
                        }
                    }
                }
            } else {
                event(
                    RegistrationAvatarViewEvent.UniqueNameValidated(
                        offlineValidationResult
                    )
                )
            }
        }
    }

    fun setReferralCode(code: String?) {
        userUseCase.userData?.referralCode = code
    }

    fun openMediaPickerClicked() {
        isMediaPickerOpened = true
        amplitudeHelper.logAvatarPickerOpen()
    }

    fun closedMediaPicker() {
        isMediaPickerOpened = false
    }

    fun isMediaPickerOpened(): Boolean {
        return isMediaPickerOpened
    }

    fun isAvatarNotExist(): Boolean {
        return userUseCase.userData?.avatar?.big.isNullOrEmpty()
            && userUseCase.userData?.avatarAnimation.isNullOrEmpty()
            && userUseCase.userData?.animatedPhoto.isNullOrEmpty()
            && userUseCase.userData?.photo.isNullOrEmpty()

    }

    fun isUniqueNameValid(): Boolean {
        return userUseCase.userData?.isUniqueNameValid == true
    }

    fun clearAll() {
        event(RegistrationAvatarViewEvent.None)
        finishRegistrationSuccess()
    }

    fun uploadProfileData() {
        progress(true)
        val photoToUpload = getPhotoToUpload() ?: return
        uploadUserPhoto(photoToUpload, userUseCase.userData?.avatarAnimation)
    }

    private fun getGenderForAvatarGenerator(): Int {
        return if (userUseCase.userData?.gender == RegistrationUserData.GENDER_FEMALE) AVATAR_GENDER_FEMALE
        else AVATAR_GENDER_MALE
    }

    private fun showAvatarOrPhotoIfExist() {
        val avatar = userUseCase.userData?.avatar
        val photo = userUseCase.userData?.photo
        val animatedAvatar = userUseCase.userData?.avatarAnimation
        when {
            avatar != null && !avatar.big.isNullOrEmpty() -> checkAvatarIsAnimated(avatar)
            !photo.isNullOrEmpty() -> event(RegistrationAvatarViewEvent.ShowPhoto(photo))
            !animatedAvatar.isNullOrEmpty() -> checkAvatarGender(animatedAvatar)
        }
    }

    private fun checkAvatarIsAnimated(avatar: Avatar) {
        if (avatar.animation.isNullOrEmpty()) {
            avatar.big?.let { event(RegistrationAvatarViewEvent.ShowPhoto(it)) }
        } else {
            checkAvatarGender(avatar.animation)
        }
    }

    private fun checkAvatarGender(avatar: String) {
        if (userUseCase.userData?.gender != userUseCase.userData?.avatarGender) generateRandomAvatar()
        else event(RegistrationAvatarViewEvent.ShowAvatar(avatar))
    }

    private fun getPhotoToUpload(): String? {
        return userUseCase.userData?.photo ?: userUseCase.userData?.animatedPhoto
    }

    private fun uploadUserPhoto(path: String, avatarState: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            uploadAvatarUseCase.execute(params = UploadAvatarParams(
                imagePath = path, avatarAnimation = avatarState
            ), success = {
                userUseCase.userData?.avatar = Avatar(big = it.avatarBig, small = it.avatarSmall, it.avatarAnimation)
                uploadUserData(
                    RegistrationUserData(
                        avatar = userUseCase.userData?.avatar,
                        uniqueName = userUseCase.userData?.uniqueName,
                    )
                )
            }, fail = {
                progress(false)
                Timber.e(it)
            })
        }
    }

    private fun deletePhoto(path: String?) {
        if (path.isNullOrEmpty()) return
        try {
            if (path.isEditorTempFile(cacheDir)) fileManager.deleteFile(path)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun uploadSuccess(userProfile: UserProfileNew) {
        deletePhoto(getPhotoToUpload())
        saveProfileInDatabase(userProfile)
    }

    private fun saveProfileInDatabase(profile: UserProfileNew) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.userProfileDao().insert(profile)
            if (!userUseCase.userData?.referralCode.isNullOrEmpty()) {
                userUseCase.userData?.referralCode?.let { registerReferral(it) }
            } else {
                finishRegistrationSuccess()
            }
        }
    }

    private fun registerReferral(refCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = referralUseCase.registerReferralCode(refCode)
            appSettings.getReferralVip = (result.data != null)
            finishRegistrationSuccess()
        }
    }

    private fun finishRegistrationSuccess() {
        val photoType = when {
            userUseCase.userData?.photo != null -> AmplitudePropertyRegistrationAvatarPhotoType.PHOTO
            userUseCase.userData?.avatarAnimation != null -> AmplitudePropertyRegistrationAvatarPhotoType.AVATAR
            else -> AmplitudePropertyRegistrationAvatarPhotoType.EMPTY
        }
        val haveReferral = if (userUseCase.userData?.referralCode.isNullOrEmpty()) {
            AmplitudePropertyRegistrationAvatarHaveReferral.FALSE
        } else {
            AmplitudePropertyRegistrationAvatarHaveReferral.TRUE
        }
        appSettings.writeIsWorthToShow(true)
        amplitudeHelper.logRegistrationPhotoUniqueName(photoType, avatarEditTime, haveReferral)
        userUseCase.clear()
        logRegistrationCompleted()
        event(RegistrationAvatarViewEvent.FinishRegistration)
    }

    private fun logRegistrationCompleted() {
        GlobalScope.launch(Dispatchers.IO) {
            val inviterId = getInviterIdUseCase.invoke()

            val age = userUseCase.userData?.birthday?.getValidAge() ?: 0

            val gender = if (userUseCase.userData?.gender == 1) AmplitudePropertyRegistrationGender.MALE
            else AmplitudePropertyRegistrationGender.FEMALE

            val photoType =
                if (userUseCase.userData?.animatedPhoto == null) AmplitudePropertyRegistrationAvatarPhotoType.PHOTO
                else AmplitudePropertyRegistrationAvatarPhotoType.AVATAR

            val countryNumberArg =
                if (authType == AmplitudePropertyInputType.EMAIL.property) authType else countryNumber

            val countryName =userUseCase.userData?.country?.name

            amplitudeHelper.logRegistrationCompleted(
                regType = authType ?: "",
                countryNumber = countryNumberArg ?: "",
                age = age,
                hideAge = userUseCase.userData?.hideAge ?: false,
                gender = gender,
                hideGender = userUseCase.userData?.hideGender ?: false,
                country = countryName ?: "",
                city = userUseCase.userData?.city?.title_ ?: "",
                photoType = photoType,
                uniqueNameChange = generatedUniqueName != userUseCase.userData?.uniqueName,
                haveReferral = userUseCase.userData?.referralCode != null,
                inviterId = inviterId ?: -1,
            )
        }

    }

    private fun event(event: RegistrationAvatarViewEvent) {
        eventLiveData.postValue(event)
    }

    companion object {

        private const val UNIQNAME = "uniqname"
        private const val STATUS = "status"
        private const val RESPONSE = "response"
        private const val USER_MESSAGE = "user_message"
        private const val RESERVED = "reserved"
        private const val ALREADY_BEEN_TAKEN = "already been taken"

    }
}

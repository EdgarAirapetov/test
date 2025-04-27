package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.data.websocket.STATUS_ERROR
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.domain.interactornew.CitySuggestionUseCase
import com.numplates.nomera3.domain.interactornew.GenerateRandomAvatarParam
import com.numplates.nomera3.domain.interactornew.GenerateRandomAvatarUseCase
import com.numplates.nomera3.domain.interactornew.GetReferralsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.IsCreateAvatarRegisterUserHintShown
import com.numplates.nomera3.domain.interactornew.IsCreateAvatarUserPersonalInfoHintShownUseCase
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.domain.interactornew.SetUserDateOfBirthUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAnimatedAvatarFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.DATE_OF_BIRTH
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.FIRST_OPEN_DAY
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.FIRST_OPEN_MONTH
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.FIRST_OPEN_WEEK
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.USER_NAME
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.modules.user.domain.usecase.GetUserEmailUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserPhoneUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UploadUserAvatarUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UserUploadAvatarParams
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.UpdateOwnUserProfileUseCase
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnAccountEmailUpdated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnAccountPhoneUpdated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnNicknameValidated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnProfileUploaded
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnRandomAvatarGenerated
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnShowCityDialog
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnShowCountryDialog
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoFragmentEvents.OnUniquenameValidated
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.BirthdayValidator
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidationResult
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidationResult.IsUnknownError
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidationResult.IsValid
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidator
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidator
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserPersonalInfoViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val isCreateAvatarRegisterUserHintShown: IsCreateAvatarRegisterUserHintShown,
    private val isCreateAvatarUserPersonalInfoHintShown: IsCreateAvatarUserPersonalInfoHintShownUseCase,
    private val citySuggestionUseCase: CitySuggestionUseCase,
    private val websocketChannel: WebSocketMainChannel,
    private val uploadAvatarUseCase: UploadUserAvatarUseCase,
    private val referralUseCase: GetReferralsUseCase,
    private val analyticsInteractor: AnalyticsInteractor,
    private val refreshOwnProfileUseCase: UpdateOwnUserProfileUseCase,
    private val observeLocalOwnUserProfileUseCase: ObserveLocalOwnUserProfileModelUseCase,
    private val generateRandomAvatarUseCase: GenerateRandomAvatarUseCase,
    private val getUserEmailUseCase: GetUserEmailUseCase,
    private val getUserPhoneUseCase: GetUserPhoneUseCase,
    private val processAnimatedAvatar: ProcessAnimatedAvatar,
    private val setUserDateOfBirthUseCase: SetUserDateOfBirthUseCase,
    private val fileManager: FileManager,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudeEditor: AmplitudeEditor
) : BaseViewModel() {

    val events = SingleLiveEvent<UserPersonalInfoFragmentEvents>()

    private val disposables = CompositeDisposable()
    private var nicknameValidationDisposable: Disposable? = null
    private var uploadUserPhotoDisposable: Disposable? = null
    private var newUserInfo = UserPersonalInfoContainer()
    private var oldUserInfo = UserPersonalInfoContainer()
    private var defaultUserName = ""
    private var isUserRegistered = true
    private var avatarEditorOpenedTimeStamp: Long = -1L
    private var userPhone = UserPhone()
    private var userEmail = UserEmail()
    private var hiddenEmail: String? = null
    private var hiddenPhone: String? = null
    private var email: String? = null
    private var phone: String? = null


    fun getLiveProfile() = observeLocalOwnUserProfileUseCase.invoke().asLiveData()

    fun setUserAvatarState(state: String) {
        appSettings.userAvatarState = (state)
    }

    fun setIsRegistered(calledFromProfile: Boolean) {
        //if called from profile then registered
        isUserRegistered = calledFromProfile
    }

    fun countryClicked() {
        events.postValue(OnShowCountryDialog)
    }

    fun isCreateAvatarRegisterUserHintShown() = isCreateAvatarRegisterUserHintShown.invoke()

    fun isCreateAvatarUserPersonalInfoHintShown() = isCreateAvatarUserPersonalInfoHintShown.invoke()

    fun getCitySuggestion(countryId: Long?, query: String? = null) {
        if (countryId != null) {
            val getCitySuggestionDisposable = citySuggestionUseCase
                .getCitySuggestionOrEmptyList(countryId, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { events.value = OnShowCityDialog(it) },
                    { Timber.e(it) }
                )

            disposables.addAll(getCitySuggestionDisposable)
        }
    }

    fun loadProfileAndSaveInDatabase(exit: Boolean) {
        viewModelScope.launch {
            kotlin.runCatching {
                refreshOwnProfileUseCase()
            }.onSuccess {
                events.postValue(UserPersonalInfoFragmentEvents.OnProfileSaveResult(true, exit))
            }.onFailure {
                events.postValue(UserPersonalInfoFragmentEvents.OnProfileSaveResult(false, exit))
            }
        }
    }

    fun validateNick(newNick: String) {
        nicknameValidationDisposable?.dispose()
        nicknameValidationDisposable = Observable.just(newNick)
            .subscribeOn(Schedulers.io())
            .map { nick: String -> NicknameValidator.validate(nick) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result: NicknameValidationResult ->
                    events.value = OnNicknameValidated(result, newNick)
                },
                { exception: Throwable? ->
                    exception?.printStackTrace()
                    events.value = OnNicknameValidated(IsUnknownError)
                }
            )
        nicknameValidationDisposable?.let { disposables.addAll(it) }
    }

    fun validateUserName(newUsername: String) {
        if (newUsername == defaultUserName) {
            events.postValue(OnUniquenameValidated(UniqueUsernameValidationResult.IsValid, newUsername))
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val offlineValidationResult = UniqueUsernameValidator.validate(newUsername)
                if (offlineValidationResult == UniqueUsernameValidationResult.IsValid) {
                    val params = hashMapOf("uniqname" to newUsername)
                    val response = websocketChannel.isUsernameUnique(params)
                    val status = response.payload["status"]
                    when (status) {
                        STATUS_OK -> {
                            events.postValue(OnUniquenameValidated(UniqueUsernameValidationResult.IsValid, newUsername))
                        }

                        STATUS_ERROR -> {
                            try {
                                val errorExplanation: Any? =
                                    (response.payload["response"] as LinkedTreeMap<*, *>)["user_message"]
                                if (errorExplanation != null && errorExplanation is String && errorExplanation.contains(
                                        "reserved"
                                    )
                                ) {
                                    events.postValue(OnUniquenameValidated(UniqueUsernameValidationResult.IsNotAllowed))
                                } else {
                                    events.postValue(OnUniquenameValidated(UniqueUsernameValidationResult.IsTookByAnotherUser))
                                }
                            } catch (error: Exception) {
                                Timber.e(error)
                                events.postValue(OnUniquenameValidated(UniqueUsernameValidationResult.IsTookByAnotherUser))
                            }
                        }
                    }
                } else {
                    events.postValue(OnUniquenameValidated(offlineValidationResult))
                }
            }
        }
    }

    fun doSilentProfileCheck(callback: (Boolean) -> Unit): Job {
        return runWithDispatcherIORaw(
            coroutine = {
                val nickValidateResult = NicknameValidator.validate(newUserInfo.nickname)
                if (nickValidateResult != IsValid) {
                    return@runWithDispatcherIORaw false
                }

                if (!BirthdayValidator.validate(newUserInfo.birthday)) {
                    return@runWithDispatcherIORaw false
                }

                if (newUserInfo.countryId == null || newUserInfo.countryId == 0L) {
                    return@runWithDispatcherIORaw false
                }

                if (newUserInfo.cityId == null || newUserInfo.cityId == 0L) {
                    return@runWithDispatcherIORaw false
                }

                if (newUserInfo.avatarAnimation.isNullOrEmpty() && newUserInfo.photo.isNullOrEmpty()) {
                    return@runWithDispatcherIORaw false
                }

                val nameValidateResult = UniqueUsernameValidator.validate(newUserInfo.username)
                if (nameValidateResult == UniqueUsernameValidationResult.IsValid) {
                    if (newUserInfo.username != defaultUserName) {
                        val params = hashMapOf("uniqname" to newUserInfo.username)
                        val response = withContext(Dispatchers.Default) { websocketChannel.isUsernameUnique(params) }
                        return@runWithDispatcherIORaw response.payload["status"] != STATUS_ERROR
                    }
                } else {
                    return@runWithDispatcherIORaw false
                }


                return@runWithDispatcherIORaw true
            },
            onStart = {

            },
            onSuccess = {
                callback(it)
            },
            onError = {
                callback(false)
            }
        )
    }

    fun isProfileChanged(): Boolean {
        return oldUserInfo != newUserInfo
    }

    fun createAvatarUserPersonalInfoTooltipWasShown() {
        appSettings.isCreateAvatarUserPersonalInfoHintShown = true
        appSettings.markTooltipAsShownSession(AppSettings.CREATE_AVATAR_USER_PERSONAL_INFO_HINT_SHOWN)
    }

    fun createAvatarRegisterInfoTooltipWasShown() {
        appSettings.isCreateAvatarRegisterUserHintShown = true
        appSettings.markTooltipAsShownSession(AppSettings.CREATE_AVATAR_REGISTER_USER_HINT_SHOWN)
    }

    fun saveProfileOnServer(refCode: String?, exitFromScreenAfterSave: Boolean = true) {
        // формат newUserInfo.birthday переводится в секунды только здесь
        val birthday = TimeUnit
            .MILLISECONDS
            .toSeconds(newUserInfo.birthday ?: 0)
            .toInt()

        val payload: HashMap<String, Any?> = hashMapOf(
            "name" to newUserInfo.nickname,
            "uniqname" to newUserInfo.username,
            "city_id" to newUserInfo.cityId,
            "birthday" to birthday, // Seconds
            "country_id" to newUserInfo.countryId,
            "gender" to if (newUserInfo.isMale) 1 else 0
        )

        newUserInfo.username?.let { appSettings.writeUniquieName(it) }

        var photoToUpload = ""
        if (newUserInfo.photo != null) {
            val newUserPhoto = newUserInfo.photo
            val oldUserPhoto = oldUserInfo.photo
            if (newUserPhoto != null && newUserPhoto != oldUserPhoto) {
                photoToUpload = newUserPhoto
            }
        } else {
            val newUserPhoto = newUserInfo.animatedPhoto
            val oldUserPhoto = oldUserInfo.animatedPhoto
            if (newUserPhoto != null && newUserPhoto != oldUserPhoto) {
                photoToUpload = newUserPhoto
            }
        }
        if (photoToUpload.isNotEmpty()) {
            uploadUserPhoto(photoToUpload, newUserInfo.avatarAnimation) { _ ->
                events.postValue(UserPersonalInfoFragmentEvents.OnPhotoUploaded(getUserUidUseCase.invoke()))
                uploadUserData(payload, refCode, exitFromScreenAfterSave) {
                    analyticsInteractor.identifyUserProperty {
                        it.apply {
                            set(USER_NAME, newUserInfo.nickname)
                            set(DATE_OF_BIRTH, birthday)

                            val c = Calendar.getInstance()
                            c.time = Date()

                            setOnce(FIRST_OPEN_DAY, c.get(Calendar.DAY_OF_WEEK))
                            setOnce(FIRST_OPEN_WEEK, c.get(Calendar.WEEK_OF_MONTH))
                            setOnce(FIRST_OPEN_MONTH, c.get(Calendar.MONTH))
                        }
                    }
                }
            }
        } else {
            uploadUserData(payload, refCode, exitFromScreenAfterSave) {
                analyticsInteractor.identifyUserProperty {
                    it.apply {
                        set(USER_NAME, newUserInfo.nickname)
                        set(DATE_OF_BIRTH, birthday)
                    }
                }
            }
        }
    }

    private fun uploadUserData(
        payload: Map<String, Any?>,
        refCode: String?,
        exitFromScreenAfterSave: Boolean = true,
        action: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val responseUpdateProfile = websocketChannel.pushUpdateUserProfileSuspend(payload)
                val r1 = responseUpdateProfile.payload["status"] as? Map<String, Any>?

                if (r1 != null) {
                    val r2 = r1["reponse"] as? Map<String, Any>?
                    if (r2 != null) {
                        val r3 = r2["id"]
                        if (r3 != null) {
                            val id = r3 as? String?
                            if (id != null) {
                                action()
                            }
                        }
                    }
                }

                when (responseUpdateProfile.payload["status"]) {
                    STATUS_OK -> {
                        delay(1000)

                        refCode?.let {
                            val response = referralUseCase.registerReferralCode(it)
                            appSettings.getReferralVip = (response.data != null)
                        }
                        val dateOfBirth = payload["birthday"] as Int
                        setUserDateOfBirthUseCase.invoke(dateOfBirth)
                        events.postValue(OnProfileUploaded(true, exitFromScreenAfterSave))
                    }

                    STATUS_ERROR -> {
                        events.postValue(OnProfileUploaded(false, exitFromScreenAfterSave))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                events.postValue(OnProfileUploaded(false, exitFromScreenAfterSave))
            }
        }
    }

    fun saveAvatarInFile(avatarState: String) {
        Observable.just(avatarState)
            .flatMap {
                Observable.fromCallable {
                    val bitmap = processAnimatedAvatar.createBitmap(avatarState)
                    processAnimatedAvatar.saveInFile(bitmap)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ path ->
                Timber.i("Avatar saved path:${path}")
                if (path.isNotEmpty()) {
                    setAvatarAnimationPhoto(path)
                }
            }, { error ->
                Timber.i("Save avatar in file failed:${error}")
            }).addDisposable()
    }

    private fun uploadUserPhoto(path: String, avatarState: String?, callback: (Boolean) -> Unit) {
        uploadUserPhotoDisposable?.dispose()
        uploadUserPhotoDisposable = uploadAvatarUseCase
            .execute(UserUploadAvatarParams(path, avatarState))
            .map {
                fileManager.deleteFile(path)
                it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response ->
                    val isProfilePhotoEmpty = response.data
                        ?.avatarSmall
                        ?.isEmpty()
                        ?: false
                    callback(isProfilePhotoEmpty)
                },
                { error ->
                    Timber.e(error)
                    callback(false)
                }
            )
        uploadUserPhotoDisposable?.let { disposables.add(it) }
    }

    fun generateRandomAvatar(gender: Int) {
        viewModelScope.launch {
            generateRandomAvatarUseCase.execute(GenerateRandomAvatarParam(gender),
                { response ->
                    if (response.data != null) {
                        Timber.i("Generate Random Avatar success:${response.data.animation},${response.data.imageUrl}")
                        events.postValue(OnRandomAvatarGenerated(response.data.animation, response.data.imageUrl))
                    }
                }, { err ->
                    Timber.i("Generate Random Avatar failed:${err}")
                })
        }
    }

    fun setOldUserInfo(userInfo: UserPersonalInfoContainer) {
        oldUserInfo = userInfo
    }

    fun setNewUserInfo(userInfo: UserPersonalInfoContainer) {
        newUserInfo = userInfo
    }

    fun getNewUserInfo() = newUserInfo

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun setAvatarAnimationPhoto(saveAvatarAnimationPath: String?) {
        newUserInfo = newUserInfo.copy(
            animatedPhoto = saveAvatarAnimationPath
        )
    }

    fun setUserPhoto(editedImagePath: String?) {
        newUserInfo = newUserInfo.copy(
            photo = editedImagePath
        )
    }

    fun setUserNickname(nickname: String?) {
        newUserInfo = newUserInfo.copy(
            nickname = nickname
        )
    }

    fun setUserUsername(username: String?) {
        newUserInfo = newUserInfo.copy(
            username = username
        )
    }

    fun setUserGender(isMale: Boolean) {
        newUserInfo = newUserInfo.copy(
            isMale = isMale
        )
    }

    fun setUserBirthday(birthday: Long?) {
        newUserInfo = newUserInfo.copy(
            birthday = birthday
        )
    }

    fun setUserBirthday(birthday: Long?, birthdayStr: String?) {
        newUserInfo = newUserInfo.copy(
            birthday = birthday,
            birthdayStr = birthdayStr
        )
    }

    fun setUserCountryId(countryId: Long?) {
        newUserInfo = newUserInfo.copy(
            countryId = countryId
        )
    }

    fun setUserCountry(
        countryId: Long?,
        countryName: String?,
        countryFlag: String?
    ) {
        newUserInfo = newUserInfo.copy(
            countryId = countryId,
            countryName = countryName,
            countryFlag = countryFlag
        )
    }

    fun setUserCountryFlag(
        countryFlag: String?
    ) {
        newUserInfo = newUserInfo.copy(
            countryFlag = countryFlag
        )
    }

    fun setUserCityId(cityId: Long?) {
        newUserInfo = newUserInfo.copy(
            cityId = cityId
        )
    }

    fun setUserCity(cityId: Long?, cityName: String?, cityNameTextError: String?) {
        newUserInfo = newUserInfo.copy(
            cityId = cityId,
            cityName = cityName,
            cityNameTextError = cityNameTextError
        )
    }

    fun setDefaultUserName(uniquename: String) {
        defaultUserName = uniquename
    }

    fun setAvatarAnimation(avatarState: String?) {
        newUserInfo = newUserInfo.copy(
            avatarAnimation = avatarState
        )
    }

    fun openMediaPickerClicked() {
        analyticsInteractor.logAvatarPickerOpen()
    }

    fun logAvatarOpen(isRegistration: Boolean) {
        val property = if (isRegistration) AmplitudePropertyAnimatedAvatarFrom.FROM_REGISTRATION
        else AmplitudePropertyAnimatedAvatarFrom.FROM_SETTINGS
        analyticsInteractor.logAnimatedAvatarOpen(property)
    }

    fun logAvatarCreated(isRegistration: Boolean) {
        val property = if (isRegistration) AmplitudePropertyAnimatedAvatarFrom.FROM_REGISTRATION
        else AmplitudePropertyAnimatedAvatarFrom.FROM_SETTINGS
        analyticsInteractor.logAnimatedAvatarCreated(property)
    }

    fun noteTimeWhenAvatarOpened() {
        avatarEditorOpenedTimeStamp = System.currentTimeMillis()
    }

    fun logPhotoSelection(isPhoto: Boolean) {
        val current = System.currentTimeMillis()
        val diff = current - avatarEditorOpenedTimeStamp
        val type = if (isPhoto) AmplitudePropertyAvatarType.PHOTO else AmplitudePropertyAvatarType.ANIMATED_AVATAR
        analyticsInteractor.logPhotoSelection(type, if (isPhoto.not()) formatLogTime(diff) else null)
    }

    fun logPhotoEdits(nmrAmplitude: NMRPhotoAmplitude) =
        viewModelScope.launch {
            amplitudeEditor.photoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = AmplitudePropertyWhere.PROFILE
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    fun setHiddenUserEmail(email: String?) {
        hiddenEmail = email
        userEmail = UserEmail(email, isHidden = true)
        events.value = OnAccountEmailUpdated(userEmail)
    }

    fun setHiddenUserPhone(phone: String?) {
        hiddenPhone = phone
        userPhone = UserPhone(phone, isHidden = true)
        events.value = OnAccountPhoneUpdated(userPhone)
    }

    fun updateEmail() {
        if (userEmail.isHidden && email.isNullOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                getUserEmailUseCase.execute(
                    params = DefParams(),
                    success = { data ->
                        if (!data.email.isNullOrBlank()) {
                            email = data.email
                            userEmail = userEmail.copy(email = data.email, isHidden = false)
                            events.postValue(OnAccountEmailUpdated(userEmail))
                        }
                    },
                    fail = { exception ->
                        Timber.e(exception)
                    }
                )
            }
        } else if (userEmail.isHidden && !email.isNullOrBlank()) {
            userEmail = userEmail.copy(email = email, isHidden = false)
            events.value = OnAccountEmailUpdated(userEmail)
        } else {
            userEmail = userEmail.copy(email = hiddenEmail, isHidden = true)
            events.value = OnAccountEmailUpdated(userEmail)
        }
    }

    fun updatePhone() {
        if (userPhone.isHidden && phone.isNullOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                getUserPhoneUseCase.execute(
                    params = DefParams(),
                    success = { data ->
                        if (!data.phoneNumber.isNullOrBlank()) {
                            phone = data.phoneNumber
                            userPhone =
                                userPhone.copy(phoneNumber = data.phoneNumber, isHidden = false)
                            events.postValue(OnAccountPhoneUpdated(userPhone))
                        }
                    },
                    fail = { exception ->
                        Timber.e(exception)
                    }
                )
            }
        } else if (userPhone.isHidden && !phone.isNullOrBlank()) {
            userPhone = userPhone.copy(phoneNumber = phone, isHidden = false)
            events.value = OnAccountPhoneUpdated(userPhone)
        } else {
            userPhone = userPhone.copy(phoneNumber = hiddenPhone, isHidden = true)
            events.value = OnAccountPhoneUpdated(userPhone)
        }
    }

    private fun formatLogTime(time: Long): String {
        val date = Date(time)
        val timeDate = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeDate.timeZone = TimeZone.getTimeZone("GMT")
        return timeDate.format(date)
    }
}

package com.numplates.nomera3.modules.auth.data.repository

import com.google.gson.Gson
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.HardwareIdUtil
import com.meera.db.DataStore
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.userprofile.isProfileDeleted
import com.meera.db.models.userprofile.isProfileFilled
import com.meera.db.models.userprofile.isRegistrationCompleted
import com.numplates.nomera3.HTTP_CODE_BAD_REQUEST
import com.numplates.nomera3.HTTP_CODE_NOT_FOUND
import com.numplates.nomera3.HTTP_CODE_SUCCESS
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.AuthUser
import com.numplates.nomera3.modules.auth.data.AuthStatusMapper
import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.auth.data.api.AuthApi
import com.numplates.nomera3.modules.auth.data.entity.AuthUserBlocked
import com.numplates.nomera3.modules.auth.data.entity.Authenticate
import com.numplates.nomera3.modules.auth.data.entity.AuthenticateError
import com.numplates.nomera3.modules.auth.data.entity.SendCodeBlock.Companion.TYPE_COUNTRY
import com.numplates.nomera3.modules.auth.data.entity.SendCodeResponse
import com.numplates.nomera3.modules.auth.data.entity.Token
import com.numplates.nomera3.modules.auth.data.utils.getAuthCodeChallenge
import com.numplates.nomera3.modules.auth.data.utils.randomString
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject


private const val CODE_CHALLENGE_METHOD = "sha256"
private const val CODE_VERIFIER_LENGTH = 12
const val ANON_TOKEN_LEADING = "anon-"
const val DEFAULT_ANON_UID = 0L

/**
 * code - received code from email
 * code_challenge - generated code base64(sha256(12 random string))
 * code_challenge_method - crypto method (sha256)
 *
 * https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/1257144490/Authentication
 */
@AppScope
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val profileRepository: ProfileRepository,
    private val appSettings: AppSettings,
    private val gson: Gson,
    private val socket: WebSocketMainChannel,
    private val dataStore: DataStore,
    private val hardwareId: HardwareIdUtil
) : AuthRepository {

    private val authUserState = ReplaySubject.create<AuthUser>()

    private val authStatusMapper = AuthStatusMapper()

    override suspend fun init() {
        authUserState.onNext(getAuthUser())
    }

    override fun getAuthUserStateObservable(): ReplaySubject<AuthUser> {
        return authUserState
    }

    override suspend fun getAuthUser(): AuthUser {
        val rawAuthStatusValue = appSettings.userAuthStatus ?: 0
        var authStatus = authStatusMapper.mapFromPref(rawAuthStatusValue)
        val accessToken = appSettings.readAccessToken()

        if (authStatus == AuthStatus.Unspecified
            && accessToken.isNotEmpty()
            && !accessToken.contains(ANON_TOKEN_LEADING)
        ) {
            // тут кейс с обновлением версии приложения со старой авторизацией на новую
            // в этом случае старая версия не содержит AuthStatus в префах и нужно выставить
            // AuthStatus самостоятельно

            authStatus = AuthStatus.Authorized(AuthStatus.Authorized.Reason.ConnectedOnAppStart)
            saveAuthStatus(authStatus)
        }

        // необходимо запрашивать профиль сначала именно по рест (а не из БД) так как при установки
        // версии с новой авторизацией
        // на версию со старой авторизацией в БД профиля не будет (так как при смене приложений БД перетирается)
        val profile = withContext(Dispatchers.IO) {
            try {
                if (isAuthorizedUser()) {
                    profileRepository.requestOwnProfileSynch().apply {
                        dataStore.userProfileDao().insert(this)
                    }
                } else {
                    null
                }
            } catch (exception: Throwable) {
                Timber.e(exception)
                dataStore.userProfileDao().getUserProfile()
            }
        }
        val isFilled = profile?.isProfileFilled() ?: false
        val isDeleted = profile?.isProfileDeleted() ?: false
        val isCompleted = profile?.isRegistrationCompleted() ?: false

        return AuthUser(
            userId = appSettings.readUID(),
            authStatus = authStatus,
            authToken = appSettings.readAccessToken(),
            refreshToken = appSettings.userRefreshToken,
            expiresToken = appSettings.userExpiresToken,
            isProfileFilled = isFilled,
            isProfileDeleted = isDeleted,
            isRegistrationCompleted = isCompleted
        )
    }

    override fun isAuthorizedUser(): Boolean {
        val authStatusPref = appSettings.userAuthStatus ?: 0
        return when (authStatusMapper.mapFromPref(authStatusPref)) {
            is AuthStatus.Authorized -> true
            else -> false
        }
    }

    override suspend fun logout() {
        disconnectSocketBeforeNewConnect()

        authenticateAnonymously({
            Timber.d("LOGOUT SUCCESS")
        }, {
            Timber.e("LOGOUT ERROR")
        })
    }

    override suspend fun sendCodeEmail(
        email: String,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    ) {
        try {
            val response = authApi.sendCodeEmail(email)
            handleSendCode(response, success, fail)
        } catch (e: Exception) {
            fail(SendCodeErrors.SendCodeFail)
            Timber.e(e)
        }
    }

    override suspend fun sendCodePhone(
        phone: String,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    ) {
        try {
            val response = authApi.sendCodePhone(phone)
            handleSendCode(response, success, fail)
        } catch (e: Exception) {
            fail(SendCodeErrors.SendCodeFail)
            Timber.e(e)
        }
    }

    private fun handleSendCode(
        response: Response<SendCodeResponse>,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    ) {
        if (response.body().toString().isNotEmpty() && response.code() != HTTP_CODE_NOT_FOUND) {
            val responseObj = gson.fromJson<AuthUserBlocked>(gson.toJson(response.body()))
            if (responseObj.success != null) {
                val error = responseObj.success.error
                error?.blockTime?.let {
                    fail(
                        SendCodeErrors.UserIsBlockedWithoutHideContent(
                            error.blockReason,
                            error.blockTime
                        )
                    )
                } ?: kotlin.run {
                    fail(SendCodeErrors.UserIsBlockedWithHideContent(error?.blockReason))
                }
            } else {
                val isSuccess = response.code() == 200
                val responseBody = response.body()
                var timeout = responseBody?.timeout
                var blockTime = responseBody?.block?.time
                if(responseBody?.block?.type == TYPE_COUNTRY) {
                    timeout = blockTime
                    blockTime = null
                }
                success(isSuccess, timeout, blockTime)
            }
        } else if (response.code() == HTTP_CODE_NOT_FOUND) {
            val bodyString = response.errorBody()?.string()
            if (bodyString != null) {
                val errObj = gson.fromJson<AuthenticateError>(bodyString)
                fail(SendCodeErrors.UserNotFound(errObj.error))
            }
        } else {
            fail(SendCodeErrors.SendCodeFail)
        }
    }

    override suspend fun authenticateEmail(
        email: String,
        code: String,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        try {
            val codeVerifier = randomString(CODE_VERIFIER_LENGTH)
            val response = authApi.authenticateEmail(
                email,
                code,
                getAuthCodeChallenge(codeVerifier),
                CODE_CHALLENGE_METHOD
            )
            handleAuthenticateResponse(response, codeVerifier, success, fail)
        } catch (e: Exception) {
            fail(AuthenticationErrors.AuthenticateError(R.string.error_try_later))
            Timber.e(e)
        }
    }

    override suspend fun authenticatePhone(
        phone: String,
        code: String,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        try {
            val codeVerifier = randomString(CODE_VERIFIER_LENGTH)
            val response = authApi.authenticatePhone(
                phone,
                code,
                getAuthCodeChallenge(codeVerifier),
                CODE_CHALLENGE_METHOD
            )
            handleAuthenticateResponse(response, codeVerifier, success, fail)
        } catch (e: Exception) {
            fail(AuthenticationErrors.AuthenticateError(R.string.error_try_later))
            Timber.e(e)
        }
    }

    override suspend fun authenticateAnonymously(
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        try {
            val anonToken = "$ANON_TOKEN_LEADING${hardwareId.getHardwareId()}"

            appSettings.writeAccessToken(anonToken)
            appSettings.writeUID(DEFAULT_ANON_UID)
            appSettings.userRefreshToken = anonToken
            appSettings.userAuthStatus = (
                authStatusMapper.mapToPref(AuthStatus.Anon(AuthStatus.Anon.Reason.ConnectedAfterNone))
                )

            publishAuthUser(
                authStatus = AuthStatus.Anon(AuthStatus.Anon.Reason.ConnectedAfterNone),
                authToken = anonToken,
            )

            success(true)
        } catch (e: Exception) {
            publishAuthUser(
                authStatus = AuthStatus.Anon(AuthStatus.Anon.Reason.DeclinedAfterLogin),
                authToken = "unknown",
            )
            fail(AuthenticationErrors.AuthenticateError(R.string.error_try_later))
            Timber.e(e)
        }
    }

    private suspend fun handleAuthenticateResponse(
        response: Response<Authenticate>,
        codeVerifier: String,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        when (response.code()) {
            HTTP_CODE_SUCCESS -> {
                response.body()?.codeAuthentication?.let { codeAuth ->
                    getAuthToken(codeAuth, codeVerifier, success, fail)
                } ?: kotlin.run {
                    fail(AuthenticationErrors.NetworkAuthenticationError(R.string.auth_code_failed_to_send))
                }
            }

            HTTP_CODE_BAD_REQUEST -> {
                val bodyString = response.errorBody()?.string()
                if (bodyString != null) {
                    val errObj = gson.fromJson<Authenticate>(bodyString)
                    errObj.errors?.let { err ->
                        if (err.isNotEmpty()) {
                            val field = err[0].field
                            val reason = err[0].reason
                            fail(
                                AuthenticationErrors.AuthenticateErrorExt(
                                    R.string.auth_code_server_error,
                                    field,
                                    reason
                                )
                            )
                        }
                    }
                } else {
                    fail(AuthenticationErrors.AuthenticateError(R.string.auth_code_failed_to_send))
                }
            }
        }
    }

    private suspend fun getAuthToken(
        authenticationCode: String,
        verifierCode: String,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        try {
            val response = authApi.getAuthToken(authenticationCode, verifierCode)
            when (response.code()) {
                HTTP_CODE_SUCCESS -> {
                    saveAuthToken(response.body(), success, fail)
                }

                HTTP_CODE_BAD_REQUEST -> fail(
                    AuthenticationErrors
                        .NetworkAuthenticationError(R.string.auth_code_failed_to_send)
                )

                else -> fail(
                    AuthenticationErrors
                        .NetworkAuthenticationError(R.string.auth_code_failed_to_send)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("ERROR get auth token network exception: ${e.message}")
        }
    }

    private suspend fun saveAuthToken(
        token: Token?,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        disconnectSocketBeforeNewConnect()

        if (token != null) {
            token.accessToken?.let { appSettings.writeAccessToken(it) }
            token.refreshToken?.let { appSettings.userRefreshToken = (it) }
            token.expiresIn?.toLong()?.let { appSettings.userExpiresToken = (it) }
            getUserProfile(success, fail)
        } else {
            Timber.e("INVALID Token - Server ERROR")
            fail(AuthenticationErrors.NetworkAuthenticationError(R.string.error_try_later))
        }
    }

    /**
     * Если логинимся из под анонима (когда сокет уже работает) необходимо
     * перед переподключением произвести дисконнект сокета
     */
    private fun disconnectSocketBeforeNewConnect() {
        val isFirstLogin = appSettings.firstLogin
        if (!isFirstLogin) {
            socket.disconnectMainChannel()
            socket.disconnectSocket()
        }
    }

    private suspend fun getUserProfile(
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        try {
            val profile = profileRepository.requestOwnProfileSynch()
            success(true)
            handleUserProfile(profile)
        } catch (e: Exception) {
            fail(AuthenticationErrors.NetworkAuthenticationError(R.string.error_try_later))
            e.printStackTrace()
        }
    }

    private suspend fun handleUserProfile(
        user: UserProfileNew
    ) {
        appSettings.apply {
            user.avatarSmall?.let { avatar = it }
            user.gender?.let { gender = it }
            user.accountType?.let { accountType = it }
            user.accountColor?.let { accountColor = it }
            preventReceivingAnonymousChat = user.canWriteAnonymousMessages == 1
            user.birthdayFlag?.let { birthdayFlag = it }
        }
        runBlocking { dataStore.privacySettingsDao().updateValue(SettingsKeyEnum.SHOW_ON_MAP.key, user.showOnMap) }
        appSettings.userName.set(user.name ?: String.empty())
        appSettings.writeUID(user.userId)
        appSettings.writeUniquieName(user.uniquename)
        dataStore.userProfileDao().insert(user)
        publishAuthorizedUser(user)
    }

    private suspend fun publishAuthorizedUser(user: UserProfileNew) {
        appSettings.userAuthStatus = (
            authStatusMapper
                .mapToPref(AuthStatus.Authorized(AuthStatus.Authorized.Reason.ConnectedAfterLoginOrRegistration))
            )

        publishAuthUser(
            authStatus = AuthStatus.Authorized(AuthStatus.Authorized.Reason.ConnectedAfterLoginOrRegistration),
            uid = user.userId,
            authToken = appSettings.readAccessToken(),
            refreshToken = appSettings.userRefreshToken,
            expiresToken = appSettings.userExpiresToken,
            isProfileFilled = user.isProfileFilled(),
            isProfileDeleted = user.isProfileDeleted()
        )
    }

    /**
     * Default state as AuthStatus.None
     */
    private suspend fun publishAuthUser(
        authStatus: AuthStatus,
        uid: Long = 0,
        authToken: String = String.empty(),
        refreshToken: String = String.empty(),
        expiresToken: Long = 0,
        isProfileFilled: Boolean = false,
        isProfileDeleted: Boolean = false
    ) {
        saveAuthStatus(authStatus)
        authUserState.onNext(
            AuthUser(
                userId = uid,
                authStatus = authStatus,
                authToken = authToken,
                refreshToken = refreshToken,
                expiresToken = expiresToken,
                isProfileFilled = isProfileFilled,
                isProfileDeleted = isProfileDeleted
            )
        )
    }

    private suspend fun saveAuthStatus(authStatus: AuthStatus) {
        appSettings.userAuthStatus = (authStatusMapper.mapToPref(authStatus))
    }
}

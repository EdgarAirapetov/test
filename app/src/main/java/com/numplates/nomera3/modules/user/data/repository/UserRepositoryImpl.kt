package com.numplates.nomera3.modules.user.data.repository

import com.google.gson.Gson
import com.meera.core.common.SHARE_PROFILE_BASE_URL
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.FRIEND_STATUS_OUTGOING
import com.numplates.nomera3.data.network.BlockRequest
import com.numplates.nomera3.data.network.BlockSuggestionDto
import com.numplates.nomera3.data.network.ListFriendsResponse
import com.numplates.nomera3.data.network.ListMutualUsersDto
import com.numplates.nomera3.data.network.ListSubscriptionResponse
import com.numplates.nomera3.data.network.PrivacySetting
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.data.newmessenger.response.ResponseMapUserState
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.data.websocket.PAYLOAD_STATUS_KEY
import com.numplates.nomera3.data.websocket.STATUS_ERROR
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.modules.appInfo.data.entity.AppLinks
import com.numplates.nomera3.modules.auth.util.isAuthorizedUser
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.user.data.api.UserApi
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPermissionResponse
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.modules.user.data.entity.UserSettingsDataEffect
import com.numplates.nomera3.modules.user.data.mapper.UserCoordinateMapper
import com.numplates.nomera3.modules.user.data.mapper.UserProfileDomainMapper
import com.numplates.nomera3.modules.user.data.mapper.UserSettingsEffectDataMapper
import com.numplates.nomera3.modules.user.data.model.DeleteAvatarItemRequest
import com.numplates.nomera3.modules.user.data.model.UploadAvatarIdRequest
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.entity.UserCoordinateModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject


private const val FRIEND_ID_PARAM = "friend_id"
private const val USER_TYPE_ONLY_FRIEND = "only_friend"
private const val USER_IDS = "ids"
private const val ID_PARAM = "id"
private const val MEDIA_TYPE = "image/*"
private const val FILE = "file"
private const val EMPTY_RESPONSE = "empty response"
private const val FROM_PARAM = "from"
private const val SHAKE_PARAM = "shake"


@AppScope
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val socket: WebSocketMainChannel,
    private val dataStore: DataStore,
    private val appSettings: AppSettings,
    private val userCoordinateMapper: UserCoordinateMapper,
    private val gson: Gson,
    private val userSettingsEffectDataMapper: UserSettingsEffectDataMapper,
    private val userProfileMapper: UserProfileDomainMapper,
) : UserRepository {

    private val publishUserState = PublishSubject.create<UserState>()
    private val publishUserAvatarUpdates = PublishSubject.create<UploadAvatarResponse>()

    private val updatePrefFlow = MutableSharedFlow<UserSettingsDataEffect>()

    override suspend fun addUserToFriend(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        val payload = hashMapOf<String, Any>(FRIEND_ID_PARAM to userId)
        val response = socket.pushAddFriendsCoroutine(payload)
        when (response.payload[PAYLOAD_STATUS_KEY]) {
            STATUS_OK -> {
                publishUserState.onNext(
                    UserState.AddUserToFriendSuccess(userId, FRIEND_STATUS_OUTGOING)
                )
                success(true)
            }
            STATUS_ERROR -> fail(Exception("Add user to friend network exception"))
        }
    }

    override suspend fun addUserToFriend(
        userId: Long,
        isRequestFromShake: Boolean
    ) {
        val payload = if (isRequestFromShake) {
            hashMapOf(
                FRIEND_ID_PARAM to userId,
                FROM_PARAM to SHAKE_PARAM
            )
        } else {
            hashMapOf<String, Any>(
                FRIEND_ID_PARAM to userId
            )
        }
        val response = socket.pushAddFriendsCoroutine(payload)
        when (response.payload[PAYLOAD_STATUS_KEY]) {
            STATUS_OK -> {
                publishUserState.onNext(
                    UserState.AddUserToFriendSuccess(userId, FRIEND_STATUS_OUTGOING)
                )
            }
            STATUS_ERROR -> Timber.e("Add user to friend network exception")
        }
    }

    override suspend fun deleteFriendCancelSubscription(userId: Long) {
        userApi.removeUserAndCancelSubscription(userId)
    }

    override suspend fun deleteFriendSaveSubscription(userId: Long) {
        userApi.removeUserAndSaveSubscription(userId, USER_TYPE_ONLY_FRIEND)
    }

    override fun getUserStateObserver(): PublishSubject<UserState> =
        publishUserState


    override fun getUserAvatarObserver(): PublishSubject<UploadAvatarResponse> =
        publishUserAvatarUpdates

    override fun getUserPrefObserver(): Flow<UserSettingsEffect> =
        updatePrefFlow.map(userSettingsEffectDataMapper::mapFromDataEffect)


    override suspend fun removeUserFromFriendAndCancelSubscription(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.removeUserAndCancelSubscription(userId)
            publishUserState.onNext(
                UserState.CancelFriendRequest(userId, FRIEND_STATUS_NONE)
            )
            success(true)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun removeUserFromFriendAndSaveSubscription(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.removeUserAndSaveSubscription(userId, USER_TYPE_ONLY_FRIEND)
            publishUserState.onNext(
                UserState.CancelFriendRequest(userId, FRIEND_STATUS_NONE)
            )
            success(true)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun subscribeUser(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.subscribeUser(hashMapOf(USER_IDS to mutableListOf(userId)))
            success(true)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun subscribeUser(userid: Long) {
        userApi.subscribeUser(hashMapOf(USER_IDS to mutableListOf(userid)))
    }

    override suspend fun unsubscribeUser(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.unsubscribeUser(hashMapOf(USER_IDS to mutableListOf(userId)))
            success(true)
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun unsubscribeUser(userId: Long) {
        userApi.unsubscribeUser(hashMapOf(USER_IDS to mutableListOf(userId)))
    }

    override suspend fun removeSubscriber(userId: Long) {
        userApi.removeSubscriber(hashMapOf(USER_IDS to mutableListOf(userId)))
    }

    override suspend fun blockUser(
        userId: Long,
        remoteUserId: Long,
        isBlocked: Boolean
    ) {
        withContext(Dispatchers.IO) {
            try {
                val res = userApi.setBlockedStatusToUser(
                    userId, BlockRequest(remoteUserId, isBlocked)
                ).data
                if (res != null) {
                    publishUserState.onNext(
                        UserState.BlockStatusUserChanged(
                            userId = remoteUserId,
                            isBlocked = isBlocked
                        )
                    )
                    val roomId = dataStore.dialogDao().getDialogByCompanionId(userId).firstOrNull()?.roomId
                    roomId?.let { dataStore.draftsDao().deleteDraft(it) }
                } else throw Exception("response data is null")
            } catch (e: Exception) {
                Timber.e(e)
                throw e
            }
        }
    }

    override fun uploadAvatar(
        imagePath: String,
        avatarAnimation: String?
    ): Single<ResponseWrapper<UploadAvatarResponse?>> {
        val imageFile = File(imagePath)
        val imageRequestFile = imageFile.asRequestBody(MEDIA_TYPE.toMediaTypeOrNull())
        val image = MultipartBody.Part.createFormData(FILE, imageFile.name, imageRequestFile)
        val avatarAnimationParam: RequestBody? = avatarAnimation?.toRequestBody(MultipartBody.FORM)
        return userApi.uploadAvatar(image, avatarAnimationParam).doAfterSuccess {
            it.data?.let { response ->
                response.avatarSmall?.let { avatarSmall -> appSettings.avatar = avatarSmall }
                publishUserAvatarUpdates.onNext(response)
            }
        }
    }

    override suspend fun uploadAvatarSuspend(
        uploadId: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int
    ): UploadAvatarResponse {
        val response = userApi.uploadAvatarWithUploadId(
            UploadAvatarIdRequest(
                uploadId = uploadId,
                avatarAnimation = animation,
                saveSettings = saveSettings,
                createAvatarPost = createAvatarPost
            )
        )
        response.data?.let { avatar ->
            avatar.avatarSmall?.let { avatarSmall -> appSettings.avatar = avatarSmall }
            publishUserAvatarUpdates.onNext(avatar)
        }

        return response.data
    }

    override suspend fun saveUserAvatarStateLocally(avatar: String) = withContext(Dispatchers.IO) {
        appSettings.userAvatarState = avatar
    }

    override suspend fun getShareProfileLink() = withContext(Dispatchers.IO) {
        return@withContext if (appSettings.prefAppLinks(AppLinks::class.java) != null) {
            appSettings.prefAppLinks(AppLinks::class.java)?.uniqname ?: SHARE_PROFILE_BASE_URL
        } else {
            SHARE_PROFILE_BASE_URL
        }
    }

    override fun deleteUserAvatar(userId: Long): Single<ResponseWrapper<UploadAvatarResponse?>> =
        userApi.deleteUserAvatar(userId)

    override suspend fun requestUserPermissions(
        success: (UserPermissionResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = userApi.requestUserPermissions()
            response.data?.let { permission ->
                success(permission)
            } ?: kotlin.run {
                fail(error(EMPTY_RESPONSE))
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun getUserEmail(success: (UserEmail) -> Unit, fail: (Exception) -> Unit) {
        try {
            val response = userApi.getOwnUserEmail()
            response.data?.let { userEmail ->
                success(userEmail)
            } ?: kotlin.run {
                fail(error(EMPTY_RESPONSE))
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun getUserPhoneNumber(
        success: (UserPhone) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = userApi.getOwnUserPhone()
            response.data?.let { userPhone ->
                success(userPhone)
            } ?: kotlin.run {
                fail(error(EMPTY_RESPONSE))
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun disablePrivateMessages(
        userIds: List<Long>,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.addPrivateMessageUserToBlackList(userIds)
            success(true)
        }catch (e: Exception){
            Timber.d(e)
            fail(e)
        }
    }

    override suspend fun enablePrivateMessages(
        userIds: List<Long>,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.addPrivateMessageUserToWhiteList(userIds)
            success(true)
        } catch (e: Exception){
            Timber.d(e)
            fail(e)
        }
    }

    override suspend fun setProfileStatisticsAsRead(
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userApi.setProfileStatisticsAsRead()
            success(true)
        } catch (e: Exception) {
            Timber.d(e)
            fail(e)
        }
    }

    override suspend fun setUserCallPrivacy(userId: Long, isSet: Boolean) {
        val payload = hashMapOf(
            "user_id" to userId,
            "value" to isSet,
        )
        socket.pushSetCallPrivacyForUserSuspend(payload)
    }

    override fun getUserProfileRx(): Single<UserProfileModel> =
        dataStore.userProfileDao().getUserProfileRx().map { userProfileMapper.mapDataToDomain(it) }

    override suspend fun pushUserSettingsChanged(state: UserSettingsEffect) {
        try {
            updatePrefFlow.emit(userSettingsEffectDataMapper.mapToDataEffect(state))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun pushFriendStatusChanged(userId: Long, isSubscribe: Boolean) =
        updatePrefFlow.emit(UserSettingsDataEffect.UserFriendStatusChanged(userId, isSubscribe))

    override suspend fun setBlockStatus(userId: Long, isUserBlocked: Boolean) =
        updatePrefFlow.emit(UserSettingsDataEffect.UserBlockStatusChanged(userId,  isUserBlocked))

    override fun updateChatMembersDatabase(members: List<ChatMember>) {
        dataStore.daoChatMembers().insert(members)
    }

    override fun removeChatMembersDatabase(membersIds: List<Long>) {
        dataStore.daoChatMembers().deleteMembers(membersIds)
    }

    override fun changeChatMembersType(membersIds: List<Long>, type: String) {
        dataStore.daoChatMembers().updateMembersType(membersIds, type)
    }

    override fun clearChatMembers() {
        dataStore.daoChatMembers().clearDb()
    }

    override suspend fun updateBirthdayDialogShown() = userApi.uploadBirthdayViewed()

    override fun getDateOfBirth(): Long = appSettings.birthdayFlag

    override fun isNeedShowBirthdayDialog() =
        appSettings.isNeedShowBirthdayDialog

    override suspend fun getIsNeedShowBirthdayDialogFlow(): Flow<Boolean> {
        return appSettings.isNeedShowBirthdayDialog.asFlow().mapNotNull { it ?: false }
    }

    override fun getUserNeedShowFriendsSubscribersPopup() =
        appSettings.isNeedShowFriendsFollowersPrivacy

    override fun isUserAuthorized(): Boolean =
        appSettings.isAuthorizedUser()

    override suspend fun pushSetPrivacySettings(
        key: String,
        model: CustomRowSelector.CustomRowSelectorModel
    ) {
        val settings = mutableListOf(
            PrivacySetting(
                key = key,
                value = model.selectorModelId
            )
        )
        val payload = mutableMapOf<String, Any>(
            "settings" to settings
        )
        socket.pushSetPrivacySettingsSuspended(payload)
    }

    override suspend fun getUserMutual(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListMutualUsersDto? =
        userApi.getUserMutual(
            userId = userId,
            limit = limit,
            offset = offset,
            querySearch = querySearch
        ).data

    override suspend fun getUserFriends(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListFriendsResponse? =
        userApi.getUserFriends(
            userId = userId,
            limit = limit,
            offset = offset,
            querySearch = querySearch
        ).data

    override suspend fun getUserSubscribers(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListSubscriptionResponse? =
        userApi.getUserSubscribers(
            userId = userId,
            limit = limit,
            offset = offset,
            querySearch = querySearch
        ).data

    override suspend fun getUserSubscription(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListSubscriptionResponse? =
        userApi.getUserSubscriptions(
            userId = userId,
            limit = limit,
            offset = offset,
            querySearch = querySearch
        ).data

    override suspend fun getUserCoordinate(userId: Long): UserCoordinateModel = withContext(Dispatchers.IO) {
        val payload = mapOf(
            ID_PARAM to userId
        )
        val message = socket.pushGetMapUserState(payload)
        val response = gson.fromJson<ResponseWrapperWebSock<ResponseMapUserState>>(message.payload).response
            ?: error("Coordinates are empty")
        return@withContext userCoordinateMapper.mapDataToDomain(response)
    }

    override suspend fun setUserFriendsPrivacyDialogShowed(isNeedShow: Boolean) = withContext(Dispatchers.IO){
        appSettings.isNeedShowFriendsFollowersPrivacy.set(isNeedShow)
    }

    override fun setPrivacyFriendsFollowers(key: Int) {
        appSettings.showFriendsAndSubscribers = key
    }

    override fun getUserUid(): Long = appSettings.readUID()

    override suspend fun getUserSmallAvatar(): String? = dataStore.userProfileDao().getUserProfile()?.avatarSmall

    override fun readOnboardingShowed(): Boolean = appSettings.readOnBoardingWelcomeShowed()

    override fun isShowTooltipSession(key: String): Boolean {
        return appSettings.isShownTooltipSession(key)
    }

    override fun saveLastSmsCodeTime() = appSettings.writeLastSmsCodeTime(System.currentTimeMillis())

    override fun saveAdminSupportId(adminSupportId: Long) {
        appSettings.adminSupportId = adminSupportId
    }

    override fun getAdminSupportId(): Long = appSettings.adminSupportId

    override suspend fun deleteGalleryItem(galleryItemId: Long) = userApi.deleteGalleryItem(galleryItemId)

    override suspend fun deleteAvatarItem(avatarItemId: Long) =
        userApi.deleteAvatarItem(DeleteAvatarItemRequest(avatarItemId))

    override suspend fun blockSuggestionById(userId: Long) {
        userApi.blockSuggestion(BlockSuggestionDto(userId))
    }

    override suspend fun emitSuggestionRemovedEffect(userId: Long) {
        updatePrefFlow.emit(UserSettingsDataEffect.SuggestionRemoved(userId))
    }

    override fun getUserAccountType(): Int = appSettings.readAccountType()

    override fun getUserAccountColor(): Int = appSettings.readAccountColor()

    override fun getUserAvatar(): String? = appSettings.avatar

    override fun getUserGender(): Gender = appSettings.gender.let(Gender::fromValue)
}

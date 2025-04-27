package com.numplates.nomera3.modules.user.data.repository

import com.meera.core.preferences.datastore.Preference
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.data.network.ListFriendsResponse
import com.numplates.nomera3.data.network.ListMutualUsersDto
import com.numplates.nomera3.data.network.ListSubscriptionResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPermissionResponse
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.entity.UserCoordinateModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun addUserToFriend(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun addUserToFriend(userId: Long, isRequestFromShake: Boolean)

    suspend fun deleteFriendCancelSubscription(userId: Long)

    suspend fun deleteFriendSaveSubscription(userId: Long)

    fun getUserStateObserver(): PublishSubject<UserState>

    fun getUserAvatarObserver(): PublishSubject<UploadAvatarResponse>

    fun getUserPrefObserver(): Flow<UserSettingsEffect>

    suspend fun removeUserFromFriendAndCancelSubscription(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun removeUserFromFriendAndSaveSubscription(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun subscribeUser(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun subscribeUser(userid: Long)

    suspend fun unsubscribeUser(
        userId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun unsubscribeUser(userId: Long)

    suspend fun removeSubscriber(userId: Long)

    suspend fun blockUser(
        userId: Long,
        remoteUserId: Long,
        isBlocked: Boolean
    )

    fun uploadAvatar(
        imagePath: String,
        avatarAnimation: String? = null
    ): Single<ResponseWrapper<UploadAvatarResponse?>>

    suspend fun uploadAvatarSuspend(
        uploadId: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int
    ): UploadAvatarResponse

    suspend fun saveUserAvatarStateLocally(
        avatar: String
    )

    suspend fun getShareProfileLink(): String

    fun deleteUserAvatar(
        userId: Long
    ): Single<ResponseWrapper<UploadAvatarResponse?>>

    suspend fun requestUserPermissions(
        success: (UserPermissionResponse) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getUserEmail(
        success: (UserEmail) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun getUserPhoneNumber(
        success: (UserPhone) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun disablePrivateMessages(
        userIds: List<Long>,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun enablePrivateMessages(
        userIds: List<Long>,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun setProfileStatisticsAsRead(
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    fun updateChatMembersDatabase(members: List<ChatMember>)

    fun removeChatMembersDatabase(membersIds: List<Long>)

    fun changeChatMembersType(membersIds: List<Long>, type: String)

    fun clearChatMembers()

    /**
     * @param userId users identifier
     * @param isSet flag which indicated to block (false) or enable (true) users calls
     */
    suspend fun setUserCallPrivacy(userId: Long, isSet: Boolean)

    /**
     * Через данный метод будет отправляться состояние настроек приватности чата
     * Проблема в том, что при слишком слабоб интернете socket выкидывает
     * [java.net.SocketTimeoutException] и состояние не будет обновлено
     */
    suspend fun pushUserSettingsChanged(state: UserSettingsEffect)

    /**
     * Метод должен вызвать emit event
     * [com.numplates.nomera3.modules.user.data.entity.UserSettingsDataEffect.UserFriendStatusChanged]
     * Когда юзер:
     * 1. Добавляет в друзья
     * 2. Подписывается
     * 3. Отписывается
     * @param userId - id другого юзера
     */
    suspend fun pushFriendStatusChanged(userId: Long, isSubscribe: Boolean)

    /**
     * Отправляем данные в глобальный репозиторий о том, что юзер заблокировал/разблокировал
     * другого юзера.
     * @param userId - id другого юзера
     * @param isUserBlocked - Статус блокировки юзера
     */
    suspend fun setBlockStatus(userId: Long, isUserBlocked: Boolean)

    suspend fun pushSetPrivacySettings(
        key: String,
        model: CustomRowSelector.CustomRowSelectorModel
    )

    /**
     * Получаем список общих подписок
     */
    suspend fun getUserMutual(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ) : ListMutualUsersDto?

    /**
     * Получаем список друзей другого юзера
     */
    suspend fun getUserFriends(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListFriendsResponse?

    /**
     * Получаем список подписчиков другого юзера
     */
    suspend fun getUserSubscribers(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListSubscriptionResponse?

    /**
     * Получаем список подписок другого юзера
     */
    suspend fun getUserSubscription(
        userId: Long,
        limit: Int,
        offset: Int,
        querySearch: String?
    ): ListSubscriptionResponse?

    suspend fun getUserCoordinate(userId: Long) : UserCoordinateModel

    suspend fun setUserFriendsPrivacyDialogShowed(isNeedShow: Boolean)

    fun setPrivacyFriendsFollowers(key: Int)

    fun getUserUid(): Long

    suspend fun getUserSmallAvatar(): String?

    suspend fun updateBirthdayDialogShown() : ResponseWrapper<Any>

    fun getDateOfBirth(): Long

    fun isNeedShowBirthdayDialog(): Preference<Boolean>

    suspend fun getIsNeedShowBirthdayDialogFlow(): Flow<Boolean>

    fun getUserNeedShowFriendsSubscribersPopup(): Preference<Boolean>

    fun isUserAuthorized(): Boolean

    fun readOnboardingShowed() : Boolean

    fun isShowTooltipSession(key: String): Boolean

    fun saveLastSmsCodeTime()

    fun saveAdminSupportId(adminSupportId: Long)

    fun getAdminSupportId(): Long

    suspend fun deleteGalleryItem(galleryItemId: Long)

    suspend fun deleteAvatarItem(avatarItemId: Long)

    fun getUserProfileRx(): Single<UserProfileModel>

    suspend fun blockSuggestionById(userId: Long)

    suspend fun emitSuggestionRemovedEffect(userId: Long)

    fun getUserAccountType(): Int

    fun getUserAccountColor(): Int

    fun getUserAvatar(): String?

    fun getUserGender(): Gender
}

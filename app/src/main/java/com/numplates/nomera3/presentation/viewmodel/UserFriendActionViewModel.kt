package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.R
import com.numplates.nomera3.USER_SUBSCRIBED
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.DeleteFriendCancelSubscriptionUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.UnsubscribeUserUseCaseNew
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.toScreenAddFriendAmplitude
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.toScreenFollowActionAmplitude
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendActionViewEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserFriendActionViewModel @Inject constructor(
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val unsubscribeUserUseCase: UnsubscribeUserUseCaseNew,
    private val deleteFriendCancelSubscriptionUseCase: DeleteFriendCancelSubscriptionUseCase,
    private val pushFriendStatusChangedUseCase: PushFriendStatusChangedUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudeFollowButton: AmplitudeFollowButton,
    private val amplitudeAddFriendAnalytic: AmplitudeAddFriendAnalytic
) : ViewModel() {

    private val _userFriendActionSharedFlow = MutableSharedFlow<UserFriendActionViewEvent>()
    val userFriendActionSharedFlow: SharedFlow<UserFriendActionViewEvent> =
        _userFriendActionSharedFlow

    fun addToFriendSocket(
        model: FriendsFollowersUiModel,
        isAcceptFriendRequest: Boolean
    ) {
        viewModelScope.launch {
            runCatching { addUserToFriendUseCase.invoke(model.userSimple?.userId ?: 0) }
                .onSuccess {
                    if (isAcceptFriendRequest) {
                        val isSubscribed = model.userSimple?.settingsFlags?.subscription_on == USER_SUBSCRIBED
                        val message = if (isSubscribed) R.string.request_acepted else R.string.request_accepted_notif_on
                        emitViewEvent(UserFriendActionViewEvent.ShowSuccessSnackBar(message))
                    } else {
                        val messageRes = R.string.enabled_new_post_notif
                        emitViewEvent(UserFriendActionViewEvent.ShowSuccessSnackBar(messageRes))
                    }
                    pushFriendStatusChangedUseCase.invoke(model.userSimple?.userId ?: 0)
                }.onFailure { e ->
                    emitViewEvent(UserFriendActionViewEvent.ShowErrorSnackBar(R.string.error_try_later))
                    Timber.e(e)
                }
        }
    }

    fun unsubscribeUser(model: FriendsFollowersUiModel) {
        viewModelScope.launch {
            runCatching { unsubscribeUserUseCase.invoke(model.userSimple?.userId ?: 0) }
                .onSuccess {
                    val messageRes = R.string.disabled_new_post_notif
                    emitViewEvent(UserFriendActionViewEvent.ShowSuccessSnackBar(messageRes))
                    pushFriendStatusChangedUseCase.invoke(
                        userId = model.userSimple?.userId ?: 0,
                        isSubscribe = true
                    )
                }.onFailure { e ->
                    emitViewEvent(
                        UserFriendActionViewEvent.ShowErrorSnackBar(
                        R.string.error_try_later
                    ))
                    Timber.e(e)
                }
        }
    }

    fun declineUserFriendRequest(model: FriendsFollowersUiModel) {
        viewModelScope.launch {
            runCatching {
                deleteFriendCancelSubscriptionUseCase.invoke(
                    model.userSimple?.userId ?: 0
                )
            }.onSuccess {
                emitViewEvent(
                    UserFriendActionViewEvent.ShowSuccessSnackBar(R.string.request_rejected)
                )
                pushFriendStatusChangedUseCase.invoke(model.userSimple?.userId ?: 0)
            }.onFailure { e ->
                emitViewEvent(
                    UserFriendActionViewEvent.ShowErrorSnackBar(
                    R.string.error_try_later
                ))
                Timber.e(e)
            }
        }
    }

    fun subscribeUser(model: FriendsFollowersUiModel) {
        viewModelScope.launch {
            runCatching { subscribeUserUseCase.invoke(model.userSimple?.userId ?: 0) }
                .onSuccess {
                    emitViewEvent(
                        UserFriendActionViewEvent.ShowSuccessSnackBar(
                            R.string.subscribed_on_user_notif_on
                        )
                    )
                    pushFriendStatusChangedUseCase.invoke(
                        userId = model.userSimple?.userId ?: 0,
                        isSubscribe = true
                    )
                }.onFailure { t ->
                    emitViewEvent(
                        UserFriendActionViewEvent.ShowErrorSnackBar(
                        R.string.error_try_later
                    ))
                    Timber.e(t)
                }
        }
    }

    fun logAddToFriendAmplitude(
        userId: Long,
        screenMode: Int,
        approved: Boolean,
        topContentMaker: Boolean
    ) {
        val screenType = screenMode.toScreenAddFriendAmplitude() ?: return
        val influencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker,
            approved = approved
        )
        amplitudeAddFriendAnalytic.logAddFriend(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            type = screenType,
            influencer = influencerProperty
        )
    }

    fun logFollowAmplitude(
        userId: Long,
        screenMode: Int,
        accountApproved: Boolean,
        topContentMaker: Boolean
    ) {
        val screenType = screenMode.toScreenFollowActionAmplitude() ?: return
        val influencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker,
            approved = accountApproved
        )
        amplitudeFollowButton.followAction(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            where = screenType,
            type = AmplitudePropertyType.OTHER,
            amplitudeInfluencerProperty = influencerProperty
        )
    }

    fun logUnfollowAmplitudeAction(
        userId: Long,
        screenMode: Int,
        accountApproved: Boolean,
        topContentMaker: Boolean
    ) {
        val screenType = screenMode.toScreenFollowActionAmplitude() ?: return
        val influencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker,
            approved = accountApproved
        )
        amplitudeFollowButton.logUnfollowAction(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            where = screenType,
            type = AmplitudePropertyType.OTHER,
            amplitudeInfluencerProperty = influencerProperty
        )
    }

    private fun emitViewEvent(event: UserFriendActionViewEvent) {
        viewModelScope.launch {
            _userFriendActionSharedFlow.emit(event)
        }
    }
}

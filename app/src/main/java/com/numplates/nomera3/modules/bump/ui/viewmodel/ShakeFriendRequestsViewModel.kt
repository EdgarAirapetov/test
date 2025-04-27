package com.numplates.nomera3.modules.bump.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.R
import com.numplates.nomera3.UPDATE_ADD
import com.numplates.nomera3.UPDATE_CONFIRM
import com.numplates.nomera3.UPDATE_DELETE
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.DeleteFriendCancelSubscriptionUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.ObserveUpdateFriendshipUseCase
import com.numplates.nomera3.domain.interactornew.ObserveWebSocketConnectionUseCase
import com.numplates.nomera3.domain.model.WebSocketConnectionState
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.bump.domain.usecase.ClearShakeUsersResultUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.GetShakeUsersResultUseCase
import com.numplates.nomera3.modules.bump.ui.ShakeAnalyticDelegate
import com.numplates.nomera3.modules.bump.ui.ShakeRequestsDismissListener
import com.numplates.nomera3.modules.bump.ui.entity.ShakeFriendRequestsUiEffect
import com.numplates.nomera3.modules.bump.ui.entity.ShakeRequestsContentActions
import com.numplates.nomera3.modules.bump.ui.entity.UserFriendShakeStatus
import com.numplates.nomera3.modules.bump.ui.entity.UserShakeUiModel
import com.numplates.nomera3.modules.bump.ui.entity.UserShakeUiState
import com.numplates.nomera3.modules.bump.ui.mapper.ShakeUiMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class ShakeFriendRequestsViewModel @Inject constructor(
    private val shakeUiMapper: ShakeUiMapper,
    private val getShakeUsersResultUseCase: GetShakeUsersResultUseCase,
    private val clearShakeUsersResultUseCase: ClearShakeUsersResultUseCase,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val deleteFriendCancelSubscriptionUseCase: DeleteFriendCancelSubscriptionUseCase,
    private val observeUpdateFriendshipUseCase: ObserveUpdateFriendshipUseCase,
    private val observeWebSocketConnectionUseCase: ObserveWebSocketConnectionUseCase,
    private val shakeRequestsDismissListener: ShakeRequestsDismissListener,
    private val analyticDelegate: ShakeAnalyticDelegate,
    private val getUserUidUseCase: GetUserUidUseCase,
) : ViewModel() {

    /**
     * Хранит текущее состояние юзера во фрагменте
     */
    private val _shakeFriendRequestsUiState = MutableLiveData<UserShakeUiState>()
    val shakeFriendRequestsUiState: LiveData<UserShakeUiState> = _shakeFriendRequestsUiState

    private val _shakeFriendRequestsUiFlow = MutableSharedFlow<ShakeFriendRequestsUiEffect>()
    val shakeFriendRequestsUiFlow: SharedFlow<ShakeFriendRequestsUiEffect> = _shakeFriendRequestsUiFlow

    /**
     * Хранит общее кол-во юзеров, которые "шейкнулись".
     */
    private val allShakeUsers = mutableListOf<UserShakeUiModel>()

    init {
        observeShakeUsers()
        observeUpdateFriendship()
        initWebSocketConnectionHandler()
    }

    override fun onCleared() {
        clearShakeUsersResultUseCase.invoke()
        shakeRequestsDismissListener.emitShakeClosed()
        super.onCleared()
    }

    fun setContentAction(typeAction: ShakeRequestsContentActions) {
        when (typeAction) {
            ShakeRequestsContentActions.OnFriendActionButtonClicked -> {
                val currentShakeUser = _shakeFriendRequestsUiState.value?.shakeUser ?: return
                handleFriendActionButtonClicked(currentShakeUser)
            }
            ShakeRequestsContentActions.OnFriendDeclineFriendRequestClicked -> {
                val userId = _shakeFriendRequestsUiState.value?.shakeUser?.userId ?: 0
                handleDeclineFriendAction(userId)
            }
            ShakeRequestsContentActions.OnCloseShakeUserClicked -> {
                handleShakeUserClickedAction()
            }
            ShakeRequestsContentActions.TryToRequestNextUserAction -> {
                tryToRequestFirstUserAnimated()
            }
        }
    }

    private fun handleDeclineFriendAction(userId: Long) {
        val fromId = getUserUidUseCase.invoke()
        logDeclineFriendRequest(
            fromId = fromId,
            toId = userId
        )
        declineFriendRequest(userId)
    }

    private fun handleFriendActionButtonClicked(userShakeUiModel: UserShakeUiModel) {
        logAddToFriends(userShakeUiModel)
        addUserToFriend(userShakeUiModel)
    }

    private fun logAddToFriends(model: UserShakeUiModel) {
        val fromId = getUserUidUseCase.invoke()
        val toId = model.userId
        when (model.userFriendShakeStatus) {
            UserFriendShakeStatus.USER_SHAKE_FRIEND_REQUESTED_BY_USER -> {
                analyticDelegate.logConfirmFriendRequest(
                    fromId = fromId,
                    toId = toId
                )
            }
            else -> {
                val influencer = createInfluencerAmplitudeProperty(
                    topContentMaker = model.topContentMaker,
                    approved = model.approvedUser
                )
                analyticDelegate.logAddToFriends(
                    fromId = getUserUidUseCase.invoke(),
                    toId = model.userId,
                    influencer = influencer
                )
            }
        }
    }

    private fun logDeclineFriendRequest(
        fromId: Long,
        toId: Long
    ) {
        analyticDelegate.logFriendRequestDenied(
            fromId = fromId,
            toId = toId
        )
    }

    private fun handleShakeUserClickedAction() {
        animateSkipOrCloseShakeFriendRequests()
    }

    private fun addUserToFriend(model: UserShakeUiModel) {
        viewModelScope.launch {
            runCatching {
                addUserToFriendUseCase.invoke(
                    userId = model.userId,
                    isRequestFromShake = true
                )
            }.onSuccess {
                Timber.d("Success friend added!")
                tryToRemoveCurrentUser()
                if (hasMoreShakeUsers()) {
                    emitEffect(ShakeFriendRequestsUiEffect.AnimateNextUserUiEffect)
                } else {
                    emitEffect(ShakeFriendRequestsUiEffect.ShowSuccessToast(R.string.shake_friend_added))
                    emitEffect(ShakeFriendRequestsUiEffect.NavigateToUserFragment(model.userId))
                }
            }.onFailure { t ->
                Timber.d(t)
                handleHttpError(t)
            }
        }
    }

    private fun declineFriendRequest(userId: Long) {
        viewModelScope.launch {
            runCatching {
                deleteFriendCancelSubscriptionUseCase.invoke(userId)
            }.onSuccess {
                Timber.d("Success decline friend request!")
                animateSkipOrCloseShakeFriendRequests()
            }.onFailure { t ->
                Timber.d(t)
                handleHttpError(t)
            }
        }
    }

    private fun emitEffect(typeEffect: ShakeFriendRequestsUiEffect) {
        viewModelScope.launch {
            _shakeFriendRequestsUiFlow.emit(typeEffect)
        }
    }

    private fun tryToRequestFirstUserAnimated() {
        if (hasMoreShakeUsers().not()) return
        val currentState = _shakeFriendRequestsUiState.value ?: return
        _shakeFriendRequestsUiState.value = shakeUiMapper.mapNextSelectedUser(
            nextUser = allShakeUsers.first(),
            currentState = currentState
        )
        if (allShakeUsers.first().userFriendShakeStatus == UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS) {
            emitEffect(ShakeFriendRequestsUiEffect.AnimateVisibleAppearWithoutButtonsEffect)
        } else {
            emitEffect(ShakeFriendRequestsUiEffect.AnimateVisibleAppearWithButtonsEffect)
        }
    }

    private fun tryToRemoveCurrentUser() = allShakeUsers.removeFirstOrNull()

    private fun addUniqueUsersToShakeList(model: List<UserShakeUiModel>) {
        val newElements = model.toSet()
        val currentElements = allShakeUsers.toMutableSet()
        currentElements.addAll(newElements)
        allShakeUsers.clear()
        allShakeUsers.addAll(currentElements)
    }

    private fun observeShakeUsers() {
        getShakeUsersResultUseCase.invoke()
            .distinctUntilChanged()
            .onEach { users ->
                if (users.isNullOrEmpty()) return@onEach
                addUniqueUsersToShakeList(shakeUiMapper.createSuccessUiListEntity(users))
                if (shakeFriendRequestsUiState.value == null) {
                    _shakeFriendRequestsUiState.value = shakeUiMapper.createShakeUserUiStateModel(
                        currentUser = allShakeUsers.first(),
                        allUsers = allShakeUsers.distinct()
                    )
                } else {
                    val currentState = _shakeFriendRequestsUiState.value ?: return@onEach
                    _shakeFriendRequestsUiState.value = shakeUiMapper.changeCurrentUserState(
                        currentUserState = currentState,
                        allUsers = allShakeUsers.distinct()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun hasMoreShakeUsers(): Boolean {
        return allShakeUsers.isNotEmpty()
    }

    private fun handleHttpError(throwable: Throwable) {
        when (throwable) {
            is UnknownHostException -> {
                emitEffect(ShakeFriendRequestsUiEffect.ShowErrorToast(R.string.error_bad_connection))
            }
            else -> {
                emitEffect(ShakeFriendRequestsUiEffect.ShowErrorToast(R.string.error_message_went_wrong_bad_connection))
            }
        }
    }

    private fun observeUpdateFriendship() {
        observeUpdateFriendshipUseCase.invoke()
            .onEach { model ->
                handleUpdateFriendshipAction(
                    userId = model.userId,
                    action = model.action
                )
            }
            .catch { e ->
                Timber.d(e)
                handleHttpError(e)
            }
            .launchIn(viewModelScope)
    }

    private fun handleUpdateFriendshipAction(
        userId: Long,
        action: String
    ) {
        when (action) {
            UPDATE_ADD -> {
                handleAddFriendActionBySocket(userId)
            }
            UPDATE_CONFIRM -> {
                handleConfirmFriendActionBySocket(userId)
            }
            UPDATE_DELETE -> {
                handleDeleteUserActionBySocket(userId)
            }
        }
    }

    private fun handleAddFriendActionBySocket(userId: Long) {
        allShakeUsers.find { friendRequestUser ->
            userId == friendRequestUser.userId
        }?.let { user ->
            val requestedUserIndex = allShakeUsers.indexOf(user)
            if (requestedUserIndex == -1) return@let
            val userResult = user.copy(
                userFriendShakeStatus = UserFriendShakeStatus.USER_SHAKE_FRIEND_REQUESTED_BY_USER
            )
            allShakeUsers[requestedUserIndex] = userResult
            if (_shakeFriendRequestsUiState.value?.shakeUser?.userId == user.userId) {
                _shakeFriendRequestsUiState.postValue(
                    _shakeFriendRequestsUiState.value?.copy(
                        shakeUser = userResult
                    )
                )
            }
        }
    }

    private fun handleConfirmFriendActionBySocket(userId: Long) {
        allShakeUsers.find { friendRequestUser ->
            userId == friendRequestUser.userId
        }?.let { user ->
            val requestedUserIndex = allShakeUsers.indexOf(user)
            if (requestedUserIndex == -1) return@let
            allShakeUsers[requestedUserIndex] = user.copy(
                userFriendShakeStatus = UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS
            )
            if (_shakeFriendRequestsUiState.value?.shakeUser?.userId == user.userId) {
                animateNextOrCloseShakeFriendRequests()
            }
        }
    }

    private fun handleDeleteUserActionBySocket(userId: Long) {
        allShakeUsers.find { friendRequestUser ->
            userId == friendRequestUser.userId
        }?.let { user ->
            val requestedUserIndex = allShakeUsers.indexOf(user)
            if (requestedUserIndex == -1) return@let
            val changedUser = user.copy(
                userFriendShakeStatus = UserFriendShakeStatus.USER_SHAKE_REQUEST_UNKNOWN
            )
            allShakeUsers[requestedUserIndex] = changedUser
            if (_shakeFriendRequestsUiState.value?.shakeUser?.userId == user.userId) {
                _shakeFriendRequestsUiState.postValue(
                    _shakeFriendRequestsUiState.value?.copy(
                        shakeUser = changedUser,
                    )
                )
            }
        }
    }

    private fun animateNextOrCloseShakeFriendRequests() {
        tryToRemoveCurrentUser()
        if (hasMoreShakeUsers()) {
            emitEffect(ShakeFriendRequestsUiEffect.AnimateNextUserUiEffect)
        } else {
            emitEffect(ShakeFriendRequestsUiEffect.CloseShakeFriendRequests)
        }
    }

    private fun animateSkipOrCloseShakeFriendRequests() {
        tryToRemoveCurrentUser()
        if (hasMoreShakeUsers()) {
            emitEffect(ShakeFriendRequestsUiEffect.AnimateSkipUserUiEffect)
        } else {
            emitEffect(ShakeFriendRequestsUiEffect.CloseShakeFriendRequests)
        }
    }

    private fun initWebSocketConnectionHandler() {
        observeWebSocketConnectionUseCase.invoke()
            .onEach(::handleSocketConnectionState)
            .catch { e ->
                Timber.d(e)
            }
            .launchIn(viewModelScope)
    }

    private fun handleSocketConnectionState(socketState: WebSocketConnectionState) {
        when (socketState) {
            WebSocketConnectionState.CONNECTED -> observeUpdateFriendship()
            WebSocketConnectionState.DISCONNECTED -> Timber.d("Web socket offline!")
        }
    }
}

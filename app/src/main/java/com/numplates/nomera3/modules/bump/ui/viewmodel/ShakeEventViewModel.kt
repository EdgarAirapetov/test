package com.numplates.nomera3.modules.bump.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.baseCore.ui.location.LocationEnableProvider
import com.numplates.nomera3.modules.bump.domain.entity.UserShakeModel
import com.numplates.nomera3.modules.bump.domain.usecase.DeleteShakeUserUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.GetShakeFriendRequestsUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.TryToRegisterShakeEventUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.SetShakeCoordinatesUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.EmitShakeUserNotFoundUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.GetNeedToRegisterShakeUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetCurrentLocationUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.HasActiveShakeUsersUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.SetNeedToRegisterShakeUseCase
import com.numplates.nomera3.modules.bump.ui.ShakeAnalyticDelegate
import com.numplates.nomera3.modules.bump.ui.ShakeOpenedTypeEnum
import com.numplates.nomera3.modules.bump.ui.ShakeRequestsDismissListener
import com.numplates.nomera3.modules.bump.ui.entity.ShakeEventDelegateUiEvent
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

@Deprecated("Перенести всю логику в MainActivityViewModel")
class ShakeEventViewModel @Inject constructor(
    private val tryToRegisterShakeEventUseCase: TryToRegisterShakeEventUseCase,
    private val geoLocationEnableProvider: LocationEnableProvider,
    private val setShakeCoordinatesUseCase: SetShakeCoordinatesUseCase,
    private val getShakeFriendRequestsUseCase: GetShakeFriendRequestsUseCase,
    private val emitShakeUserNotFoundUseCase: EmitShakeUserNotFoundUseCase,
    private val dialogDismissListener: DialogDismissListener,
    private val hasActiveShakeUsersResultUseCase: HasActiveShakeUsersUseCase,
    private val shakeRequestsDismissListener: ShakeRequestsDismissListener,
    private val deleteShakeUserUseCase: DeleteShakeUserUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getNeedToRegisterShakeUseCase: GetNeedToRegisterShakeUseCase,
    private val setNeedToRegisterShakeUseCase: SetNeedToRegisterShakeUseCase,
    private val shakeAnalyticDelegate: ShakeAnalyticDelegate,
    private val getUserUidUseCase: GetUserUidUseCase
) : ViewModel() {

    private val _shakeUiEffectFlow = MutableSharedFlow<ShakeEventDelegateUiEvent>()
    val shakeUiEffectFlow: SharedFlow<ShakeEventDelegateUiEvent> = _shakeUiEffectFlow

    private var forceEnabledShake = false
    private var shakeOpenedTypeEnum = ShakeOpenedTypeEnum.DEFAULT

    init {
        observeDialogDismissListener()
        observeShakeClosed()
    }

    fun unregisterEventListener() {
        viewModelScope.launch {
            tryToRegisterShakeEventUseCase.invoke(DISABLE_REGISTER_SHAKE_EVENT)
        }
    }

    fun registerEventListener() {
        viewModelScope.launch {
            tryToRegisterShakeEventUseCase.invoke(NEED_TO_REGISTER_SHAKE_EVENT)
        }
    }

    fun triggerShowShakeOrLocationDialog(isShowDialogByShake: Boolean) {
        setShakeOpenedType(isShowDialogByShake)
        if (isShowDialogByShake.not()) {
            registerShakeIfDisabledByPrivacy()
        }
        if (!geoLocationEnableProvider.hasLocationPermission() || !geoLocationEnableProvider.isLocationEnabled()) {
            emitUiEffect(ShakeEventDelegateUiEvent.ShowGeoEnabledDialog)
            return
        }
        sendUserCoordinatesByShake()
        emitUiEffect(ShakeEventDelegateUiEvent.ShowShakeDialog(isShowDialogByShake))
    }

    fun sendUserCoordinatesByShake() {
        viewModelScope.launch {
            runCatching {
                val currentLocation = getCurrentLocationUseCase.invoke() ?: run {
                    emitUiEffect(
                        ShakeEventDelegateUiEvent.ShowErrorToast(
                            errorMessageRes = R.string.general_google_services_are_not_available
                        )
                    )
                    return@launch
                }
                setShakeCoordinatesUseCase.invoke(
                    gpsX = currentLocation.lat.toFloat(),
                    gpsY = currentLocation.lon.toFloat()
                )
            }.onFailure { e ->
                Timber.d(e)
                handleShakeFriendRequestsError(e)
            }
        }
    }

    fun observeFindFriendsByShake() {
        getShakeFriendRequestsUseCase.invoke()
            .onEach(::handleFindFriendsSocketResponse)
            .catch { e ->
                Timber.d(e)
                handleShakeFriendRequestsError(e)
            }
            .launchIn(viewModelScope)
    }

    fun triggerShakeActionChanged(isShowDialogByShake: Boolean) {
        if (shakeOpenedTypeEnum != ShakeOpenedTypeEnum.SHAKE_OPENED_BY_CLICK) {
            setShakeOpenedType(isShowDialogByShake)
        }
        if (!geoLocationEnableProvider.hasLocationPermission() || !geoLocationEnableProvider.isLocationEnabled()) {
            emitUiEffect(ShakeEventDelegateUiEvent.ShowGeoEnabledDialog)
            return
        }
        sendUserCoordinatesByShake()
        emitUiEffect(ShakeEventDelegateUiEvent.ShowShakeDialog(isShowDialogByShake))
    }

    fun removeShakeUser() {
        if (featureTogglesContainer.shakeFeatureToggle.isEnabled.not()) return
        viewModelScope.launch {
            runCatching { deleteShakeUserUseCase.invoke() }
                .onFailure { e ->
                    Timber.e(e)
                }
        }
    }

    private fun handleFindFriendsSocketResponse(users: List<UserShakeModel>) {
        logShakeResults(users)
        handleShakeFriendRequestsResult(users)
    }

    private fun logShakeResults(users: List<UserShakeModel>) {
        users.forEach { user ->
            val userMutualSize = user.mutualUserModel?.mutualUsers?.size ?: 0
            val moreCount = user.mutualUserModel?.moreCount ?: 0
            val totalMutualCount = userMutualSize + moreCount
            shakeAnalyticDelegate.logShakeResults(
                shakeCalled = shakeOpenedTypeEnum,
                countMutualAudience = totalMutualCount,
                countUserShake = users.size,
                fromId = getUserUidUseCase.invoke(),
                toId = user.userId
            )
        }
    }

    private fun emitUiEffect(event: ShakeEventDelegateUiEvent) {
        viewModelScope.launch {
            _shakeUiEffectFlow.emit(event)
        }
    }

    private fun handleShakeFriendRequestsResult(shakeUsers: List<UserShakeModel>) {
        if (shakeUsers.isNotEmpty()) {
            emitUiEffect(ShakeEventDelegateUiEvent.ShowShakeFriendRequests)
        } else {
            emitUsersNotFoundEvent()
        }
    }

    private fun emitUsersNotFoundEvent() {
        viewModelScope.launch {
            emitShakeUserNotFoundUseCase.invoke()
        }
    }

    private fun handleShakeFriendRequestsError(throwable: Throwable) {
        when (throwable) {
            is UnknownHostException -> {
                emitUiEffect(ShakeEventDelegateUiEvent.ShowErrorToast(R.string.error_bad_connection))
            }
            else -> {
                emitUiEffect(ShakeEventDelegateUiEvent.ShowErrorToast(R.string.error_message_went_wrong_bad_connection))
            }
        }
    }

    private fun observeDialogDismissListener() {
        viewModelScope.launch {
            dialogDismissListener.sharedFlow.collect { type ->
                when (type) {
                    DismissDialogType.SHAKE -> {
                        if (!hasActiveShakeUsersResultUseCase.invoke()) removeShakeUser()
                        unRegisterShakeIfDisabledByPrivacy()
                        resetShakeOpenedType()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun observeShakeClosed() {
        shakeRequestsDismissListener.observeShakeRequestsClosed()
            .onEach {
                removeShakeUser()
            }
            .launchIn(viewModelScope)
    }

    private fun registerShakeIfDisabledByPrivacy() {
        if (getNeedToRegisterShakeUseCase.invoke().not()) {
            viewModelScope.launch {
                setNeedToRegisterShakeUseCase.invoke(true)
            }
            forceEnabledShake = true
        }
    }

    private fun unRegisterShakeIfDisabledByPrivacy() {
        if (forceEnabledShake) {
            viewModelScope.launch {
                setNeedToRegisterShakeUseCase.invoke(false)
            }
            forceEnabledShake = false
        }
    }

    private fun resetShakeOpenedType() {
        shakeOpenedTypeEnum = ShakeOpenedTypeEnum.DEFAULT
    }

    private fun setShakeOpenedType(showDialogByShake: Boolean) {
        shakeOpenedTypeEnum = if (showDialogByShake) ShakeOpenedTypeEnum.SHAKE_OPENED_BY_SENSOR else
            ShakeOpenedTypeEnum.SHAKE_OPENED_BY_CLICK
    }

    private companion object {
        const val NEED_TO_REGISTER_SHAKE_EVENT = true
        const val DISABLE_REGISTER_SHAKE_EVENT = false
    }
}

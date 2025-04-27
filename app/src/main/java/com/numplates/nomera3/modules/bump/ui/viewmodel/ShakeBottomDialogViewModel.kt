package com.numplates.nomera3.modules.bump.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.doDelayed
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.baseCore.ui.location.LocationEnableProvider
import com.numplates.nomera3.modules.bump.domain.entity.ShakeEvent
import com.numplates.nomera3.modules.bump.domain.usecase.ObserveRegisterShakeEventUseCase
import com.numplates.nomera3.modules.bump.hardware.ShakeEventListener
import com.numplates.nomera3.modules.bump.hardware.ShakeVibrator
import com.numplates.nomera3.modules.bump.ui.entity.ShakeBottomDialogUiEffect
import com.numplates.nomera3.modules.bump.ui.entity.ShakeDialogStatus
import com.numplates.nomera3.modules.bump.ui.entity.ShakeDialogUiState
import com.numplates.nomera3.modules.bump.ui.fragment.ShakeBottomDialogFragment.Companion.DIALOG_OPENED_BY_SHAKE
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val SHAKE_ANIMATION_PLAY = 4000L

class ShakeBottomDialogViewModel @Inject constructor(
    private val dismissListener: DialogDismissListener,
    private val observeRegisterShakeEventUseCase: ObserveRegisterShakeEventUseCase,
    private val shakeVibrator: ShakeVibrator,
    private val locationEnableProvider: LocationEnableProvider,
    private val shakeEventListener: ShakeEventListener
)  : ViewModel() {

    private val _shakeDialogState = MutableLiveData(getDefaultState())
    val shakeDialogState: LiveData<ShakeDialogUiState> = _shakeDialogState

    private val _shakeDialogUiEffect = MutableSharedFlow<ShakeBottomDialogUiEffect>()
    val shakeDialogUiEffect: SharedFlow<ShakeBottomDialogUiEffect> = _shakeDialogUiEffect

    private var jobShakeTimer: Job? = null

    init {
        initShakeUsersFoundObserver()
        observeShake()
    }

    override fun onCleared() {
        super.onCleared()
        jobShakeTimer?.cancel()
    }

    fun setDialogDismissed() {
        viewModelScope.launch {
            dismissListener.dialogDismissed(DismissDialogType.SHAKE)
        }
    }

    fun setUsersNotFoundUiState() {
        _shakeDialogState.value = _shakeDialogState.value?.copy(
            shakeLabelTextRes = R.string.shake_users_not_found,
            shakeMessageTextRes = R.string.shake_users_not_found_message,
            shakeDialogStatus = ShakeDialogStatus.NOT_FOUND
        )
    }

    fun setSelectedOpenType(openType: String?) {
        if (openType.isNullOrEmpty()) return
        val isShowTurnOnShakeText = openType == DIALOG_OPENED_BY_SHAKE
        val isCoarseLocation = locationEnableProvider.isCoarseLocation()
        _shakeDialogState.value = _shakeDialogState.value?.copy(
            isShowShakeLocationEnableDescription = isShowTurnOnShakeText,
            isShowTurnOnAccurateLocationButton = isCoarseLocation
        )
    }

    fun startAnimationTimer() {
        jobShakeTimer?.cancel()
        jobShakeTimer = doDelayed(SHAKE_ANIMATION_PLAY) {
            emitUiEffect(ShakeBottomDialogUiEffect.ResetShakeViewsPosition)
        }
    }

    private fun initShakeUsersFoundObserver() {
        observeRegisterShakeEventUseCase.invoke()
            .onEach(::handleShakeEvent)
            .launchIn(viewModelScope)
    }

    private fun handleShakeEvent(event: ShakeEvent) {
        if (event is ShakeEvent.ShakeUsersNotFound) {
            shakeVibrator.vibrateSingleTime()
            emitUiEffect(ShakeBottomDialogUiEffect.AnimateFadeUiEffect)
        }
    }

    private fun emitUiEffect(effect: ShakeBottomDialogUiEffect) {
        viewModelScope.launch {
            _shakeDialogUiEffect.emit(effect)
        }
    }

    private fun getDefaultState() = ShakeDialogUiState(
        shakeLabelTextRes = R.string.shake,
        shakeMessageTextRes = R.string.shake_dialog_description
    )

    private fun observeShake() {
        shakeEventListener.observeShakeChanged()
            .onEach {
                if (shakeDialogState.value?.shakeDialogStatus == ShakeDialogStatus.NOT_FOUND) {
                    val newState = _shakeDialogState.value?.copy(
                        shakeLabelTextRes = R.string.shake,
                        shakeMessageTextRes = R.string.shake_dialog_description,
                        shakeDialogStatus = ShakeDialogStatus.SEARCHING
                    )
                    _shakeDialogState.postValue(newState)
                }
                startAnimationTimer()
                emitUiEffect(ShakeBottomDialogUiEffect.AnimateShakeUiEffect)
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }
}

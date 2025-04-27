package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.SHOW_FRIENDS_AND_SUBSCRIBERS
import com.numplates.nomera3.domain.interactornew.PushSetPrivacySettingsUseCase
import com.numplates.nomera3.domain.interactornew.IsNeedShowFriendsFollowersPrivacyUseCase
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.presentation.view.fragments.entity.FriendsFollowersPrivacyUiEvent
import com.numplates.nomera3.presentation.view.fragments.entity.FriendsFollowersPrivacyUiState
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FriendsFollowersPrivacyViewModel : BaseViewModel() {

    @Inject
    lateinit var dismissListener: DialogDismissListener

    @Inject
    lateinit var pushSetPrivacySettingsUseCase: PushSetPrivacySettingsUseCase

    @Inject
    lateinit var isNeedShowFriendsFollowersPrivacyUseCase: IsNeedShowFriendsFollowersPrivacyUseCase

    private val _friendsPrivacyLiveState = MutableLiveData<FriendsFollowersPrivacyUiState>()
    val friendsPrivacyLiveState: LiveData<FriendsFollowersPrivacyUiState> =
        _friendsPrivacyLiveState
    private val _friendsPrivacyViewEvent = MutableSharedFlow<FriendsFollowersPrivacyUiEvent>()
    val friendsPrivacyViewUiEvent: SharedFlow<FriendsFollowersPrivacyUiEvent> =
        _friendsPrivacyViewEvent
    var privacyRow: CustomRowSelector.CustomRowSelectorModel? = null

    init {
        App.component.inject(this)
    }

    fun updateButtonState(buttonText: String?, isButtonEnabled: Boolean) {
        _friendsPrivacyLiveState.value = FriendsFollowersPrivacyUiState(
            isButtonEnabled = isButtonEnabled,
            buttonText = buttonText ?: ""
        )
    }

    fun setFollowersPrivacy(model: CustomRowSelector.CustomRowSelectorModel) {
        viewModelScope.launch(SupervisorJob()) {
            runCatching {
                pushSetPrivacySettingsUseCase.invoke(
                    key = SHOW_FRIENDS_AND_SUBSCRIBERS,
                    model = model
                )
            }.onSuccess {
                privacyRow = model
                updateButtonState(
                    isButtonEnabled = true,
                    buttonText = _friendsPrivacyLiveState.value?.buttonText
                )
            }.onFailure { e ->
                Timber.e(e)
                pushErrorEvent(FriendsFollowersPrivacyUiEvent.ErrorSelectPrivacyUi)
            }
        }
    }

    fun pushDialogShowed() {
        viewModelScope.launch {
            isNeedShowFriendsFollowersPrivacyUseCase.invoke(false)
        }
    }

    fun setDialogDismissed() {
        viewModelScope.launch {
            dismissListener.dialogDismissed(DismissDialogType.FRIENDS_FOLLOWERS_PRIVACY)
        }
    }

    private fun pushErrorEvent(typeUiEvent: FriendsFollowersPrivacyUiEvent) {
        viewModelScope.launch {
            _friendsPrivacyViewEvent.emit(typeUiEvent)
        }
    }
}

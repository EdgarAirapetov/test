package com.numplates.nomera3.modules.peoples.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetAdminSupportIdUseCase
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleOnboardingUiState
import com.numplates.nomera3.modules.peoples.ui.onboarding.PeopleOnboardingFragment.Companion.ONBOARDING_SHOW_FIRST_TIME_ACTION
import kotlinx.coroutines.launch
import javax.inject.Inject

class PeopleOnboardingViewModel @Inject constructor(
    private val dialogDismissListener: DialogDismissListener,
    private val getAdminSupportIdUseCase: GetAdminSupportIdUseCase
) : ViewModel() {

    private val _peopleOnboardingState = MutableLiveData(PeopleOnboardingUiState())
    val peopleOnboardingState: LiveData<PeopleOnboardingUiState> = _peopleOnboardingState

    fun setButtonTextState(@StringRes buttonTextRes: Int) {
        val newState = _peopleOnboardingState.value?.copy(
            buttonTextRes = buttonTextRes
        )
        _peopleOnboardingState.postValue(newState)
    }

    fun setPageIndicatorVisibility(onBoardingMode: String) {
        val needShowPageIndicator = onBoardingMode == ONBOARDING_SHOW_FIRST_TIME_ACTION
        val state = _peopleOnboardingState.value?.copy(
            pageIndicatorVisibility = needShowPageIndicator
        )
        _peopleOnboardingState.value = state
    }

    fun setDialogDismissed() = viewModelScope.launch {
        dialogDismissListener.dialogDismissed(DismissDialogType.PEOPLE_ONBOARDING)
    }

    fun getAdminSupportId(): Long = getAdminSupportIdUseCase.invoke()
}

package com.numplates.nomera3.presentation.view.fragments.meerasettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.rateus.domain.RateUsSaveLastShowUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsWriteIsRatedUseCase
import com.numplates.nomera3.presentation.view.fragments.meerasettings.RateUsDialogAction.OnCancelClicked
import com.numplates.nomera3.presentation.view.fragments.meerasettings.RateUsDialogAction.OnSendRatingClick
import com.numplates.nomera3.presentation.view.fragments.meerasettings.RateUsDialogAction.OnTextChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TEXT_INPUT_LENGTH_FOR_SHOWING_DESCRIPTION = 231

class MeeraRateUsViewModel @Inject constructor(
    private val rateUsUseCase: RateUsUseCase,
    //Ждем документацию по аналитике
//    private val rateUsAnalyticUseCase: RateUsAnalyticUseCase,
    private val rateUsWriteIsRatedUseCase: RateUsWriteIsRatedUseCase,
    private val rateUsSaveLastShowUseCase: RateUsSaveLastShowUseCase,
    private val rateUsUiMapper: RateUsUiMapper
) : ViewModel() {

    private val _rateUsDialogState = MutableLiveData<RateUsDialogState>()
    val rateUsDialogState: LiveData<RateUsDialogState> = _rateUsDialogState

    init {
        rateUsSaveLastShowUseCase.invoke()
    }

    fun onAction(action: RateUsDialogAction) {
        viewModelScope.launch {
            when (action) {
                is OnSendRatingClick -> {
                    rateUsUseCase.invoke(action.rating, action.comment)
                    rateUsWriteIsRatedUseCase.invoke(isRated = true)
                    _rateUsDialogState.value = rateUsUiMapper.mapRateUsDialogState(isRatingSent = true)
                }
                is OnTextChanged -> {
                    if (action.text.length >= TEXT_INPUT_LENGTH_FOR_SHOWING_DESCRIPTION) {
                        _rateUsDialogState.value = rateUsUiMapper.mapRateUsDialogState(text = action.text)
                    } else {
                        _rateUsDialogState.value = RateUsDialogState(description = "")
                    }
                }
                OnCancelClicked -> {
                    rateUsWriteIsRatedUseCase.invoke(isRated = false)
                    _rateUsDialogState.value = rateUsUiMapper.mapRateUsDialogState(isCancel = true)
                }
            }
        }
    }

}

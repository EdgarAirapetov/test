package com.numplates.nomera3.modules.complains.ui.change

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.complains.domain.usecase.GetActualComplainsUseCase
import com.numplates.nomera3.modules.complains.ui.change.ChangeReasonEvent.FinishFlow
import com.numplates.nomera3.modules.complains.ui.mapper.ComplainReasonMapper
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeReasonViewModel @Inject constructor(
    private val getActualComplainsUseCase: GetActualComplainsUseCase,
    private val complainReasonMapper: ComplainReasonMapper,
) : ViewModel() {

    private val _screenLiveData = MutableLiveData<List<ChangeReasonUiModel>>()
    val screenLiveData: LiveData<List<ChangeReasonUiModel>> = _screenLiveData.distinctUntilChanged()

    private val _screenEvents = MutableSharedFlow<ChangeReasonEvent>()
    val screenEvents = _screenEvents.asSharedFlow()

    private var selectReasonJob: Job? = null

    fun initializeMenuItems(complain: UserComplainUiModel, complaintType: Int?) {
        val items = getActualComplainsUseCase.invoke(complaintType)
            .map(complainReasonMapper::mapReasonToModel)
            .map { complainUiModel ->
                ChangeReasonUiModel(
                    complainUiModel = complainUiModel,
                    isChecked = complainUiModel.reasonId == complain.reasonId,
                )
            }
        _screenLiveData.postValue(items)
    }

    fun selectComplain(complain: ChangeReasonUiModel) {
        selectReasonJob?.cancel()
        selectReasonJob = viewModelScope.launch {
            val items = _screenLiveData.value?.toMutableList() ?: return@launch
            items.forEachIndexed { index, item ->
                if (item.isChecked && item.complainUiModel.reasonId != complain.complainUiModel.reasonId) {
                    items[index] = item.copy(isChecked = false)
                } else if (item.complainUiModel.reasonId == complain.complainUiModel.reasonId) {
                    items[index] = item.copy(isChecked = true)
                }
            }
            _screenLiveData.postValue(items)
            delay(400)
            _screenEvents.emit(FinishFlow)
        }
    }
}

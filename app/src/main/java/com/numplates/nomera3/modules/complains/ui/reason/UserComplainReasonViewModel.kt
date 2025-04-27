package com.numplates.nomera3.modules.complains.ui.reason

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.AmplitudeComplaints
import com.numplates.nomera3.modules.baseCore.helper.amplitude.complaints.RulesOpenWhere
import com.numplates.nomera3.modules.complains.domain.usecase.GetActualComplainsUseCase
import com.numplates.nomera3.modules.complains.ui.mapper.ComplainReasonMapper
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.user.ui.entity.UserComplainItemType.HEADER
import javax.inject.Inject

class UserComplainReasonViewModel @Inject constructor(
    private val getActualComplainsUseCase: GetActualComplainsUseCase,
    private val complainReasonMapper: ComplainReasonMapper,
    private val analyticsComplaints: AmplitudeComplaints,
) : ViewModel() {

    private val _screenLiveData = MutableLiveData<List<UserComplainUiModel>>()
    val screenLiveData: LiveData<List<UserComplainUiModel>> = _screenLiveData.distinctUntilChanged()

    fun initializeMenuItems(complaintType: Int?) {
        _screenLiveData.postValue(getListComplains(complaintType))
    }

    fun logMenuOpened() {
        analyticsComplaints.profileReportStart()
    }

    fun logOpenRules() {
        analyticsComplaints.rulesOpen(RulesOpenWhere.REPORT)
    }

    private fun getListComplains(complaintType: Int?): List<UserComplainUiModel> {
        val complainList = mutableListOf<UserComplainUiModel>()
        complainList.add(
            UserComplainUiModel(
                itemType = HEADER,
                titleRes = R.string.user_complain_header_title
            )
        )
        complainList
            .addAll(getActualComplainsUseCase.invoke(complaintType)
            .map(complainReasonMapper::mapReasonToModel))
        return complainList
    }
}

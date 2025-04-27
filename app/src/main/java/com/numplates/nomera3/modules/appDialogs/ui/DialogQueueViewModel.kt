package com.numplates.nomera3.modules.appDialogs.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.appDialogs.DialogEntity
import com.numplates.nomera3.modules.appDialogs.DialogState
import com.numplates.nomera3.modules.appDialogs.DialogType
import com.numplates.nomera3.modules.appDialogs.domain.DialogPrepareParams
import com.numplates.nomera3.modules.appDialogs.domain.DialogPrepareUseCase
import com.numplates.nomera3.modules.appDialogs.domain.DialogQueueUseCase
import com.meera.core.preferences.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DialogQueueViewModel : ViewModel() {

    val eventLiveData = MutableLiveData<DialogQueueViewEvent>()

    @Inject
    lateinit var dialogQueue: DialogQueueUseCase

    @Inject
    lateinit var dialogPrepare: DialogPrepareUseCase

    @Inject
    lateinit var appSettings: AppSettings

    private var dialogShowState: DialogShowState = DialogShowState.IDLE
    private var dialogToShow: DialogEntity? = null

    init {
        App.component.inject(this)
    }

    fun getDialogToShow() {
        when (dialogShowState) {
            DialogShowState.IDLE -> requestDialogToShow()
            DialogShowState.READY_TO_PREPARE -> prepareDialogToShow()
            DialogShowState.PREPARED -> showDialog()
            DialogShowState.SHOWING -> showDialog()
            else -> return
        }
    }

    fun isNeedToShowOnBoarding() = appSettings.readNeedOnBoarding()

    fun setDialogShown(dialogType: DialogType) {
        clearProcessingDialog()
        viewModelScope.launch(Dispatchers.IO) {
            dialogQueue.setDialogShown(dialogType)
        }
    }

    fun setDialogNotCompleted(dialogType: DialogType) {
        clearProcessingDialog()
        viewModelScope.launch(Dispatchers.IO) {
            dialogQueue.setDialogNotCompleted(dialogType)
        }
    }

    private fun requestDialogToShow() {
        dialogShowState = DialogShowState.REQUESTED
        viewModelScope.launch(Dispatchers.IO) {
            handleDialogToShow(dialogQueue.getDialogToShow())
        }
    }

    private fun clearProcessingDialog() {
        dialogShowState = DialogShowState.IDLE
        dialogToShow = null
    }

    private fun handleDialogToShow(dialogEntity: DialogEntity?) {
        if (dialogToShow?.type == dialogEntity?.type) return
        dialogToShow = dialogEntity
        dialogShowState = DialogShowState.READY_TO_PREPARE
        if (dialogEntity != null) prepareDialogToShow()
        else requestDialogToShow()
    }

    private fun prepareDialogToShow() {
        dialogShowState = DialogShowState.PREPARING
        val dialog = dialogToShow
        if (dialog != null) {
            viewModelScope.launch(Dispatchers.IO) {
                dialogPrepare.execute(
                    params = DialogPrepareParams(dialog),
                    success = ::handleDialogState,
                    fail = {
                        dialogShowState = DialogShowState.READY_TO_PREPARE
                        Timber.e(it)
                    }
                )
            }
        } else {
            requestDialogToShow()
        }
    }

    private fun handleDialogState(dialogEntity: DialogEntity) {
        dialogShowState = DialogShowState.PREPARED
        when (dialogEntity.state) {
            DialogState.COMPLETED -> {
                dialogShowState = DialogShowState.IDLE
                getDialogToShow()
            }
            DialogState.NOT_SHOWN,
            DialogState.NOT_COMPLETED_LAST_STEP -> showDialog()
        }
    }

    private fun showDialog() {
        dialogShowState = DialogShowState.SHOWING
        when (dialogToShow?.type) {
            DialogType.ONBOARDING -> {
                event(DialogQueueViewEvent.ShowDialog.Onboarding)
            }
            DialogType.ENABLE_CALLS -> {
                event(DialogQueueViewEvent.ShowDialog.EnableCalls)
            }
            else -> return
        }
        dialogToShow = null
    }

    private fun event(event: DialogQueueViewEvent) {
        if (eventLiveData.value == event) return
        eventLiveData.postValue(event)
    }

    private enum class DialogShowState {
        IDLE,
        REQUESTED,
        READY_TO_PREPARE,
        PREPARING,
        PREPARED,
        SHOWING
    }
}

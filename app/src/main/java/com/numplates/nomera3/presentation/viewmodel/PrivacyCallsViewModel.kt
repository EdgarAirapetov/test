package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.WHO_CAN_CALL
import com.numplates.nomera3.data.network.PrivacySetting
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallsSettings
import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector
import com.numplates.nomera3.presentation.viewmodel.viewevents.CallsEnabledViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PrivacyCallsViewModel: ViewModel() {

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var dialogDismissListener: DialogDismissListener

    val liveEvent = MutableLiveData<CallsEnabledViewEvent>()

    private val disposables = CompositeDisposable()

    fun init() = App.component.inject(this)

    override fun onCleared() = disposables.dispose()

    fun setCallSetting(model: CustomRowSelector.CustomRowSelectorModel) {
        val settings = mutableListOf(PrivacySetting(WHO_CAN_CALL, model.selectorModelId))
        val payload = mutableMapOf<String, Any>(
                "settings" to settings
        )

        val d = webSocketMainChannel.pushSetPrivacySettings(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveEvent.value = CallsEnabledViewEvent.SettingSaved(model)
                },{
                    liveEvent.value = CallsEnabledViewEvent.SettingSavedError
                    Timber.e(it)
                })

        disposables.add(d)
    }

    fun dialogShowed() = appSettings.writeIsWorthToShow(true)

    fun closeClicked(selectorModelId: Int?) = logWhoCanCallSettings(selectorModelId)

    fun onDialogDismissed() = viewModelScope.launch {
        dialogDismissListener.dialogDismissed(DismissDialogType.CALL_ENABLE)
    }

    private fun logWhoCanCallSettings(setting: Int?) {
        val whoCanCall =  when(setting) {
            0 -> AmplitudePropertyCallsSettings.NOBODY
            1 -> AmplitudePropertyCallsSettings.ALL
            2 -> AmplitudePropertyCallsSettings.FRIENDS
            else -> AmplitudePropertyCallsSettings.NONE
        }
        tracker.logCallsPermission(whoCanCall)
    }

}

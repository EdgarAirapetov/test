package com.numplates.nomera3.modules.redesign

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.network.websocket.WebSocketConnectionManager
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.redesign.deeplink.DeeplinkController
import com.numplates.nomera3.modules.redesign.deeplink.MeeraDeeplinkParam
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.telecom.MeeraSignalingServiceConnectionWrapper
import dagger.Lazy
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// TODO: https://nomera.atlassian.net/browse/BR-30131
class MeeraActivityViewModel : ViewModel() {

    @Inject
    lateinit var socketManager: WebSocketConnectionManager

    @Inject
    lateinit var socketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var loadSignupCountriesUseCase: LoadSignupCountriesUseCase

    @Inject
    lateinit var meeraSignalingServiceConnectionWrapper: MeeraSignalingServiceConnectionWrapper

    @Inject
    lateinit var deeplinkController: DeeplinkController

    @Inject
    lateinit var getAppInfoUseCase: Lazy<GetAppInfoAsyncUseCase>

    @Inject
    lateinit var appSettings: Lazy<AppSettings>


    init {
        App.component.inject(this)
        Timber.d("INIT_MEERA Activity VM -> Connect socket")
        loadCountries()
        getAppInfo()
    }

    fun connect() {
        Timber.d("INIT_MEERA Activity VM -> Connect socket FUNC")
        socketManager.test()
    }

    fun emitDeeplinkCall(intent: Intent) {
        val data = if (intent.data != null) {
            MeeraDeeplinkParam.MeeraDeeplinkData(false, intent)
        } else if (intent.action != null) {
            MeeraDeeplinkParam.MeeraDeeplinkData(true, intent)
        } else {
            return
        }

        viewModelScope.launch {
            deeplinkController.processDeeplinkCall(data)
        }
    }

    fun emitDeeplinkCall(action: MeeraDeeplinkParam) {
        viewModelScope.launch {
            deeplinkController.processDeeplinkCall(action)
        }
    }

    fun getSocket() = socketMainChannel

    fun getSignalingServiceConnectionWrapper() = meeraSignalingServiceConnectionWrapper

    private fun loadCountries() {
        viewModelScope.launch {
            runCatching { loadSignupCountriesUseCase.invoke() }
                .onFailure { Timber.e(it) }
        }
    }

    private fun getAppInfo() {
        viewModelScope.launch {
            try {
                val appInfo = getAppInfoUseCase.get().executeAsync().await()
                handleAppInfoResponse(appInfo)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun handleAppInfoResponse(settings: Settings?) {
        appSettings.get().saveAppLinks(settings?.links)
    }

}

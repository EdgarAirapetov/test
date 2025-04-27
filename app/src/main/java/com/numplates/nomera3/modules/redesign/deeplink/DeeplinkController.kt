package com.numplates.nomera3.modules.redesign.deeplink

import android.content.Intent
import com.meera.core.di.scopes.AppScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import javax.inject.Inject

@AppScope
class DeeplinkController @Inject constructor() {

    private val _mutableDeeplinkSharedFlow = MutableSharedFlow<MeeraDeeplinkAction>(replay = 1)
    val deeplinkSharedFlow = _mutableDeeplinkSharedFlow.asSharedFlow()

    suspend fun processDeeplinkCall(data: MeeraDeeplinkParam) {
        val action = when (data) {
            MeeraDeeplinkParam.None -> MeeraDeeplinkAction.None

            is MeeraDeeplinkParam.MeeraDeeplinkData -> {
                if (data.isPush) {
                    handleDeeplinkAsPush(data.intent)
                } else {
                    handleDeeplinkByDefault(data.intent?.data.toString())
                }
            }

            is MeeraDeeplinkParam.MeeraDeeplinkActionContainer -> {
                data.action
            }
        }

        _mutableDeeplinkSharedFlow.emit(action)
    }

    private fun handleDeeplinkByDefault(uri: String): MeeraDeeplinkAction {
        return try {
            MeeraDeeplink.getAction(uri) ?: MeeraDeeplinkAction.None
        } catch (error: Throwable) {
            Timber.e(error)
            MeeraDeeplinkAction.None
        }
    }

    private fun handleDeeplinkAsPush(intent: Intent?): MeeraDeeplinkAction =
        MeeraDeeplinkAction.PushWrapper(intent)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearDeeplinkCache() {
        _mutableDeeplinkSharedFlow.resetReplayCache()
    }
}

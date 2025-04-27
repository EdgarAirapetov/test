package com.numplates.nomera3.modules.auth.util

import com.meera.core.preferences.AppSettings
import com.meera.core.utils.HardwareIdUtil
import com.meera.db.DataStore
import com.numplates.nomera3.di.MAIN_ROAD_FILTER_SETTINGS
import com.numplates.nomera3.domain.interactornew.ClearMainRoadFilterRecommendedStateUseCase
import com.numplates.nomera3.domain.interactornew.GetMainFilterSettingsUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.auth.data.repository.ANON_TOKEN_LEADING
import com.numplates.nomera3.modules.auth.domain.AuthIsAuthorizedUseCase
import com.numplates.nomera3.modules.auth.domain.AuthLogoutUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.ClearMediaKeyboardStickerPacksUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.SetSubPostsRequestedInSession
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.registration.ui.FirebasePushSubscriberDelegate
import io.reactivex.Completable
import kotlinx.coroutines.rx2.await
import timber.log.Timber
import javax.inject.Inject

class LogoutDelegate @Inject constructor(
    private val isAuthorizedUserUseCase: AuthIsAuthorizedUseCase,
    private val authLogoutUseCase: AuthLogoutUseCase,
    private val repository: PostsRepository,
    private val authenticatorDelegate: FirebasePushSubscriberDelegate,
    private val appSettings: AppSettings,
    private val settings: AppSettings,
    private val hardwareId: HardwareIdUtil,
    private val dataStore: DataStore,
    private val clearMediaKeyboardStickersUseCase: ClearMediaKeyboardStickerPacksUseCase,
    private val setSubPostsRequestedInSession: SetSubPostsRequestedInSession,
    private val clearMainRoadFilterRecommendedStateUseCase: ClearMainRoadFilterRecommendedStateUseCase,
    private val getMainFilterSettingsUseCase: GetMainFilterSettingsUseCase,
    private val analyticsInteractor: AnalyticsInteractor
) {

    suspend fun logout(
        isCancelledRegistration: Boolean,
        unsubscribePush: Boolean,
        changeServer: Boolean = false,
        action: suspend (
            isNotCancelledRegistration: Boolean,
            isHolidayIntroDialogShown: Boolean
        ) -> Unit,
        innerAction: suspend () -> Unit
    ) {
        if (!changeServer) {
            if (isAuthorizedUserUseCase.isAuthorizedUser().not()) return
        }

        analyticsInteractor.logUserExit(settings.readUID())

        val logoutToken = appSettings.readAccessToken()

        authLogoutUseCase.logout()

        repository.clear()

        if (unsubscribePush) {
            authenticatorDelegate.unsubscribePush(logoutToken)
        }

        clearDataAndLogOut()

        innerAction()
        action(!isCancelledRegistration, false)
    }

    private suspend fun clearDataAndLogOut() {
        val mainFilterSettings = getMainFilterSettingsUseCase.invoke().data
        appSettings.clearPreferences()
        appSettings.writeFilterSettings(MAIN_ROAD_FILTER_SETTINGS, mainFilterSettings)

        val anonToken = "$ANON_TOKEN_LEADING${hardwareId.getHardwareId()}"
        appSettings.writeAccessToken(anonToken)
        appSettings.userRefreshToken = anonToken

        runCatching {
            Completable.fromAction { dataStore.clearAllTables() }.await()
        }.onFailure { Timber.e("Cannot clear DB, error $it") }

        clearMediaKeyboardStickersUseCase.invoke()
        setSubPostsRequestedInSession.invoke(false)

        appSettings.profileNotification.set(false)

        clearMainRoadFilterRecommendedStateUseCase.invoke()
    }
}

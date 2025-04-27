package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.auth.domain.AuthLogoutUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfileEditTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.profilesettings.model.ProfileSettingsEffect
import com.numplates.nomera3.modules.registration.ui.FirebasePushSubscriberDelegate
import com.numplates.nomera3.modules.userprofile.domain.maper.toChatInitUserProfile
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.UpdateOwnUserProfileUseCase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val SUPPORT_USER_ID_DEFAULT = 0L

class ProfileSettingsViewModel @Inject constructor(
    private val authLogoutUseCase: AuthLogoutUseCase,
    private val settings: AppSettings,
    private val analyticsInteractor: AnalyticsInteractor,
    private val friendInviteTapAnalytics: FriendInviteTapAnalytics,
    private val refreshOwnProfileUseCase: UpdateOwnUserProfileUseCase,
    private val ownProfileFlow: ObserveLocalOwnUserProfileModelUseCase,
    private val authenticatorDelegate: FirebasePushSubscriberDelegate,
    private val getProfileUserCase: GetProfileUseCase,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val profileAnalytics: AmplitudeProfile,
) : BaseViewModel() {

    private val _profileSettingsEffectFlow: MutableSharedFlow<ProfileSettingsEffect> = MutableSharedFlow()
    val profileSettingsEffectFlow: SharedFlow<ProfileSettingsEffect> = _profileSettingsEffectFlow

    val disposables = CompositeDisposable()

    fun getOwnProfileFlow() = ownProfileFlow.invoke().distinctUntilChanged()

    /**
     * Request user profile and refresh in Db
     */
    fun refreshOwnUserProfile() {
        Timber.d("Load user INFO")
        viewModelScope.launch {
            runCatching {
                refreshOwnProfileUseCase.invoke()
            }.onSuccess {
                Timber.d("User profile successfully")
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun supportClicked() {
        val supportUserId = settings.supportUserId
        if (supportUserId != null && supportUserId != SUPPORT_USER_ID_DEFAULT) {
            processSupportUserId(supportUserId)
        }
    }

    fun aboutMeeraClicked() {
        val supportUserId = settings.supportUserId
        if (supportUserId != null && supportUserId != SUPPORT_USER_ID_DEFAULT) {
            viewModelScope.launch{
                _profileSettingsEffectFlow.emit(ProfileSettingsEffect.AboutMeeraUserIdFound(supportUserId))
            }
        }
    }

    private fun processSupportUserId(userId: Long) {
        viewModelScope.launch {
            val supportUser = runCatching {
                getProfileUserCase(userId)
            }.getOrNull()?.toChatInitUserProfile()
            cacheCompanionUserUseCase.invoke(supportUser)
            _profileSettingsEffectFlow.emit(ProfileSettingsEffect.SupportUserIdFound(userId))
        }
    }

    fun logInviteFriend(where: FriendInviteTapProperty) {
        friendInviteTapAnalytics.logFiendInviteTap(where)
    }

    fun logProfileEditTap() {
        profileAnalytics.logProfileEditTap(
            userId = settings.readUID(),
            where = AmplitudeProfileEditTapProperty.SETTINGS
        )
    }

    fun logout(action: () -> Unit) {
        analyticsInteractor.logUserExit(settings.readUID())
        viewModelScope.launch(Dispatchers.IO) {
            authenticatorDelegate.unsubscribePush()
            authLogoutUseCase.logout()
            action()
        }
    }
}

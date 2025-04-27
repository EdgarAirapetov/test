package com.numplates.nomera3.modules.complains.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserParams
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserUseCase
import com.numplates.nomera3.modules.moments.settings.notshow.domain.MomentSettingsNotShowAddExclusionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserComplainViewModel @Inject constructor(
    private val blockStatusUseCase: BlockStatusUseCase,
    private val hideUserRoads: HidePostsOfUserUseCase,
    private val hideUserMoments: MomentSettingsNotShowAddExclusionUseCase,
    private val appSettings: AppSettings,
    private val analyticsInteractor: AnalyticsInteractor,
) : ViewModel() {
    private val _complainEvents = MutableSharedFlow<ComplainEvents>()
    val complainEvents: Flow<ComplainEvents> = _complainEvents

    private fun launchEvent(event: ComplainEvents) = viewModelScope.launch {
        _complainEvents.emit(event)
    }

    fun hideUserRoad(userId: Long?) {
        userId?.let { id ->
            viewModelScope.launch {
                hideUserRoads.execute(
                    params = HidePostsOfUserParams(id),
                    success = {
                        launchEvent(ComplainEvents.PostsDisabledEvents)
                        Timber.e("Success hide user road")
                    },
                    fail = {
                        Timber.e("Fail hide user road")
                        launchEvent(ComplainEvents.ComplainFailed)
                    }
                )
            }
        }

    }

    fun hideUserMoments(remoteUserId: Long?) {
        val userId = remoteUserId ?: return
        viewModelScope.launch {
            runCatching {
                hideUserMoments.invoke(listOf(userId))
            }.onSuccess {
                launchEvent(ComplainEvents.MomentsHidden(userId))
            }.onFailure {
                Timber.e("Failed to hide user(id=$userId) moments, error=$it")
              launchEvent(ComplainEvents.ComplainFailed)
            }
        }
    }

    fun hideUserRequestModerators() {
        launchEvent(ComplainEvents.RequestModerators)
    }

    fun blockUser(remoteUserId: Long?, isBlock: Boolean) {
        remoteUserId?.let {
            viewModelScope.launch {
                val blockParams = DefBlockParams(
                    userId = appSettings.readUID(),
                    remoteUserId = remoteUserId,
                    isBlocked = isBlock
                )
                kotlin.runCatching {
                    blockStatusUseCase.invoke(
                        params = blockParams
                    )
                }.onSuccess {
                    launchEvent(ComplainEvents.UserBlocked)
                }.onFailure {
                    launchEvent(ComplainEvents.ComplainFailed)
                }
            }
        }
    }

    fun logAdditionalEvent(action: ComplainExtraActions) =
        analyticsInteractor.logComplainExtraAction(action)

}

sealed class ComplainEvents {
    object PostsDisabledEvents : ComplainEvents()
    data class MomentsHidden(val userId: Long) : ComplainEvents()
    object UserBlocked : ComplainEvents()
    object ComplainFailed : ComplainEvents()
    object RequestModerators: ComplainEvents()
}

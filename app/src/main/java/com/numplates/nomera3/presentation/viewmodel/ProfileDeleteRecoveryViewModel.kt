package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.DeleteRestoreProfileUseCase
import com.numplates.nomera3.modules.auth.domain.AuthLogoutUseCase
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.userprofile.domain.usecase.UpdateOwnUserProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserProfileViewEvent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ProfileDeleteRecoveryViewModel: BaseViewModel() {

    @Inject
    lateinit var profileManager: DeleteRestoreProfileUseCase

    @Inject
    lateinit var refreshOwnProfileUseCase: UpdateOwnUserProfileUseCase

    @Inject
    lateinit var authLogoutUseCase: AuthLogoutUseCase

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var getOwnLocalProfileUseCase: GetOwnLocalProfileUseCase

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var ownProfileUseCase: ObserveLocalOwnUserProfileModelUseCase

    private val disposables = CompositeDisposable()

    val liveViewEvent = MutableLiveData<UserProfileViewEvent>()

    init {
        App.component.inject(this)
    }

    fun getUserProfileLive() = ownProfileUseCase.invoke()

    fun deleteProfile(reasonId: Int?){
        requestCallback({
            profileManager.deleteOwnProfile(reasonId)
        },{
            onSuccess {
                appSettings.holidayCalendarShownToUserWithId = 0L
                logProfileDeleted(reasonId)
            }
            onError { _, _ ->
                liveViewEvent.postValue(UserProfileViewEvent.ProfileDeleteError)
            }
        })

    }

    fun recoverProfile(){
        requestCallback({
            profileManager.restoreOwnProfile()
        },{
            onSuccess {
                refreshOwnUserProfile()
            }

            onError { _, _ ->
                liveViewEvent.postValue(UserProfileViewEvent.ProfileRecoveryError)
            }
        })
    }

    private fun logProfileDeleted(reasonId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = getOwnLocalProfileUseCase.invoke()
            tracker.logUserProfileDelete(profile?.userId ?: 0L, reasonId ?: -1)
            liveViewEvent.postValue(UserProfileViewEvent.ProfileDeleteSuccess)
        }
    }

    /**
     * Request user profile and refresh in Db
     */
    private fun refreshOwnUserProfile() {
        Timber.d("Load user INFO")
        viewModelScope.launch {
            runCatching {
                refreshOwnProfileUseCase()
            }.onSuccess {
                Timber.d("Own profileUpdated")
                liveViewEvent.postValue(UserProfileViewEvent.ProfileRecoverySuccess)
            }.onFailure { e ->
                Timber.e(e)
                liveViewEvent.postValue(UserProfileViewEvent.ProfileRecoverySuccess)
            }
        }
    }

    override fun onCleared() {
        disposables.clear()
    }
}

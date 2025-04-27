package com.numplates.nomera3.modules.notifications.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toEpoch
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.meera.db.DataStore
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.NotificationCounterUseCase
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.notifications.data.mediator.PAGE_SIZE
import com.numplates.nomera3.modules.notifications.domain.DefParams
import com.numplates.nomera3.modules.notifications.domain.mapper.NotificationMapper
import com.numplates.nomera3.modules.notifications.domain.usecase.ConvertHeaderActionUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteAllEventsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteBlockUserNotificationsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteNotificationByIdUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.FetchAndCacheNotificationsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.FlowNotificationsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAllNotificationAsReadUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadNotificationParams
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadNotificationsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.TriggerNotificationUpdateUsecase
import com.numplates.nomera3.modules.notifications.domain.usecase.UpdateNotificationsAvatarMomentsUseCase
import com.numplates.nomera3.modules.notifications.helpers.HeaderMaker
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.user.domain.usecase.UploadUserAvatarUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UserUploadAvatarParams
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val appSettings: AppSettings,
    @Deprecated("Adhere to presentation-domain-data architecture.")
    private val ds: DataStore,
    private val deleteNotificationByIdUseCase: DeleteNotificationByIdUseCase,
    private val deleteAllNotificationUseCase: DeleteAllEventsUseCase,
    private val deleteBlockUserNotificationsUseCase: DeleteBlockUserNotificationsUseCase,
    private val markAllNotificationAsReadUseCase: MarkAllNotificationAsReadUseCase,
    private val markEventAsReadUseCase: MarkAsReadNotificationsUseCase,
    private val notificationCounterUseCase: NotificationCounterUseCase,
    private val convertHeaderActionUseCase: ConvertHeaderActionUseCase,
    private val syncService: SyncNotificationService,
    private val amplitudeHelper: AnalyticsInteractor,
    private val processAnimatedAvatar: ProcessAnimatedAvatar,
    private val uploadAvatarUseCase: UploadUserAvatarUseCase,
    private val triggerNotificationUpdateUsecase: TriggerNotificationUpdateUsecase,
    private val ownProfile: ObserveLocalOwnUserProfileModelUseCase,
    private val fileManager: FileManager,
    private val momentsObserverUseCase: SubscribeMomentsEventsUseCase,
    private val updateNotificationsAvatarMomentsUseCase: UpdateNotificationsAvatarMomentsUseCase,
    private val fetchAndCacheNotificationsUseCase: FetchAndCacheNotificationsUseCase,
    private val headerMaker: HeaderMaker,
    private val uiNotificationMapper: com.numplates.nomera3.modules.notifications.ui.mapper.NotificationMapper,
    flowNotificationsUseCase: FlowNotificationsUseCase,
) : BaseViewModel() {

    private var canRequestUpdate: Boolean = true
    private var lastTimestamp = 0L
    private var latestPage = -1

    private val domainNotificationMapper = NotificationMapper()

    private val _liveViewEvent: MutableLiveData<NotificationViewEvent> = MutableLiveData()
    var liveViewEvent: LiveData<NotificationViewEvent> = _liveViewEvent

    val notifications: StateFlow<List<NotificationUiModel>> = flowNotificationsUseCase.invoke()
        .mapNotNull { notifications ->
            headerMaker.clearHeaders()
            headerMaker.insertHeaders(
                notifications
                    .map(domainNotificationMapper::apply)
                    .map(uiNotificationMapper::apply)
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val liveUnreadNotificationCounter = MutableLiveData<Int>()

    var isLast = false
        private set
    var isLoading = false
        private set

    private val refreshListener: (Long) -> Unit = {
        refresh()
    }

    init {
        setupSyncService()
        observeMoments()
    }

    fun setUserAvatarState(avatarState: String) {
        appSettings.userAvatarState = avatarState
    }

    fun getOwnAvatarAnimationFlow() = ownProfile.invoke().map { it.avatarAnimation }

    private fun setupSyncService() {
        syncService.addRefreshListener(refreshListener)
        syncService.setEventListener {
            _liveViewEvent.postValue(NotificationViewEvent.ShowRefreshBtn)
        }
        syncService.unreadNotificationCountObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    liveUnreadNotificationCounter.postValue(it)
                },
                onError = {
                    Timber.e("Network error when request counter:${it.message}")
                },
                onComplete = {}
            ).addDisposable()
    }

    fun readAll() {
        viewModelScope.launch {
            markAllNotificationAsReadUseCase.invoke()
            amplitudeHelper.logAllNotificationsDeleted()
            requestCounter()
        }
    }

    fun loadNotifications(page: Int) {
        viewModelScope.launch {
            runCatching {
                latestPage = page
                isLoading = true
                isLast = false
                fetchAndCacheNotificationsUseCase.invoke(lastTimestamp.toEpoch(), PAGE_SIZE)
            }.onSuccess { items ->
                if (items.isEmpty()) {
                    isLast = true
                } else {
                    val lastItem = items.last()
                    if (lastItem.date.time == lastTimestamp || items.size < PAGE_SIZE) {
                        isLast = true
                    }
                    lastTimestamp = lastItem.date.time
                }
                isLoading = false
                canRequestUpdate = true
            }.onFailure {
                isLoading = false
                canRequestUpdate = true
            }
        }
    }

    private fun reloadNotifications(page: Int) {
        viewModelScope.launch {
            headerMaker.clearHeaders()
            runCatching {
                isLoading = true
                fetchAndCacheNotificationsUseCase.invoke(0, page * PAGE_SIZE + PAGE_SIZE)
            }.onSuccess { items ->
                if (items.isNotEmpty()) {
                    val lastItem = items.last()
                    lastTimestamp = lastItem.date.time
                }
                isLoading = false
                canRequestUpdate = true
            }.onFailure {
                isLoading = false
                canRequestUpdate = true
            }
        }
    }

    fun markAsRead(notificationId: String, isGroup: Boolean) {
        markEventAsReadUseCase
            .execute(MarkAsReadNotificationParams(notificationId, isGroup))
            .subscribe({ requestCounter() }, {})
            .addDisposable()
    }

    fun deleteAll() {
        deleteAllNotificationUseCase.execute(DefParams()).clearAllAndRecount()
    }

    private fun Single<ResponseWrapper<Boolean>>.clearAllAndRecount() {
        this.flatMap { ds.notificationDao().deleteAllNotifications() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                amplitudeHelper.logAllNotificationsDeleted()
                requestCounter()
            }, {})
            .addDisposable()
    }

    fun refresh() {
        if (!canRequestUpdate) return
        canRequestUpdate = false
        reloadNotifications(latestPage)
    }

    fun deleteNotificationsWhenUserBlocked() {
        viewModelScope.launch {
            runCatching {
                deleteBlockUserNotificationsUseCase.invoke()
            }.onFailure { Timber.e("ERROR When delete block user Notifications") }
        }
    }

    fun deleteNotification(notificationId: String, isGroup: Boolean) {
        viewModelScope.launch {
            runCatching {
                deleteNotificationByIdUseCase.invoke(notificationId = notificationId, isGroup = isGroup)
            }.onSuccess {
                refresh()
            }.onFailure {
                restoreNotificationIfNotNull(notificationId)
                Timber.e(it)
            }
        }
    }

    fun logCommunityScreenOpened() {
        amplitudeHelper.logCommunityScreenOpened(
            AmplitudePropertyWhereCommunityOpen.NOTIFICATIONS
        )
    }

    fun restoreNotificationIfNotNull(id: String) {
        viewModelScope.launch {
            runCatching {
                triggerNotificationUpdateUsecase.invoke(id)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncService.removeRefreshListener(refreshListener)
    }

    fun requestCounter() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = notificationCounterUseCase.getCounter()
                if (response.data != null) {
                    val count = response.data.count ?: 0
                    Timber.d("RESP counter:$count")
                    if (count == 0) {
                        convertHeaderActionUseCase.invoke()
                        _liveViewEvent.postValue(NotificationViewEvent.HideRefreshBtn)
                    }
                    liveUnreadNotificationCounter.postValue(count)
                } else {
                    Timber.e("ERROR when request counter")
                }
            } catch (e: Exception) {
                Timber.e("Network error when request counter:${e.message}")
            }
        }
    }

    fun saveAvatarInFile(avatarState: String) {
        Observable.just(avatarState)
            .flatMap {
                Observable.fromCallable {
                    val bitmap = processAnimatedAvatar.createBitmap(avatarState)
                    processAnimatedAvatar.saveInFile(bitmap)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ path ->
                Timber.i("Avatar saved path:${path}")
                if (path.isNotEmpty()) {
                    uploadUserAvatar(path, avatarState)
                }
            }, { error ->
                Timber.i("Save avatar in file failed:${error}")
            }).addDisposable()
    }

    private fun uploadUserAvatar(path: String, avatarState: String? = null) {
        Timber.d("Upload user avatar => path: $path")
        uploadAvatarUseCase
            .execute(UserUploadAvatarParams(path, avatarState))
            .map {
                fileManager.deleteFile(path)
                it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                Timber.d("Upload avatar success ${response.data}")
            }, { error ->
                Timber.e("ERROR: upload user avatar: $error")
            }).addDisposable()
    }

    private fun observeMoments() {
        momentsObserverUseCase.invoke()
            .onEach { momentEvent ->
                if (momentEvent is MomentRepositoryEvent.UserMomentsStateUpdated) {
                    handleUserMomentsState(momentEvent.userMomentsStateUpdate)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleUserMomentsState(momentsState: UserMomentsStateUpdateModel) {
        viewModelScope.launch {
            runCatching {
                updateNotificationsAvatarMomentsUseCase.invoke(momentsState)
            }.onFailure { Timber.e(it) }
        }
    }
}

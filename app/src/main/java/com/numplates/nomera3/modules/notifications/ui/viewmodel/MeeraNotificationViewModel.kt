package com.numplates.nomera3.modules.notifications.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.meera.core.extensions.combineWith
import com.meera.core.utils.files.FileManager
import com.meera.db.DataStore
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.domain.interactornew.NotificationCounterUseCase
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.notifications.data.callback.MeeraNotificationBoundaryCallback
import com.numplates.nomera3.modules.notifications.data.mediator.preparePagingConfig
import com.numplates.nomera3.modules.notifications.domain.DefParams
import com.numplates.nomera3.modules.notifications.domain.mapper.NotificationMapper
import com.numplates.nomera3.modules.notifications.domain.usecase.ConvertHeaderActionUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteAllEventsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteBlockUserNotificationsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteNotificationByIdUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAllNotificationAsReadUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadNotificationParams
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadNotificationsUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.TriggerNotificationUpdateUsecase
import com.numplates.nomera3.modules.notifications.domain.usecase.UpdateNotificationsAvatarMomentsUseCase
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationScreenState
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.mapper.CachedUserForChatMapper
import com.numplates.nomera3.modules.notifications.ui.mapper.UiKitNotificationCellMapper
import com.numplates.nomera3.modules.user.domain.usecase.UploadUserAvatarUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UserUploadAvatarParams
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject


class MeeraNotificationViewModel @Inject constructor(
    private val boundaryCallback: MeeraNotificationBoundaryCallback,
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
    private val fileManager: FileManager,
    private val momentsObserverUseCase: SubscribeMomentsEventsUseCase,
    private val updateNotificationsAvatarMomentsUseCase: UpdateNotificationsAvatarMomentsUseCase,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val cachedCompanionMapper: CachedUserForChatMapper,
    private val uiKitNotificationCellMapper: UiKitNotificationCellMapper,
    private val uiNotificationMapper: com.numplates.nomera3.modules.notifications.ui.mapper.NotificationMapper
) : ViewModel() {

    private var canRequestUpdate: Boolean = true

    private val disposables = CompositeDisposable()

    private val domainNotificationMapper = NotificationMapper()

    private val _liveViewEvent: MutableSharedFlow<NotificationViewEvent> = MutableSharedFlow()
    var liveViewEvent: SharedFlow<NotificationViewEvent> = _liveViewEvent

    val liveNotificationPaged: LiveData<PagedList<NotificationCellUiModel>> by lazy { setupDataFetch() }

    val screenStateFlow: Flow<NotificationScreenState> =
        liveNotificationPaged.combineWith(boundaryCallback.networkState) { pagedList, networkState ->
            Timber.d("Fetch Observer pagedList=$pagedList, networkState=$networkState,")
            NotificationScreenState(
                isEmptyNotifications = pagedList?.isEmpty() == true && networkState != NetworkState.Status.RUNNING,
                isInitialLoading = pagedList.isNullOrEmpty() && networkState == NetworkState.Status.RUNNING,
            )
        }.asFlow()

    private val fetchObserver = Observer<NetworkState.Status> { state ->
        Timber.d("Fetch Observer state=$state")
        canRequestUpdate = when (state) {
            NetworkState.Status.RUNNING -> false
            NetworkState.Status.SUCCESS,
            NetworkState.Status.FAILED -> true
        }
    }

    private val refreshListener: (Long) -> Unit = {
        refresh()
    }

    init {
        syncService.addRefreshListener(refreshListener)
        syncService.setEventListener {
            viewModelScope.launch {
                _liveViewEvent.emit(NotificationViewEvent.ShowRefreshBtn)
            }
        }
        boundaryCallback.networkState.observeForever(fetchObserver)
    }

    override fun onCleared() {
        super.onCleared()
        boundaryCallback.networkState.removeObserver(fetchObserver)
        boundaryCallback.clear()
        syncService.removeRefreshListener(refreshListener)
        disposables.dispose()
    }

    fun readAll() {
        viewModelScope.launch {
            markAllNotificationAsReadUseCase.invoke()
            amplitudeHelper.logAllNotificationsDeleted()
            requestCounter()
            refresh()
        }
    }

    fun markAsRead(notificationId: String, isGroup: Boolean) {
        disposables.add(
            markEventAsReadUseCase
                .execute(MarkAsReadNotificationParams(notificationId, isGroup))
                .subscribe({ requestCounter() }, {})
        )
    }

    fun deleteAll() {
        deleteAllNotificationUseCase.execute(DefParams()).clearAllAndRecount()
    }

    private fun Single<ResponseWrapper<Boolean>>.clearAllAndRecount() {
        disposables.add(
            this.flatMap { ds.notificationDao().deleteAllNotifications() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    amplitudeHelper.logAllNotificationsDeleted()
                    requestCounter()
                }, {})
        )
    }

    fun refresh() {
        if (!canRequestUpdate) return
        canRequestUpdate = false
        boundaryCallback.onZeroItemsLoaded()
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
            }.onSuccess {
                refresh()
            }
        }
    }

    fun requestCounter() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = notificationCounterUseCase.getCounter()
                if (response.data != null) {
                    val count = response.data.count ?: 0
                    Timber.d("NOTIFICATION_COUNTER_LOG RESP counter:$count")
                    if (count == 0) {
                        convertHeaderActionUseCase.invoke()
                        _liveViewEvent.emit(NotificationViewEvent.HideRefreshBtn)
                    }
                    _liveViewEvent.emit(NotificationViewEvent.UpdateGlobalNotificationCounter(count))
                } else {
                    Timber.e("ERROR when request counter")
                }
            } catch (e: Exception) {
                Timber.e("Network error when request counter:${e.message}")
            }
        }
    }

    fun saveAvatarInFile(avatarState: String) {
        disposables.add(
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
                })
        )
    }

    fun uploadUserAvatar(path: String, avatarState: String? = null) {
        Timber.d("Upload user avatar => path: $path")
        disposables.add(
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
                })
        )
    }

    fun observeMoments() {
        momentsObserverUseCase.invoke()
            .onEach { momentEvent ->
                if (momentEvent is MomentRepositoryEvent.UserMomentsStateUpdated) {
                    handleUserMomentsState(momentEvent.userMomentsStateUpdate)
                }
            }
            .launchIn(viewModelScope)
    }

    fun cacheUserProfileForChat(user: User?) {
        user?.let {
            val cachedUser = cachedCompanionMapper.mapToCachedUser(user)
            cacheCompanionUserUseCase.invoke(cachedUser)
        }
    }

    private fun setupLocalDataFetch(): DataSource.Factory<Int, NotificationCellUiModel> {
        return ds.notificationDao()
            .getAllNotificationPaged()
            .map(domainNotificationMapper::apply)
            .map(uiNotificationMapper::apply)
            .map(uiKitNotificationCellMapper::apply)
    }

    private fun setupDataFetch(): LiveData<PagedList<NotificationCellUiModel>> {
        return LivePagedListBuilder(setupLocalDataFetch(), preparePagingConfig())
            .setBoundaryCallback(boundaryCallback)
            .setFetchExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
            .build()
    }

    private fun handleUserMomentsState(momentsState: UserMomentsStateUpdateModel) {
        viewModelScope.launch {
            runCatching {
                updateNotificationsAvatarMomentsUseCase.invoke(momentsState)
            }.onFailure { Timber.e(it) }
        }
    }

}

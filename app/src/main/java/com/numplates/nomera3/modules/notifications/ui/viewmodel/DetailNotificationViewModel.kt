package com.numplates.nomera3.modules.notifications.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.meera.db.DataStore
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.notifications.data.callback.GroupNotificationBoundaryCallback
import com.numplates.nomera3.modules.notifications.data.mediator.preparePagingConfig
import com.numplates.nomera3.modules.notifications.domain.mapper.NotificationMapper
import com.numplates.nomera3.modules.notifications.domain.usecase.ClearCachedNotificationGroupUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.DeleteNotificationByIdUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkNotificationAsReadUseCase
import com.numplates.nomera3.modules.notifications.domain.usecase.TriggerNotificationUpdateUsecase
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DetailNotificationViewModel @Inject constructor(
    private val boundaryCallback: GroupNotificationBoundaryCallback,
    @Deprecated("For new functionality route actions to domain layer instead")
    private val ds: DataStore,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val clearCachedNotificationGroupUseCase: ClearCachedNotificationGroupUseCase,
    private val deleteNotificationByIdUseCase: DeleteNotificationByIdUseCase,
    private val syncNotificationService: SyncNotificationService,
    val analyticsInteractor: AnalyticsInteractor,
    private val triggerNotificationUpdateUsecase: TriggerNotificationUpdateUsecase,
    private val uiNotificationMapper: com.numplates.nomera3.modules.notifications.ui.mapper.NotificationMapper
) : BaseViewModel() {

    private val domainNotificationMapper = NotificationMapper()

    private val _liveEvent: MutableLiveData<NotificationViewEvent> = MutableLiveData()
    var liveEvent = _liveEvent as LiveData<NotificationViewEvent>

    var liveNotificationPaged: LiveData<PagedList<NotificationUiModel>>? = null
        private set

    private val refreshListener: (Long) -> Unit = {
        refresh()
    }

    var notificationGroupId: String? = null
        private set
    var notificationType: String? = null
        private set
    var isNotificationRead: Boolean? = null
        private set

    init {
        syncNotificationService.addRefreshListener(refreshListener)
    }

    override fun onCleared() {
        super.onCleared()
        boundaryCallback.clear()
        syncNotificationService.removeRefreshListener(refreshListener)
    }

    fun setAttributes(
        notificationGroupId: String? = null,
        notificationType: String? = null,
        isNotificationRead: Boolean? = null
    ) {
        this.notificationGroupId = notificationGroupId
        this.notificationType = notificationType
        this.isNotificationRead = isNotificationRead
        setupPagingNotification()
    }

    fun refresh() {
        val notificationGroupId = this.notificationGroupId ?: return
        viewModelScope.launch {
            clearCachedNotificationGroupUseCase.invoke(notificationGroupId)
        }
    }

    fun markAsRead(
        notificationId: String,
        isGroup: Boolean,
        unreadNotifications: List<NotificationUiModel>
    ) {
        viewModelScope.launch {
            postLoaderVisibility(isVisible = true)
            runCatching {
                markNotificationAsReadUseCase.invoke(
                    notificationId = notificationId,
                    isGroup = isGroup,
                    unreadNotifications = unreadNotifications
                )
            }.onFailure {
                Timber.e(it)
            }
            postLoaderVisibility(isVisible = true)
        }
    }

    fun deleteNotification(notificationId: String, isGroup: Boolean) {
        viewModelScope.launch {
            postLoaderVisibility(isVisible = true)
            runCatching {
                deleteNotificationByIdUseCase.invoke(notificationId = notificationId, isGroup = isGroup)
            }.onSuccess {
                liveNotificationPaged?.value?.dataSource?.invalidate()
            }.onFailure {
                Timber.e(it)
            }
            postLoaderVisibility(isVisible = false)
        }
    }

    private fun postLoaderVisibility(isVisible: Boolean) {
        _liveEvent.postValue(NotificationViewEvent.SetLoadIndicatorVisibility(isVisible = isVisible))
    }

    private fun setupPagingNotification() {
        val notificationGroupId = this.notificationGroupId ?: return
        boundaryCallback.apply {
            onNoNotificationData = { _liveEvent.postValue(NotificationViewEvent.CloseFragment) }
            groupId = notificationGroupId
        }

        val roadPostsDb = ds
            .notificationDao()
            .getAllNotificationPagedByGroupId(notificationGroupId)
            .map { domainNotificationMapper.apply(it) }
            .map { uiNotificationMapper.apply(it) }

        liveNotificationPaged = LivePagedListBuilder(roadPostsDb, preparePagingConfig())
            .setBoundaryCallback(boundaryCallback)
            .build()
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
}

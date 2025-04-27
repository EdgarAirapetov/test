package com.numplates.nomera3.modules.notifications.data.callback

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.meera.core.extensions.simpleName
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.modules.notifications.data.api.NotificationApi
import com.numplates.nomera3.modules.notifications.data.mapper.ListOfNotificationResponseMapperById
import com.numplates.nomera3.modules.notifications.data.mediator.makePage
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.presentation.view.adapter.newpostlist.PagingRequestHelper
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

class GroupNotificationBoundaryCallback @Inject constructor(
        private val notificationApi: NotificationApi,
        private val repository: NotificationRepository
) : PagedList.BoundaryCallback<NotificationUiModel>() {

    private val coroutineScope = MainScope()

    val networkState: MutableLiveData<NetworkState.Status> = MutableLiveData()

    var onNoNotificationData: () -> Unit = {}

    lateinit var groupId: String

    private val helper = PagingRequestHelper(Executors.newSingleThreadExecutor())

    private val mapper = ListOfNotificationResponseMapperById()

    private val disposables = CompositeDisposable()

    private var pageN: Int = 0

    private var lastBool: Boolean? = null

    override fun onZeroItemsLoaded() {
        pageN = 0
        lastBool = null
        val page = pageN.makePage()

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            fetchNotification(
                limit = page.limit,
                offset = page.offset,
                helperCallback = helperCallback,
                actionRunning = { networkState.postValue(NetworkState.Status.RUNNING) },
                actionSuccess =  { networkState.postValue(NetworkState.Status.SUCCESS) },
                actionFailure =  { networkState.postValue(NetworkState.Status.FAILED) },
                isFirstLoad = true
            )
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: NotificationUiModel) {
        val page = pageN.makePage()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            fetchNotification(page.limit, page.offset, helperCallback,
                    { },
                    { networkState.postValue(NetworkState.Status.SUCCESS) },
                    { networkState.postValue(NetworkState.Status.FAILED) }
            )
        }
    }

    private fun fetchNotification(
            limit: Int,
            offset: Int,
            helperCallback: PagingRequestHelper.Request.Callback,
            actionRunning: () -> Any,
            actionSuccess: () -> Any,
            actionFailure: () -> Any,
            isFirstLoad: Boolean = false
    ) {
        actionRunning()
        coroutineScope.launch {
            runCatching {
                val response = notificationApi.fetchExpandedNotification(groupId, limit, offset)
                val data = response.data
                val dataMapped = mapper.apply(data, groupId)
                if (dataMapped.isNotEmpty()) repository.insertNotificationsSuspend(dataMapped)
                Timber.d("fetchNotification id_groupId: ${dataMapped.map { it.id to it.groupId }}")
                dataMapped
            }.onSuccess { dataMapped ->
                if (isFirstLoad && dataMapped.isEmpty()) onNoNotificationData.invoke()
                actionSuccess.invoke()
                helperCallback.recordSuccess()
                pageN += 1
            }.onFailure {
                Timber.tag(this@GroupNotificationBoundaryCallback.simpleName).e(it)
                actionFailure.invoke()
                helperCallback.recordFailure(it)
            }
        }
    }

    fun clear() {
        disposables.clear()
    }
}

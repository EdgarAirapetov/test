package com.numplates.nomera3.modules.notifications.data.callback

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.meera.core.extensions.toEpoch
import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_NOTHING
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.meera.db.models.notifications.InfoSectionEntity
import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.helpers.PositionHandler
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.presentation.view.adapter.newpostlist.PagingRequestHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

private const val INITIAL_MILLISECONDS_OFFSET = 0L

class MeeraNotificationBoundaryCallback @Inject constructor(
    private val repository: NotificationRepository
) : PagedList.BoundaryCallback<NotificationCellUiModel>() {

    val networkState: MutableLiveData<NetworkState.Status> = MutableLiveData()

    private val helper = PagingRequestHelper(Executors.newSingleThreadExecutor())

    private val scope = MainScope()

    private val positionHandler = PositionHandler<NotificationEntity>()

    override fun onZeroItemsLoaded() {
        positionHandler.clearHeaders()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            fetchNotification(
                milliSecondOffset = INITIAL_MILLISECONDS_OFFSET,
                limit = positionHandler.limit,
                helperCallback = helperCallback,
                actionRunning = { networkState.postValue(NetworkState.Status.RUNNING) },
                actionSuccess = { networkState.postValue(NetworkState.Status.SUCCESS) },
                actionFailure = { networkState.postValue(NetworkState.Status.FAILED) }
            )
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: NotificationCellUiModel) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            fetchNotification(
                milliSecondOffset = itemAtEnd.data.date.time.toEpoch(),
                limit = positionHandler.limit,
                helperCallback = helperCallback,
                actionRunning = { networkState.postValue(NetworkState.Status.RUNNING) },
                actionSuccess = { networkState.postValue(NetworkState.Status.SUCCESS) },
                actionFailure = { networkState.postValue(NetworkState.Status.FAILED) }
            )
        }
    }

    fun clear() {
        scope.coroutineContext.cancelChildren()
    }

    private fun getId(index: Int): String = "${index + 1}"

    private fun insertHeaders(list: List<NotificationEntity>): List<NotificationEntity> {
        if (list.isNotEmpty()) {
            val isNotReadEvent: Boolean = list.find { !it.isRead } != null
            val mutableList = list.toMutableList()

            var offset = 0

            list.forEachIndexed { i, en ->
                if (positionHandler.isEmptyHeaders) {
                    mutableList.add(i + offset, makeHeader(en, false, isNotReadEvent))
                    offset += 1
                } else if (!positionHandler.headersContainsName(en.dateGroup)) {
                    mutableList.add(
                        index = i + offset,
                        element = makeHeader(data = en, suppressAction = true, isNotReadEvent = false)
                    )
                    offset += 1
                }
            }

            return mutableList
        }

        return list
    }

    private fun fetchNotification(
        milliSecondOffset: Long,
        limit: Int,
        helperCallback: PagingRequestHelper.Request.Callback,
        actionRunning: () -> Any,
        actionSuccess: () -> Any,
        actionFailure: () -> Any
    ) {
        actionRunning()
        scope.launch {
            runCatching {
                val notificationList = repository.fetchNotificationsRemote(milliSecondOffset, limit)
                deleteOldNotifications(notificationList, milliSecondOffset)
                val listWithHeaders = insertHeaders(notificationList)
                insertToDb(listWithHeaders)
                listWithHeaders
            }.onSuccess {
                actionSuccess.invoke()
                helperCallback.recordSuccess()
                positionHandler.increasePage()
            }.onFailure {
                Timber.e(it)
                actionFailure.invoke()
                helperCallback.recordFailure(it)
            }
        }
    }

    private fun makeHeader(
        data: NotificationEntity, suppressAction: Boolean,
        isNotReadEvent: Boolean
    ): NotificationEntity {
        val action =
            if (suppressAction) {
                ACTION_TYPE_NOTHING
            } else if (positionHandler.isEmptyHeaders) {
                if (isNotReadEvent) {
                    ACTION_TYPE_READ_ALL
                } else {
                    ACTION_TYPE_DELETE_ALL
                }
            } else {
                ACTION_TYPE_NOTHING
            }

        val name = data.dateGroup

        val lastIndex = positionHandler.lastIndexHeader
        val newIndex = getId(lastIndex)

        val section =
            InfoSectionEntity(
                newIndex,
                getPriority(lastIndex),
                name,
                action
            )

        val notificationEntity =
            NotificationEntity(
                id = newIndex,
                infoSection = section,
                dateGroup = data.dateGroup,
                dateLong = data.dateLong
            )

        return positionHandler.addHeader(notificationEntity)
    }

    private fun getPriority(index: Int): Int = index + 1

    private suspend fun insertToDb(list: List<NotificationEntity>): List<Long> =
        repository.insertNotificationsSuspend(list)

    private suspend fun deleteOldNotifications(list: List<NotificationEntity>, milliSecondOffset: Long) {
        if (milliSecondOffset == INITIAL_MILLISECONDS_OFFSET) {
            repository.deleteOlderOrEqual(Long.MAX_VALUE)
        } else {
            val date = list.minByOrNull { it.dateLong }?.dateLong ?: return
            repository.deleteOlderOrEqual(date)
        }
    }

}

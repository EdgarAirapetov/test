package com.numplates.nomera3.modules.chat.requests.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.chat.drafts.domain.GetAllDraftsUseCase
import com.numplates.nomera3.modules.chat.drafts.ui.DraftsUiMapper
import com.meera.db.models.DraftUiModel
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.requests.domain.usecase.GetChatRequestRoomsUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.HideChatRequestRoomsUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UnhideRequestRoomsUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UpdateMessagesAsChatRequestParams
import com.numplates.nomera3.modules.chat.requests.domain.usecase.UpdateMessagesAsChatRequestUseCase
import com.numplates.nomera3.modules.chat.requests.ui.viewevent.ChatRequestViewEvent
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val LIST_PAGE_SIZE = 20

class ChatRequestViewModel @Inject constructor(
    private val blockReportHelper: ChatRequestBlockReportHelper,
    private val chatRequestRoomsUseCase: GetChatRequestRoomsUseCase,
    private val hideChatRequestRoomsUseCase: HideChatRequestRoomsUseCase,
    private val unhideRequestRoomsUseCase: UnhideRequestRoomsUseCase,
    private val updateMessagesAsChatRequestUseCase: UpdateMessagesAsChatRequestUseCase,
    private val appSettings: AppSettings,
    private val networkStatusProvider: NetworkStatusProvider,
    private val getAllDraftsUseCase: GetAllDraftsUseCase,
    private val draftsMapper: DraftsUiMapper
): ViewModel() {

    private val _chatRequestEventsFlow = MutableSharedFlow<ChatRequestViewEvent>(
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val chatRequestEventsFlow = _chatRequestEventsFlow as Flow<ChatRequestViewEvent>

    var liveChatRequestList: LiveData<PagedList<DialogEntity>> = MutableLiveData()

    private val pendingWork = mutableListOf<ChatRequestActionUiModel>()

    private val drafts = mutableListOf<DraftUiModel>()

    init {
        startCollectingActionResult()
    }

    override fun onCleared() {
        pendingWork.clear()
        super.onCleared()
    }

    private fun emitViewEvent(event: ChatRequestViewEvent) {
        viewModelScope.launch { _chatRequestEventsFlow.emit(event) }
    }

    fun startAllPendingWork() {
        for (work in pendingWork) {
            executePendingAction(work)
        }
    }

    fun cancelPendingAction(action: ChatRequestActionUiModel) {
        pendingWork.remove(action)
        when(action) {
            is ChatRequestActionUiModel.BlockUserWorkData -> unhideChatRequest(action.companionData)
            is ChatRequestActionUiModel.BlockReportUserWorkData -> {
                unhideChatRequest(action.companionDialog)
                blockReportHelper.postReportUserWork(
                    userId = action.userId,
                    companionDialog = action.companionDialog,
                    complaintReasonId = action.complaintReasonId
                )
            }
        }
    }

    fun executePendingAction(action: ChatRequestActionUiModel) {
        viewModelScope.launch {
            when(action) {
                is ChatRequestActionUiModel.BlockUserWorkData -> executeBlockAction(action)
                is ChatRequestActionUiModel.BlockReportUserWorkData -> executeBlockReportAction(action)
            }
            pendingWork.remove(action)
        }
    }

    fun prepareBlockUserJob(dialog: DialogEntity) {
        if (networkStatusProvider.isInternetConnected()) {
            viewModelScope.launch { hideChatRequest(dialog) }
            val blockWorkData = ChatRequestActionUiModel
                .BlockUserWorkData(
                    userId = getOwnUserId(),
                    companionData = dialog
                )
            pendingWork.add(blockWorkData)
            emitViewEvent(ChatRequestViewEvent.BlockUserJobCreated(blockWorkData))
        } else {
            emitViewEvent(
                ChatRequestViewEvent.BlockUserResult(
                    dialogToDelete = dialog,
                    isSuccess = false,
                )
            )
        }
    }

    fun prepareBlockReportUserAction(dialog: DialogEntity, complaintReasonId: Int) {
        if (networkStatusProvider.isInternetConnected()) {
            viewModelScope.launch { hideChatRequest(dialog) }
            val blockWorkData = ChatRequestActionUiModel
                .BlockReportUserWorkData(
                    userId = getOwnUserId(),
                    companionDialog = dialog,
                    complaintReasonId = complaintReasonId,
                )
            pendingWork.add(blockWorkData)
            emitViewEvent(ChatRequestViewEvent.BlockReportUserJobCreated(blockWorkData))
        } else {
            emitViewEvent(
                ChatRequestViewEvent.BlockReportUserResult(
                    dialogToDelete = dialog,
                    isSuccess = false,
                )
            )
        }
    }

    fun unhideChatRequest(dialog: DialogEntity) {
        viewModelScope.launch {
            runCatching {
                unhideRequestRoomsUseCase.invoke(dialog)
            }
        }
    }

    fun getDrafts() {
        viewModelScope.launch {
            drafts.clear()
            drafts += runCatching {
                getAllDraftsUseCase.invoke().map(draftsMapper::mapDomainToUiModel)
            }.getOrDefault(emptyList())

            initPaging()
        }
    }

    private fun initPaging() {
        val dataSourceFactory = chatRequestRoomsUseCase.invoke()

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(LIST_PAGE_SIZE)
            .build()

        liveChatRequestList = dataSourceFactory
            .map { dialog ->
                val draft = drafts.firstOrNull { it.roomId == dialog.roomId } ?: return@map dialog
                return@map dialog.apply { this.draft = draft }
            }.toLiveData(pagedListConfig)
        emitViewEvent(ChatRequestViewEvent.OnPagingInitialized)
    }

    fun updateMessagesAsChatRequest(roomId: Long?, isShowBlur: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateMessagesAsChatRequestUseCase.execute(
                params = UpdateMessagesAsChatRequestParams(
                    roomId = roomId,
                    isShowBlur = isShowBlur
                ),
                success = {
                    Timber.d("Success update some messages as chat request")
                },
                fail = { exception ->
                    Timber.e("Internal Db error when update messages as chat request:$exception")
                }
            )
        }
    }

    fun getOwnUserId() = appSettings.readUID()

    private fun executeBlockAction(action: ChatRequestActionUiModel.BlockUserWorkData) {
        blockReportHelper.postBlockUserWork(action.userId, action.companionData)
    }

    private fun executeBlockReportAction(action: ChatRequestActionUiModel.BlockReportUserWorkData) {
        blockReportHelper.postBlockReportUserWork(
            userId = action.userId,
            companionDialog = action.companionDialog,
        )
    }

    private suspend fun hideChatRequest(dialog: DialogEntity) {
        runCatching {
            hideChatRequestRoomsUseCase.invoke(dialog)
        }
    }

    private fun startCollectingActionResult() {
        viewModelScope.launch {
            blockReportHelper.sharedResultFlow.collect { result ->
                when (result) {
                    is ChatRequestBlockReportHelper.Result.BlockResult ->
                        handleBlockUserActionResult(result)
                    is ChatRequestBlockReportHelper.Result.BlockReportResult ->
                        handleBlockReportUserActionResult(result)
                    else -> {}
                }
            }
        }
    }

    private suspend fun handleBlockReportUserActionResult(
        result: ChatRequestBlockReportHelper.Result.BlockReportResult
    ) {
        if (!result.isSuccess) {
            unhideChatRequest(result.companionDialog)
        }
        _chatRequestEventsFlow.emit(
            ChatRequestViewEvent.BlockUserResult(
                dialogToDelete =  result.companionDialog,
                isSuccess = result.isSuccess
            )
        )
    }

    private suspend fun handleBlockUserActionResult(
        result: ChatRequestBlockReportHelper.Result.BlockResult
    ) {
        if (!result.isSuccess) {
            unhideChatRequest(result.companionDialog)
        }
        _chatRequestEventsFlow.emit(
            ChatRequestViewEvent.BlockReportUserResult(
                dialogToDelete = result.companionDialog,
                isSuccess = result.isSuccess
            )
        )
    }
}

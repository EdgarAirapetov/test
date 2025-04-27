package com.numplates.nomera3.modules.chat.requests.ui.viewmodel

import com.meera.core.di.scopes.AppScope
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.chat.common.utils.getComplaintById
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.ReportUserFromChatUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AppScope
class ChatRequestBlockReportHelper @Inject constructor(
    private val blockStatusUseCase: BlockStatusUseCase,
    private val reportUserFromChatUseCase: ReportUserFromChatUseCase,
    private val analyticsInteractor: AnalyticsInteractor
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _sharedResultFlow = MutableSharedFlow<Result>()
    val sharedResultFlow = _sharedResultFlow as Flow<Result>

    fun postBlockUserWork(userId: Long, companionDialog: DialogEntity) {
        coroutineScope.launch {
            _sharedResultFlow.emit(
                Result.BlockResult(
                    companionDialog = companionDialog,
                    isSuccess = blockUser(userId, companionDialog)
                )
            )
        }
    }

    fun postBlockReportUserWork(
        userId: Long,
        companionDialog: DialogEntity,
    ): Flow<Boolean> {
        val resultFlow = MutableSharedFlow<Boolean>()
        coroutineScope.launch {
            val isBlockSuccess = blockUser(userId, companionDialog)
            resultFlow.emit(isBlockSuccess)
            _sharedResultFlow.emit(
                Result.BlockReportResult(
                    companionDialog = companionDialog,
                    isSuccess = isBlockSuccess
                )
            )
        }
        return resultFlow
    }

    fun postReportUserWork(
        userId: Long,
        companionDialog: DialogEntity,
        complaintReasonId: Int
    ) {
        coroutineScope.launch {
            _sharedResultFlow.emit(
                Result.ReportResult(
                    companionDialog = companionDialog,
                    isSuccess = reportUserFromChatRequest(
                        userId = userId,
                        companionDialog = companionDialog,
                        complaintReasonId = complaintReasonId
                    )
                )
            )
        }
    }

    private suspend fun blockUser(userId: Long, companionDialog: DialogEntity): Boolean {
        return runCatching {
            blockStatusUseCase.invoke(
                userId = userId,
                companionId = companionDialog.companion.userId ?: 0,
                isBlocked = true
            )
        }.fold(
            onSuccess = {
                analyticsInteractor.logBlockUser(userId, companionDialog.companionUid)
                return@fold true
            },
            onFailure = { false }
        )
    }

    private suspend fun reportUserFromChatRequest(
        userId: Long,
        companionDialog: DialogEntity,
        complaintReasonId: Int
    ): Boolean {
        return runCatching {
            reportUserFromChatUseCase.invoke(
                companionId = companionDialog.companionUid,
                reasonId = complaintReasonId,
                roomId = companionDialog.roomId
            )
        }.onSuccess {
            val complaintType = getComplaintById(complaintReasonId)
            if (complaintType != null) {
                analyticsInteractor.logComplain(
                    type = complaintType,
                    from = userId,
                    to = companionDialog.companionUid
                )
            }
        }.fold(
            onSuccess = { true },
            onFailure = { false }
        )
    }

    sealed class Result {
        data class BlockResult(val isSuccess: Boolean, val companionDialog: DialogEntity): Result()
        data class BlockReportResult(val isSuccess: Boolean, val companionDialog: DialogEntity): Result()
        data class ReportResult(val isSuccess: Boolean, val companionDialog: DialogEntity): Result()
    }
}

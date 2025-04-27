package com.numplates.nomera3.modules.appDialogs.domain

import com.numplates.nomera3.SHOW_CALL_POPUP
import com.numplates.nomera3.modules.appDialogs.DialogEntity
import com.numplates.nomera3.modules.appDialogs.DialogState
import com.numplates.nomera3.modules.appDialogs.DialogType
import com.numplates.nomera3.modules.appDialogs.data.DialogPreparationRepository
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoRepository
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import javax.inject.Inject
import kotlin.Exception

class DialogPrepareUseCase @Inject constructor(
    private val appInfoRepository: AppInfoRepository,
    private val dialogPreparationRepository: DialogPreparationRepository
) : BaseUseCaseCoroutine<DialogPrepareParams, DialogEntity> {

    override suspend fun execute(
        params: DialogPrepareParams,
        success: (DialogEntity) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        when (params.dialogEntity.type) {
            DialogType.ONBOARDING -> prepareOnboarding(params.dialogEntity) { success(it) }
            DialogType.ENABLE_CALLS -> prepareCallsEnable(params.dialogEntity,
                success = { success(it) }, fail = { fail(it) })
            else -> {}
        }
    }

    private fun prepareOnboarding(
        dialogEntity: DialogEntity,
        success: (DialogEntity) -> Unit
    ) {
        if (dialogPreparationRepository.isOnBoardingReady()) success(dialogEntity)
    }

    private suspend fun prepareCallsEnable(
        dialogEntity: DialogEntity,
        success: (DialogEntity) -> Unit,
        fail: (Exception) -> Unit
    ) {
        when {
            !dialogPreparationRepository.isOutCallsReady() -> {
                fail(Exception("CALL ENABLE DIALOG IS NOT READY"))
            }
            else -> {
                appInfoRepository.getSettings().appInfo
                    .find { it.name == SHOW_CALL_POPUP }
                    ?.let {
                        if (it.value == TRUE) {
                            success(dialogEntity)
                        } else {
                            dialogEntity.state = DialogState.COMPLETED
                            success(dialogEntity)
                        }
                    } ?: run {
                    success(dialogEntity)
                }
            }
        }
    }

    companion object {
        private const val TRUE = "true"
    }
}

data class DialogPrepareParams(val dialogEntity: DialogEntity) : DefParams()

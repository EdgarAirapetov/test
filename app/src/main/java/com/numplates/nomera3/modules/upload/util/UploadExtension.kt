package com.numplates.nomera3.modules.upload.util

import android.os.Bundle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.coroutines.coroutineContext

const val UPLOAD_BUNDLE_KEY = "upload_key"

fun Bundle.getUploadBundle(): String? {
    return getString(UPLOAD_BUNDLE_KEY)
}

fun Bundle.hasUploadBundle(): Boolean {
    return !getString(UPLOAD_BUNDLE_KEY).isNullOrBlank()
}

suspend inline fun <T> Flow<T>.safeCollect(crossinline action: suspend (T) -> Unit) {
    collect {
        coroutineContext.ensureActive()
        action(it)
    }
}

fun CoroutineScope.waitWorkStateAsync(
    workManager: WorkManager,
    operationId: UUID,
    finishCondition: (WorkInfo.State) -> Boolean,
    updateIntervalMs: Long = 100
): Deferred<WorkInfo.State?> {
    return async {
        var currentState = workManager.getState(operationId) ?: let {
            cancel()
            return@async null
        }

        while (!finishCondition(currentState)) {
            currentState = workManager.getState(operationId) ?: let {
                cancel()
                return@async null
            }
            delay(updateIntervalMs)
        }

        return@async currentState
    }
}

fun WorkManager.getState(operationId: UUID): WorkInfo.State? {
    return this.getWorkInfoById(operationId).get()?.state
}

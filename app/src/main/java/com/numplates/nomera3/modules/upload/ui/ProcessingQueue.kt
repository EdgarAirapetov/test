package com.numplates.nomera3.modules.upload.ui

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import com.numplates.nomera3.modules.upload.ui.model.QueueItem

/**
 * Очередь для обработки данных произвольного типа:
 *
 * * Для каждого элемента, поступившего в очередь, в фазе обработки вызываются методы [onPayloadEnter] на входе
 * и [onPayloadExit] на выходе
 *
 * * Элемент при поступлении в очередь вытесняет предыдущий из фазы обработки
 *
 * * Данные поступают в очередь через метод [postItem] обёрнутыми в класс [QueueItem], имеющий параметры длительности
 * нахождения элемента в фазе обработки
 *
 * * [QueueItem.maxDuration] определяет максимальное время до автоматического выхода элемента из фазы обработки. Если
 * этот параметр не указан, элемент остаётся в фазе обработки, пока не будет вытеснен следующим элементом
 *
 * * [QueueItem.minDuration] определяет минимальное время до выхода элемента из фазы обработки, пока он не может быть
 * вытеснен предыдущим элементом. Если этот параметр не указан, элемент не может быть вытеснен следующим элементом
 *
 * * Метод [forceExit] производит принудительное вытеснение элемента из фазы обработки и переход к обработке следующего
 *
 * * Метод [clear] очищает очередь и производит принудительное вытеснение элемента из фазы обработки
 */
class ProcessingQueue<T>(
    val onPayloadEnter: (payload: T) -> Unit,
    val onPayloadExit: (payload: T, forced: Boolean) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val queue = ArrayDeque<QueueItem<T>>()
    private var currentItem: QueueItem<T>? = null
    private var minDurationExpired = true

    fun postItem(item: QueueItem<T>) {
        queue.add(item)
        if (currentItem == null || minDurationExpired) {
            processNextItem()
        }
    }

    fun forceExit() {
        minDurationExpired = true
        processNextItem(true)
    }

    fun clear() {
        queue.clear()
        forceExit()
    }

    private fun processNextItem(forced: Boolean = false) {
        handler.removeCallbacksAndMessages(null)
        currentItem?.let {
            onPayloadExit(it.payload, forced)
        }
        currentItem = null
        queue.removeFirstOrNull()?.let { item ->
            currentItem = item
            onPayloadEnter(item.payload)
            minDurationExpired = false
            item.minDuration?.let { minDuration ->
                handler.postDelayed(minDuration) {
                    minDurationExpired = true
                    if (queue.isNotEmpty()) {
                        processNextItem()
                    }
                }
            }
            item.maxDuration?.let { maxDuration ->
                handler.postDelayed(maxDuration) {
                    processNextItem()
                }
            }
        }
    }
}

package com.numplates.nomera3.modules.notifications.helpers

import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.modules.notifications.data.mediator.makePage
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel

internal class PositionHandler<T> {

    private var _pageN: Int = 0

    private var _lastHeader: T? = null

    private val headers = mutableListOf<T>()

    val limit by lazy { 0.makePage().limit }

    val lastHeader: T?
        get() = _lastHeader

    val lastIndexHeader
        get() = headers.lastIndex

    val isEmptyHeaders
        get() = headers.isEmpty()

    val pageNumber: Int
        get() = _pageN

    fun addHeader(header: T): T {
        headers.add(header)
        updateLastItem()
        return header
    }

    fun removeHeader(header: T) {
        headers.remove(header)
        updateLastItem()
    }

    fun removeHeader(index: Int) {
        headers.removeAt(index)
        updateLastItem()
    }

    fun headersContainsName(name: String): Boolean {
        return headers.firstOrNull { item ->
            when (item) {
                is NotificationUiModel -> item.infoSection?.name == name
                is NotificationEntity -> item.infoSection?.name == name
                else -> false
            }
        } != null
    }

    fun clearHeaders() {
        headers.clear()
        _lastHeader = null
        _pageN = 0
    }

    fun increasePage() {
        _pageN += 1
    }

    private fun updateLastItem() {
        _lastHeader = headers.lastOrNull()
    }
}

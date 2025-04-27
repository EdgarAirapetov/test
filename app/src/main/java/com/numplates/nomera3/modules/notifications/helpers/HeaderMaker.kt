package com.numplates.nomera3.modules.notifications.helpers

import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_NOTHING
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import javax.inject.Inject

class HeaderMaker @Inject constructor() {

    private val positionHandler = PositionHandler<NotificationUiModel>()

    fun clearHeaders() {
        positionHandler.clearHeaders()
    }

    fun insertHeaders(list: List<NotificationUiModel>): List<NotificationUiModel> {
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

    private fun makeHeader(
        data: NotificationUiModel,
        suppressAction: Boolean,
        isNotReadEvent: Boolean
    ): NotificationUiModel {
        val action = if (suppressAction) {
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
        val newIndex = "${lastIndex + 1}"

        val section = InfoSection(
            id = newIndex,
            priority = lastIndex + 1,
            name = name,
            action = action
        )

        val header = data.copy(
            id = newIndex,
            infoSection = section,
            dateGroup = data.dateGroup,
            date = data.date
        )

        return positionHandler.addHeader(header)
    }
}

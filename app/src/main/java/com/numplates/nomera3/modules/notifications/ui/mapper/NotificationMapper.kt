package com.numplates.nomera3.modules.notifications.ui.mapper

import android.content.Context
import com.meera.core.utils.timeAgoExtended
import com.numplates.nomera3.modules.notifications.domain.entity.MetaNotification
import com.numplates.nomera3.modules.notifications.domain.entity.Notification
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.entity.Meta
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import io.reactivex.functions.Function
import javax.inject.Inject

class NotificationMapper @Inject constructor(
    private val context: Context
) : Function<Notification, NotificationUiModel> {

    private val userMapper = UserMapper()

    private val metaMapper = MetaMapper()

    override fun apply(t: Notification): NotificationUiModel =
        NotificationUiModel(
            id = t.id,
            isRead = t.isRead,
            isGroup = t.isGroup,
            date = t.date,
            timeAgo = timeAgoExtended(context, t.date.time),
            groupId = t.groupId,
            count = t.count,
            users = t.users.map { mapUser(it) },
            type = t.type,
            meta = mapMetaNotification(t.meta),
            changedFlag = t.changedFlag,
            infoSection = if (t.infoSection != null) mapToInfoSection(t.infoSection) else null,
            dateGroup = t.dateGroup,
            commentId = t.commentId,
        )

    private fun mapUser(value: com.numplates.nomera3.modules.notifications.domain.entity.User): User =
        userMapper.apply(value)

    private fun mapMetaNotification(value: MetaNotification?): Meta =
        if (value != null) metaMapper.apply(value) else Meta()

    private fun mapToInfoSection(e: com.numplates.nomera3.modules.notifications.domain.entity.InfoSection): InfoSection =
        InfoSection(e.id, e.priority, e.name, e.action)
}

package com.numplates.nomera3.modules.notifications.domain.mapper

import com.meera.db.models.notifications.InfoSectionEntity
import com.meera.db.models.notifications.MetaNotificationEntity
import com.meera.db.models.notifications.NotificationEntity
import com.meera.db.models.notifications.UserEntity
import com.numplates.nomera3.modules.notifications.domain.entity.InfoSection
import com.numplates.nomera3.modules.notifications.domain.entity.MetaNotification
import com.numplates.nomera3.modules.notifications.domain.entity.Notification
import com.numplates.nomera3.modules.notifications.domain.entity.User
import io.reactivex.functions.Function

class NotificationMapper : Function<NotificationEntity, Notification> {

    private val userMapper = UserMapper()

    private val metaNotificationMapper = MetaNotificationMapper()

    override fun apply(t: NotificationEntity): Notification =
            Notification(
                    id = t.id,
                    isRead = t.isRead,
                    isGroup = t.isGroup,
                    date = t.date,
                    groupId = t.groupId.orEmpty(),
                    count = t.count,
                    users = t.users.map { mapUser(it) },
                    type = t.type,
                    meta = mapMetaNotification(t.meta),
                    changedFlag = t.changedFlag,
                    infoSection = if (t.infoSection != null) mapToInfoSection(t.infoSection!!) else null,
                    dateGroup = t.dateGroup,
                    commentId = t.meta?.commentId
            )

    private fun mapUser(value: UserEntity): User = userMapper.apply(value)

    private fun mapMetaNotification(value: MetaNotificationEntity?): MetaNotification =
            if (value != null) metaNotificationMapper.apply(value) else MetaNotification()

    private fun mapToInfoSection(e: InfoSectionEntity): InfoSection =
            InfoSection(e.id, e.priority, e.name, e.action)
}

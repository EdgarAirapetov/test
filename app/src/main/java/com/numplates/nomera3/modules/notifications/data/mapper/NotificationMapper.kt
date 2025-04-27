package com.numplates.nomera3.modules.notifications.data.mapper

import com.meera.db.models.notifications.UserEntity
import com.meera.db.models.notifications.MetaNotificationEntity
import com.numplates.nomera3.modules.notifications.data.entity.MetaNotificationEntityResponse
import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.modules.notifications.data.entity.NotificationEntityResponse
import com.numplates.nomera3.modules.notifications.data.entity.UserEntityResponse
import com.numplates.nomera3.modules.notifications.ui.entity.MentionNotificationType
import io.reactivex.functions.Function
import java.util.*
import javax.inject.Inject

private const val MILLISECONDS = 1000

class NotificationMapper : Function<NotificationEntityResponse, NotificationEntity> {

    private val userMapper = UserMapper()

    private val metaNotificationMapper = MetaNotificationMapper()

    override fun apply(t: NotificationEntityResponse): NotificationEntity =
        NotificationEntity(
            id = t.id,
            isRead = t.read,
            isGroup = t.isGroup,
            date = Date(t.date * MILLISECONDS),
            groupId = t.groupId,
            count = t.count,
            users = if (t.users != null) t.users.map { mapUser(it) } else listOf(),
            type = t.type,
            meta = if (t.meta != null)
                mapMetaNotification(t.meta, MentionNotificationType.make(t.type)) else null,
            dateGroup = t.dateGroup ?: "",
            dateLong = t.date
        )

    private fun mapUser(value: UserEntityResponse): UserEntity = userMapper.apply(value)

    private fun mapMetaNotification(
        value: MetaNotificationEntityResponse,
        type: MentionNotificationType?
    ): MetaNotificationEntity = metaNotificationMapper.apply(value, type)

}

class ListOfNotificationMapper @Inject constructor(): Function<List<NotificationEntityResponse>, List<NotificationEntity>> {

    private val mapper = NotificationMapper()

    override fun apply(t: List<NotificationEntityResponse>): List<NotificationEntity> =
        t.map { mapper.apply(it) }

}

class ListOfNotificationResponseMapperById {

    private val mapper = GroupNotificationMapper()

    fun apply(t: List<NotificationEntityResponse>, groupId: String): List<NotificationEntity> =
        t.map {
            mapper.apply(it, groupId)
        }

}

class GroupNotificationMapper {

    private val userMapper = UserMapper()

    private val metaNotificationMapper = MetaNotificationMapper()

    fun apply(t: NotificationEntityResponse, groupId: String): NotificationEntity =
        NotificationEntity(
            id = t.id,
            isRead = t.read,
            isGroup = t.isGroup,
            date = Date(t.date * MILLISECONDS),
            groupId = groupId,
            count = t.count,
            users = if (t.users != null) t.users.map { mapUser(it) } else listOf(),
            type = t.type,
            meta = if (t.meta != null)
                mapMetaNotification(t.meta, MentionNotificationType.make(t.type)) else null,
            dateGroup = t.dateGroup ?: "",
            dateLong = t.date
        )

    private fun mapUser(value: UserEntityResponse): UserEntity = userMapper.apply(value)

    private fun mapMetaNotification(
        value: MetaNotificationEntityResponse,
        type: MentionNotificationType?
    ): MetaNotificationEntity = metaNotificationMapper.apply(value, type)

}

package com.numplates.nomera3.data.fcm

import com.numplates.nomera3.data.fcm.data.CommonPushModel

interface CommonNotificationCreator {
    fun showNotification(data: CommonPushModel)
}

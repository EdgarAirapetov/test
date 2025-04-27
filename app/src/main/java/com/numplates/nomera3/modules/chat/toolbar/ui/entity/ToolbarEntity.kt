package com.numplates.nomera3.modules.chat.toolbar.ui.entity


import com.meera.core.extensions.empty
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel

data class ToolbarEntity(
    val avatar: String = String.empty(),
    val title: String = String.empty(),
    val onlineStatus: ChatOnlineStatusEntity? = null,
    val callSwitchState: CallSwitchState? = null,
    val updatedChatData: UpdatedChatData? = null,
    val accountType: Int = 0,
    val approved: Boolean = false,
    val topContentMaker: Boolean = false,
    val moments: UserMomentsModel? = null
)

data class CallSwitchState(
        val blacklistedByMe: Int? = null,
        val blacklistedMe: Int? = null,
        val iCanCall: Int? = null,
        val userCanCallMe: Int? = null
)

/**
 * Обновлённые данные юзера или данных комнаты
 * Необходимы для основного функционала чата
 */
data class UpdatedChatData(
        val companion: UserChat
)

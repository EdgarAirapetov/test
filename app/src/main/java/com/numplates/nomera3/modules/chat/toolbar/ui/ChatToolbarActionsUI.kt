package com.numplates.nomera3.modules.chat.toolbar.ui

import com.numplates.nomera3.modules.chat.data.ChatEntryData

sealed interface ChatToolbarActionsUI {

    class SetupToolbar(val entryData: ChatEntryData): ChatToolbarActionsUI {
        override fun toString() = "CHAT_TOOLBAR_ACTION UpdateToolbar(entryData:$entryData)"
    }

    class SetAvailabilityGroupMenuAbout(val isEnabled: Boolean): ChatToolbarActionsUI {
        override fun toString() = "CHAT_TOOLBAR_ACTION SetAvailabilityGroupMenuAbout(isEnabled = $isEnabled)"
    }

    class ChangeChatRequestMenuVisibility(val isVisible: Boolean): ChatToolbarActionsUI {
        override fun toString() = "CHAT_TOOLBAR_ACTION ChangeChatRequestMenuVisibility(isVisible = $isVisible)"
    }

    class ChangeChatRequestMenuStatus(
        val isChatRequest: Boolean,
        val isGroupChat: Boolean,
        val isDialogAllowed: Boolean,
        val isSubscribed: Boolean,
        val isBlocked: Boolean,
        val hasConversationStarted: Boolean
    ): ChatToolbarActionsUI {
        override fun toString(): String {
            return "ChangeChatRequestMenuStatus(" +
                "\nisChatRequest=$isChatRequest, " +
                "\nisGroupChat=$isGroupChat, " +
                "\nisDialogAllowed=$isDialogAllowed, " +
                "\nisSubscribed=$isSubscribed, " +
                "\nisBlocked=$isBlocked, " +
                "\nhasConversationStarted=$hasConversationStarted)"
        }
    }

    data object CloseToolbarMenu: ChatToolbarActionsUI {
        override fun toString() = "CHAT_TOOLBAR_ACTION CloseToolbarMenu"
    }

    class UpdateCompanion(val roomId: Long, val userId: Long): ChatToolbarActionsUI {
        override fun toString() = "CHAT_TOOLBAR_ACTION UpdateCompanion(roomId=$roomId, userId=$userId)"
    }
}

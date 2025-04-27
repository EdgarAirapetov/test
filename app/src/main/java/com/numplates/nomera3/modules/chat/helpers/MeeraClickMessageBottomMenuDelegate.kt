package com.numplates.nomera3.modules.chat.helpers

import android.view.View
import androidx.fragment.app.Fragment
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer


class MeeraClickMessageBottomMenuDelegate(
    private val fragment: Fragment,
    private val featureToggles: FeatureTogglesContainer,
    private val onAction: (ChatActions) -> Unit
) {

    fun showMeeraBottomMenu(
        message: MessageEntity?,
        room: DialogEntity?,
        messageView: View? = null,
        unsentMessageCounter: Int = 0,
        mediaPreview: MediaPreviewUiModel? = null,
        isEditMessageAvailable: Boolean = false,
        menuPayload: ChatBottomMenuPayload? = null,
        isUploadMediaProgress: Boolean = false
    ) {
        if (message != null && room != null) {
            val dialogMenu = MeeraBaseBottomSheetDialogMenu()
            dialogMenu.show(
                fm = fragment.childFragmentManager,
                data = MeeraClickMessageDialogData(
                    message = message,
                    room = room,
                    messageView = messageView,
                    unsentMessageCounter = unsentMessageCounter,
                    mediaPreview = mediaPreview,
                    isEditMessageAvailable = isEditMessageAvailable,
                    menuPayload = menuPayload,
                    isUploadMediaProgress = isUploadMediaProgress,
                    menuType = MeeraBaseBottomSheetDialogType.CLICK_MESSAGE
                ),
                onClickAction = { menuAction ->
                    if (menuAction != null) onAction.invoke(menuAction)
                }
            )
        }
    }

    fun openMeeraForbidChatRequestMenu() {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(menuType = MeeraBaseBottomSheetDialogType.FORBID_CHAT_REQUEST_MENU),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }

    fun openMeeraDialogMoreMenu(companion: UserChat?) {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(
                companion = companion,
                menuType = MeeraBaseBottomSheetDialogType.DIALOG_MORE
            ),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }

    fun openMeeraGroupChatSettingsDialog(
        room: DialogEntity?,
        ownUserId: Long
    ) {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(
                room = room,
                ownUserId = ownUserId,
                featureToggles = featureToggles,
                menuType = MeeraBaseBottomSheetDialogType.GROUP_MORE
            ),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }

    fun showMeeraCompletelyRemoveMessageDialog(message: MessageEntity) {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(
                message = message,
                menuType = MeeraBaseBottomSheetDialogType.COMPLETE_REMOVE_MESSAGE
            ),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }
}

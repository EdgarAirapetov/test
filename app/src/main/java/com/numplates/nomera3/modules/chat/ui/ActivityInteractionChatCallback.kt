package com.numplates.nomera3.modules.chat.ui

import android.view.View
import com.meera.db.models.dialog.UserChat
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.utils.apphints.Hint

interface ActivityInteractionChatCallback {

    fun onGetActionFromChat(action: ActivityInteractChatActions)

    fun onProvideMediaEditorControllerToChat(): MediaControllerFeatureApi

}

sealed interface ActivityInteractChatActions {
    class EnablePush(val roomId: Long) : ActivityInteractChatActions
    class DisablePush(val roomId: Long) : ActivityInteractChatActions
    class SetAllowedSwipeDirection(val direction: NavigatorViewPager.SwipeDirection?) : ActivityInteractChatActions
    object HideHints : ActivityInteractChatActions
    object ShowFireworkAnimation : ActivityInteractChatActions
    class StartCall(val companion: UserChat) : ActivityInteractChatActions
    class ShowAppHint(val hint: Hint): ActivityInteractChatActions
    object HideAppHints: ActivityInteractChatActions
    class OpenLink(val url: String?): ActivityInteractChatActions
    class OpenUserMoments(
        val userId: Long?,
        val view: View?,
        val where: AmplitudePropertyMomentScreenOpenWhere,
        val hasNewMoments : Boolean
    ) : ActivityInteractChatActions
}

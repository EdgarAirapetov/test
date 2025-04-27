package com.numplates.nomera3.modules.common

import com.meera.core.views.TooltipViewController
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.numplates.nomera3.modules.auth.ui.AuthNavigator
import com.numplates.nomera3.modules.auth.ui.MeeraAuthNavigator
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.ReactionBubbleViewController
import com.numplates.nomera3.modules.redesign.util.MeeraAuthNavigation
import com.numplates.nomera3.modules.upload.ui.MeeraStatusToastViewController
import com.numplates.nomera3.modules.upload.ui.StatusToastViewController

interface ActivityToolsProvider {
    fun getAuthenticationNavigator(): AuthNavigator

    @Deprecated("Use getMeeraAuthNavigation")
    fun getMeeraAuthenticationNavigator(): MeeraAuthNavigator

    fun getMeeraAuthNavigation(): MeeraAuthNavigation

    fun getMediaControllerFeature(): MediaControllerFeatureApi
    fun getMomentsViewController(): com.numplates.nomera3.modules.moments.show.presentation.MomentCreateViewController
    fun getReactionBubbleViewController(): ReactionBubbleViewController
    fun getMeeraReactionBubbleViewController(): MeeraReactionBubbleViewController
    fun getStatusToastViewController(): StatusToastViewController
    fun getMeeraStatusToastViewController(): MeeraStatusToastViewController
    fun getTooltipController(): TooltipViewController
}

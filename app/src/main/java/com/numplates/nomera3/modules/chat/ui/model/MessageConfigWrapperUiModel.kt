package com.numplates.nomera3.modules.chat.ui.model

import com.meera.uikit.widgets.chat.call.UiKitCallConfig
import com.meera.uikit.widgets.chat.emoji.UiKitEmojiConfig
import com.meera.uikit.widgets.chat.gift.UiKitGiftConfig
import com.meera.uikit.widgets.chat.media.UiKitMediaConfig
import com.meera.uikit.widgets.chat.moment.UiKitMomentConfig
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileConfig
import com.meera.uikit.widgets.chat.regular.UiKitRegularConfig
import com.meera.uikit.widgets.chat.repost.UiKitRepostConfig
import com.meera.uikit.widgets.chat.status.UiKitStatusConfig
import com.meera.uikit.widgets.chat.sticker.UiKitStickersConfig
import com.meera.uikit.widgets.chat.voice.UiKitVoiceConfig


sealed interface MessageConfigWrapperUiModel {

    data class Default(val config: UiKitRegularConfig) : MessageConfigWrapperUiModel

    data class Emoji(val config: UiKitEmojiConfig) : MessageConfigWrapperUiModel

    data class Media(val config: UiKitMediaConfig) : MessageConfigWrapperUiModel

    data class Sticker(val config: UiKitStickersConfig) : MessageConfigWrapperUiModel

    data class Repost(val config: UiKitRepostConfig) : MessageConfigWrapperUiModel

    data class Moment(val config: UiKitMomentConfig) : MessageConfigWrapperUiModel

    data class Call(val config: UiKitCallConfig) : MessageConfigWrapperUiModel

    data class Voice(val config: UiKitVoiceConfig) : MessageConfigWrapperUiModel

    data class ShareProfile(val config: UiKitShareProfileConfig) : MessageConfigWrapperUiModel

    data class ShareCommunity(val config: UiKitShareProfileConfig) : MessageConfigWrapperUiModel

    data class Gift(val config: UiKitGiftConfig) : MessageConfigWrapperUiModel

    data class Deleted(val statusConfig: UiKitStatusConfig, val isMy: Boolean) : MessageConfigWrapperUiModel

    data object NotImplemented : MessageConfigWrapperUiModel

}

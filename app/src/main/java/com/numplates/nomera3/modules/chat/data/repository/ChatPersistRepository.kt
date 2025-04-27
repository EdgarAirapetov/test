package com.numplates.nomera3.modules.chat.data.repository

import com.google.gson.Gson
import com.meera.core.common.DEFAULT_KEYBOARD_HEIGHT_PX
import com.meera.core.preferences.AppSettings
import javax.inject.Inject

interface ChatPersistRepository {
    fun isShownTooltipSession(): Boolean
    fun getPhoneAbilityShowedTimes(): Int
    fun setPhoneAbilityTipWasShowed(showedTimes: Int)
    fun getReleaseBtnSentAudioMsgTipWasShowed(): Int
    fun setReleaseBtnSentAudioMsgTipWasShowed(showedTimes: Int)
    fun isReleaseBtnSentAudioMsgTip(): Boolean
    fun getRecordAudioMsgTipShowedTimes(): Int
    fun setRecordAudioMsgTipWasShowed(showedTimes: Int)
    fun getUserBirthday(): Long
    fun getKeyBoardHeight(): Int
    fun isKeyBoardHeightSaved(): Boolean
}

class ChatPersistRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings,
    private val gson: Gson
) : ChatPersistRepository {

    override fun isShownTooltipSession() = !appSettings.shownTooltipsMap.contains(
        AppSettings.KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES
    )

    override fun getPhoneAbilityShowedTimes() = appSettings.isPhoneAbilityWasShowed

    override fun setPhoneAbilityTipWasShowed(showedTimes: Int) {
        appSettings.isPhoneAbilityWasShowed = showedTimes + 1
    }

    override fun getReleaseBtnSentAudioMsgTipWasShowed() = appSettings.releaseButtonSentAudioMessageTooltipWasShownTimes

    override fun setReleaseBtnSentAudioMsgTipWasShowed(showedTimes: Int) {
        appSettings.releaseButtonSentAudioMessageTooltipWasShownTimes = showedTimes + 1
        appSettings.markTooltipAsShownSession(
            AppSettings.KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES
        )
    }

    override fun isReleaseBtnSentAudioMsgTip() = !appSettings.shownTooltipsMap.contains(
        AppSettings.KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES
    )

    override fun getRecordAudioMsgTipShowedTimes() = appSettings.recordAudioMessageTooltipWasShownTimes

    override fun setRecordAudioMsgTipWasShowed(showedTimes: Int) {
        appSettings.recordAudioMessageTooltipWasShownTimes = showedTimes + 1
        appSettings.markTooltipAsShownSession(
            AppSettings.KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES
        )
    }

    override fun getUserBirthday() = appSettings.readUserBirthday()

    override fun getKeyBoardHeight() = appSettings.keyboardHeight

    override fun isKeyBoardHeightSaved(): Boolean = appSettings.keyboardHeight != DEFAULT_KEYBOARD_HEIGHT_PX
}

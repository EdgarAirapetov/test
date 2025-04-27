package com.numplates.nomera3.presentation.view.utils.apphints

import com.meera.core.preferences.AppSettings

/**
 * Static hints
 * hints[HintTypes.CALL_SPEAKER_ON]?.let { hint ->
 *       act.showAppHint(hint)
 *   }
 */
class HintManager(private val appSettings: AppSettings) {

    fun showRoadFilterHint(showHint: () -> Unit){
        if (!appSettings.containsRoadFilterAppHint()) {
            appSettings.roadFilterAppHint = (false)
        }

        appSettings.roadFilterAppHint.let { isShown ->
            if (!isShown) {
                showHint.invoke()
            }
        }
    }


    fun showRoadNewPostHint(showHint: () -> Unit){
        if (!appSettings.containsRoadNewPostAppHint()) {
            appSettings.roadNewPostAppHint = false
        }

        appSettings.roadNewPostAppHint.let { isShown ->
            if (!isShown) {
                showHint.invoke()
            }
        }
    }


    fun showCreateGroupChatHint(showHint: () -> Unit){
        if (!appSettings.containsCreateGroupChatAppHint()) {
            appSettings.createGroupChatAppHint = false
        }

        appSettings.createGroupChatAppHint.let { isShown ->
            if (!isShown) {
                showHint.invoke()
            }
        }
    }


    fun showMapFilterAppHint(showHint: () -> Unit){
        if (!appSettings.containsMapFilterAppHint()) {
            appSettings.mapFilterAppHint = false
        }
        appSettings.mapFilterAppHint.let { isShow ->
            if (!isShow) {
                showHint.invoke()
            }
        }
    }


    fun showRecordAudioMessageAppHint(showHint: () -> Unit) {
        if (!appSettings.containsCreateGroupChatAppHint()) {
            appSettings.recordAudioMessageAppHint = false
        }
        appSettings.recordAudioMessageAppHint.let { isShow ->
            if (!isShow) {
                showHint.invoke()
            }
        }
    }


    fun showSendAudioMessageAppHint(showHint: () -> Unit) {
        if (!appSettings.containsSendAudioMessageAppHint()) {
            appSettings.sendAudioMessageAppHint = false
        }
        appSettings.sendAudioMessageAppHint.let { isShow ->
            if (!isShow) {
                showHint.invoke()
            }
        }
    }

    @Deprecated("Using another logic (SimpleHint) on click")
    fun showAccountNewFunctionAppHint(showHint: () -> Unit){
        if (!appSettings.containsAccountNewFunctionAppHint()) {
            appSettings.accountNewFunctionAppHint = false
        }
        appSettings.accountNewFunctionAppHint.let { isShow ->
            if (!isShow) {
                showHint.invoke()
            }
        }
    }


    fun markShownHint(hint: Hint){
        when (hint.type) {
            HintTypes.ROAD_FILTER -> appSettings.roadFilterAppHint = true
            HintTypes.ROAD_NEW_POST -> appSettings.roadNewPostAppHint = true
            HintTypes.GROUP_CHAT_CREATE -> appSettings.createGroupChatAppHint = true
            HintTypes.MAP_FILTER -> appSettings.mapFilterAppHint = true
            HintTypes.MAP_FILTER_V2 -> appSettings.mapFilterAppHint = true
            HintTypes.SEND_AUDIO_MESSAGE -> appSettings.sendAudioMessageAppHint = true
            HintTypes.RECORD_AUDIO_MESSAGE -> appSettings.recordAudioMessageAppHint = true
            // HintTypes.ACCOUNT_NEW_FUNCTION -> rxPreferences.accountNewFunctionAppHint.set(true)
            else -> {}
        }
    }


}

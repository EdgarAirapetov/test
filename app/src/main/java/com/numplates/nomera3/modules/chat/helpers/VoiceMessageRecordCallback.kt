package com.numplates.nomera3.modules.chat.helpers

import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager

interface VoiceMessageRecordCallback {

    fun requestPermissions()

    fun tapRecordBtn()

    fun releaseRecordBtn()

    fun onLockButtonIsVisible(isVisible: Boolean)

    fun sendTypingStatus()

    fun sendVoiceMessage(filePath: String, amplitudes: List<Int>, durationSec: Long)

    fun onFinishRecordingProcess()

    fun onUpdateVoiceTimer(seconds: Int, milliseconds: Int)

    fun allowSwipeDirectionNavigator(direction: NavigatorViewPager.SwipeDirection?)
}
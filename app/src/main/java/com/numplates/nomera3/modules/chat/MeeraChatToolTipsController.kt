@file:Suppress("unused")

package com.numplates.nomera3.modules.chat

import android.content.Context
import androidx.fragment.app.Fragment
import com.meera.uikit.tooltip.TooltipShowHandler
import com.meera.uikit.tooltip.createTooltip
import com.meera.uikit.widgets.tooltip.TooltipMessage
import com.meera.uikit.widgets.tooltip.UiKitTooltipBubbleMode
import com.meera.uikit.widgets.tooltip.UiKitTooltipViewState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraChatFragmentBinding
import com.numplates.nomera3.modules.chat.ui.MeeraChatViewModel
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_ALLOWED
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_LOCKED
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

private const val DEFAULT_RECORD_BTN_OFFSET_X = 16
private const val DEFAULT_RECORD_BTN_OFFSET_Y = 8
private const val DEFAULT_TOOLTIP_SHOWING_DELAY = 500L

class MeeraChatToolTipsController(
    private val context: Context,
    private val fragment: Fragment,
    private val binding: MeeraChatFragmentBinding,
    private val chatViewModel: MeeraChatViewModel
) {

    var areTooltipsEnabled = true
    var areCallTooltipsEnabled = true

    private val tooltipScope: CoroutineScope = MainScope()
    private var tooltipJob: Job? = null
    private var isCallSwitchChecked = false

    // Нажмите и удерживайте для записи аудиосообщения
    // Эта подсказка показывается каждый раз когда пользователь
    // кликает, но не удерживает кнопку записи аудио, либо держит
    // слишком мало времени.
    private var tooShortAudioMessageTooltipJob: Job? = null
    private val meeraTooShortAudioMessageTooltip: TooltipShowHandler by lazy {
        fragment.createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_BOTTOM,
                tooltipMessage = TooltipMessage.TooltipMessageTextRes(R.string.tooltip_text_let_record_audio_message),
                showCloseButton = false
            )
        )
    }

    // Отпустите, чтобы отправить аудиосообщение собеседнику
    // Данная подсказка открывается один раз когда пользователь
    // начал запись аудиосообщения.
    private var releaseButtonSentAudioMessageTooltipJob: Job? = null
    private val meeraReleaseButtonSentAudioMessageTooltip: TooltipShowHandler by lazy {
        fragment.createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_BOTTOM,
                tooltipMessage = TooltipMessage.TooltipMessageTextRes(R.string.tooltip_release_button_audio_message),
                showCloseButton = false
            )
        )
    }

    /**
     * - текст: начинайте звонок
     * - поведение: открывается каждый раз при входе в чат и включенном тогле вызова
     * - время отображения: 4 сек
     * */
    private val meeraStartCallTooltip: TooltipShowHandler by lazy {
        fragment.createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_TOP,
                tooltipMessage = TooltipMessage.TooltipMessageTextRes(R.string.tooltip_start_call),
                showCloseButton = false
            )
        )
    }

    /**
     * - текст: этот пользователь сможет вам звонить
     * - поведение: открывается каждый раз при входе в чат и выключенном тогле вызова
     * - время отображения: 4 сек
     * */
    private val meeraAllowCallTooltip: TooltipShowHandler by lazy {
        fragment.createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_TOP,
                tooltipMessage = TooltipMessage.TooltipMessageTextRes(R.string.tooltip_allow_call),
                showCloseButton = false
            )
        )
    }

    /**
     * - текст: Пользователь ограничил входящие звонк
     * * - время отображения: 4 сек
     * */
    private val meeraNotAllowToCallTooltip: TooltipShowHandler by lazy {
        fragment.createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_TOP,
                tooltipMessage = TooltipMessage.TooltipMessageTextRes(R.string.tooltip_not_allowed_to_call),
                showCloseButton = false
            )
        )
    }

    /**
     * Переменные callToggleState, isFragmentStarted, isCallToggleVisible, isCallToggleTooltipShowed
     * нужны для показа подсказок под переключателем аудиовызова в тулбаре. Флаг показана подсказки
     * вынесен в переменную isCallToggleTooltipShowed т.к. показываем подзказки каждый раз при входе
     * в экране чата
     * */
    private var isMeAvailableForCalls: Int by Delegates.observable(-1) { _, _, _ -> showStartCallTooltip() }
    private var isFragmentStarted: Boolean by Delegates.observable(false) { _, _, _ -> showStartCallTooltip() }
    private var isCallToggleVisible: Boolean by Delegates.observable(false) { _, _, _ -> showStartCallTooltip() }
    private var isCallToggleTooltipShowed: Boolean by Delegates.observable(false) { _, _, _ -> showStartCallTooltip() }

    fun showCallNotAllowedTooltip() {
        if (!areTooltipsEnabled || !areCallTooltipsEnabled) return
        meeraNotAllowToCallTooltip.showUniversal(binding.meeraChatToolbar.chatCallSwitch)
        isCallToggleTooltipShowed = true
    }

    fun setCallVariablesSettings(callToggleVisible: Boolean, meAvailableForCalls: Int) {
        isCallToggleVisible = callToggleVisible
        isMeAvailableForCalls = meAvailableForCalls
    }

    fun releaseRecordBtn() {
        releaseButtonSentAudioMessageTooltipJob?.cancel()
        meeraReleaseButtonSentAudioMessageTooltip.dismiss()
    }

    /**
     * Подсказка при коротком нажатии на кнопку
     */
    fun tapVoiceRecordButtonTooltips(seconds: Int, milliseconds: Int) {
        if (!areTooltipsEnabled) return
        if (seconds == 0 && milliseconds in 0..1000) {
            tooShortAudioMessageTooltipJob?.cancel()
            meeraTooShortAudioMessageTooltip.dismiss()
            tooShortAudioMessageTooltipJob = tooltipScope.launch {
                delay(TooltipDuration.COMMON_START_DELAY)
                meeraTooShortAudioMessageTooltip.showUniversal(binding.btnVoiceMessage)
                delay(TooltipDuration.COMMON_END_DELAY)
                meeraTooShortAudioMessageTooltip.dismiss()
            }
        } else if (milliseconds > 100) {
            tooShortAudioMessageTooltipJob?.cancel()
            meeraTooShortAudioMessageTooltip.dismiss()
        } else if (seconds == 1 && milliseconds in 0..100) {
            releaseButtonSentAudioMessageTooltipJob?.cancel()
            meeraReleaseButtonSentAudioMessageTooltip.dismiss()
            if (chatViewModel.isReleaseButtonSentAudioMessageTooltipWasShownTimes()) {
                releaseButtonSentAudioMessageTooltipJob = tooltipScope.launch {
                    delay(TooltipDuration.COMMON_START_DELAY)
                    chatViewModel.incReleaseButtonSentAudioMessageTooltipWasShown()
                    meeraReleaseButtonSentAudioMessageTooltip.showUniversal(binding.btnVoiceMessage)
                    delay(TooltipDuration.COMMON_END_DELAY)
                    meeraReleaseButtonSentAudioMessageTooltip.dismiss()
                }
            }
        } else if (seconds == 5 && milliseconds in 0..100) {
            releaseButtonSentAudioMessageTooltipJob?.cancel()
            meeraReleaseButtonSentAudioMessageTooltip.dismiss()
        }
    }

    private fun hideAllHints() {
        tooltipScope.coroutineContext.cancelChildren()
        meeraTooShortAudioMessageTooltip.dismiss()
        meeraReleaseButtonSentAudioMessageTooltip.dismiss()
        meeraStartCallTooltip.dismiss()
        meeraAllowCallTooltip.dismiss()
        meeraNotAllowToCallTooltip.dismiss()
    }

    fun setIsFragmentStarted(isStarted: Boolean) {
        isFragmentStarted = isStarted
    }

    private fun showStartCallTooltip() {
        if (!areTooltipsEnabled || !areCallTooltipsEnabled) return
        if (isFragmentStarted && isCallToggleVisible && !isCallToggleTooltipShowed) {
            val callSwitch = binding.meeraChatToolbar.chatCallSwitch
            when (isMeAvailableForCalls) {
                SWITCH_CALL_STATE_ALLOWED -> {
                    hideAllHints()
                    showTooltipWithDelay {
                        meeraStartCallTooltip.showUniversal(callSwitch)
                        isCallToggleTooltipShowed = true
                    }
                }

                SWITCH_CALL_STATE_LOCKED -> {
                    hideAllHints()
                    val showHandler = when (callSwitch.getCurrentState == SWITCH_CALL_STATE_ALLOWED) {
                        true -> meeraAllowCallTooltip
                        else -> meeraNotAllowToCallTooltip
                    }
                    showTooltipWithDelay {
                        showHandler.showUniversal(callSwitch)
                        isCallToggleTooltipShowed = true
                    }
                }

                else -> Unit
            }
        }
    }

    private fun showTooltipWithDelay(delay: Long = DEFAULT_TOOLTIP_SHOWING_DELAY, tooltipAction: () -> Unit) {
        if (tooltipJob?.isActive == true) {
            tooltipJob?.cancel()
        }
        tooltipJob = tooltipScope.launch {
            delay(delay)
            tooltipAction.invoke()
        }
    }
}

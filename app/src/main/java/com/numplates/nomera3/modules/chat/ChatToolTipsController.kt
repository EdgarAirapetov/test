package com.numplates.nomera3.modules.chat

import android.content.Context
import android.view.Gravity
import android.widget.PopupWindow
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.whenResumed
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentChatBinding
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_ALLOWED
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_LOCKED
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.show
import com.numplates.nomera3.presentation.view.utils.apphints.showForCallSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

private const val DEFAULT_RECORD_BTN_OFFSET_X = 16
private const val DEFAULT_RECORD_BTN_OFFSET_Y = 8

class ChatToolTipsController constructor(
    private val context: Context,
    private val fragment: Fragment,
    private val binding: FragmentChatBinding,
    private val chatViewModel: ChatViewModel
) {

    val tooltipScope: CoroutineScope = MainScope()
    var areTooltipsEnabled = true
    var areCallTooltipsEnabled = true
    private var isInitBtnVoiceMessage = false
    private var btnVoiceMessageAbsY = 0


    // todo сдеать одну точку входа для подсказок
    // https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Аудиосообщение
    // Нажмите и удерживайте для записи аудиосообщения
    // Эта подсказка показывается один раз.
    private val inviteRecordAudioMessageTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_invite_record_audio_message)
    }

    // https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Аудио-сообщение-(При-нажатии)
    // Нажмите и удерживайте для записи аудиосообщения
    // Эта подсказка показывается каждый раз когда пользователь
    // кликает, но не удерживает кнопку записи аудио, либо держит
    // слишком мало времени.
    private var tooShortAudioMessageTooltipJob: Job? = null
    private val tooShortAudioMessageTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_too_short_record_audio_message)
    }

    // https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Аудиосообщение-(Отправка)
    // Отпустите, чтобы отправить аудиосообщение собеседнику
    // Данная подсказка открывается один раз когда пользователь
    // начал запись аудиосообщения.
    private var releaseButtonSentAudioMessageTooltipJob: Job? = null
    private val releaseButtonSentAudioMessageTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_release_button_send_audio_message)
    }

    /**
     * - документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Звонок-(Начинайте-звонок)
     * - текст: начинайте звонок
     * - поведение: открывается каждый раз при входе в чат и включенном тогле вызова
     * - время отображения: 4 сек
     * */
    private val startCallTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_start_call)
    }

    /**
     * - документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Звонок-(Разрешите-звонки)
     * - текст: этот пользователь сможет вам звонить
     * - поведение: открывается каждый раз при входе в чат и выключенном тогле вызова
     * - время отображения: 4 сек
     * */
    private val allowCallTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_allow_call)
    }

    /**
     *
     * - текст: Пользователь ограничил входящие звонк
     * * - время отображения: 4 сек
     * */
    private val notAllowToCallTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_not_allow_to_call)
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
        tooltipScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)
            notAllowToCallTooltip?.showForCallSwitch(
                fragment = fragment,
                view = binding.rvChat,
                gravity = Gravity.END or Gravity.TOP
            )
            delay(TooltipDuration.COMMON_END_DELAY)
        }.invokeOnCompletion {
            notAllowToCallTooltip?.dismiss()
            isCallToggleTooltipShowed = true
        }
    }

    fun setCallVariablesSettings(callToggleVisible: Boolean, meAvailableForCalls: Int) {
        isCallToggleVisible = callToggleVisible
        isMeAvailableForCalls = meAvailableForCalls
    }

    fun releaseRecordBtn() {
        releaseButtonSentAudioMessageTooltipJob?.cancel()
        releaseButtonSentAudioMessageTooltip?.dismissIfShowing()
    }

    /**
     * Подсказка при коротком нажатии на кнопку
     */
    fun tapVoiceRecordButtonTooltips(seconds: Int, milliseconds: Int) {
        if (!areTooltipsEnabled) return
        calculateOnceBtnVoiceMessageYAbsLocation()
        if (seconds == 0 && milliseconds in 0..1000) {
            tooShortAudioMessageTooltipJob?.cancel()
            tooShortAudioMessageTooltip?.dismiss()
            tooShortAudioMessageTooltipJob = tooltipScope.launch {
                delay(TooltipDuration.COMMON_START_DELAY)
                tooShortAudioMessageTooltip?.show(
                    fragment = fragment,
                    gravity = Gravity.END or Gravity.TOP,
                    absX = DEFAULT_RECORD_BTN_OFFSET_X.dp,
                    absY = btnVoiceMessageAbsY
                )
                delay(TooltipDuration.COMMON_END_DELAY)
                tooShortAudioMessageTooltip?.dismiss()
            }
        } else if (milliseconds > 100) {
            tooShortAudioMessageTooltipJob?.cancel()
            tooShortAudioMessageTooltip?.dismissIfShowing()
        } else if (seconds == 1 && milliseconds in 0..100) {
            releaseButtonSentAudioMessageTooltipJob?.cancel()
            releaseButtonSentAudioMessageTooltip?.dismiss()
            if (chatViewModel.isReleaseButtonSentAudioMessageTooltipWasShownTimes()) {
                releaseButtonSentAudioMessageTooltipJob = tooltipScope.launch {
                    delay(TooltipDuration.COMMON_START_DELAY)
                    chatViewModel.incReleaseButtonSentAudioMessageTooltipWasShown()
                    binding.btnVoiceMessage.let { voiceRecordBtn ->
                        releaseButtonSentAudioMessageTooltip?.show(
                            fragment = fragment,
                            view = voiceRecordBtn,
                            gravity = Gravity.END or Gravity.TOP
                        )
                    }
                    delay(TooltipDuration.COMMON_END_DELAY)
                    releaseButtonSentAudioMessageTooltip?.dismiss()
                }
            }
        } else if (seconds == 5 && milliseconds in 0..100) {
            releaseButtonSentAudioMessageTooltipJob?.cancel()
            releaseButtonSentAudioMessageTooltip?.dismissIfShowing()
        }
    }

    fun hideAllHints() {
        tooltipScope.coroutineContext.cancelChildren()
    }

    fun showRecordAudiMessageTooltip() {
        if (!areTooltipsEnabled) return
        if (chatViewModel.isLetRecordAudioMessageTooltipWasShownTimes()) {
            tooltipScope.launch {
                fragment.whenResumed {
                    delay(TooltipDuration.COMMON_START_DELAY)
                    chatViewModel.incRecordAudioMessageTooltipWasShown()
                    binding.btnVoiceMessage.let {
                        inviteRecordAudioMessageTooltip?.show(
                            fragment = fragment,
                            view = it,
                            gravity = Gravity.END or Gravity.TOP
                        )
                    }
                    delay(TooltipDuration.COMMON_END_DELAY)
                }
            }.invokeOnCompletion {
                inviteRecordAudioMessageTooltip?.dismiss()
            }
        }
    }

    fun setIsFragmentStarted(isStarted: Boolean) {
        isFragmentStarted = isStarted
    }

    private fun showStartCallTooltip() {
        if (!areTooltipsEnabled || !areCallTooltipsEnabled) return
        if (isFragmentStarted && isCallToggleVisible && !isCallToggleTooltipShowed) {
            binding.rvChat.let { rv ->
                when (isMeAvailableForCalls) {
                    SWITCH_CALL_STATE_ALLOWED -> {
                        hideAllHints()
                        tooltipScope.launch {
                            delay(TooltipDuration.COMMON_START_DELAY)
                            if (chatViewModel.shouldShowPhoneAbilityTooltip()) {
                                startCallTooltip?.showForCallSwitch(
                                    fragment = fragment,
                                    view = rv,
                                    gravity = Gravity.END or Gravity.TOP
                                )
                                delay(TooltipDuration.COMMON_END_DELAY)
                            }
                        }.invokeOnCompletion {
                            startCallTooltip?.dismiss()
                            isCallToggleTooltipShowed = true
                        }
                    }
                    SWITCH_CALL_STATE_LOCKED -> {
                        hideAllHints()
                        if (!binding.toolbarV2.toolbarCallSwitch.isCheckedToggle) {
                            tooltipScope.launch {
                                delay(TooltipDuration.COMMON_START_DELAY)
                                allowCallTooltip?.showForCallSwitch(
                                    fragment = fragment,
                                    view = rv,
                                    gravity = Gravity.END or Gravity.TOP
                                )
                                delay(TooltipDuration.COMMON_END_DELAY)
                            }.invokeOnCompletion {
                                allowCallTooltip?.dismiss()
                                isCallToggleTooltipShowed = true
                            }
                        } else {
                            tooltipScope.launch {
                                delay(TooltipDuration.COMMON_START_DELAY)
                                notAllowToCallTooltip?.showForCallSwitch(
                                    fragment = fragment,
                                    view = rv,
                                    gravity = Gravity.END or Gravity.TOP
                                )
                                delay(TooltipDuration.COMMON_END_DELAY)
                            }.invokeOnCompletion {
                                notAllowToCallTooltip?.dismiss()
                                isCallToggleTooltipShowed = true
                            }
                        }
                    }
                    else -> {
                        /** STUB */
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Кнопка записи аудио сообщений
    ///////////////////////////////////////////////////////////////////////////
    private fun PopupWindow?.dismissIfShowing() {
        val isShowing = this?.isShowing ?: false
        if (isShowing) {
            this?.dismiss()
        }
    }

    private fun calculateOnceBtnVoiceMessageYAbsLocation() {
        if (!isInitBtnVoiceMessage) {
            binding.btnVoiceMessage.doOnLayout { view ->
                val viewLocation = IntArray(2)
                view.getLocationInWindow(viewLocation)
                btnVoiceMessageAbsY = viewLocation[1] - view.height - DEFAULT_RECORD_BTN_OFFSET_Y.dp
            }
            isInitBtnVoiceMessage = true
        }
    }
}

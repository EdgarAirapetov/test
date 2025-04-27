package com.numplates.nomera3.modules.appDialogs.ui

import androidx.activity.viewModels
import com.meera.core.extensions.doDelayed
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.appDialogs.ui.show.DialogShowViewEvent
import com.numplates.nomera3.modules.appDialogs.ui.show.DialogShowViewModel
import com.numplates.nomera3.modules.holidays.ui.calendar.HolidayCalendarBottomDialog
import com.numplates.nomera3.modules.holidays.ui.calendar.MeeraHolidayCalendarBottomDialogBuilder
import com.numplates.nomera3.modules.holidays.ui.dialog.HolidayIntroDialog
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.presentation.birthday.ui.BirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.view.fragments.CallsEnabledFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.FriendsFollowersPrivacyFragment

/**
 * Задержка используется для того, что бы добавить в стек перед звонками доиалог шаринга поста.
 * Кейс возможен если авториазция/регистрация открывается после нажатие на опции поста(точки меню)
 * */
const val CALLS_DIALOG_DELAY = 705L
const val PRIVACY_DIALOG_DELAY = 705L
const val KEY_CALLS_DIALOG_DISMISS = "dismiss calls dialog"

class DialogNavigator(
    private val rootActivity: Act
) {

    private val dialogQueueViewModel by rootActivity.viewModels<DialogQueueViewModel>()
    private val dialogShowViewModel by rootActivity.viewModels<DialogShowViewModel>()

    init {
        dialogShowViewModel.eventLiveData.observe(rootActivity) { handleDialogShowEvent(it) }
        dialogQueueViewModel.eventLiveData.observe(rootActivity) { handleDialogQueueEvent(it) }
    }

    fun triggerDialogToShow() {
        dialogQueueViewModel.getDialogToShow()
    }

    fun showCallsEnableDialog(dismissListener: (() -> Unit)? = null) {
        rootActivity.lifecycle.doDelayed(getPrivateCallsDialogDelay()) {
            if (rootActivity.baseContext == null) return@doDelayed
            val callDialog = CallsEnabledFragment()
            rootActivity.supportFragmentManager.setFragmentResultListener(KEY_CALLS_DIALOG_DISMISS, rootActivity
            ) { _, _ ->
                dismissListener?.invoke()
                rootActivity.supportFragmentManager.clearFragmentResultListener(KEY_CALLS_DIALOG_DISMISS)
            }
            callDialog.show(rootActivity.supportFragmentManager, callDialog.tag)
        }
    }

    fun showFriendsSubscribersPrivacyDialog(dismissListener: (() -> Unit)? = null) {
        rootActivity.lifecycle.doDelayed(PRIVACY_DIALOG_DELAY) {
            if (rootActivity.baseContext == null) return@doDelayed
            FriendsFollowersPrivacyFragment().apply {
                dismissListener?.let { listener ->
                    setDismissListener(listener)
                }
                show(rootActivity.supportFragmentManager)
            }
        }
    }

    fun showHolidayDialog() = HolidayIntroDialog().apply {
        show(rootActivity.supportFragmentManager, tag)
    }

    fun showBirthdayDialog(
        typeArgument: String = BirthdayBottomDialogFragment.ACTION_DEFAULT
    ) = BirthdayBottomDialogFragment.create(typeArgument).apply {
        show(rootActivity.supportFragmentManager)
    }

    fun showHolidayCalendarDialog(
        visits: HolidayVisits,
        onShowListener: () -> Unit,
        openGiftListener: () -> Unit
    ) = checkAppRedesigned(
        isRedesigned = {
           MeeraHolidayCalendarBottomDialogBuilder()
               .setVisits(visits)
               .setShowGiftBtnClickListener {
                   onShowListener()
               }
               .setLongLiveLollipopsBtnClickListener {
                   openGiftListener()
               }
               .show(rootActivity.supportFragmentManager)
        },
        isNotRedesigned = {
            HolidayCalendarBottomDialog().apply {
                this.onShowListener = { onShowListener() }
                onOpenGiftsListener = { openGiftListener() }
                setVisits(visits)
                show(rootActivity.supportFragmentManager, tag)
            }
        }
    )



    private fun handleDialogShowEvent(event: DialogShowViewEvent) {
        when (event) {
            is DialogShowViewEvent.Completed ->
                dialogQueueViewModel.setDialogShown(event.dialogType)
            is DialogShowViewEvent.NotCompleted ->
                dialogQueueViewModel.setDialogNotCompleted(event.dialogType)
        }
    }

    private fun handleDialogQueueEvent(event: DialogQueueViewEvent) {
        when (event) {
            is DialogQueueViewEvent.ShowDialog -> handleShowDialog(event)
        }
    }

    private fun handleShowDialog(showDialog: DialogQueueViewEvent.ShowDialog) {
        when (showDialog) {
            is DialogQueueViewEvent.ShowDialog.EnableCalls -> showCallsEnableDialog { rootActivity.getHolidayInfo() }
            else -> {}
        }
    }

    private fun getPrivateCallsDialogDelay() = if (dialogQueueViewModel.isNeedToShowOnBoarding()) {
        0
    } else {
        CALLS_DIALOG_DELAY
    }
}

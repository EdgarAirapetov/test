package com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraPushNotifSettingsDescVhBinding
import com.numplates.nomera3.databinding.MeeraPushNotifSettingsExclVhBinding
import com.numplates.nomera3.databinding.MeeraPushNotifSettingsSwitchVhBinding
import com.numplates.nomera3.databinding.MeeraPushNotifSettingsTitleVhBinding
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.MeeraPushNotificationSettingsAction
import timber.log.Timber

private const val MARGIN_BOTTOM_LIST_LAST_ELEMENT = 8

class PushSettingsAdapter(
    private val actionListener: (action: MeeraPushNotificationSettingsAction) -> Unit
) : BaseAsyncAdapter<String, PushSettingsData>() {

    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<PushSettingsData, *> {
        return when (viewType) {
            TYPE_PUSH_SETTINGS_TITLE -> TitleVh(parent.toBinding())
            TYPE_PUSH_SETTINGS_DESCRIPTION -> DescVh(parent.toBinding())
            TYPE_PUSH_SETTINGS_SWITCH -> SwitchVh(parent.toBinding(), actionListener)
            TYPE_PUSH_SETTINGS_EXCLUDE -> ExcludeVh(parent.toBinding(), actionListener)
            else -> throw RuntimeException("Unknown PushSettingsAdapter view holder type")
        }
    }

    private inner class TitleVh(
        private val binding: MeeraPushNotifSettingsTitleVhBinding
    ) : BaseVH<PushSettingsData, MeeraPushNotifSettingsTitleVhBinding>(binding) {
        override fun bind(data: PushSettingsData) {
            data as PushSettingsData.PushSettingsTitle
            binding.tvPushSettingsTitle.text = data.title
        }
    }

    private inner class SwitchVh(
        private val binding: MeeraPushNotifSettingsSwitchVhBinding,
        private val actionListener: (action: MeeraPushNotificationSettingsAction) -> Unit
    ) : BaseVH<PushSettingsData, MeeraPushNotifSettingsSwitchVhBinding>(binding) {
        override fun bind(data: PushSettingsData) {
            data as PushSettingsData.PushSettingsSwitch
            binding.apply {
                if (!data.isEnable) {
                    cellPushSettingsSwitch.setCellRightElementEnable(false)
                    cellPushSettingsSwitch.setRightElementContainerClickable(false)
                } else {
                    cellPushSettingsSwitch.setCellRightElementEnable(true)
                    cellPushSettingsSwitch.setRightIconClickListener {
                        actionListener.invoke(data.action)
                        cellPushSettingsSwitch.setCellRightElementChecked(!cellPushSettingsSwitch.getCellRightElementChecked())
                    }
                    cellPushSettingsSwitch.setRightElementContainerClickable(true)
                }
                cellPushSettingsSwitch.setTitleValue(data.title)
                cellPushSettingsSwitch.setCellRightElementChecked(data.isChosen)
                cellPushSettingsSwitch.setCellRightElementClickable(false)
                cellPushSettingsSwitch.cellPosition = data.position
                cellPushSettingsSwitch.setMargins(0, data.topMargin.dp, 0, 0)

            }
        }
    }

    private inner class DescVh(
        private val binding: MeeraPushNotifSettingsDescVhBinding
    ) : BaseVH<PushSettingsData, MeeraPushNotifSettingsDescVhBinding>(binding) {
        override fun bind(data: PushSettingsData) {
            data as PushSettingsData.PushSettingsDesc
            binding.tvPushSettingsDesc.text = data.title
        }
    }

    private inner class ExcludeVh(
        private val binding: MeeraPushNotifSettingsExclVhBinding,
        private val actionListener: (action: MeeraPushNotificationSettingsAction) -> Unit
    ) : BaseVH<PushSettingsData, MeeraPushNotifSettingsExclVhBinding>(binding) {
        override fun bind(data: PushSettingsData) {
            data as PushSettingsData.PushSettingsExclude
            with(binding) {
                binding.cellPushSettingsExclude.setTitleValue(data.title)
                cellPushSettingsExclude.setRightTextboxValue(
                    if (data.usersCount > 0) data.usersCount.toString() else data.actionTitle
                )

                cellPushSettingsExclude.cellRightElementColor = data.actionTitleColor
                if (data.action is MeeraPushNotificationSettingsAction.Post.FriendAndSubscriptions) {
                    cellPushSettingsExclude.setMargins(bottom = MARGIN_BOTTOM_LIST_LAST_ELEMENT.dp)
                } else {
                    cellPushSettingsExclude.setMargins(bottom = 0.dp)
                }
                cellPushSettingsExclude.cellPosition = data.position
                cellPushSettingsExclude.setRightElementContainerClickable(false)
                cellPushSettingsExclude.setThrottledClickListener {
                    when (data.action) {
                        is MeeraPushNotificationSettingsAction.Post.FriendAndSubscriptions -> {
                            actionListener.invoke(MeeraPushNotificationSettingsAction.Post.FriendAndSubscriptions(data.usersCount))
                        }

                        is MeeraPushNotificationSettingsAction.Message.ExceptUser -> {
                            actionListener.invoke(MeeraPushNotificationSettingsAction.Message.ExceptUser(data.usersCount))
                        }

                        else -> {
                            Timber.d("A new version of the list item has appeared")
                        }
                    }
                }
            }
        }
    }
}

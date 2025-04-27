package com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.adapter


import androidx.annotation.ColorRes
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.presentation.view.fragments.meerasettings.presentatin.pushnotif.MeeraPushNotificationSettingsAction

const val TYPE_PUSH_SETTINGS_TITLE = 1
const val TYPE_PUSH_SETTINGS_DESCRIPTION = 2
const val TYPE_PUSH_SETTINGS_SWITCH = 3
const val TYPE_PUSH_SETTINGS_EXCLUDE = 4

sealed interface PushSettingsData : RecyclerData<String, PushSettingsData> {

    data class PushSettingsTitle(val title: String) : PushSettingsData {

        override fun getItemId() = ""

        override fun contentTheSame(newItem: PushSettingsData) = this == newItem

        override fun itemViewType() = TYPE_PUSH_SETTINGS_TITLE
    }

    data class PushSettingsDesc(val title: String) : PushSettingsData {

        override fun getItemId() = ""

        override fun contentTheSame(newItem: PushSettingsData) = this == newItem

        override fun itemViewType() = TYPE_PUSH_SETTINGS_DESCRIPTION

    }

    data class PushSettingsSwitch(
        val id: String,
        val title: String,
        val isChosen: Boolean,
        val action: MeeraPushNotificationSettingsAction,
        val position: CellPosition,
        val topMargin: Int = 0,
        val isEnable: Boolean
    ) : PushSettingsData {

        override fun getItemId() = id

        override fun contentTheSame(newItem: PushSettingsData) = this == newItem

        override fun itemViewType() = TYPE_PUSH_SETTINGS_SWITCH

    }

    data class PushSettingsExclude(
        val id: String,
        val title: String,
        val action: MeeraPushNotificationSettingsAction,
        val position: CellPosition,
        val actionTitle: String,
        @ColorRes
        val actionTitleColor: Int,
        val usersCount: Int
    ) : PushSettingsData {

        override fun getItemId() = id

        override fun contentTheSame(newItem: PushSettingsData) = this == newItem

        override fun itemViewType() = TYPE_PUSH_SETTINGS_EXCLUDE

    }
}

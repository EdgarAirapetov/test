package com.numplates.nomera3.modules.moments.settings.util

import android.content.Context
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.meera.core.extensions.empty
import com.meera.core.extensions.isTrue
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum

class SettingsViewHolderUtilsImpl(private val context: Context) : SettingsViewHolderUtils {
    override fun controlSwitches(isEnabled: Boolean, vararg switches: SwitchCompat) {
        if (isEnabled) {
            switches.forEach { it.isEnabled = true }
        } else
            switches.forEach { it.isEnabled = false }
    }

    override fun setSwitch(isEnabled: Int?, switch: SwitchCompat) {
        isEnabled?.let {
            switch.isChecked = isEnabled.isTrue()
        }
    }

    @Deprecated("Not using at this time")
    override fun setUserExclusionType(value: Int?, textView: TextView) {
        when (value) {
            SettingsUserTypeEnum.ALL.key -> textView.text =
                context.getString(R.string.settings_everybody)
            SettingsUserTypeEnum.FRIENDS.key -> textView.text =
                context.getString(R.string.settings_friends)
            SettingsUserTypeEnum.NOBODY.key -> textView.text =
                context.getString(R.string.settings_nobody)
        }
    }

    override fun setCountBlacklist(countBlacklist: Int?, textView: TextView) {
        textView.text = (countBlacklist ?: 0).toString()
    }

    override fun setUserExclusionTypeWithCount(
        textView: TextView,
        value: Int?,
        countBlacklist: Int?,
        countWhitelist: Int?
    ) {
        when (value) {
            SettingsUserTypeEnum.ALL.key -> {
                textView.text = context.getString(
                    R.string.settings_everybody_count,
                    getCountExclusionsText(countBlacklist, 0)
                )
            }
            SettingsUserTypeEnum.FRIENDS.key -> {
                textView.text = context.getString(
                    R.string.settings_friends_count,
                    getCountExclusionsText(countBlacklist, countWhitelist)
                )
            }
            SettingsUserTypeEnum.NOBODY.key -> {
                textView.text = context.getString(
                    R.string.settings_nobody_count,
                    getCountExclusionsText(0, countWhitelist)
                )
            }
        }
    }

    private fun getCountExclusionsText(
        countBlacklist: Int?,
        countWhitelist: Int?
    ): String {

        if (countBlacklist != null && countWhitelist != null) {
            if (countBlacklist > 0 && countWhitelist > 0) {
                return "(+$countWhitelist,-$countBlacklist)"
            }
            if (countBlacklist > 0 && countWhitelist == 0) {
                return "(-$countBlacklist)"
            }
            if (countWhitelist > 0 && countBlacklist == 0) {
                return "(+$countWhitelist)"
            }
        }
        return String.empty()
    }
}

interface SettingsViewHolderUtils {
    fun controlSwitches(isEnabled: Boolean, vararg switches: SwitchCompat)
    fun setSwitch(isEnabled: Int?, switch: SwitchCompat)
    fun setUserExclusionType(value: Int?, textView: TextView)
    fun setCountBlacklist(countBlacklist: Int?, textView: TextView)
    fun setUserExclusionTypeWithCount(
        textView: TextView,
        value: Int?,
        countBlacklist: Int?,
        countWhitelist: Int?
    )
}

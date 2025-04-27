package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders

import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.viewbinding.ViewBinding
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.empty
import com.meera.core.extensions.isTrue
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.settings.util.SettingsViewHolderUtils
import com.numplates.nomera3.modules.moments.settings.util.SettingsViewHolderUtilsImpl
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData

/**
 * Base View holder class for common methods
 */
abstract class MeeraBaseSettingsViewHolder(
    binding: ViewBinding
) : BaseVH<MeeraPrivacySettingsData, ViewBinding>(binding), SettingsViewHolderUtils by SettingsViewHolderUtilsImpl(
    binding.root.context
) {

    private val context = itemView.context

    override fun controlSwitches(isEnabled: Boolean, vararg switches: SwitchCompat) {
        if (isEnabled) {
            switches.forEach { it.isEnabled = true }
        } else switches.forEach { it.isEnabled = false }
    }

    override fun setSwitch(isEnabled: Int?, switch: SwitchCompat) {
        isEnabled?.let {
            switch.isChecked = isEnabled.isTrue()
        }
    }

    // All, friends, nobody
    @Deprecated("Not using at this time")
    override fun setUserExclusionType(value: Int?, textView: TextView) {
        when (value) {
            SettingsUserTypeEnum.ALL.key -> textView.text = context.getString(R.string.settings_everybody)
            SettingsUserTypeEnum.FRIENDS.key -> textView.text = context.getString(R.string.settings_friends)
            SettingsUserTypeEnum.NOBODY.key -> textView.text = context.getString(R.string.settings_nobody)
        }
    }

    fun getUserExclusionType(value: Int?): String {
        return when (value) {
            SettingsUserTypeEnum.ALL.key -> context.getString(R.string.settings_everybody)
            SettingsUserTypeEnum.FRIENDS.key -> context.getString(R.string.settings_friends)
            SettingsUserTypeEnum.NOBODY.key -> context.getString(R.string.settings_nobody)
            else -> String.empty()
        }
    }

    override fun setCountBlacklist(countBlacklist: Int?, textView: TextView) {
        textView.text = (countBlacklist ?: 0).toString()
    }

    fun getCountBlacklist(countBlacklist: Int?): String {
        return (countBlacklist ?: 0).toString()
    }

    fun setNewAvatarPostValue(value: Int?, textView: TextView) {
        textView.text = when (value) {
            CreateAvatarPostEnum.PRIVATE_ROAD.state -> context.getString(R.string.settings_privacy_new_avatar_post_private)
            CreateAvatarPostEnum.MAIN_ROAD.state -> context.getString(R.string.settings_privacy_new_avatar_post_main)
            CreateAvatarPostEnum.NOT_PUBLIC.state -> context.getString(R.string.settings_privacy_new_avatar_post_not_publish)
            else -> throw java.lang.IllegalArgumentException("Unknown CreateAvatarPostEnum")
        }
    }

    // Never(+3, -1)
    override fun setUserExclusionTypeWithCount(
        textView: TextView, value: Int?, countBlacklist: Int?, countWhitelist: Int?
    ) {
        when (value) {
            SettingsUserTypeEnum.ALL.key -> {
                textView.text = context.getString(
                    R.string.settings_everybody_count, getCountExclusionsText(countBlacklist, 0)
                )

            }

            SettingsUserTypeEnum.FRIENDS.key -> {
                textView.text = context.getString(
                    R.string.settings_friends_count, getCountExclusionsText(countBlacklist, countWhitelist)
                )
            }

            SettingsUserTypeEnum.NOBODY.key -> {
                textView.text = context.getString(
                    R.string.settings_nobody_count, getCountExclusionsText(0, countWhitelist)
                )

            }
        }
    }

    fun getUserExclusionTypeWithCount(value: Int?, countBlacklist: Int?, countWhitelist: Int?): String {
        return when (value) {
            SettingsUserTypeEnum.ALL.key -> {
                context.getString(
                    R.string.settings_everybody_count, getCountExclusionsText(countBlacklist, 0)
                )

            }

            SettingsUserTypeEnum.FRIENDS.key -> {
                context.getString(
                    R.string.settings_friends_count, getCountExclusionsText(countBlacklist, countWhitelist)
                )
            }

            SettingsUserTypeEnum.NOBODY.key -> {
                context.getString(
                    R.string.settings_nobody_count, getCountExclusionsText(0, countWhitelist)
                )
            }

            else -> String.empty()
        }
    }


    private fun getCountExclusionsText(
        countBlacklist: Int?, countWhitelist: Int?
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

package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.settings.util.SettingsViewHolderUtils
import com.numplates.nomera3.modules.moments.settings.util.SettingsViewHolderUtilsImpl
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.MAIN_ROAD
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.NOT_PUBLIC
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum.PRIVATE_ROAD
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum

class PrivacySettingsAdapter(
    private val adapterCallback: IPrivacySettingsInteractor
) : RecyclerView.Adapter<PrivacySettingsAdapter.BaseSettingsViewHolder>() {

    private var collection = mutableListOf<PrivacySettingsModel>()

    override fun getItemViewType(position: Int): Int {
        return collection[position].viewType
    }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSettingsViewHolder {
        return when (viewType) {
            SETTING_ITEM_TYPE_MAP -> SettingMapViewHolder(parent.inflate(R.layout.item_type_settings_map))
            SETTING_ITEM_TYPE_COMMON -> SettingCommonViewHolder(parent.inflate(R.layout.item_type_settings_common))
            SETTING_ITEM_TYPE_PROFILE -> SettingsProfileViewHolder(parent.inflate(R.layout.item_type_settings_profile))
            SETTING_ITEM_TYPE_COMMUNICATION -> SettingCommunicationViewHolder(parent.inflate(R.layout.item_type_settings_communication))
            SETTING_ITEM_TYPE_MY_BIRTHDAY -> SettingMyBirthdayViewHolder(parent.inflate(R.layout.item_type_settings_birhtday))
            SETTING_ITEM_TYPE_ROAD -> SettingRoadViewHolder(parent.inflate(R.layout.item_type_settings_road))
            SETTING_ITEM_TYPE_BLACKLIST -> SettingBlacklistViewHolder(parent.inflate(R.layout.item_type_settings_blacklist))
            SETTING_ITEM_TYPE_RESTORE_DEFAULTS -> SettingRestoreDefaultsViewHolder(parent.inflate(R.layout.item_type_settings_restore_defaults))
            SETTING_ITEM_TYPE_MOMENTS -> SettingMomentsViewHolder(parent.inflate(R.layout.item_type_settings_moments))
            SETTING_ITEM_TYPE_SHAKE -> ShakeSettingsViewHolder(parent.inflate(R.layout.item_type_setting_shake))
            else -> throw IllegalArgumentException("No such view type in adapter")
        }
    }

    override fun onBindViewHolder(holder: BaseSettingsViewHolder, position: Int) {
        holder.bind(collection[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addDataSet(items: List<PrivacySettingsModel>) {
        collection.clear()
        collection.addAll(items)
        notifyDataSetChanged()
    }

    inner class SettingsProfileViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {
        private val vgCloseProfile: ViewGroup = itemView.findViewById(R.id.vg_close_profile)
        private val switchCloseProfile: SwitchCompat = itemView.findViewById(R.id.sw_switcher_close_profile)
        private val tvClosedProfileHint: TextView = itemView.findViewById(R.id.tv_closed_profile_hint)

        override fun bind(data: PrivacySettingsModel) {
            controlSwitches(data.isEnabled, switchCloseProfile)

            data.settings?.forEach { settings ->
                when (settings.key) {
                    SettingsKeyEnum.CLOSED_PROFILE.key -> {
                        setSwitch(settings.value, switchCloseProfile)
                        tvClosedProfileHint.isVisible = settings.value?.toBoolean() == true
                    }
                }
            }
            vgCloseProfile.setOnClickListener {
                adapterCallback.switchClosedProfile(SettingsKeyEnum.CLOSED_PROFILE.key, !switchCloseProfile.isChecked)
            }
            switchCloseProfile.isClickable = false
        }
    }

    fun updateItemByPosition(
        position: Int,
        item: PrivacySettingsModel
    ) {
        if (collection.isEmpty() || position < 0 || position >= collection.size) return
        collection[position] = item
        notifyItemChanged(position)
    }

    fun getCurrentCollection() = collection

    inner class SettingCommonViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val switchGender: SwitchCompat = itemView.findViewById(R.id.sw_switcher_gender)
        private val vgGender: ViewGroup = itemView.findViewById(R.id.vg_gender)
        private val vGenderDivider: View = itemView.findViewById(R.id.v_gender_divider)
        private val switchAge: SwitchCompat = itemView.findViewById(R.id.sw_switcher_age)
        private val vgAge: ViewGroup = itemView.findViewById(R.id.vg_age)
        private val vAgeDivider: View = itemView.findViewById(R.id.v_age_divider)
        private val tvAboutMeSettings: TextView = itemView.findViewById(R.id.tv_about_me)
        private val vgAboutMeSettings: ViewGroup = itemView.findViewById(R.id.vg_about_me)
        private val tvGarageSettings: TextView = itemView.findViewById(R.id.tv_garage)
        private val vgGarageSettings: ViewGroup = itemView.findViewById(R.id.vg_garage)
        private val tvGiftsSettings: TextView = itemView.findViewById(R.id.tv_gifts)
        private val vgGiftsSettings: ViewGroup = itemView.findViewById(R.id.vg_gifts)
        private val vgFriendsFollowers: ViewGroup = itemView.findViewById(R.id.vg_friends_followers)
        private val tvFollowersFriendsSettings: TextView = itemView.findViewById(R.id.tv_friends_and_followers)

        private val ageCheckedChangeListener = OnCheckedChangeListener { _, isChecked ->
            adapterCallback.switchAge(SettingsKeyEnum.SHOW_BIRTHDAY.key, isChecked)
        }

        override fun bind(data: PrivacySettingsModel) {
            // Disable / Enable all switches
            controlSwitches(data.isEnabled, switchGender, switchAge)

            val genderVisible = data.settings?.any { it.key == SettingsKeyEnum.SHOW_GENDER.key } ?: false
            vgGender.isVisible = genderVisible
            vGenderDivider.isVisible = genderVisible

            val ageVisible = data.settings?.any { it.key == SettingsKeyEnum.SHOW_BIRTHDAY.key } ?: false
            vgAge.isVisible = ageVisible
            vAgeDivider.isVisible = ageVisible

            // Set all setting
            data.settings?.forEach { setting ->
                when (setting.key) {
                    SettingsKeyEnum.SHOW_GENDER.key -> setSwitch(setting.value, switchGender)
                    SettingsKeyEnum.SHOW_BIRTHDAY.key -> {
                        switchAge.setOnCheckedChangeListener(null)
                        setSwitch(setting.value, switchAge)
                        switchAge.setOnCheckedChangeListener(ageCheckedChangeListener)
                    }
                    SettingsKeyEnum.SHOW_ABOUT_ME.key -> {
                        setUserExclusionType(setting.value, tvAboutMeSettings)
                        vgAboutMeSettings.setOnClickListener {
                            adapterCallback.clickAboutMePrivacy(setting.value)
                        }
                    }
                    SettingsKeyEnum.SHOW_GARAGE.key -> {
                        setUserExclusionType(setting.value, tvGarageSettings)
                        vgGarageSettings.setOnClickListener {
                            adapterCallback.clickGaragePrivacy(setting.value)
                        }
                    }
                    SettingsKeyEnum.SHOW_GIFTS.key -> {
                        setUserExclusionType(setting.value, tvGiftsSettings)
                        vgGiftsSettings.setOnClickListener {
                            adapterCallback.clickGiftPrivacy(setting.value)
                        }
                    }

                    SettingsKeyEnum.SHOW_FRIENDS_AND_FOLLOWERS.key -> {
                        setUserExclusionType(setting.value, tvFollowersFriendsSettings)
                        vgFriendsFollowers.setOnClickListener {
                            adapterCallback.onFriendsAndFollowersClicked(
                                setting.value
                            )
                        }
                    }
                }
            }
            vgGender.setOnClickListener {
                switchGender.isChecked = !switchGender.isChecked
            }
            vgAge.setOnClickListener {
                adapterCallback.switchAge(SettingsKeyEnum.SHOW_BIRTHDAY.key, !switchAge.isChecked)
            }
            switchGender.setOnCheckedChangeListener { _, isChecked ->
                adapterCallback.switchGender(SettingsKeyEnum.SHOW_GENDER.key, isChecked)
            }
            switchAge.setOnCheckedChangeListener(ageCheckedChangeListener)
        }

    }


    inner class SettingRoadViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val vgHidePostsContainer: ViewGroup = itemView.findViewById(R.id.vg_road_hide_post)
        private val vgNewAvatarPostContainer: ViewGroup = itemView.findViewById(R.id.vg_road_new_avatar_post)
        private val vgAbscene: ViewGroup = itemView.findViewById(R.id.vg_abscene)
        private val tvHidePosts: TextView = itemView.findViewById(R.id.tv_road_hidden_post_count)

        private val switchNewAvatarPost: SwitchCompat = itemView.findViewById(R.id.sw_new_avatar_post)
        private val switchAntiObscene: SwitchCompat = itemView.findViewById(R.id.sw_anti_obscene)

        private val tvPersonalFeedSettings: TextView = itemView.findViewById(R.id.tv_personal_road)
        private val vgPersonalFeedSettings: ViewGroup = itemView.findViewById(R.id.vg_personal_road)

        override fun bind(data: PrivacySettingsModel) {
            controlSwitches(data.isEnabled, switchAntiObscene)

            var count: Int? = 0
            // Set all setting
            data.settings?.forEach { setting ->
                when (setting.key) {
                    SettingsKeyEnum.HIDE_POSTS.key -> {
                        count = setting.countBlacklist

                        // show hidden post container if needed
                        count?.let { hiddenPostCount: Int ->
                            if (hiddenPostCount > 0) {
                                vgHidePostsContainer.visible()
                                setCountBlacklist(hiddenPostCount, tvHidePosts)
                            } else {
                                vgHidePostsContainer.gone()
                            }
                        }
                    }
                    SettingsKeyEnum.PROFANITY_ENABLED.key -> setSwitch(
                        setting.value,
                        switchAntiObscene
                    )
                    SettingsKeyEnum.CREATE_AVATAR_POST.key -> {
                        vgNewAvatarPostContainer.visible()
                        val settingValue = if (setting.value == PRIVATE_ROAD.state) MAIN_ROAD.state else setting.value
                        setSwitch(settingValue, switchNewAvatarPost)
                    }
                    SettingsKeyEnum.SHOW_PERSONAL_ROAD.key -> {
                        setUserExclusionType(setting.value, tvPersonalFeedSettings)
                        vgPersonalFeedSettings.setOnClickListener {
                            adapterCallback.clickPersonalFeedPrivacy(setting.value)
                        }
                    }
                }
            }
            vgNewAvatarPostContainer.setOnClickListener {
                switchNewAvatarPost.isChecked = !switchNewAvatarPost.isChecked
            }
            switchNewAvatarPost.setOnCheckedChangeListener { _, isChecked ->
                adapterCallback.switchNewAvatarPost(SettingsKeyEnum.CREATE_AVATAR_POST.key, isChecked)
            }
            vgAbscene.setOnClickListener {
                switchAntiObscene.isChecked = !switchAntiObscene.isChecked
            }
            switchAntiObscene.setOnCheckedChangeListener { _, isChecked ->
                adapterCallback.switchAntiObscene(SettingsKeyEnum.PROFANITY_ENABLED.key, isChecked)
            }

            if (data.isEnabled) {
                vgHidePostsContainer.setOnClickListener { adapterCallback.clickHideRoadPosts(count) }
            }
        }

    }


    inner class SettingMapViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val tvMapPermission: TextView = itemView.findViewById(R.id.tv_map_permission)
        private val vMap: View = itemView.findViewById(R.id.v_map)

        override fun bind(data: PrivacySettingsModel) {
            if (data.isEnabled) {
                var showOnlineValue: Int? = null
                var countBlacklist: Int? = null
                var countWhitelist: Int? = null

                data.settings?.forEach { setting ->
                    if (setting.key == SettingsKeyEnum.SHOW_ON_MAP.key) {
                        setUserExclusionTypeWithCount(
                            tvMapPermission, setting.value, setting.countBlacklist, setting.countWhitelist
                        )
                        showOnlineValue = setting.value
                        countBlacklist = setting.countBlacklist
                        countWhitelist = setting.countWhitelist
                    }
                }
                vMap.setOnClickListener {
                    adapterCallback.clickMapPermissions(
                        showOnlineValue,
                        countBlacklist,
                        countWhitelist
                    )
                }
            }
        }
    }

    inner class SettingMomentsViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val momentSettingsButton: FrameLayout =
            itemView.findViewById(R.id.vg_moment_settings_button)

        init {
            momentSettingsButton.setOnClickListener {
                openMomentSettings()
            }
        }

        override fun bind(data: PrivacySettingsModel) = Unit

        private fun openMomentSettings() {
            adapterCallback.clickMomentSettings()
        }
    }


    inner class SettingBlacklistViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val tvBlacklistUsers: TextView = itemView.findViewById(R.id.tv_blacklist_users)
        private val vBlackList: View = itemView.findViewById(R.id.v_blacklist)

        override fun bind(data: PrivacySettingsModel) {
            if (data.isEnabled) {
                var count: Int? = 0
                data.settings?.forEach { setting ->
                    if (setting.key == SettingsKeyEnum.BLACKLIST.key) {
                        setCountBlacklist(setting.countBlacklist, tvBlacklistUsers)
                        count = setting.countBlacklist
                    }
                }
                vBlackList.setOnClickListener { adapterCallback.clickBlacklistUsers(count) }
            }
        }
    }

    inner class SettingMyBirthdayViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val tvBirthdayRemind: TextView = itemView.findViewById(R.id.tv_birthday_remind)
        private val vgBday: ViewGroup = itemView.findViewById(R.id.vg_bday)

        override fun bind(data: PrivacySettingsModel) {
            if (data.isEnabled) {
                var value: Int? = null
                data.settings?.forEach { setting ->
                    if (setting.key == SettingsKeyEnum.REMIND_MY_BIRTHDAY.key) {
                        setUserExclusionTypeWithCount(
                            textView = tvBirthdayRemind,
                            value = setting.value,
                            countBlacklist = null,
                            countWhitelist = null,
                        )
                        value = setting.value
                    }
                    vgBday.setOnClickListener { adapterCallback.clickBirthdayDetails(value) }
                }
            }
        }
    }

    inner class SettingCommunicationViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val tvOnlineExclusions: TextView = itemView.findViewById(R.id.tvOnlinePermission)
        private val vgOnline: ViewGroup = itemView.findViewById(R.id.vg_online)

        private val tvCallsPermission: TextView = itemView.findViewById(R.id.tvCallsPermission)
        private val vgCalls: View = itemView.findViewById(R.id.vg_calls)

        private val tvMessagePermission: TextView = itemView.findViewById(R.id.tvPersonalMessagesPermission)
        private val vgMessage: View = itemView.findViewById(R.id.vg_personal_messages)

        private val vgSyncContacts: ViewGroup = itemView.findViewById(R.id.vg_sync_contacts)
        private val scSyncContacts: SwitchCompat = vgSyncContacts.findViewById(R.id.sc_sync_contacts)

        private val vgShareScreenshot: ViewGroup = itemView.findViewById(R.id.vg_share_screenshot)
        private val scShareScreenshot: SwitchCompat = vgShareScreenshot.findViewById(R.id.sc_share_screenshot)

        override fun bind(data: PrivacySettingsModel) {

            var showOnlineValue: Int? = null
            var countBlacklist: Int? = null
            var countWhitelist: Int? = null

            var showCallOnlineValue: Int? = null
            var countCallBlacklist: Int? = null
            var countCallWhitelist: Int? = null

            var showChatMessageValue: Int? = null
            var countChatBlackList: Int? = null
            var countChatWhiteList: Int? = null

            data.settings?.forEach { setting ->
                when (setting.key) {
                    SettingsKeyEnum.SHOW_ONLINE.key -> {
                        setUserExclusionTypeWithCount(
                            tvOnlineExclusions, setting.value, setting.countBlacklist, setting.countWhitelist
                        )
                        showOnlineValue = setting.value
                        countBlacklist = setting.countBlacklist
                        countWhitelist = setting.countWhitelist
                    }

                    SettingsKeyEnum.WHO_CAN_CHAT.key -> {
                        setUserExclusionTypeWithCount(
                            tvMessagePermission, setting.value, setting.countBlacklist, setting.countWhitelist
                        )
                        showChatMessageValue = setting.value
                        countChatBlackList = setting.countBlacklist
                        countChatWhiteList = setting.countWhitelist
                    }
                    SettingsKeyEnum.ALLOW_CONTACT_SYNC.key -> {
                        setSwitch(
                            isEnabled = setting.value,
                            switch = scSyncContacts
                        )
                    }
                    SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING.key -> {
                        setSwitch(
                            isEnabled = setting.value,
                            switch = scShareScreenshot
                        )
                    }
                }
            }

            if (data.isEnabled) {
                data.settings?.forEach { setting ->
                    if (setting.key == SettingsKeyEnum.HOW_CAN_CALL.key) {
                        setUserExclusionTypeWithCount(
                            tvCallsPermission, setting.value, setting.countBlacklist, setting.countWhitelist
                        )
                        showCallOnlineValue = setting.value
                        countCallBlacklist = setting.countBlacklist
                        countCallWhitelist = setting.countWhitelist
                    }
                }

                vgCalls.setOnClickListener {
                    adapterCallback.clickCallPermissions(showCallOnlineValue, countCallBlacklist, countCallWhitelist)
                }

                vgOnline.setOnClickListener {
                    adapterCallback.clickOnlineStatus(showOnlineValue, countBlacklist, countWhitelist)
                }

                vgMessage.setOnClickListener {
                    adapterCallback.clickPersonalMessages(showChatMessageValue, countChatBlackList, countChatWhiteList)
                }
                scSyncContacts.setOnCheckedChangeListener { _, isChecked ->
                    if (!scSyncContacts.isPressed) return@setOnCheckedChangeListener
                    adapterCallback.switchContactSync(
                        key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key,
                        isEnabled = isChecked
                    )
                }
                scShareScreenshot.setOnCheckedChangeListener { _, isChecked ->
                    if (!scShareScreenshot.isPressed) return@setOnCheckedChangeListener
                    adapterCallback.switchShareScreenshot(
                        key = SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING.key,
                        isEnabled = isChecked
                    )
                }
            }
        }
    }

    inner class ShakeSettingsViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val shakeEnabledSwitch: SwitchCompat = itemView.findViewById(R.id.sc_setting_shake)
        private val settingShakeAbscene: ViewGroup = itemView.findViewById(R.id.vg_setting_shake_abscene)

        init {
            initListeners()
        }

        override fun bind(data: PrivacySettingsModel) {
            data.settings?.forEach { model ->
                when (model.key) {
                    SettingsKeyEnum.ALLOW_SHAKE_GESTURE.key -> {
                        setSwitch(
                            isEnabled = model.value, switch = shakeEnabledSwitch
                        )
                    }
                }
            }
        }

        private fun initListeners() {
            initSwitchListener()
            initClickListeners()
        }

        private fun initClickListeners() {
            settingShakeAbscene.setOnClickListener {
                shakeEnabledSwitch.isChecked = !shakeEnabledSwitch.isChecked
            }
        }

        private fun initSwitchListener() {
            shakeEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                adapterCallback.switchShake(
                    key = SettingsKeyEnum.ALLOW_SHAKE_GESTURE.key, isEnabled = isChecked
                )
            }
        }
    }

    inner class SettingRestoreDefaultsViewHolder(itemView: View) : BaseSettingsViewHolder(itemView) {

        private val tvRestoreDefaults: TextView = itemView.findViewById(R.id.tv_restore_defaults)

        init {
            tvRestoreDefaults.setOnClickListener { adapterCallback.clickRestoreDefaultSettings() }
        }

        override fun bind(data: PrivacySettingsModel) = Unit
    }


    /**
     * Base View holder class for common methods
     */
    abstract inner class BaseSettingsViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        SettingsViewHolderUtils by SettingsViewHolderUtilsImpl(itemView.context) {

        private val context = itemView.context

        abstract fun bind(data: PrivacySettingsModel)

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

        override fun setCountBlacklist(countBlacklist: Int?, textView: TextView) {
            textView.text = (countBlacklist ?: 0).toString()
        }

        fun setNewAvatarPostValue(value: Int?, textView: TextView) {
            textView.text = when (value) {
                PRIVATE_ROAD.state -> context.getString(R.string.settings_privacy_new_avatar_post_private)
                MAIN_ROAD.state -> context.getString(R.string.settings_privacy_new_avatar_post_main)
                NOT_PUBLIC.state -> context.getString(R.string.settings_privacy_new_avatar_post_not_publish)
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


    interface IPrivacySettingsInteractor {

        fun switchGender(key: String, isEnabled: Boolean)
        fun switchAge(key: String, isEnabled: Boolean)
        fun switchAntiObscene(key: String, isEnabled: Boolean)
        fun switchNewAvatarPost(key: String, isEnabled: Boolean)
        fun switchShake(key: String, isEnabled: Boolean)
        fun switchClosedProfile(key: String, isEnabled: Boolean)
        fun switchContactSync(key: String, isEnabled: Boolean)
        fun switchShareScreenshot(key: String, isEnabled: Boolean)

        fun clickBirthdayDetails(value: Int?)
        fun clickOnlineStatus(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun clickHideRoadPosts(count: Int?)
        fun clickMapPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun clickCallPermissions(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun clickBlacklistUsers(count: Int?)
        fun clickAboutMePrivacy(value: Int?)
        fun clickGaragePrivacy(value: Int?)
        fun clickGiftPrivacy(value: Int?)
        fun clickPersonalFeedPrivacy(value: Int?)
        fun clickPersonalMessages(value: Int?, countBlacklist: Int?, countWhitelist: Int?)
        fun onFriendsAndFollowersClicked(value: Int?)
        fun clickRestoreDefaultSettings()
        fun clickMomentSettings()
    }

    companion object {
        const val SETTING_ITEM_TYPE_PROFILE = -1
        const val SETTING_ITEM_TYPE_COMMON = 0
        const val SETTING_ITEM_TYPE_ROAD = 1
        const val SETTING_ITEM_TYPE_MAP = 2
        const val SETTING_ITEM_TYPE_CALLS = 3
        const val SETTING_ITEM_TYPE_STORY = 4
        const val SETTING_ITEM_TYPE_BLACKLIST = 5
        const val SETTING_ITEM_TYPE_MY_BIRTHDAY = 6
        const val SETTING_ITEM_TYPE_COMMUNICATION = 7
        const val SETTING_ITEM_TYPE_RESTORE_DEFAULTS = 8
        const val SETTING_ITEM_TYPE_MOMENTS = 9
        const val SETTING_ITEM_TYPE_SHAKE = 10
    }

}

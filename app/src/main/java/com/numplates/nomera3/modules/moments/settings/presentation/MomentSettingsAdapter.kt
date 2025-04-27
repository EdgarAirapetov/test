package com.numplates.nomera3.modules.moments.settings.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.ItemTypeMomentSettingsCommentsBinding
import com.numplates.nomera3.databinding.ItemTypeMomentSettingsSaveBinding
import com.numplates.nomera3.databinding.ItemTypeMomentSettingsVisibleBinding
import com.numplates.nomera3.modules.moments.settings.util.SettingsViewHolderUtils
import com.numplates.nomera3.modules.moments.settings.util.SettingsViewHolderUtilsImpl
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsModel

private const val TOGGLE_THROTTLE_DELAY_MS = 60L

class MomentSettingsAdapter(
    private val callback: MomentSettingsAdapterCallback,
) : RecyclerView.Adapter<MomentSettingsAdapter.BaseSettingsViewHolder>() {

    private var collection = mutableListOf<PrivacySettingsModel>()

    override fun getItemViewType(position: Int): Int {
        return collection[position].viewType
    }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSettingsViewHolder {
        return when (viewType) {
            VISIBLE_SETTINGS -> {
                val itemBinding = ItemTypeMomentSettingsVisibleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                VisibleSettingsViewHolder(itemBinding)
            }
            COMMENT_SETTINGS -> {
                val itemBinding = ItemTypeMomentSettingsCommentsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CommentSettingsViewHolder(itemBinding)
            }
            SAVE_SETTINGS -> {
                val itemBinding = ItemTypeMomentSettingsSaveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SaveSettingsViewHolder(itemBinding)
            }
            else -> error("No such view type in adapter")
        }
    }

    override fun onBindViewHolder(holder: BaseSettingsViewHolder, position: Int) {
        holder.bind(collection[position])
    }

    override fun getItemId(position: Int): Long {
        return collection[position].viewType.toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addDataSet(items: List<PrivacySettingsModel>) {
        collection.clear()
        collection.addAll(items)
        notifyDataSetChanged()
    }

    private inner class VisibleSettingsViewHolder(
        private val binding: ItemTypeMomentSettingsVisibleBinding
    ) : BaseSettingsViewHolder(binding.root) {

        init {
            binding.vgMomentSettingsShowOnlyFriendsContainer.setThrottledClickListener(
                TOGGLE_THROTTLE_DELAY_MS
            ) {
                val isToggled = binding.swMomentSettingsShowOnlyFriendsToggle.isChecked.not()
                callback.onShowMomentForFriendSwitch(isToggled)
            }
        }

        override fun bind(data: PrivacySettingsModel) {
            bindShowOnlyForFriends(data)
            bindNotShow(data)
            bindHideFrom(data)
        }

        private fun bindShowOnlyForFriends(settingsModel: PrivacySettingsModel) {
            val setting =
                settingsModel.settings?.getSetting(SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS) ?: return

            binding.swMomentSettingsShowOnlyFriendsToggle.setSwitch(setting)
        }

        private fun bindNotShow(settingsModel: PrivacySettingsModel) {
            val setting =
                settingsModel.settings?.getSetting(SettingsKeyEnum.MOMENTS_NOT_SHOW) ?: return

            setCountBlacklist(setting.countBlacklist, binding.tvMomentSettingsNotShowCount)

            binding.vgMomentSettingsNotShowContainer.setThrottledClickListener {
                callback.notShowMomentsClick()
            }

            val countBlacklist = setting.countBlacklist ?: 0

            if (countBlacklist > 0) {
                binding.vgMomentSettingsNotShowContainer.visible()
            } else {
                binding.vgMomentSettingsNotShowContainer.gone()
            }
        }

        private fun bindHideFrom(settingsModel: PrivacySettingsModel) {
            val setting =
                settingsModel.settings?.getSetting(SettingsKeyEnum.MOMENTS_HIDE_FROM) ?: return

            val countBlacklist = setting.countBlacklist ?: 0
            setCountBlacklist(setting.countBlacklist, binding.tvMomentSettingsHideFromCount)

            binding.vgMomentSettingsHideFromContainer.setThrottledClickListener {
                callback.hideFromMomentsClick(countBlacklist)
            }
        }
    }

    private inner class CommentSettingsViewHolder(
        private val binding: ItemTypeMomentSettingsCommentsBinding
    ) : BaseSettingsViewHolder(binding.root) {

        override fun bind(data: PrivacySettingsModel) {
            val setting =
                data.settings?.getSetting(SettingsKeyEnum.MOMENTS_ALLOW_COMMENT) ?: return

            val userTypeValue = setting.value ?: return

            binding.momentSettingsAllowCommentContainer.setOnClickListener {
                callback.onMomentWhoCanCommentClick(userTypeValue)
            }

            setUserExclusionTypeWithCount(
                textView = binding.momentSettingsAllowCommentType,
                value = userTypeValue,
                countBlacklist = setting.countBlacklist,
                countWhitelist = setting.countWhitelist
            )
        }
    }

    private inner class SaveSettingsViewHolder(
        private val binding: ItemTypeMomentSettingsSaveBinding
    ) : BaseSettingsViewHolder(binding.root) {

        init {
            setupSaveToGallery()
        }

        override fun bind(data: PrivacySettingsModel) {
            val settingSaveGallery =
                data.settings?.getSetting(SettingsKeyEnum.SAVE_MOMENTS_TO_GALLERY) ?: return
            binding.momentSettingsSaveGalleryToggle.setSwitch(settingSaveGallery)

            val settingSaveArchive =
                data.settings.getSetting(SettingsKeyEnum.SAVE_MOMENTS_TO_ARCHIVE) ?: return
            binding.momentSettingsSaveArchiveToggle.setSwitch(settingSaveArchive)
        }

        /**
         * Если первый запуск Настроек моментов, то включаем тогл Сохранения в галлерею если дан доступ к галереии
         * Если не первый запуск Настроек моментов, то включаем тогл Сохранения в галлерею из префов при условии, что доступ к галлереи сохраняется
         */
        private fun setupSaveToGallery() {
            binding.momentSettingsSaveGalleryContainer.setThrottledClickListener(
                delay = TOGGLE_THROTTLE_DELAY_MS,
                clickListener = callback::onSaveToGallerySwitch
            )
        }

        @Suppress("unused")
        private fun setupSaveToArchive() {
            binding.momentSettingsSaveArchiveContainer.setThrottledClickListener(
                TOGGLE_THROTTLE_DELAY_MS
            ) {
                val isToggled = binding.momentSettingsSaveArchiveToggle.isChecked.not()
                binding.momentSettingsSaveArchiveToggle.setSwitch(isToggled)

                callback.onSaveToArchiveSwitch(isToggled)
            }

            binding.momentSettingsSaveArchiveToggle.setOnCheckedChangeListener { _, isToggled ->
                callback.onSaveToArchiveSwitch(isToggled)
            }
        }
    }

    abstract inner class BaseSettingsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        SettingsViewHolderUtils by SettingsViewHolderUtilsImpl(itemView.context) {

        abstract fun bind(data: PrivacySettingsModel)
    }


    interface MomentSettingsAdapterCallback {
        fun onShowMomentForFriendSwitch(isToggled: Boolean)
        fun onMomentWhoCanCommentClick(userTypeValue: Int)
        fun onSaveToGallerySwitch()
        fun onSaveToArchiveSwitch(isToggled: Boolean)
        fun notShowMomentsClick()
        fun hideFromMomentsClick(count: Int)
    }

    private fun List<PrivacySettingUiModel>.getSetting(settingEnum: SettingsKeyEnum): PrivacySettingUiModel? {
        return this.find { it.key == settingEnum.key }
    }

    private fun SwitchCompat.setSwitch(setting: PrivacySettingUiModel) {
        this.setSwitch(setting.value == 1)
    }

    private fun SwitchCompat.setSwitch(isToggled: Boolean) {
        this.isChecked = isToggled
    }

    companion object {
        const val VISIBLE_SETTINGS = 1
        const val COMMENT_SETTINGS = 2
        const val SAVE_SETTINGS = 3
    }
}

package com.numplates.nomera3.modules.moments.settings.presentation

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.cell.CellRightElement
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMomentSettingsDoubleItemBinding
import com.numplates.nomera3.databinding.MeeraMomentSettingsSingleItemBinding
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter.Companion.MOMENTS_ALLOW_COMMENT
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter.Companion.SAVE_MOMENTS_TO_GALLERY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsAdapter.Companion.SETTING_MOMENT_VISIBILITY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.viewholders.MeeraBaseSettingsViewHolder
import timber.log.Timber

class MeeraMomentSettingsAdapter(
    private val isGalleryPermissionEnable: Boolean,
    private val callback: (MeeraMomentSettingsAction) -> Unit
) : BaseAsyncAdapter<String, MeeraPrivacySettingsData>() {

    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<MeeraPrivacySettingsData, *> {
        return when (viewType) {
            SETTING_MOMENT_VISIBILITY -> {
                MeeraMomentSettingsDoubleHolder(parent.toBinding())
            }

            MOMENTS_ALLOW_COMMENT -> {
                MeeraMomentSettingsSingleHolder(parent.toBinding(), MeeraMomentsSettingsItemType.ALLOW_COMMENTS)
            }

            SAVE_MOMENTS_TO_GALLERY -> {
                MeeraMomentSettingsSingleHolder(parent.toBinding(), MeeraMomentsSettingsItemType.GALLERY_SAVING)
            }

            else -> throw IllegalArgumentException("No such view type in adapter")
        }
    }

    inner class MeeraMomentSettingsDoubleHolder(
        val binding: MeeraMomentSettingsDoubleItemBinding,
    ) : MeeraBaseSettingsViewHolder(binding) {
        override fun bind(data: MeeraPrivacySettingsData) {
            data as MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsVisibility
            val settingShowForFriends = searchSetting(
                listSettings = data?.settings,
                settingEnum = SettingsKeyEnum.SHOW_MOMENTS_ONLY_FOR_FRIENDS
            ) ?: return
            val settingMomentNorShow = searchSetting(
                listSettings = data?.settings,
                settingEnum = SettingsKeyEnum.MOMENTS_NOT_SHOW
            ) ?: return
            val settingHideFrom = searchSetting(
                listSettings = data?.settings,
                settingEnum = SettingsKeyEnum.MOMENTS_HIDE_FROM
            ) ?: return
            val countBlacklist = settingHideFrom.countBlacklist ?: 0

            initShowOnlyFriends(settingShowForFriends)
            initMomentSettingHideFrom(countBlacklist)
            initHideMoment(settingMomentNorShow.countBlacklist ?: 0)
        }

        private fun initShowOnlyFriends(settingShowForFriends: PrivacySettingUiModel) {
            binding.vgMomentSettingsShowOnlyFriends.apply {
                cellCityText = false
                setCellRightElementChecked(settingShowForFriends.value.toBoolean())
                setCellRightElementClickable(false)
                cellRightIconClickListener = {
                    callback.invoke(MeeraMomentSettingsAction.ShowOnlyFriends(!getCellRightElementChecked()))
                }
            }
        }

        private fun initHideMoment(countBlacklist: Int) {
            if (countBlacklist > 0) {
                binding.vMomentSettingsHideMoment.setRightTextboxValue(countBlacklist.toString())
            } else {
                binding.vMomentSettingsHideMoment.gone()
            }
            binding.vMomentSettingsHideMoment.setRightElementContainerClickable(false)
            binding.vMomentSettingsHideMoment.setThrottledClickListener {
                callback.invoke(MeeraMomentSettingsAction.HideMoment(countBlacklist))
            }
        }

        private fun initMomentSettingHideFrom(countBlacklist: Int) {
            if (countBlacklist > 0) {
                binding.vMomentSettingsHideFrom.setRightTextboxValue(countBlacklist.toString())
            } else {
                binding.vMomentSettingsHideFrom.setRightTextboxValue(binding.root.resources.getString(R.string.settings_add))
            }
            binding.vMomentSettingsHideFrom.setRightElementContainerClickable(false)
            binding.vMomentSettingsHideFrom.setThrottledClickListener {
                callback.invoke(MeeraMomentSettingsAction.HideFrom(countBlacklist))
            }
        }
    }

    inner class MeeraMomentSettingsSingleHolder(
        val binding: MeeraMomentSettingsSingleItemBinding,
        val type: MeeraMomentsSettingsItemType,
    ) : MeeraBaseSettingsViewHolder(binding) {
        override fun bind(data: MeeraPrivacySettingsData) {
            when (type) {
                MeeraMomentsSettingsItemType.ALLOW_COMMENTS -> {
                    val settingCommentAllow =
                        (data as MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsAllowCommentModel).settings
                    val textAllowComments = getTextAllowComments(settingCommentAllow?.value ?: return)
                    binding.apply {
                        tvMomentSettingsSingleElementHeaderItem.text = binding.root.resources.getString(
                            R.string.moments_settings_comments_title
                        )
                        vMomentSettingsSingleElementItem.apply {
                            cellCityText = false
                            cellRightElement = CellRightElement.TEXT
                            setTitleValue(binding.root.resources.getString(R.string.moment_settings_allow_comments))
                            setRightTextboxValue(textAllowComments)
                            setRightElementContainerClickable(false)
                            setThrottledClickListener {
                                callback.invoke(MeeraMomentSettingsAction.AllowComments(settingCommentAllow.value))
                            }
                        }
                    }
                }

                MeeraMomentsSettingsItemType.GALLERY_SAVING -> {
                    val settingSaveGallery =
                        (data as MeeraPrivacySettingsData.MeeraPrivacySettingsMomentsSaveToGalleryModel).settings
                    binding.apply {
                        tvMomentSettingsSingleElementHeaderItem.text = binding.root.resources.getString(
                            R.string.moments_settings_save_title
                        )
                        vMomentSettingsSingleElementItem.apply {
                            cellCityText = false
                            cellRightElement = CellRightElement.SWITCH
                            cellArrowRight = false
                            setTitleValue(binding.root.resources.getString(R.string.moment_settings_saving_gallery))
                            if (isGalleryPermissionEnable) {
                                setCellRightElementChecked(settingSaveGallery?.value.toBoolean())
                            } else {
                                setCellRightElementChecked(false)
                            }
                            setCellRightElementClickable(false)
                            cellRightIconClickListener = {
                                callback.invoke(MeeraMomentSettingsAction.SaveGallery(!getCellRightElementChecked()))
                            }
                        }
                    }
                }

                else -> {
                    Timber.e("Unknown format")
                }
            }
        }

        private fun getTextAllowComments(keyType: Int): String {
            return when (keyType) {
                SettingsUserTypeEnum.NOBODY.key -> {
                    binding.root.resources.getString(R.string.meera_settings_everybody_count)
                }

                SettingsUserTypeEnum.FRIENDS.key -> {
                    binding.root.resources.getString(R.string.meera_settings_friends_count)
                }

                else -> binding.root.resources.getString(R.string.meera_settings_nobody_count)
            }
        }
    }

    private fun searchSetting(
        listSettings: List<PrivacySettingUiModel?>?,
        settingEnum: SettingsKeyEnum
    ): PrivacySettingUiModel? {
        return listSettings?.find { it?.key == settingEnum.key }
    }
}

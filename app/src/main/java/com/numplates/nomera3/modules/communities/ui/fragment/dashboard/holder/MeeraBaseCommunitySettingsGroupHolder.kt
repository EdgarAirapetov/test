package com.numplates.nomera3.modules.communities.ui.fragment.dashboard.holder

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraCommunitySettingsGroupItemBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.MeeraBaseCommunityDashboardAction

class MeeraBaseCommunitySettingsGroupHolder(
    val binding: MeeraCommunitySettingsGroupItemBinding,
    private val clickListener: (listener: MeeraBaseCommunityDashboardAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(settingsModel: CommunityInformationScreenUIModel?) {
        binding.vGroupInfo.apply {
            cellCityText = false
            setTitleValue(settingsModel?.communityName ?: "")
            settingsModel?.communityCoverImageURL?.let { url ->
                setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarUrl = url,
                        userAvatarErrorPlaceholder = R.drawable.ic_empty_community_avatar,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                )
            } ?: {
                setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarRes = R.drawable.ic_empty_community_avatar,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                )
            }

            cellRightIconClickListener = {
                clickListener.invoke(MeeraBaseCommunityDashboardAction.SettingsGroup(
                    clickableState = {
                        isClickable = it
                    },
                    progressBarState = {
                        if (it) binding.pbGroupInfo.visible() else binding.pbGroupInfo.gone()
                    }
                ))
            }
        }
    }
}

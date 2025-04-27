package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.MeeraCommunitySettingsDeleteGroupItemBinding
import com.numplates.nomera3.databinding.MeeraCommunitySettingsGroupItemBinding
import com.numplates.nomera3.databinding.MeeraCommunitySettingsUserListItemBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.holder.MeeraBaseCommunitySettingsDeleteGroupHolder
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.holder.MeeraBaseCommunitySettingsGroupHolder
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.holder.MeeraBaseCommunitySettingsUsersHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraBaseCommunityDashboardAdapter(
    private val settingsListItem: List<MeeraBaseCommunitySettingsType>,
    private val deleteIsVisible: Boolean,
    private val clickListener: (listener: MeeraBaseCommunityDashboardAction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var settingsModel: CommunityInformationScreenUIModel? = null

    override fun getItemViewType(position: Int): Int {
        return settingsListItem[position].position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MeeraBaseCommunitySettingsType.SETTINGS_GROUP.position -> {
                MeeraBaseCommunitySettingsGroupHolder(
                    parent.inflateBinding(MeeraCommunitySettingsGroupItemBinding::inflate),
                    clickListener
                )
            }

            MeeraBaseCommunitySettingsType.USERS.position -> {
                MeeraBaseCommunitySettingsUsersHolder(
                    parent.inflateBinding(MeeraCommunitySettingsUserListItemBinding::inflate),
                    clickListener
                )
            }

            MeeraBaseCommunitySettingsType.DELETE_GROUP.position -> {
                MeeraBaseCommunitySettingsDeleteGroupHolder(
                    parent.inflateBinding(MeeraCommunitySettingsDeleteGroupItemBinding::inflate),
                    clickListener
                )
            }

            else -> error("Unknown type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MeeraBaseCommunitySettingsGroupHolder -> holder.bind(settingsModel)
            is MeeraBaseCommunitySettingsUsersHolder -> holder.bind(settingsModel)
            is MeeraBaseCommunitySettingsDeleteGroupHolder -> holder.bind(deleteIsVisible)
        }
    }

    override fun getItemCount() = settingsListItem.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSettingsModel(model: CommunityInformationScreenUIModel) {
        settingsModel = model
        notifyDataSetChanged()
    }
}

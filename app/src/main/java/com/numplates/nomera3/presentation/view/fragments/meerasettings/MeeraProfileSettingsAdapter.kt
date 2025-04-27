package com.numplates.nomera3.presentation.view.fragments.meerasettings

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraProfileParamSettingsItemBinding
import timber.log.Timber

class MeeraProfileSettingsAdapter(
    private val items: List<MeeraProfileSettingsItemType>,
    private val callback: (action: MeeraProfileSettingsAction) -> Unit
) : RecyclerView.Adapter<MeeraProfileSettingsAdapter.ProfileSettingsItemViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return items[position].position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileSettingsItemViewHolder {
        val binding = MeeraProfileParamSettingsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val type = MeeraProfileSettingsItemType.entries.find { it.position == viewType }
        return ProfileSettingsItemViewHolder(binding, type, parent.context.resources)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ProfileSettingsItemViewHolder, position: Int) {
        holder.bind()
    }

    inner class ProfileSettingsItemViewHolder(
        val binding: MeeraProfileParamSettingsItemBinding,
        val type: MeeraProfileSettingsItemType?,
        val resources: Resources
    ) : RecyclerView.ViewHolder(binding.root) {
        private val item = binding.itemProfileSettings
        fun bind() {
            item.setRightElementContainerClickable(false)
            when (type) {
                MeeraProfileSettingsItemType.PUSH_NOTIFICATION -> {
                    initItem(
                        position = CellPosition.TOP,
                        title = resources.getString(R.string.notification_on_turn_on_push),
                        icon = R.drawable.ic_outlined_bell_on_m
                    )
                    item.setThrottledClickListener {
                        callback.invoke(MeeraProfileSettingsAction.MeeraPushNotificationAction)
                    }
                }

                MeeraProfileSettingsItemType.PRIVACY_SECURITY -> {
                    initItem(
                        position = CellPosition.MIDDLE,
                        title = resources.getString(R.string.profile_privacy),
                        icon = R.drawable.ic_outlined_lock_m
                    )
                    item.setThrottledClickListener {
                        callback.invoke(MeeraProfileSettingsAction.MeeraPrivacySecurityAction)
                    }
                }

                MeeraProfileSettingsItemType.RATE_APP -> {
                    initItem(
                        position = CellPosition.MIDDLE,
                        title = resources.getString(R.string.rate_application_txt),
                        icon = R.drawable.ic_outlined_star2_m
                    )
                    item.setThrottledClickListener {
                        callback.invoke(MeeraProfileSettingsAction.MeeraRateAppAction)
                    }
                }

                MeeraProfileSettingsItemType.ABOUT_MEERA -> {
                    initItem(
                        position = CellPosition.MIDDLE,
                        title = resources.getString(R.string.about_meera),
                        icon = R.drawable.ic_outlined_info_circle_m
                    )
                    item.setThrottledClickListener {
                        callback.invoke(MeeraProfileSettingsAction.MeeraAboutMeeraAction)
                    }
                }

                MeeraProfileSettingsItemType.SUPPORT -> {
                    initItem(
                        position = CellPosition.BOTTOM,
                        title = resources.getString(R.string.settings_tech_support),
                        icon = R.drawable.ic_outlined_magic_m
                    )
                    item.setThrottledClickListener {
                        callback.invoke(MeeraProfileSettingsAction.MeeraSupportAction)
                    }
                }

                null -> Timber.d("Unknown type of settings")
            }
        }

        private fun initItem(position: CellPosition, title: String, icon: Int) {
            item.cellPosition = position
            item.setTitleValue(title)
            item.setLeftIcon(icon)
        }
    }
}
